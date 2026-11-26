package si.fri.ais.spec.core.models.commands;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import si.fri.ais.spec.core.models.SpecSourceContext;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import java.util.List;
import lombok.NoArgsConstructor;

/** Omitted properties preserve the existing selection. */
@Getter
@SuperBuilder
@NoArgsConstructor
public class SpecSourceUpdate {
    /** Source whose configuration will be updated. */
    @Valid @NotNull private SpecSourceContext context;

    private String root;
    private String subfolder;
    private boolean clearSubfolder;
    private List<@NotBlank String> selectionRules;

    /** The subfolder cannot be supplied when the update requests clearing it. */
    @AssertTrue(message = "Cannot set a subfolder when clearSubfolder is true")
    public boolean isSubfolderOptionsValid() {
        return !clearSubfolder || subfolder == null;
    }
}
