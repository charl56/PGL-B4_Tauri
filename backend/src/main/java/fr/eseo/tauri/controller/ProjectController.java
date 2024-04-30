package fr.eseo.tauri.controller;

import fr.eseo.tauri.model.Project;
import fr.eseo.tauri.service.ProjectService;
import fr.eseo.tauri.util.CustomLogger;
import fr.eseo.tauri.validator.project.CreateProjectValidator;
import fr.eseo.tauri.validator.project.UpdateProjectValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects")
@Tag(name = "projects")
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects(@RequestHeader("Authorization") String token) {
        List<Project> projects = projectService.getAllProjects(token);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        Project project = projectService.getProjectById(token, id);
        return ResponseEntity.ok(project);
    }

    @PostMapping
    public ResponseEntity<String> createProject(@RequestHeader("Authorization") String token, @Valid @RequestBody CreateProjectValidator newProject) {
        projectService.createProject(token, newProject);
        CustomLogger.info("The project have been created");
        return ResponseEntity.ok("The project have been created");
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> updateProject(@RequestHeader("Authorization") String token, @PathVariable Integer id, @Valid @RequestBody UpdateProjectValidator requestBody) {
        projectService.updateProject(token, id, requestBody);
        CustomLogger.info("The project has been updated");
        return ResponseEntity.ok("The project has been updated");
    }

    @DeleteMapping
    public ResponseEntity<String> deleteAllProjects(@RequestHeader("Authorization") String token) {
        projectService.deleteAllProjects(token);
        CustomLogger.info("All the projects have been deleted");
        return ResponseEntity.ok("All the projects have been deleted");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        projectService.deleteProject(token, id);
        CustomLogger.info("The project has been deleted");
        return ResponseEntity.ok("The project has been deleted");
    }

}
