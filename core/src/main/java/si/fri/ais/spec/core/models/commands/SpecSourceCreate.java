package si.fri.ais.spec.core.models.commands;

import jakarta.validation.constraints.*;
import lombok.Getter;
import si.fri.ais.common.core.models.NamedObject;
import lombok.experimental.SuperBuilder;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.Builder;

/** Configuration for registering a workspace source. */
@Getter
@SuperBuilder
@NoArgsConstructor
public class SpecSourceCreate {
    @NotNull @Pattern(regexp = NamedObject.NAME_FORMAT) private String name;
    @NotBlank private String type;
    @NotBlank private String root;
    private String subfolder;
    @Builder.Default @NotNull private List<@NotBlank String> selectionRules = List.of();
}
