package si.fri.ais.workspace.integrations.local.values;

import com.arcadedb.schema.Schema;
import com.arcadedb.schema.Type;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbChangelog;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbVersion;

import java.util.List;

public interface WorkspaceArcadeDbValues {
    String TYPE = "Workspace";
    String ID = "id";
    String OWNER_ID = "ownerId";
    String NAME = "name";
    String TITLE = "title";
    String DESCRIPTION = "description";

    List<ArcadeDbChangelog> CHANGELOGS = List.of(new ArcadeDbChangelog(
            "Workspaces",
            new ArcadeDbVersion(1, 0, 0, null),
            "create-workspace-schema",
            "Creates the Workspace document type and its unique indexes",
            db -> {
                var type = db.getSchema().getOrCreateDocumentType(TYPE);
                for (String field : List.of(ID, OWNER_ID, NAME)) {
                    type.getOrCreateProperty(field, Type.STRING).setMandatory(true).setNotNull(true);
                }
                type.getOrCreateProperty(TITLE, Type.STRING);
                type.getOrCreateProperty(DESCRIPTION, Type.STRING);
                type.getOrCreateTypeIndex(Schema.INDEX_TYPE.LSM_TREE, true, ID);
                type.getOrCreateTypeIndex(Schema.INDEX_TYPE.LSM_TREE, true, OWNER_ID, NAME);
            },
            false
    ));
}
