package si.fri.ais.workspace.integrations.local.mappers;

import com.arcadedb.database.Document;
import com.arcadedb.database.MutableDocument;
import jakarta.enterprise.context.ApplicationScoped;
import si.fri.ais.workspace.core.models.Workspace;

import java.util.UUID;

import static si.fri.ais.workspace.integrations.local.values.WorkspaceArcadeDbValues.*;

@ApplicationScoped
public class WorkspaceDocumentMapper {
    public Workspace fromDocument(Document document) {
        return Workspace.builder()
                .id(UUID.fromString(document.getString(ID)))
                .ownerId(UUID.fromString(document.getString(OWNER_ID)))
                .name(document.getString(NAME))
                .title(document.getString(TITLE))
                .description(document.getString(DESCRIPTION))
                .build();
    }

    public void initialize(Workspace workspace, MutableDocument document) {
        document.set(ID, workspace.getId().toString());
        document.set(OWNER_ID, workspace.getOwnerId().toString());
        update(workspace, document);
    }

    public void update(Workspace workspace, MutableDocument document) {
        document.set(NAME, workspace.getName());
        document.set(TITLE, workspace.getTitle());
        document.set(DESCRIPTION, workspace.getDescription());
    }
}
