package si.fri.ais.spec.core.services;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.RequiredArgsConstructor;
import si.fri.ais.common.core.config.StorageConfig;
import si.fri.ais.common.core.utils.files.ResettableFile;
import si.fri.ais.common.core.utils.files.ResettableTempFile;
import si.fri.ais.spec.core.exceptions.DocumentProcessingException;
import si.fri.ais.spec.core.models.*;
import si.fri.ais.spec.core.models.Spec.ProcessingStatus;
import si.fri.ais.spec.core.models.Spec.Status;
import si.fri.ais.spec.core.models.SpecSyncResult;
import si.fri.ais.spec.core.ports.outgoing.*;
import si.fri.ais.spec.core.exceptions.SpecSourceReadException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.DigestInputStream;
import java.security.NoSuchAlgorithmException;

import lombok.extern.jbosslog.JBossLog;

import java.util.*;

import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

// TODO some error handling is misssing
@JBossLog
@ApplicationScoped
@RequiredArgsConstructor
class SpecSynchronizer {
    private static final String SPEC_SYNC_FOLDER = "spec-sync";

    private final StorageConfig storageConfig;
    private final DocumentProcessor processor;
    private final SpecProviders providers;
    private final SpecRepository specs;


    /**
     * Discovers the source, applies move/change/delete phases, retries remaining work and returns changed Specs.
     */
    public SpecSyncResult process(SpecSource source) {
        SpecIndex existing = new SpecIndex(specs.identifiers(source.getId()));
        List<RemoteSpec> discovered = providers.get(source.getType()).discover(source);
        ArrayList<Spec> updates = new ArrayList<>();
        // TODO step should be added to normalize RemoteSpec changes (like A modify A->B and B->C modify(C) moves should be minimized to move(A,C) so no duplicate external ids could be present in the index and there is no issue with the changes being applied in the wrong order
        updates.addAll(processMoved(source, discovered, existing));
        // TODO check modified
        updates.addAll(processModified(source, discovered, existing));
        updates.addAll(processDeleted(discovered, existing));
        // TODO check this
        updates.addAll(processNotSucceeded(source));
        markSuccessful(source);
        return SpecSyncResult.builder().source(source).specs(updates).build();
    }

    /**
     * Persists moved item metadata, then reads and converts each moved item's current content.
     */
    private List<Spec> processMoved(SpecSource source, List<RemoteSpec> discovered, SpecIndex existing) {
        SpecProvider provider = providers.get(source.getType());
        List<Spec> moved = discovered.stream().filter(item -> item.status() == RemoteSpec.Status.MOVED).toList()
                .stream().map(remote -> resolveMovedSpec(source, remote, existing))
                .toList();
        moved.forEach(spec -> {
            specs.update(spec);
            processContent(source, provider, spec);
        });
        return moved;
    }

    /**
     * Persists new or modified item metadata before reading and converting its content.
     */
    private List<Spec> processModified(SpecSource source, List<RemoteSpec> discovered, SpecIndex existing) {
        SpecProvider provider = providers.get(source.getType());
        List<Spec> modified = specsToUpdate(source, discovered, existing);
        modified.forEach(spec -> {
            specs.update(spec);
            processContent(source, provider, spec);
        });
        return modified;
    }

    /**
     * Applies confirmed deletions after moves and content changes have been handled.
     */
    private List<Spec> processDeleted(List<RemoteSpec> discovered, SpecIndex existing) {
        return discovered.stream().filter(remote -> remote.status() == RemoteSpec.Status.DELETED)
                .map(remote -> delete(remote, existing))
                .flatMap(Optional::stream).toList();
    }

    /**
     * Retries pending or failed Specs not already processed from this discovery result. To be done once all other processing is done.
     */
    private List<Spec> processNotSucceeded(SpecSource source) {
        SpecProvider provider = providers.get(source.getType());
        List<Spec> unsuccessful = specs.queryByProcessingStatus(SpecRepository.ProcessingStatusQuery.builder()
                        .specSourceId(source.getId())
                        .processingStatuses(List.of(ProcessingStatus.PENDING, ProcessingStatus.FAILED)).build());
        unsuccessful.forEach(spec -> processContent(source, provider, spec));
        return unsuccessful;
    }

