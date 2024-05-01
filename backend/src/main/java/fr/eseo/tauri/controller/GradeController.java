package fr.eseo.tauri.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.eseo.tauri.model.Grade;
import fr.eseo.tauri.model.GradeType;
import fr.eseo.tauri.model.enumeration.RoleType;
import fr.eseo.tauri.repository.GradeRepository;
import fr.eseo.tauri.repository.GradeTypeRepository;
import fr.eseo.tauri.service.GradeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/grades")
@Tag(name = "grades")
public class GradeController {

    private final GradeRepository gradeRepository;
    private final GradeService gradeService;

    private final GradeTypeRepository gradeTypeRepository;

    @GetMapping
    public ResponseEntity<Iterable<Grade>> getAllGrades() {
        return ResponseEntity.ok(gradeRepository.findAll());
    }

    @PostMapping("/")
    public Grade addGrade(@RequestBody Grade grade) {
        return gradeRepository.save(grade);
    }

    @GetMapping("/{id}")
    public @Valid Grade getGradeById(@PathVariable Integer id) {
        return gradeRepository.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Grade updateGrade(@PathVariable Integer id, @RequestBody Grade gradeDetails) {
        Grade grade = gradeRepository.findById(id).orElse(null);
        if (grade != null) {
            grade.value(gradeDetails.value());
            grade.comment(gradeDetails.comment());
            return gradeRepository.save(grade);
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public String deleteGrade(@PathVariable Integer id) {
        gradeRepository.deleteById(id);
        return "Grade deleted";
    }

    /**
     * This method is a POST endpoint that accepts a JSON string representing a map of evaluations.
     *
     * @param evaluations A JSON string representing a map of evaluations.
     * @return A ResponseEntity with either a success message or an error message.
     */
    @PostMapping("/add-grade-to-team/{userId}")
    public ResponseEntity<Map<String, String>> addGradeFromArray(@RequestBody String evaluations, @PathVariable Integer userId) throws JsonProcessingException {
        //try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeReference<Map<String, List<Map<String, Object>>>> typeReference = new TypeReference<>() {
            };
            Map<String, List<Map<String, Object>>> evaluationsMap = objectMapper.readValue(evaluations, typeReference);

            for (Map.Entry<String, List<Map<String, Object>>> entry : evaluationsMap.entrySet()) {
                String teamName = entry.getKey();
                List<Map<String, Object>> evaluationsList = entry.getValue();

                for (Map<String, Object> evaluation : evaluationsList) {
                    if (evaluation.get("gradeType").equals("gradeOverallPerformance")) {
                        Integer value = (Integer) evaluation.get("grade");
                        gradeService.assignGradeToTeam(teamName, value, "Performance Globale", userId);
                    }
                    if (evaluation.get("gradeType").equals("gradeMaterialSupport")) {
                        Integer value = (Integer) evaluation.get("grade");
                        gradeService.assignGradeToTeam(teamName, value, "Support Matériel", userId);
                    }
                    if (evaluation.get("gradeType").equals("gradeContentPresentation")) {
                        Integer value = (Integer) evaluation.get("grade");
                        gradeService.assignGradeToTeam(teamName, value, "Contenu de la présentation", userId);
                    }
                    if(evaluation.get("gradeType").equals("gradeTechnicalSolution")) {
                        Integer value = (Integer) evaluation.get("grade");
                        gradeService.assignGradeToTeam(teamName, value, "Solution technique", userId);
                    }
                    if(evaluation.get("gradeType").equals("gradeProjectManagement")) {
                        Integer value = (Integer) evaluation.get("grade");
                        gradeService.assignGradeToTeam(teamName, value, "Gestion de projet", userId);
                    }
                    if(evaluation.get("gradeType").equals("gradeSprintConformity")) {
                        Integer value = (Integer) evaluation.get("grade");
                        gradeService.assignGradeToTeam(teamName, value, "Conformité au sprint", userId);
                    }
                }
            }
            return ResponseEntity.ok(Map.of("message", "Grades added successfully."));
       /* } /*catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid number format for grade value."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error occurred while adding grades: " + e.getMessage()));
        }*/
    }

    //TODO : Handle the token
    @GetMapping("/average-grades-by-grade-type-by-role/{userId}")
    public ResponseEntity<List<List<Double>>> getAverageGradesByGradeTypeByRole(@RequestHeader("Authorization") String token, @PathVariable Integer userId) {
            ArrayList<List<Double>> gradeByTypes = new ArrayList<>();
            ArrayList<Double> gradeByRoles;

            for (GradeType gradeType : gradeTypeRepository.findAllForGroup()) {
                gradeByRoles = new ArrayList<>();
                for (RoleType roleType : RoleType.values()) {
                    double grade = getAverageGradeByRoleType(userId, roleType, gradeType.name());
                    gradeByRoles.add(grade);
                }
                gradeByTypes.add(gradeByRoles);
            }

            if (gradeByTypes.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }

            return ResponseEntity.status(HttpStatus.OK).body(gradeByTypes);
    }

    /**
     * Helper method
     */
    private double getAverageGradeByRoleType(Integer userId, RoleType roleType, String gradeType) {
        try {
            return gradeService.getAverageGradesByGradeTypeByRoleType(userId, roleType, gradeType);
        } catch (NullPointerException e) {
            return -1.0;
        }
    }

    /** GET
     *      All Grades
     *     Gradess by Student
     *     Gradess by Team
     *     Gradess by Author
     *     Gradess by Type (GradeType)
     *     Grade by Id
     *  POST
     *      Add grade(s)?
     *  PUT
     *      Value
     *      Comment
     *      //Faire en sorte que les PUT pour 1 table soient tous dans une seule requete (grâce au body)
     *  DELETE
     *      Delete Grade By Id
     */


}
