package fr.eseo.tauri.service;

import fr.eseo.tauri.model.Student;
import fr.eseo.tauri.model.Team;
import fr.eseo.tauri.model.User;
import fr.eseo.tauri.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service class for managing teams.
 */
@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final StudentRepository studentRepository;

    private final GradeRepository gradeRepository;

    /**
     * Constructor for TeamService.
     * @param teamRepository the team repository
     * @param userRepository the user repository
     * @param projectRepository the project repository
     * @param studentRepository the student repository
     * @param gradeRepository the grade repository
     */
    @Autowired
    public TeamService(TeamRepository teamRepository, UserRepository userRepository, ProjectRepository projectRepository, StudentRepository studentRepository, GradeRepository gradeRepository) {
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.studentRepository = studentRepository;
        this.gradeRepository = gradeRepository;
    }

    /**
     * Change students team to null when their team is deleted.
     * @param id the team's id
     */
    public void deleteTeam(Integer id) {
        Optional<Team> team = teamRepository.findById(id);
        if (team.isPresent()) {
            List<Student> students = studentRepository.findByTeamId(team.get());
            for (Student student : students) {
                student.team(null);
                studentRepository.save(student);
            }

            // List<Grade> grades = gradeRepository.findByTeam(team.get());
            // for (Grade grade : grades) {
            //    gradeRepository.delete(grade);
            // }

            teamRepository.deleteById(id);
        }
    }

    /*@PostConstruct //Test function for the deleteTeam function
    public void initDataIfTableIsEmpty() {

        if(userRepository.count() == 0){
            User user = new User();
            User user2 = new User();
            userRepository.save(user);
            userRepository.save(user2);
        }

        if (teamRepository.count() == 0) {
            // Ajouter une ligne dans la table teams si elle est vide
            Team team = new Team();
            team.project(projectRepository.findById(1).get());
            if (userRepository.count() != 0){
                team.leader(userRepository.findById(Long.valueOf(1)).get());
            }
            teamRepository.save(team);
        }

        if(studentRepository.count() == 0){
            Student student = new Student();
            Student student2 = new Student();
            student.team(teamRepository.findById(1).get());
            student2.team(teamRepository.findById(1).get());
            studentRepository.save(student);
            studentRepository.save(student2);
        }

        if(gradeRepository.count() == 0){
            Grade grade = new Grade();
            //grade.team(teamRepository.findById(1).get());
            gradeRepository.save(grade);
        }

        if (teamRepository.count() != 0) {
            //this.deleteTeam(1);
        }

        if(userRepository.count() != 0){
            //UserService.deleteUser(1);
        }

    }*/

    /**
     * Update the leader of a team.
     * @param teamId the ID of the team
     * @param leaderId the ID of the new leader
     * @return the updated team if successful, otherwise null
     */
    public Team updateLeaderTeam(Integer teamId, Integer leaderId) {
        User leader = userRepository.findById(Long.valueOf(leaderId)).orElse(null);
        Team team = teamRepository.findById(teamId).orElse(null);
        if (team != null && leader != null) {
            team.leader(leader);
            return teamRepository.save(team);
        }
        return null;
    }

    /**
     * Update the name of a team.
     * @param teamId the ID of the team
     * @param newName the new name of the team
     * @return the updated team if successful, otherwise null
     */
    public Team updateNameTeam(Integer teamId, String newName) {

        Team team = teamRepository.findById(teamId).orElse(null);
        if (team != null) {
            team.name(newName);
            return teamRepository.save(team);
        }
        return null;
    }


    /**
     * Create teams with the given number of teams and the given ratio
     * TODO : take into account the ratioGender and try to create teams with the same average grade
     * @param nbTeams the number of teams to create
     * @param ratioGender the ratio of women in the teams
     * @return a List<Teams> if teams are created, otherwise null
     */
    @Async
    public List<Team> createTeams(Integer nbTeams, Integer ratioGender) {

        List<Student> students = this.studentRepository.findAllOrderByImportedAvg();
        int nbStudent = students.size();

        if (nbStudent < nbTeams || nbTeams < 1) {
            return null;
        }else {
            List<Team> teams = new ArrayList<Team>();

            for (int i = 0; i < nbTeams; i++) {
                Team team = new Team();
                team.name("Team " + (i + 1));
                teamRepository.save(team);
                teams.add(team);
            }

            for (int i = 0; i < nbStudent; i++) {
                Student student = students.get(i);
                student.team(teams.get(i % nbTeams));
                studentRepository.save(student);
            }

            return teams;
        }
    }

}