    /**
     * Promotes every successfully processed Spec for this source to UP_TO_DATE.
     */
    private void markSuccessful(SpecSource source) {
        // TODO we should list statuses before overwriting them
        specs.queryByProcessingStatus(SpecRepository.ProcessingStatusQuery.builder()
                        .specSourceId(source.getId()).processingStatuses(List.of(ProcessingStatus.DONE)).build())
                .stream().filter(spec -> spec.getStatus() != Status.UP_TO_DATE)
                .forEach(spec -> {
                    spec.setStatus(Status.UP_TO_DATE);
                    specs.update(spec);
                });
    }

    /**
     * Builds the NEW, UPDATED or REINTEGRATED Specs represented by modified discovery records.
     */
    private List<Spec> specsToUpdate(SpecSource source, List<RemoteSpec> discovered, SpecIndex existing) {

        return discovered.stream()
                .filter(item -> item.status() == RemoteSpec.Status.MODIFIED)
                .map(remote -> resolveModified(source, remote, existing)
                ).toList();
    }

    /**
     * Finds a moved Spec by its previous key or content hash, or creates a new Spec at the target key.
     */
    private Spec resolveMovedSpec(SpecSource source, RemoteSpec remote, SpecIndex existing) {
        Optional<Spec> sourceSpec = existing.byExternalId(remote.movedFrom()).flatMap(ids -> specs.get(ids.id()));
        Optional<Spec> targetSpec = existing.byExternalId(remote.movedTo()).flatMap(ids -> specs.get(ids.id()));

        // TODO to avoid duplicated external ids, we should see either source and target exist (if both exist we delete source and UPDATE target)...
        if (sourceSpec.isPresent() && targetSpec.isPresent()) {
            var spec = sourceSpec.get();
            ;
            spec.setStatus(Status.DELETED);
            spec.setDeleted(true);
            spec.setProcessingStatus(ProcessingStatus.DONE);
            specs.update(spec);
            // TODO moved Target should be handled here -> also why is there
        }

        // TODO rewrite so to avoid ternary operator
        return (targetSpec.isPresent() ? targetSpec : sourceSpec).map(
                spec -> {
                    spec.setExternalId(remote.movedTo());
                    spec.setExternalHash(remote.externalHash());
                    if (remote.fileName() != null) spec.setFileName(remote.fileName());
                    spec.setUpdatedAt(remote.updatedAt());
                    spec.setStatus(spec.isDeleted() || spec.getStatus() == Status.DELETED
                            ? Status.REINTEGRATED : Status.MOVED);
                    spec.setDeleted(false);
                    spec.setProcessingStatus(ProcessingStatus.PENDING);
                    return spec;
                }
        ).orElseGet(
                () -> Spec.builder()
                        .id(UUID.randomUUID()).sourceId(source.getId()).externalId(remote.movedTo())
                        .externalHash(remote.externalHash()).fileName(remote.fileName()).updatedAt(remote.updatedAt())
                        .status(Status.NEW).processingStatus(ProcessingStatus.PENDING).build()
        );
    }

    /**
     * Returns the current source key, falling back to externalId for providers without movedTo.
     */
    private String targetExternalId(RemoteSpec remote) {
        return ofNullable(remote.movedTo()).orElse(remote.externalId());
    }

    /**
     * Resolves a modified record by external key or hash and updates its pending metadata in place.
     */
    private Spec resolveModified(SpecSource source, RemoteSpec remote, SpecIndex existing) {
        // Try resolving by external id
        Optional<Spec> byId = resolveModifiedById(remote, existing);
        if (byId.isPresent()) return byId.get();

        // Try matching hash (spec implicitly moved)
        var byHash = resolveModifiedByHash(remote, existing);
        if (byHash.isPresent()) return byHash.get();

        // No match create new
        return Spec.builder()
                .id(UUID.randomUUID()).sourceId(source.getId()).externalId(remote.externalId())
                .externalHash(remote.externalHash()).fileName(remote.fileName()).updatedAt(remote.updatedAt())
                .metadata(remote.externalId()).status(Status.NEW)
                .processingStatus(ProcessingStatus.PENDING).build();
    }

    Optional<Spec> resolveModifiedById(RemoteSpec remote, SpecIndex index) {
        return index.byExternalId(remote.movedFrom()).flatMap(ids -> specs.get(ids.id())).map(spec -> {
            spec.setStatus(spec.isDeleted() ? Status.REINTEGRATED : Status.UPDATED);
            spec.setDeleted(false);
            ofNullable(remote.externalHash()).ifPresent(spec::setExternalHash);
            ofNullable(remote.fileName()).ifPresent(spec::setFileName);
            spec.setUpdatedAt(remote.updatedAt());
            spec.setProcessingStatus(ProcessingStatus.PENDING);
            return spec;
        });
    }

