package si.fri.ais.user.integrations.local.mappers;

import com.arcadedb.database.Document;
import com.arcadedb.database.MutableDocument;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.validation.constraints.NotNull;
import si.fri.ais.common.core.models.NamedObject;
import si.fri.ais.common.core.models.UnnamedObject;
import si.fri.ais.user.core.models.User;

import java.util.UUID;

@ApplicationScoped
public class UserDocumentMapper {

    public User fromDocument(Document doc) {
        if (doc == null) {
            return null;
        }

        return User.builder()
                .id(UUID.fromString(doc.getString(UnnamedObject.Fields.id)))
                .name(doc.getString(NamedObject.Fields.name))
                .passwordHash(doc.getString(User.Fields.passwordHash))
                .keySalt(doc.getBinary(User.Fields.keySalt))
                .build();
    }

    public MutableDocument toDocument(@NotNull User user, @NotNull MutableDocument doc) {
        doc.set(UnnamedObject.Fields.id, user.getId().toString());
        doc.set(NamedObject.Fields.name, user.getName());
        doc.set(User.Fields.passwordHash, user.getPasswordHash());
        doc.set(User.Fields.keySalt, user.getKeySalt());
        return doc;
    }
}