package fr.eseo.tauri.model;

import fr.eseo.tauri.model.idClass.GradeTeamsId;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "grade_teams")
@IdClass(GradeTeamsId.class)
@Getter
@Setter

public class GradeTeams {

    @Id
    @OneToOne
    @JoinColumn(name = "grade_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Grade gradeId;

    @Id
    @OneToOne
    @JoinColumn(name = "team_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Team teamId;

}
