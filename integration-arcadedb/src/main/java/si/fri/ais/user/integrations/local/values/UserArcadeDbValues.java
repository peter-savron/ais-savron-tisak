package si.fri.ais.user.integrations.local.values;

import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Schema;
import com.arcadedb.schema.Type;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbChangelog;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbVersion;
import si.fri.ais.common.core.models.NamedObject;
import si.fri.ais.common.core.models.UnnamedObject;
import si.fri.ais.user.core.models.User;

import java.util.List;

public interface UserArcadeDbValues {
    String changelogPartition = "Users";
    String userDocumentType = "User";
    List<ArcadeDbChangelog> userSchemaChangelogs = List.of(
            new ArcadeDbChangelog(
                    changelogPartition,
                    new ArcadeDbVersion(1, 0, 0, null),
                    "create-user-schema",
                    "Creates User document type, fields, and idx",
                    db -> {
                        DocumentType userType = db.getSchema().getOrCreateDocumentType(userDocumentType);
                        userType.getOrCreateProperty(UnnamedObject.Fields.id, Type.STRING);
                        userType.getOrCreateProperty(NamedObject.Fields.name, Type.STRING);
                        userType.getOrCreateProperty(User.Fields.passwordHash, Type.STRING);
                        userType.getOrCreateProperty(User.Fields.keySalt, Type.BINARY);

                        userType.getOrCreateTypeIndex(Schema.INDEX_TYPE.LSM_TREE, true, UnnamedObject.Fields.id);
                        userType.getOrCreateTypeIndex(Schema.INDEX_TYPE.LSM_TREE, true, NamedObject.Fields.name);
                    },
                    false
            ),
            new ArcadeDbChangelog(
                    changelogPartition,
                    new ArcadeDbVersion(1, 1, 0, null),
                    "add-user-logged",
                    "Adds the User login flag and initializes existing users as logged out",
                    db -> {
                        db.getSchema().getType(userDocumentType).getOrCreateProperty(User.Fields.logged, Type.BOOLEAN);
                        String update = "UPDATE %s SET %s = false WHERE %s IS NULL"
                                .formatted(userDocumentType, User.Fields.logged, User.Fields.logged);
                        db.transaction(() -> {
                            try (var results = db.command("sql", update)) {
                                while (results.hasNext()) {
                                    results.next();
                                }
                            }
                        });
                    },
                    false
            )
    );
}
