package si.fri.ais.session.integrations.local.values;

import com.arcadedb.schema.Schema;
import com.arcadedb.schema.Type;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbChangelog;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbVersion;

import java.util.List;

public interface SessionArcadeDbValues {
    String TYPE = "Session";
    String ID = "id";
    String USER_ID = "userId";
    String WORKSPACE_ID = "workspaceId";
    String USES_SESSION = "usesSession";
    String SHELL_STARTED_AT = "shellStartedAt";
    String LAST_ACCESSED_AT = "lastAccessedAt";
    String EXPIRES_AT = "expiresAt";

    List<ArcadeDbChangelog> CHANGELOGS = List.of(new ArcadeDbChangelog(
            "Sessions",
            new ArcadeDbVersion(1, 0, 0, null),
            "create-session-schema",
            "Creates the Session document type and unique slot index",
            db -> {
                var type = db.getSchema().getOrCreateDocumentType(TYPE);
                for (String field : List.of(ID, USER_ID, LAST_ACCESSED_AT, EXPIRES_AT)) {
                    type.getOrCreateProperty(field, Type.STRING).setMandatory(true).setNotNull(true);
                }
                for (String field : List.of(WORKSPACE_ID, USES_SESSION, SHELL_STARTED_AT)) {
                    type.getOrCreateProperty(field, Type.STRING);
                }
                type.getOrCreateTypeIndex(Schema.INDEX_TYPE.LSM_TREE, true, ID);
                db.getSchema().getType(TYPE).getOrCreateTypeIndex(Schema.INDEX_TYPE.LSM_TREE, false, USES_SESSION);
            },
            false
    ));
}
