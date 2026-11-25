package si.fri.ais.workspace.core.models;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import si.fri.ais.common.core.models.NamedObject;

import java.util.UUID;

/** A user-owned container with a stable ID and editable metadata. */
@Getter
@SuperBuilder
public class Workspace extends NamedObject {
    public static final int DESCRIPTION_LIMIT = 1000;

    @NotNull private final UUID ownerId;
    @Setter private String title;
    @Setter @Size(max = DESCRIPTION_LIMIT) private String description;

    @Override
    @Pattern(regexp = NAME_FORMAT)
    public String getName() {
        return super.getName();
    }

    @Override
    public void setName(String name) {
        super.setName(name);
    }
}
