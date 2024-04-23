package fr.eseo.tauri.model.id_class;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.eseo.tauri.model.Bonus;
import fr.eseo.tauri.model.User;

import java.io.Serializable;
import java.util.Objects;

public class ValidationBonusId implements Serializable {

    @JsonProperty
    private User user;

    @JsonProperty
    private Bonus bonus;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationBonusId that = (ValidationBonusId) o;
        return Objects.equals(user, that.user) &&
                Objects.equals(bonus, that.bonus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, bonus);
    }
}