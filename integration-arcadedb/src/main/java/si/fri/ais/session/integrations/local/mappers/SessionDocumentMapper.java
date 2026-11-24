package si.fri.ais.session.integrations.local.mappers;

import com.arcadedb.database.Document;
import com.arcadedb.database.MutableDocument;
import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.session.core.models.Session;
import si.fri.ais.session.core.models.SessionSlot;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static si.fri.ais.session.integrations.local.values.SessionArcadeDbValues.*;

@ApplicationScoped
public class SessionDocumentMapper {
    public Session fromDocument(Document document) {
        return Session.builder()
                .slot(SessionSlot.builder()
                        .id(document.getString(ID))
                        .shellStartedAt(shellStartedAt(document))
                        .build())
                .userId(UUID.fromString(document.getString(USER_ID)))
                .workspaceId(Optional.ofNullable(document.getString(WORKSPACE_ID)).map(UUID::fromString).orElse(null))
                .usesSession(document.getString(USES_SESSION))
                .lastAccessedAt(Instant.parse(document.getString(LAST_ACCESSED_AT)))
                .expiresAt(expiresAt(document))
                .build();
    }

    public Instant expiresAt(Document document) {
        return Instant.parse(document.getString(EXPIRES_AT));
    }

    public Instant shellStartedAt(Document document) {
        return Optional.ofNullable(document.getString(SHELL_STARTED_AT)).map(Instant::parse).orElse(null);
    }

    public void initialize(Session session, MutableDocument document) {
        document.set(ID, session.getId());
        Optional.ofNullable(session.getSlot().getShellStartedAt())
                .ifPresent(value -> document.set(SHELL_STARTED_AT, value.toString()));
        update(session, document);
    }

    public void update(Session session, MutableDocument document) {
        document.set(USER_ID, session.getUserId().toString());
        document.set(WORKSPACE_ID, Optional.ofNullable(session.getWorkspaceId()).map(UUID::toString).orElse(null));
        document.set(USES_SESSION, session.getUsesSession());
        document.set(LAST_ACCESSED_AT, session.getLastAccessedAt().toString());
        document.set(EXPIRES_AT, session.getExpiresAt().toString());
    }
}
