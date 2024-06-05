package fr.eseo.tauri.unit.service;

import fr.eseo.tauri.exception.ResourceNotFoundException;
import fr.eseo.tauri.model.Project;
import fr.eseo.tauri.model.enumeration.ProjectPhase;
import fr.eseo.tauri.repository.ProjectRepository;
import fr.eseo.tauri.service.ProjectService;
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Nested
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    @BeforeEach
    void init_mocks() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getProjectByIdShouldReturnProjectWhenAuthorizedAndIdExists() {
        Project project = new Project();
        when(projectRepository.findById(anyInt())).thenReturn(Optional.of(project));

        Project result = projectService.getProjectById(1);

        assertEquals(project, result);
    }

    @Test
    void getProjectByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(projectRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(1));
    }

    @Test
    void getAllProjectsShouldReturnProjectsWhenAuthorized() {
        List<Project> projects = new ArrayList<>();
        when(projectRepository.findAll()).thenReturn(projects);

        List<Project> result = projectService.getAllProjects();

        assertEquals(projects, result);
    }

    @Test
    void createProjectShouldCreateWhenAuthorized() {
        Project project = new Project();

        projectService.createProject(project);

        verify(projectRepository, times(1)).save(project);
    }

    @Test
    void updateProjectShouldUpdateWhenAuthorizedAndIdExists() {
        Project project = new Project();
        Project updatedProject = new Project();
        updatedProject.nbTeams(5);
        updatedProject.nbWomen(3);
        updatedProject.phase(ProjectPhase.COMPOSING);
        when(projectRepository.findById(anyInt())).thenReturn(Optional.of(project));

        projectService.updateProject(1, updatedProject);

        verify(projectRepository, times(1)).save(project);
    }

    @Test
    void updateProjectShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        Project updatedProject = new Project();
        when(projectRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(1, updatedProject));
    }

    @Test
    void deleteAllProjectsShouldDeleteWhenAuthorized() {
        projectService.deleteAllProjects();

        verify(projectRepository, times(1)).deleteAll();
    }

    @Test
    void deleteProjectByIdShouldDeleteWhenAuthorizedAndIdExists() {
        when(projectRepository.findById(anyInt())).thenReturn(Optional.of(new Project()));

        projectService.deleteProjectById(1);

        verify(projectRepository, times(1)).deleteById(1);
    }

    @Test
    void deleteProjectByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(projectRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProjectById(1));
    }


    @Test
    void testGetProjectByIdShouldReturnProjectWhenAuthorizedAndIdExists() {
        // Arrange
        Project project = new Project();
        when(projectRepository.findById(anyInt())).thenReturn(Optional.of(project));

        // Act
        Project result = projectService.getProjectById(1);

        // Assert
        assertEquals(project, result);
    }

    @Test
    void testGetProjectByIdShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        // Arrange
        when(projectRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(1));
    }

    @Test
    void testGetAllProjectsShouldReturnProjectsWhenAuthorized() {
        // Arrange
        List<Project> projects = new ArrayList<>();
        when(projectRepository.findAll()).thenReturn(projects);

        // Act
        List<Project> result = projectService.getAllProjects();

        // Assert
        assertEquals(projects, result);
    }

    @Test
    void getActualProjectShouldReturnProjectWhenExists() {
        Project project = new Project();
        when(projectRepository.findFirstByActualTrue()).thenReturn(Optional.of(project));

        Project result = projectService.getActualProject();

        assertEquals(project, result);
    }

    @Test
    void getActualProjectShouldThrowResourceNotFoundExceptionWhenDoesNotExist() {
        when(projectRepository.findFirstByActualTrue()).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getActualProject());
    }

    @Test
    void setActualProjectShouldSetWhenIdExists() {
        Project project = new Project();
        Project actualProject = new Project();
        when(projectRepository.findById(anyInt())).thenReturn(Optional.of(project));
        when(projectRepository.findFirstByActualTrue()).thenReturn(Optional.of(actualProject));

        projectService.setActualProject(1);

        verify(projectRepository, times(2)).save(any(Project.class));
    }
    @Test
    void setActualProjectShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {
        when(projectRepository.findById(anyInt())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.setActualProject(1));
    }


}