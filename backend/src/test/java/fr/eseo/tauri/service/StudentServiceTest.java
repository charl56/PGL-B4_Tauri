package fr.eseo.tauri.service;

import fr.eseo.tauri.model.Student;
import fr.eseo.tauri.model.Team;
import fr.eseo.tauri.model.enumeration.Gender;
import fr.eseo.tauri.repository.StudentRepository;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Nested
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private GradeTypeService gradeTypeService;

    @Mock
    private GradeService gradeService;

    @Mock
    private TeamService teamService;

    @InjectMocks
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Test if the student service is correctly instantiated")
    void testStudentServiceInstantiation() {
        assertNotNull(studentService);
    }

    @Test
    @DisplayName("createStudent with valid data saves student")
    void createStudent_withValidData_savesStudent() {

        Student student = new Student();
        student.name("John Doe");
        student.gender(Gender.MAN);
        student.bachelor(true);
        student.teamRole("Not assigned");
        student.project(null);
        student.team(null);
        student.password("password");
        student.privateKey("privateKey");
        student.email("john.doe@gmail.com");

        when(studentRepository.save(any(Student.class))).thenReturn(student);

        studentService.createStudent(student);

        verify(studentRepository, times(1)).save(student);
        verifyNoMoreInteractions(studentRepository);
    }

    @Test
    @DisplayName("createStudent with null name throws exception")
    void createStudent_withNullName_throwsException() {
        Student student = new Student();
        student.name(null);
        assertThrows(IllegalArgumentException.class, () -> studentService.createStudent(student));
    }

    @Test
    @DisplayName("getStudentQuantity returns correct quantity when students exist")
    void getStudentQuantity_returnsCorrectQuantity_whenStudentsExist() {
        List<Student> students = Arrays.asList(new Student(), new Student(), new Student());
        when(studentRepository.findAll()).thenReturn(students);

        Integer quantity = studentService.getStudentQuantity();

        assertEquals(3, quantity);
    }

    @Test
    @DisplayName("getStudentQuantity returns zero when no students exist")
    void getStudentQuantity_returnsZero_whenNoStudentsExist() {

        when(studentRepository.findAll()).thenReturn(new ArrayList<>());

        Integer quantity = studentService.getStudentQuantity();

        assertEquals(0, quantity);
    }

    @Test
    @DisplayName("createStudentFromData throws exception when name is null")
    void createStudentFromData_throwsExceptionWhenNameIsNull() {
        assertThrows(IllegalArgumentException.class, () -> studentService.createStudentFromData(null, "M", "B"));
    }

    @Test
    @DisplayName("createStudentFromData throws exception when name is empty")
    void createStudentFromData_throwsExceptionWhenNameIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> studentService.createStudentFromData("", "M", "B"));
    }

    @Test
    @DisplayName("createStudentFromData throws exception when gender is null")
    void createStudentFromData_throwsExceptionWhenGenderIsNull() {
        assertThrows(IllegalArgumentException.class, () -> studentService.createStudentFromData("John Doe", null, "B"));
    }

    @Test
    @DisplayName("createStudentFromData throws exception when gender is empty")
    void createStudentFromData_throwsExceptionWhenGenderIsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> studentService.createStudentFromData("John Doe", "", "B"));
    }

    @Test
    @DisplayName("createStudentFromData throws exception when bachelor is null")
    void createStudentFromData_throwsExceptionWhenBachelorIsNull() {
        assertThrows(IllegalArgumentException.class, () -> studentService.createStudentFromData("John Doe", "M", null));
    }


    @Test
    @DisplayName("isEmpty returns true when line is empty at index")
    void isEmpty_returnsTrue_whenLineIsEmptyAtIndex() {
        String[] line = {"", "test", "example"};
        assertFalse(StudentService.hasNonEmptyValue(line, 0));
    }

    @Test
    @DisplayName("isEmpty returns false when line is not empty at index")
    void isEmpty_returnsFalse_whenLineIsNotEmptyAtIndex() {
        String[] line = {"test", "example"};
        assertTrue(StudentService.hasNonEmptyValue(line, 0));
    }

    @Test
    @DisplayName("isEmpty returns true when index is out of bounds")
    void isEmpty_returnsTrue_whenIndexIsOutOfBounds() {
        String[] line = {"test", "example"};
        assertFalse(StudentService.hasNonEmptyValue(line, 3));
    }

    @Test
    @DisplayName("createStudentFromData creates student with valid data")
    void createStudentFromData_createsStudentWithValidData() {
        Student student = studentService.createStudentFromData("John Doe", "M", "B");

        assertEquals("John Doe", student.name());
        assertEquals(Gender.MAN, student.gender());
        assertTrue(student.bachelor());
        assertEquals("Not assigned", student.teamRole());
        assertNull(student.project());
        assertNull(student.team());
        assertEquals("password", student.password());
        assertEquals("privateKey", student.privateKey());
        assertEquals("john.doe@reseau.eseo.fr", student.email());
    }

    @Test
    @DisplayName("createStudentFromData creates student with empty bachelor status")
    void createStudentFromData_createsStudentWithEmptyBachelorStatus() {
        Student student = studentService.createStudentFromData("John Doe", "M", "");

        assertEquals("John Doe", student.name());
        assertEquals(Gender.MAN, student.gender());
        assertFalse(student.bachelor());
        assertEquals("Not assigned", student.teamRole());
        assertNull(student.project());
        assertNull(student.team());
        assertEquals("password", student.password());
        assertEquals("privateKey", student.privateKey());
        assertEquals("john.doe@reseau.eseo.fr", student.email());
    }

    @Test
    @DisplayName("createStudentFromData creates student with female gender")
    void createStudentFromData_createsStudentWithFemaleGender() {
        Student student = studentService.createStudentFromData("Jane Doe", "F", "B");

        assertEquals("Jane Doe", student.name());
        assertEquals(Gender.WOMAN, student.gender());
        assertTrue(student.bachelor());
        assertEquals("Not assigned", student.teamRole());
        assertNull(student.project());
        assertNull(student.team());
        assertEquals("password", student.password());
        assertEquals("privateKey", student.privateKey());
        assertEquals("jane.doe@reseau.eseo.fr", student.email());
    }

    @Test
    @DisplayName("extractNamesGenderBachelorAndGrades extracts valid data from input stream")
    @SuppressWarnings("unchecked")
    void extractNamesGenderBachelorAndGrades_extractsValidDataFromInputStream() {
        String csvContent = "1,John Doe,M,B,15,14,13\n2,Jane Doe,F,B,12,13,14";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        Map<String, Object> result = studentService.extractNamesGenderBachelorAndGrades(inputStream);

        List<String> names = (List<String>) result.get(StudentService.MAP_KEY_NAMES);
        List<String> genders = (List<String>) result.get(StudentService.MAP_KEY_GENDERS);
        List<String> bachelors = (List<String>) result.get(StudentService.MAP_KEY_BACHELORS);
        List<List<String>> grades = (List<List<String>>) result.get(StudentService.MAP_KEY_GRADES);

        assertEquals(Arrays.asList("John Doe", "Jane Doe"), names);
        assertEquals(Arrays.asList("M", "F"), genders);
        assertEquals(Arrays.asList("B", "B"), bachelors);
        assertEquals(Arrays.asList(Arrays.asList("15", "14", "13"), Arrays.asList("12", "13", "14")), grades);
    }

    @Test
    @DisplayName("extractNamesGenderBachelorAndGrades handles empty input stream")
    @SuppressWarnings("unchecked")
    void extractNamesGenderBachelorAndGrades_handlesEmptyInputStream() {
        InputStream inputStream = new ByteArrayInputStream("".getBytes());

        Map<String, Object> result = studentService.extractNamesGenderBachelorAndGrades(inputStream);

        List<String> names = (List<String>) result.get(StudentService.MAP_KEY_NAMES);
        List<String> genders = (List<String>) result.get(StudentService.MAP_KEY_GENDERS);
        List<String> bachelors = (List<String>) result.get(StudentService.MAP_KEY_BACHELORS);
        List<List<String>> grades = (List<List<String>>) result.get(StudentService.MAP_KEY_GRADES);

        assertTrue(names.isEmpty());
        assertTrue(genders.isEmpty());
        assertTrue(bachelors.isEmpty());
        assertTrue(grades.isEmpty());
    }

    @Test
    @DisplayName("extractNamesGenderBachelorAndGrades handles input stream with missing data")
    @SuppressWarnings("unchecked")
    void extractNamesGenderBachelorAndGrades_handlesInputStreamWithMissingData() {
        String csvContent = "1,John Doe,M,,15,14,13\n2,Jane Doe,,B,12,13,14";
        InputStream inputStream = new ByteArrayInputStream(csvContent.getBytes());

        Map<String, Object> result = studentService.extractNamesGenderBachelorAndGrades(inputStream);

        List<String> names = (List<String>) result.get(StudentService.MAP_KEY_NAMES);
        List<String> genders = (List<String>) result.get(StudentService.MAP_KEY_GENDERS);
        List<String> bachelors = (List<String>) result.get(StudentService.MAP_KEY_BACHELORS);
        List<List<String>> grades = (List<List<String>>) result.get(StudentService.MAP_KEY_GRADES);

        assertEquals(Arrays.asList("John Doe", "Jane Doe"), names);
        assertEquals(Arrays.asList("M", ""), genders);
        assertEquals(Arrays.asList("", "B"), bachelors);
        assertEquals(Arrays.asList(Arrays.asList("15", "14", "13"), Arrays.asList("12", "13", "14")), grades);
    }

    @Test
    @DisplayName("populateDatabaseFromCSV populates database with students from valid CSV file")
    void populateDatabaseFromCSV_populatesDatabaseWithStudentsFromValidCSVFile() throws IOException {
        MultipartFile multipartFile = mock(MultipartFile.class);
        byte[] content = "1,JOHN Doe,M,B,,,\n2,JANE Smith,F,,,,".getBytes();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getInputStream()).thenReturn(inputStream);

        ArgumentCaptor<Student> studentCaptor = ArgumentCaptor.forClass(Student.class);

        studentService.populateDatabaseFromCSV(multipartFile);

        verify(studentRepository, times(2)).save(studentCaptor.capture());
        verifyNoMoreInteractions(studentRepository);

        List<Student> capturedStudents = studentCaptor.getAllValues();

        assertEquals("JOHN Doe", capturedStudents.get(0).name());
        assertEquals("john.doe@reseau.eseo.fr", capturedStudents.get(0).email());
        assertEquals(Gender.MAN, capturedStudents.get(0).gender());
        assertTrue(capturedStudents.get(0).bachelor());

        assertEquals("JANE Smith", capturedStudents.get(1).name());
        assertEquals("jane.smith@reseau.eseo.fr", capturedStudents.get(1).email());
        assertEquals(Gender.WOMAN, capturedStudents.get(1).gender());
        assertFalse(capturedStudents.get(1).bachelor());
    }

    @Test
    @DisplayName("populateDatabaseFromCSV handles empty file")
    void populateDatabaseFromCSV_handlesEmptyFile() {
        MultipartFile multipartFile = mock(MultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(true);

        studentService.populateDatabaseFromCSV(multipartFile);

        verifyNoInteractions(studentRepository);
    }

    @Test
    void testGetStudentsByTeamId() {
        // Arrange
        Team team = new Team();
        Student student1 = new Student();
        Student student2 = new Student();
        when(teamService.getTeamById(1)).thenReturn(team);
        when(studentRepository.findStudentsByTeam(team)).thenReturn(Arrays.asList(student1, student2));

        // Act
        List<Student> students = studentService.getStudentsByTeamId(1);

        // Assert
        assertThat(students).hasSize(2);
        verify(teamService, times(1)).getTeamById(1);
        verify(studentRepository, times(1)).findStudentsByTeam(team);
    }

    @Test
    void testGetStudentsByTeamIdReturnsEmptyList() {
        // Arrange
        Team team = new Team();
        when(teamService.getTeamById(1)).thenReturn(team);
        when(studentRepository.findStudentsByTeam(team)).thenReturn(Arrays.asList());

        // Act
        List<Student> students = studentService.getStudentsByTeamId(1);

        // Assert
        assertThat(students).isEmpty();
        verify(teamService, times(1)).getTeamById(1);
        verify(studentRepository, times(1)).findStudentsByTeam(team);
    }

}