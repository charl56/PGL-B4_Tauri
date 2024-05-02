package fr.eseo.tauri.service;

import fr.eseo.tauri.model.*;
import fr.eseo.tauri.model.enumeration.Gender;
import fr.eseo.tauri.model.enumeration.RoleType;
import fr.eseo.tauri.repository.*;
import fr.eseo.tauri.util.CustomLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import fr.eseo.tauri.exception.GlobalExceptionHandler;
import fr.eseo.tauri.exception.ResourceNotFoundException;
import fr.eseo.tauri.repository.TeamRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final AuthService authService;
    private final TeamRepository teamRepository;
    private final UserService userService;
    private final ProjectService projectService;
    private final StudentRepository studentRepository;
    private final RoleRepository roleRepository;
    @Lazy private final StudentService studentService;

    public Team getTeamById(String token, Integer id) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "readTeam"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        return teamRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("team", id));
    }

    public List<Team> getAllTeamsByProject(String token, Integer projectId) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "readTeams"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        return teamRepository.findAllByProject(projectId);
    }

    /**
     * Delete already existing teams in te project and then create teams with the given number of teams.
     * // TODO : check nbTeams to be sure it is correct
     * @param nbTeams the number of teams to create
     * @return a List<Teams> if teams are created, otherwise null
     */
    private List<Team> createTeams(String token, Integer projectId, Integer nbTeams) {
        if (nbTeams < 1) {
            CustomLogger.error("TeamService.createTeams : The number of teams to create must be greater than 0");
            throw new IllegalArgumentException("The number of teams to create must be greater than 0");
        }

        Project project = projectService.getProjectById(token, projectId);

        // Delete all previous teams
        // TODO FUTURE : delete teams only when nbTeams is different from the number of teams in the project
        if(!getAllTeamsByProject(token, projectId).isEmpty()){
            deleteAllTeamsByProject(token, projectId);
        }

        ArrayList<Team> teams = new ArrayList<>();

        // Create the teams
        for (int i = 0; i < nbTeams; i++) {
            Team team = new Team();
            team.name("Team " + (i + 1));
            team.project(project);
            this.teamRepository.save(team);
            teams.add(team);
        }

        return teams;
    }

    public void updateTeam(String token, Integer id, Team updatedTeam) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "updateTeam"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }

        Team team = getTeamById(token, id);

        if (updatedTeam.name() != null) team.name(updatedTeam.name());
        if (updatedTeam.projectId() != null) team.project(projectService.getProjectById(token, updatedTeam.projectId()));
        if (updatedTeam.leaderId() != null) team.leader(userService.getUserById(token, updatedTeam.leaderId()));

        teamRepository.save(team);
    }

    public void deleteAllTeamsByProject(String token, Integer projectId) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "deleteTeam"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        studentRepository.removeAllStudentsFromTeams(projectId);
        teamRepository.deleteAllByProject(projectId);
    }

    /**
     * This method retrieves the number of women in a team by the team's ID.
     *
     * @param id The ID of the team.
     * @return The number of women in the team if the team exists, otherwise 0.
     */
    public Integer getNbWomenByTeamId(String token, Integer id){
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "deleteTeam"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        getTeamById(token, id);
        return teamRepository.countWomenInTeam(id);
    }

    /**
     * This method retrieves the number of bachelor students in a team by the team's ID.
     *
     * @param id The ID of the team.
     * @return The number of bachelor students in the team if the team exists, otherwise 0.
     */
    public Integer getNbBachelorByTeamId(String token, Integer id){
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "deleteTeam"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        getTeamById(token, id);
        return teamRepository.countBachelorInTeam(id);
    }

    public List<Student> getStudentsByTeamId(String token, Integer id) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "readTeam"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        getTeamById(token, id);
        return studentRepository.findByTeam(id);
    }

    public double getTeamAvgGrade(String token, Integer id) {
        Team team = getTeamById(token, id);
        return teamRepository.findAvgGradeByTeam(team);
    }

    public Criteria getCriteriaByTeamId(String token, Integer id, Integer projectId) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "readTeam"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        getTeamById(token, id);
        boolean validateWoman = getNbWomenByTeamId(token, id) >= projectService.getProjectById(token, projectId).nbWomen();
        boolean validateBachelor = getNbBachelorByTeamId(token, id) >= 1;
        return new Criteria(getNbWomenByTeamId(token, id), getNbBachelorByTeamId(token, id), validateWoman, validateBachelor);
    }

    /**
     * Auto generate teams with students according to the given number of teams and the number of women per team.
     * FUTURE :  create teams with the same average grade
     */
    public void generateTeams(String token, Integer projectId, Project projectDetails) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "createTeam"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }

        CustomLogger.info("TeamService.createTeams : Creating Teams");

        List<Student> women = this.studentRepository.findByGender(Gender.WOMAN);
        List<Student> men = this.studentRepository.findByGenderOrderByBachelorAndImportedAvgDesc(Gender.MAN);
        int nbStudent = men.size() + women.size();
        Integer nbTeams = projectDetails.nbTeams();
        Integer womenPerTeam = projectDetails.nbWomen();

        // Check if the number of students is enough to create the teams
        if (nbStudent < nbTeams * womenPerTeam - 1) {
            CustomLogger.error("TeamService.generateTeams : Not enough students to create the teams");
            throw new IllegalArgumentException("Not enough students to create the teams");
        }
        projectService.updateProject(token, projectId, projectDetails);
        List<Team> teams = this.createTeams(token, projectId, nbTeams);
        this.fillTeams(teams, women, men, womenPerTeam, nbStudent);
    }

    /**
     * Assign teams to the students.
     * @param teams the list of empty teams to fill
     * @param women the list of women students
     * @param men the list of men students
     * @param womenPerTeam the number of women per team
     */
    private void fillTeams(List<Team> teams, List<Student> women, List<Student> men, Integer womenPerTeam, Integer nbStudent) {
        int nbTeams = teams.size();
        int nbWomen = women.size();

        int index;

        Role role = new Role();
        role.type(RoleType.TEAM_MEMBER);

        // Assign "womenPerTeam" women to the teams first then even the teams with men if needed
        for (int i = 0; i < nbTeams; i++) {
            for (int j = 0; j < womenPerTeam; j++) {
                Student student;
                index = i * womenPerTeam + j;

                if (index < nbWomen) {
                    student = women.get(index);
                    student.team(teams.get(i));
                    role.user(student);
                    this.roleRepository.save(role);
                    this.studentRepository.save(student);
                } else if (index < nbStudent) {
                    student = men.get(index - nbWomen);
                    student.team(teams.get(i));
                    role.user(student);
                    this.roleRepository.save(role);
                    this.studentRepository.save(student);
                }
            }
        }

        // re-order the teams by average grade
        List<Team>sortedTeams = this.teamRepository.findAllOrderByAvgGradeOrderByAsc();

        index = nbTeams * womenPerTeam;

        // Assign the remaining students evenly to the teams
        for (int i = index; i < nbStudent; i++) {
            if ((i - index) % nbTeams == 0) {
                sortedTeams = this.teamRepository.findAllOrderByAvgGradeOrderByAsc();
            }

            Student student;
            if (i < nbWomen) {
                student = women.get(i);
                student.team(sortedTeams.get((i - index)% nbTeams));
            }else{
                student = men.get(i - nbWomen);
                student.team(sortedTeams.get((i - index)% nbTeams));
            }

            role.user(student);
            this.roleRepository.save(role);
            this.studentRepository.save(student);
        }
    }
}