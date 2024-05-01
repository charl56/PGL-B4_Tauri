package fr.eseo.tauri.controller;

import fr.eseo.tauri.model.Criteria;
import fr.eseo.tauri.model.Project;
import fr.eseo.tauri.model.Student;
import fr.eseo.tauri.model.Team;
import fr.eseo.tauri.service.AuthService;
import fr.eseo.tauri.service.ProjectService;
import fr.eseo.tauri.service.TeamService;
import fr.eseo.tauri.util.CustomLogger;
import fr.eseo.tauri.validator.team.GenerateTeamsValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller class for managing teams.
 */
@RestController
@RequestMapping("/api/teams")
@Tag(name = "teams")
public class TeamController {

    private static final String READ_STUDENT_BY_TEAM = "readStudentByTeam";
    private static final String READ_CRITERIA = "readCriteria";
    private static final String TEAM_CREATION = "teamCreation";
    private static final String UNAUTHORIZED_MESSAGE = "Non autorisé";
    private final AuthService authService;
    private final TeamService teamService;
    private final ProjectService projectService;

    /**
     * Constructor for TeamController.
     *
     * @param authService    the authentication service
     * @param teamService    the team service
     * @param projectService the projectService
     */
    @Autowired
    public TeamController(AuthService authService, TeamService teamService, ProjectService projectService) {
        this.authService = authService;
        this.teamService = teamService;
        this.projectService = projectService;
    }

    /**
     * Update the leader of a team.
     *
     * @param token    the authorization token
     * @param idTeam   the ID of the team
     * @param idLeader the ID of the new leader
     * @return a response entity with a success message if the update was successful, otherwise an error message
     */
    @PutMapping("/update-leader-team/{idTeam}")
    public ResponseEntity<String> updateLeaderTeam(@RequestHeader("Authorization") String token, @PathVariable Integer idTeam, @RequestParam Integer idLeader) {
        if (Boolean.TRUE.equals(authService.checkAuth(token, TEAM_CREATION))) {
            try {
                Team team = teamService.updateLeaderTeam(idTeam, idLeader);
                if (team != null) {
                    return ResponseEntity.ok("La modification a bien été prise en compte");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour du leader de l'équipe");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour du leader de l'équipe: " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED_MESSAGE);
        }
    }


    /**
     * Update the name of a team.
     *
     * @param token   the authorization token
     * @param idTeam  the ID of the team
     * @param newName the new name of a team
     * @return a response entity with a success message if the update was successful, otherwise an error message
     */
    @PutMapping("/update-name-team/{idTeam}")
    public ResponseEntity<String> updateNameTeam(@RequestHeader("Authorization") String token, @PathVariable Integer idTeam, @RequestParam String newName) {
        String permission = "teamRename";
        if (Boolean.TRUE.equals(authService.checkAuth(token, permission))) {
            try {
                Team team = teamService.updateNameTeam(idTeam, newName);
                if (team != null) {
                    return ResponseEntity.ok("La modification a bien été prise en compte");
                } else {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour du nom de l'équipe");
                }
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour du nom de l'équipe : " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED_MESSAGE);
        }
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<List<Student>> getStudentsByTeamId(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        var students = teamService.getStudentsByTeamId(token, id);
        return ResponseEntity.ok(students);
    }


    /**
     * Create teams.
     *
     * @param token the authorization token
     * @return a response entity with a success message if the update was successful, otherwise an error message
     */
    @PostMapping
    public ResponseEntity<String> generateTeams(@RequestHeader("Authorization") String token, @RequestParam ("projectId") Integer projectId, @Valid @RequestBody GenerateTeamsValidator request) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, TEAM_CREATION))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED_MESSAGE);
        }

        var nbTeams = request.nbTeams();
        var womenPerTeam = request.womenPerTeam();