    /**
     * If there is a singular spec that matches the hash returns it, else returns an empty optional.
     */
    private Optional<Spec> resolveModifiedByHash(RemoteSpec remote, SpecIndex index) {
        List<SpecIdentifiers> hits = index.byExternalHash(remote.externalHash());
        if (hits.isEmpty()) {
            return Optional.empty();
        } else if (hits.size() > 1) {
            log.debugf("Multiple files have the same hash [%s] of an remote %", hits.stream().map(SpecIdentifiers::id).toList().toString());
            return empty();
        }

        // If the external id was not matched but hash matched it is safe to assume it moved
        return hits.stream().findFirst().map(SpecIdentifiers::id).flatMap(specs::get).map(
                spec -> {
                    spec.setExternalId(remote.movedTo());
                    spec.setExternalHash(remote.externalHash());
                    if (remote.fileName() != null) spec.setFileName(remote.fileName());
                    spec.setUpdatedAt(remote.updatedAt());
                    spec.setStatus(spec.isDeleted() || spec.getStatus() == Status.DELETED
                            ? Status.REINTEGRATED : Status.MOVED);
                    spec.setDeleted(false);
                    spec.setProcessingStatus(ProcessingStatus.PENDING);
                    return spec;
                }
        );
    }

    /**
     * Marks an existing item deleted only when the scan confirms deletion and no move/change protects it.
     */
    private Optional<Spec> delete(RemoteSpec remote, SpecIndex existing) {
        return existing.byExternalId(remote.externalId()).flatMap(x -> specs.get(x.id())).map(
                spec -> {
        spec.setDeleted(true);
        spec.setStatus(Status.DELETED);
        spec.setProcessingStatus(ProcessingStatus.DONE);
        specs.update(spec);
        return spec;
        });
    }

    /**
     * Reads, hashes and converts source content, then publishes the converted bytes or records failure.
     */
    private void processContent(SpecSource source, SpecProvider provider, Spec original) {
        try (ResettableFile observed = provider.read(source, original.getExternalId(), original.getUpdatedAt());
                ResettableTempFile staging = ResettableTempFile.create("spec-content-", ".tmp");
                ) {
            // TODO diffs in metadata should be better handled / there should be an SpecMetadataDiff update(Spec stored, RemoteSpec remote) diff available)
            original.setFileName(original.getFileName());
            original.setDeleted(false);

            // Verify retrieval
            if (observed == null) {
                markProcessingUnavailable(original);
                return;
            }

            // Check content hash matches
            String newContentHash = contentHash(observed.openStream());
            if (Objects.equals(original.getContentHash(), newContentHash)) {
                original.setStatus(Status.NO_CHANGE);
                specs.update(original);
            }
            original.setContentHash(newContentHash);

            // Process original content
            try(OutputStream destination = Files.newOutputStream(staging.path())) {
                String format = processor.process(observed.path(), destination);
                original.setInputFormat(format);
            }
            original.setProcessingStatus(ProcessingStatus.DONE);

            // Try dumping staging
            specs.update(original, staging.openStream());
        } catch (SpecSourceReadException e) {
            log.warn("Could not read spec " + original.getExternalId() + " from source " + source.getId(), e);
            markProcessingUnavailable(original);
        } catch (DocumentProcessingException | IOException | RuntimeException e) {
            log.warn("Could not process spec " + original.getExternalId() + " from source " + source.getId(), e);
            markProcessingUnavailable(original);
        }
    }

    /**
     * Restores the previous publication metadata and records that the current source read failed.
     */
    private void markProcessingUnavailable(Spec spec) {
        spec.setProcessingStatus(ProcessingStatus.FAILED);
        spec.setStatus(Status.UNAVAILABLE);
        specs.update(spec);
    }

    /**
     * Computes the SHA-256 hash of the retrieved source file.
     */
    private String contentHash(InputStream document) throws IOException {
        try (DigestInputStream content = new DigestInputStream(
                document, MessageDigest.getInstance("SHA-256"))
        ) {
            content.transferTo(OutputStream.nullOutputStream());
            return HexFormat.of().formatHex(content.getMessageDigest().digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 is unavailable", e);
        }
    }
}
