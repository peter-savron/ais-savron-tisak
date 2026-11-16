package si.fri.ais.common.core.integration.local.arcadedb.scripts;

import com.arcadedb.database.Database;
import com.arcadedb.database.Document;
import com.arcadedb.database.MutableDocument;
import com.arcadedb.query.sql.executor.ResultSet;
import si.fri.ais.common.core.models.NamedObject;
import si.fri.ais.common.core.models.UnnamedObject;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

public final class CommonArcadeDbScripts {

    private static final String SELECT_BY_FIELD_TEMPLATE = "SELECT FROM %s WHERE %s = ?";
    private static final String DELETE_BY_FIELD_TEMPLATE = "DELETE FROM %s WHERE %s = ?";

    /**
     * Searches an UnnamedObject (or subclass) by its UUID id field.
     */
    public static Optional<Document> searchById(
            Database database,
            String documentType,
            UUID id
    ) {
        if (id == null) {
            return Optional.empty();
        }
        String query = SELECT_BY_FIELD_TEMPLATE.formatted(
                documentType,
                UnnamedObject.Fields.id
        );
        try (ResultSet rs = database.query("sql", query, id.toString())) {
            if (rs.hasNext()) {
                return rs.next().getElement();
            }
            return Optional.empty();
        }
    }

    /**
     * Searches a NamedObject (or subclass) by its name field.
     */
    public static <T extends NamedObject> Optional<T> searchByName(
            Database database,
            String documentType,
            String name,
            Function<Document, T> mapper
    ) {
        if (name == null) {
            return Optional.empty();
        }
        String query = SELECT_BY_FIELD_TEMPLATE.formatted(
                documentType,
                NamedObject.Fields.name
        );
        try (ResultSet rs = database.query("sql", query, name)) {
            if (rs.hasNext()) {
                return rs.next().getElement().map(mapper);
            }
            return Optional.empty();
        }
    }

    /**
     * Finds a document/vertex by UUID id, applies mutations via {@code updater}, and saves it.
     * Throws {@link RuntimeException} if no matching record exists.
     */
    public static Document updateById(
            Database database,
            String documentType,
            UUID id,
            Consumer<MutableDocument> update
    ) {
        Objects.requireNonNull(update, "update must not be null");

        Document existing = searchById(database, documentType, id)
                .orElseThrow(() -> new RuntimeException("%s not found with id: %s".formatted(documentType, id)));

        MutableDocument mutable = existing.modify();
        update.accept(mutable);
        return mutable.save();
    }

    /**
     * Optional companion helper for deleting an UnnamedObject by id.
     */
    public static void deleteById(Database database, String documentType, UUID id) {
        if (id == null) {
            return;
        }
        String query = DELETE_BY_FIELD_TEMPLATE.formatted(
                documentType,
                UnnamedObject.Fields.id
        );
        database.command("sql", query, id.toString()).close();
    }
}