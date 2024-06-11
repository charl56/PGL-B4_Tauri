package fr.eseo.tauri.service;

import fr.eseo.tauri.model.Bonus;
import fr.eseo.tauri.exception.ResourceNotFoundException;
import fr.eseo.tauri.model.Project;
import fr.eseo.tauri.model.Student;
import fr.eseo.tauri.model.User;
import fr.eseo.tauri.repository.BonusRepository;
import fr.eseo.tauri.repository.StudentRepository;
import fr.eseo.tauri.util.CustomLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BonusService {

    private final BonusRepository bonusRepository;
    private final ValidationBonusService validationBonusService;
    private final UserService userService;
    private final StudentRepository studentRepository;

    /**
     * Get a bonus by its id
     * @param id the id of the bonus
     * @return the bonus
     */
    public Bonus getBonusById(Integer id) {
        return bonusRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("bonus", id));
    }

    /**
     * Get all bonuses by project
     * @param projectId the id of the project
     * @return the list of bonuses
     */
    public List<Bonus> getAllBonusesByProject(Integer projectId) {
        return bonusRepository.findAllByProject(projectId);
    }

    /**
     * Create a bonus
     * @param bonus the bonus to create
     */
    public void createBonus(Bonus bonus) {
        bonusRepository.save(bonus);
    }

    /**
     * Update a bonus
     * @param id the id of the bonus
     * @param updatedBonus the updated bonus
     */
    public void updateBonus(Integer id, Bonus updatedBonus) {
        boolean isLimited = updatedBonus.limited();
        if(isLimited && Math.abs(updatedBonus.value()) > 4) {
            throw new IllegalArgumentException("The value of a limited bonus must be between -4 and 4");
        }

        Bonus bonus = getBonusById(id);

        bonus.value(updatedBonus.value());
        if(updatedBonus.authorId() != null) bonus.author(userService.getUserById(updatedBonus.authorId()));

        if (updatedBonus.comment() != null) bonus.comment(updatedBonus.comment());

        bonusRepository.save(bonus);

        if(isLimited) validationBonusService.deleteAllValidationBonuses(id);
    }

    /**
     * Delete a bonus with its id
     * @param id the id of the bonus
     */
    public void deleteBonus(Integer id) {
        getBonusById(id);
        bonusRepository.deleteById(id);
    }

    /**
     * Delete all bonuses by project
     * @param projectId the id of the project
     */
    public void deleteAllBonusesByProject(Integer projectId) {
        bonusRepository.deleteAllByProject(projectId);
    }


    public List<Bonus> getValidationBonusesByTeam(Integer teamId) {
        List <Student> students = studentRepository.findAllByTeamId(teamId);
        List <Bonus> bonuses = null;

        for(Student student : students) {
            CustomLogger.info("Student : " + student);
            User user = userService.getUserById(student.id());
            CustomLogger.info("User auth: " + user);
//            bonuses.add(bonusRepository.findAllByAuthorId(user.id()));
        }

        CustomLogger.info("Get all bonuses by team : " + bonuses);
        return bonuses;
    }

}
