package si.fri.ais.user.integrations.local.mappers;

import com.arcadedb.database.Document;
import com.arcadedb.database.MutableDocument;
import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.user.core.models.StoredUserSecret;
import si.fri.ais.user.core.models.UserSecret;
import si.fri.ais.user.core.models.UserSecretEnvelope;

import java.util.UUID;

import static si.fri.ais.user.integrations.local.values.UserSecretArcadeDbValues.*;

/** Maps only encrypted envelopes; never accepts or returns plaintext models. */
@ApplicationScoped
public class UserSecretDocumentMapper {
    public StoredUserSecret fromDocument(Document document) {
        return StoredUserSecret.builder()
                .id(UUID.fromString(document.getString(ID)))
                .name(document.getString(NAME))
                .owner(UUID.fromString(document.getString(OWNER)))
                .type(UserSecret.Type.valueOf(document.getString(TYPE)))
                .envelope(UserSecretEnvelope.builder()
                        .version(document.getInteger(FORMAT))
                        .ciphertext(document.getBinary(CIPHERTEXT))
                        .iv(document.getBinary(IV))
                        .wrappedKey(document.getBinary(WRAPPED_KEY))
                        .keyIv(document.getBinary(KEY_IV))
                        .build())
                .build();
    }

    public void initialize(StoredUserSecret secret, MutableDocument document) {
        document.set(ID, secret.getId().toString());
        document.set(NAME, secret.getName());
        document.set(OWNER, secret.getOwner().toString());
        update(secret, document);
    }

    public void update(StoredUserSecret secret, MutableDocument document) {
        document.set(TYPE, secret.getType().name());
        document.set(FORMAT, secret.getEnvelope().getVersion());
        document.set(CIPHERTEXT, secret.getEnvelope().getCiphertext());
        document.set(IV, secret.getEnvelope().getIv());
        document.set(WRAPPED_KEY, secret.getEnvelope().getWrappedKey());
        document.set(KEY_IV, secret.getEnvelope().getKeyIv());
    }
}
