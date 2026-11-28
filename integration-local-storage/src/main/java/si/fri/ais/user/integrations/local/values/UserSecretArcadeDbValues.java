package si.fri.ais.user.integrations.local.values;

import com.arcadedb.schema.DocumentType;
import com.arcadedb.schema.Schema;
import com.arcadedb.schema.Type;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbChangelog;
import si.fri.ais.common.integration.local.arcadedb.models.ArcadeDbVersion;

import java.util.List;

/** Schema for encrypted user secrets; no plaintext property is defined. */
public interface UserSecretArcadeDbValues {
    String DOCUMENT_TYPE = "UserSecret";
    String ID = "id";
    String NAME = "name";
    String OWNER = "owner";
    String TYPE = "type";
    String FORMAT = "format";
    String CIPHERTEXT = "ciphertext";
    String IV = "iv";
    String WRAPPED_KEY = "wrappedKey";
    String KEY_IV = "keyIv";

    List<ArcadeDbChangelog> CHANGELOGS = List.of(new ArcadeDbChangelog(
            "UserSecrets",
            new ArcadeDbVersion(1, 0, 0, null),
            "create-user-secret-schema",
            "Creates encrypted UserSecret records and owner-scoped unique names",
            database -> {
                DocumentType type = database.getSchema().getOrCreateDocumentType(DOCUMENT_TYPE);
                List.of(ID, NAME, OWNER, TYPE).forEach(field ->
                        type.getOrCreateProperty(field, Type.STRING).setMandatory(true).setNotNull(true));
                type.getOrCreateProperty(FORMAT, Type.INTEGER).setMandatory(true).setNotNull(true);
                List.of(CIPHERTEXT, IV, WRAPPED_KEY, KEY_IV).forEach(field ->
                        type.getOrCreateProperty(field, Type.BINARY).setMandatory(true).setNotNull(true));
                type.getOrCreateTypeIndex(Schema.INDEX_TYPE.LSM_TREE, true, ID);
                type.getOrCreateTypeIndex(Schema.INDEX_TYPE.LSM_TREE, true, OWNER, NAME);
            },
            false
    ));
}