        try {
            teamService.generateTeams(projectId, nbTeams, womenPerTeam);

            var projectProperties = new Project();
            projectProperties.nbTeams(nbTeams);
            projectProperties.nbWomen(womenPerTeam);
            projectService.updateProject(token, projectId, projectProperties);

            CustomLogger.info("Teams have been created");
            return ResponseEntity.ok("La creation a bien été prise en compte");
        } catch (IllegalArgumentException e){
            CustomLogger.error("Erreur lors de la création des équipes : " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour, les équipes n'ont pas pu être créées : " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    /**
     * Get All Teams.
     *
     * @return A list of all teams
     */
    @GetMapping()
    public ResponseEntity<List<Team>> getAllTeams(@RequestHeader("Authorization") String token, @RequestParam("projectId") Integer projectId) {
        if (Boolean.TRUE.equals(authService.checkAuth(token, READ_STUDENT_BY_TEAM))) {
                List<Team> teams = teamService.getAllTeams(projectId);
                return ResponseEntity.ok(teams);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<Team> getTeamById(@RequestHeader("Authorization") String token, @PathVariable Integer teamId) {
        if (Boolean.TRUE.equals(authService.checkAuth(token, READ_STUDENT_BY_TEAM))) {
            try {
                Team team = teamService.getTeamById(token, teamId);
                if (team == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                }
                return ResponseEntity.ok(team);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @GetMapping("/names")
    public ResponseEntity<List<String>> getAllTeamNames(@RequestHeader("Authorization") String token) {
        if (Boolean.TRUE.equals(authService.checkAuth(token, READ_STUDENT_BY_TEAM))) {
            try {
                List<String> teams = teamService.getAllTeamNames();
                if (teams.isEmpty()) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(teams);
                }
                return ResponseEntity.ok(teams);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @GetMapping("/{teamId}/criteria")
    public ResponseEntity<Criteria> getCriteriaByTeamId(@RequestHeader("Authorization") String token, @PathVariable Integer teamId, @RequestParam("projectId") Integer projectId) {
        if (Boolean.TRUE.equals(authService.checkAuth(token, READ_CRITERIA))) {
            try {
                Integer nbWomen = teamService.getNbWomenByTeamId(teamId);
                Integer nbBachelor = teamService.getNbBachelorByTeamId(teamId);
                Integer nbStudents = teamService.getNbStudentsByTeamId(teamId);
                Criteria criteria = getCriteria(token, projectId, nbStudents, nbWomen, nbBachelor);
                return ResponseEntity.ok(criteria);
            } catch (Exception e) {
                CustomLogger.info("Erreur au critère : " + e.getClass() + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @NotNull
    private Criteria getCriteria(String token, Integer projectId, Integer nbStudents, Integer nbWomen, Integer nbBachelor) {
        Integer womenPerTeam = projectService.getProjectById(token, projectId).nbWomen();
        boolean validateWoman = false;
        boolean validateBachelor = false;
        if (nbStudents > 0 && (nbWomen * 100) / nbStudents >= womenPerTeam) {
            validateWoman = true;
        }
        if (nbBachelor >= 1) {
            validateBachelor = true;
        }
        return new Criteria(nbWomen, nbBachelor, nbStudents, validateWoman, validateBachelor);
    }

    @GetMapping("/{idTeam}/average")
    public ResponseEntity<String> getTeamAvgGrade(@RequestHeader("Authorization") String token, @PathVariable Integer idTeam) {
        String permission = "readTeamAvgGrade";
        if (Boolean.TRUE.equals(authService.checkAuth(token, permission))) {
            try {
                double avgGrade = this.teamService.getTeamAvgGrade(idTeam);
                return ResponseEntity.ok(String.valueOf(avgGrade));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la récupération " + e.getMessage());
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED_MESSAGE);
        }
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllTeams(@RequestHeader("Authorization") String token) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "teamDelete"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UNAUTHORIZED_MESSAGE);
        }

        try {
            teamService.deleteAllTeams();
            return ResponseEntity.ok("Les équipes ont bien été supprimées");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    @GetMapping("/leader/{leaderId}")
    public ResponseEntity<Team> getTeamByLeaderId(@RequestHeader("Authorization") String token, @PathVariable Integer leaderId, @RequestParam("projectId") Integer projectId) {
        if (Boolean.TRUE.equals(authService.checkAuth(token, "readTeamBySupervisor"))){
                Team team = teamService.getTeamByLeaderId(leaderId, projectId);
                return ResponseEntity.ok(team);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

    @PutMapping("/{teamId}/move-student")
    public ResponseEntity<Team> moveTeamStudent(@RequestHeader("Authorization") String token, @PathVariable Integer teamId, @RequestParam Integer studentId) {
        if (Boolean.TRUE.equals(authService.checkAuth(token, "teamUpdate"))){
            try {
                var newTeam = teamService.moveTeamStudent(teamId, studentId);
                return ResponseEntity.ok(newTeam);
            } catch (Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }
    }

}
