package fr.eseo.tauri.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.eseo.tauri.util.valid.Create;
import fr.eseo.tauri.util.valid.Update;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "grade_types")
@Data
public class GradeType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    private Integer id;

    @NotNull(groups = { Create.class }, message = "The name field is required")
    @JsonProperty
    private String name;

    // TODO: Why is this field nullable? //Response : Can be changed later, and isn't needed to identify gradetype, so not needed at creation
    @Min(value = 0, groups = { Create.class, Update.class }, message = "The factor field must be greater than or equal to 0")
    @JsonProperty
    private Float factor;

    @NotNull(groups = { Create.class }, message = "The forGroup field is required")
    @JsonProperty
    private Boolean forGroup;

    @NotNull(groups = { Create.class }, message = "The imported field is required")
    @JsonProperty
    private Boolean imported;

    @JsonProperty
    private String scaleUrl;

}