package fr.eseo.tauri.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.eseo.tauri.model.id_class.ValidationFlagId;
import fr.eseo.tauri.util.valid.Create;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "validation_flags")
@IdClass(ValidationFlagId.class)
@Data
public class ValidationFlag {

    @JsonProperty
    private Boolean confirmed = false;

    @Id
    @ManyToOne
    @JoinColumn(name = "author_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty
    private User author;

    @Id
    @ManyToOne
    @JoinColumn(name = "flag_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonProperty
    private Flag flag;

    @NotNull(groups = { Create.class }, message = "The authorId field is required")
    @Transient
    @JsonDeserialize
    private Integer authorId;

    @NotNull(groups = { Create.class }, message = "The flagId field is required")
    @Transient
    @JsonDeserialize
    private Integer flagId;

}