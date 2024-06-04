package fr.eseo.tauri.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import fr.eseo.tauri.exception.EmptyResourceException;
import fr.eseo.tauri.exception.GlobalExceptionHandler;
import fr.eseo.tauri.exception.ResourceNotFoundException;
import fr.eseo.tauri.model.*;
import fr.eseo.tauri.model.enumeration.Gender;
import fr.eseo.tauri.model.enumeration.GradeTypeName;
import fr.eseo.tauri.model.enumeration.RoleType;
import fr.eseo.tauri.repository.BonusRepository;
import fr.eseo.tauri.repository.GradeRepository;
import fr.eseo.tauri.repository.StudentRepository;
import fr.eseo.tauri.security.ApplicationSecurity;
import fr.eseo.tauri.util.CustomLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final AuthService authService;
    private final StudentRepository studentRepository;
    private final ProjectService projectService;
    private final TeamService teamService;
    private final GradeTypeService gradeTypeService;
    private final GradeService gradeService;
    private final RoleService roleService;
    @Lazy
    private final SprintService sprintService;
    private final PresentationOrderService presentationOrderService;
    private final BonusRepository bonusRepository;
    private final BonusService bonusService;
    private final ApplicationSecurity applicationSecurity;
    private final GradeRepository gradeRepository;
    private final UserService userService;

    public static final String MAP_KEY_NAMES = "names";
    public static final String MAP_KEY_GENDERS = "genders";
    public static final String MAP_KEY_BACHELORS = "bachelors";
    public static final String MAP_KEY_GRADES = "grades";
    private static final String PASSWORD = "password";

    public Student getStudentById(String token, Integer id) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "readStudent"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("student", id));
    }

    public List<Student> getAllStudentsByProject(String token, Integer projectId) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "readStudents"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        return studentRepository.findAllByProject(projectId);
    }

    public void createStudent(String token, Student student) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "addStudent"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        if(student.projectId() != null) student.project(projectService.getProjectById(token, student.projectId()));
        if(student.teamId() != null)student.team(teamService.getTeamById(token, student.teamId()));
        studentRepository.save(student);

        Role role = new Role();
        role.user(student);
        role.type(RoleType.OPTION_STUDENT);
        roleService.createRole(token, role);

        List<Sprint> sprints = sprintService.getAllSprintsByProject(token, student.projectId());
        if(!sprints.isEmpty()) {
            for (Sprint sprint : sprints) {
                PresentationOrder presentationOrder = new PresentationOrder(sprint, student);
                presentationOrderService.createPresentationOrder(token, presentationOrder);
                Bonus limitedBonus = new Bonus((float) 0, true, sprint, student);
                Bonus unlimitedBonus = new Bonus((float) 0, false, sprint, student);
                bonusService.createBonus(limitedBonus);
                bonusService.createBonus(unlimitedBonus);
            }
        }
    }

    public void updateStudent(String token, Integer id, Student updatedStudent) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "updateStudent"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }

        Student student = getStudentById(token, id);

        if (updatedStudent.gender() != null) student.gender(updatedStudent.gender());
        if (updatedStudent.bachelor() != null) student.bachelor(updatedStudent.bachelor());
        if (updatedStudent.teamRole() != null) student.teamRole(updatedStudent.teamRole());
        if (updatedStudent.projectId() != null) student.project(projectService.getProjectById(token, updatedStudent.projectId()));
        if (updatedStudent.teamId() != null) student.team(teamService.getTeamById(token, updatedStudent.teamId()));

        studentRepository.save(student);
    }

    public void deleteStudent(String token, Integer id) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "deleteStudent"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        getStudentById(token, id);
        studentRepository.deleteById(id);
    }

    public void deleteAllStudentsByProject(String token, Integer projectId) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "deleteStudent"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }
        var students = getAllStudentsByProject(token, projectId);
        for (var student : students) {
            userService.deleteUserById(token, student.id());
        }
        gradeTypeService.deleteAllImportedGradeTypes(token);
        teamService.deleteAllTeamsByProject(token, projectId);
    }


    /**
     * <b>HELPER METHOD</b>
     * This method is used to extract student data from a CSV file.
     * The data includes the student's name, gender, bachelor status, and grades.
     *
     * @param inputStream The input stream of the CSV file.
     * @return A map containing lists of names, genders, bachelor statuses, and grades.
     */
    public Map<String, Object> extractNamesGenderBachelorAndGrades(InputStream inputStream) throws CsvValidationException, IOException {
        Map<String, Object> result = new HashMap<>();
        List<String> names = new ArrayList<>();
        List<String> genders = new ArrayList<>();
        List<String> bachelors = new ArrayList<>();
        List<List<String>> grades = new ArrayList<>();

        boolean namesStarted = false;

        CSVReader reader = new CSVReader(new InputStreamReader(inputStream));
        String[] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            if (!namesStarted && hasNonEmptyValue(nextLine, 1)) {
                namesStarted = true;
            }
            if (namesStarted && !names.isEmpty() && !hasNonEmptyValue(nextLine, 1)) {
                break;
            }
            if (namesStarted && hasNonEmptyValue(nextLine, 1)) {
                names.add(nextLine[1]);
                genders.add(nextLine[2]);
                bachelors.add(nextLine.length > 3 ? nextLine[3] : "");
                grades.add(Arrays.asList(Arrays.copyOfRange(nextLine, 4, nextLine.length)));
            }
        }

        CustomLogger.info("Successfully extracted student data (names, genders, bachelors and grades ) from the CSV file.");

        result.put(MAP_KEY_NAMES, names);
        result.put(MAP_KEY_GENDERS, genders);
        result.put(MAP_KEY_BACHELORS, bachelors);
        result.put(MAP_KEY_GRADES, grades);

        return result;
    }


    /**
     * <b>HELPER METHOD</b>
     * Checks if the specified index in the given line contains a non-empty value.
     *
     * @param line  the array representing a line from the CSV file
     * @param index the index to check
     * @return {@code true} if the index contains a non-empty value, {@code false} otherwise
     */
    public static boolean hasNonEmptyValue(String[] line, int index) {
        return line.length > index && !line[index].trim().isEmpty();
    }


    /**
     * <b>HELPER  METHOD</b>
     * This method is used to create a Student object from the provided data.
     * The data includes the student's name, gender, and bachelor status.
     *
     * @param name the name of the student
     * @param gender the gender of the student
     * @param bachelor the bachelor status of the student
     * @return the created Student object
     * @throws IllegalArgumentException if the name or gender is null or empty, or if the bachelor status is null
     */
    public Student createStudentFromData(String token, String name, String gender, String bachelor, Integer projectId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("Gender cannot be null or empty");
        }
        if (bachelor == null) {
            throw new IllegalArgumentException("Bachelor status cannot be null");
        }

        Student student = new Student();
        student.name(name);
        student.gender(gender.equals("M") ? Gender.MAN : Gender.WOMAN);
        student.bachelor(!bachelor.isEmpty());
        student.project(projectService.getProjectById(token, projectId));
//        student.password(applicationSecurity.passwordEncoder().encode(PASSWORD));
        student.privateKey("privateKey");
        String[] nameParts = name.split(" "); // Divise le nom en deux parties basées sur l'espace
        student.email(nameParts[1].toLowerCase() + "." + nameParts[0].toLowerCase() + "@reseau.eseo.fr");
        return student;
    }

    /**
     * This method is used to populate the database with student data from a CSV file.
     * The CSV file is expected to contain the following data for each student:
     * - Name
     * - Gender
     * - Bachelor status
     * - Grades
     * - Coefficients
     * - Ratings
     * @param file The CSV file containing the student data.
     */
    @SuppressWarnings("unchecked")
    public void populateDatabaseFromCSV(String token, MultipartFile file, Integer projectId) throws IOException, CsvValidationException {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "addStudent"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }

        if (file.isEmpty()) {
            CustomLogger.info("Uploaded file is empty");
            throw new EmptyResourceException("uploaded file");
        }

        List<GradeType> gradeTypes = gradeTypeService.createGradeTypesFromCSV(token, file.getInputStream());
        CustomLogger.info("Successfully created GradeType objects from the CSV file.");
        Map<String, Object> extractedData = extractNamesGenderBachelorAndGrades(file.getInputStream());

        List<String> names = (List<String>) extractedData.get(MAP_KEY_NAMES);
        List<String> genders = (List<String>) extractedData.get(MAP_KEY_GENDERS);
        List<String> bachelors = (List<String>) extractedData.get(MAP_KEY_BACHELORS);
        List<List<String>> grades = (List<List<String>>) extractedData.get(MAP_KEY_GRADES);

        for (int i = 0; i < names.size(); i++) {
            Student student = createStudentFromData(token, names.get(i), genders.get(i), bachelors.get(i), projectId);
            createStudent(token, student);
            for (int j = 0; j < grades.get(i).size(); j++) {

                if(!grades.get(i).get(j).trim().isEmpty()) {
                    try {
                        Grade grade = new Grade();
                        grade.value(Float.parseFloat(grades.get(i).get(j).trim()));
                        grade.student(student);
                        grade.gradeType(gradeTypes.get(j));
                        gradeService.createGrade(token, grade);
                    } catch (NumberFormatException ignored) {
                        // Do nothing // If the grade is not a number, it is ignored
                    }
                }
            }
        }
        CustomLogger.info(String.format("Successfully populated database with %d students and their associated grades contained in the CSV file.", names.size()));
    }

    /**
     * This method is used to create a CSV file containing student data.
     * The CSV file includes the following data for each student:
     * - Name
     * - Gender
     * - Bachelor status
     * - Grades
     *
     * @return A byte array representing the CSV file.
     * @throws RuntimeException if an IOException occurs while creating the CSV file.
     */
    public byte[] createStudentsCSV(String token, Integer projectId) throws IOException {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "exportStudents"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }

        CustomLogger.info("Downloading students CSV");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream);
        List<GradeType> importedGrades = gradeTypeService.getAllImportedGradeTypes(token);
        List<Student> students = getAllStudentsByProject(token, projectId);

        CSVWriter csvWriter = new CSVWriter(writer);
        writeHeaders(csvWriter, importedGrades);
        writeStudentData(csvWriter, students, importedGrades);
        writeSummaryData(csvWriter, importedGrades.size());
        writer.flush();
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * <b>HELPER METHOD</b>
     * This method is used to write headers to the CSV file.
     *
     * @param csvWriter The CSVWriter object that is used to write to the CSV file.
     * @param importedGrades The list of imported grade types.
     */
    public void writeHeaders(CSVWriter csvWriter, List<GradeType> importedGrades) {
        String[] factors = new String[importedGrades.size() + 4];
        String[] headers = new String[importedGrades.size() + 4];
        Arrays.fill(headers, "");
        Arrays.fill(factors, "");
        headers[2] = "sexe M / F";
        int index = 5;
        for (GradeType gradeType : importedGrades) {
            if(!gradeType.name().equals(GradeTypeName.AVERAGE.displayName())) {
                headers[index] = gradeType.name();
                factors[index++] = gradeType.factor().toString();
            }
        }
        csvWriter.writeNext(factors);
        csvWriter.writeNext(headers);
    }

    /**
     * <b>HELPER METHOD</b>
     * This method is used to write student data to the CSV file.
     *
     * @param csvWriter The CSVWriter object that is used to write to the CSV file.
     * @param students  The list of students whose data is to be written to the CSV file.
     * @param importedGradeTypes The list of imported grade types.
     */
    public void writeStudentData(CSVWriter csvWriter, List<Student> students, List<GradeType> importedGradeTypes) {
        int studentIndex = 1;
        for (Student student : students) {
            String[] studentInfo = new String[importedGradeTypes.size() + 4];
            Arrays.fill(studentInfo, "");
            studentInfo[0] = String.valueOf(studentIndex++);
            studentInfo[1] = student.name();
            studentInfo[2] = student.gender().toString().equals("MAN") ? "M" : "F";
            studentInfo[3] = Boolean.TRUE.equals(student.bachelor()) ? "B" : "";

            int gradeIndex = 4;
            for (GradeType gradeType : importedGradeTypes) {
                Float grade = gradeService.getGradeByStudentAndGradeType(student, gradeType);
                studentInfo[gradeIndex++] = grade != null ? String.valueOf(grade) : "";
            }
            csvWriter.writeNext(studentInfo);
        }
    }

    /**
     * <b>HELPER METHOD</b>
     *  This method is used to write summary data to the CSV file.
     * @param csvWriter The CSVWriter object that is used to write to the CSV file.
     * @param numberOfGrades The number of imported grade types.
     */
    public void writeSummaryData(CSVWriter csvWriter, int numberOfGrades) {
        writeEmptyRows(csvWriter, 4, numberOfGrades + 4);
        writeCountRow(csvWriter, "Nombre F", studentRepository.countWomen(), numberOfGrades + 4);
        writeCountRow(csvWriter, "Nombre M", studentRepository.countTotal() - studentRepository.countWomen(), numberOfGrades + 4);
        String[] row = new String[numberOfGrades + 4];
        Arrays.fill(row, "");
        row[1] = "Nombre B";
        row[3] = String.valueOf(studentRepository.countBachelor());
        csvWriter.writeNext(row);
    }

    /**
     * <b>HELPER METHOD</b>
     * This method is used to write empty rows in the CSV file.
     *
     * @param csvWriter The CSVWriter object that is used to write to the CSV file.
     * @param numRows The number of empty rows to write. This is an integer.
     * @param rowLength The length of the row in the CSV file. This is an integer.
     */
    public void writeEmptyRows(CSVWriter csvWriter, int numRows, int rowLength) {
        String[] emptyRow = new String[rowLength];
        Arrays.fill(emptyRow, "");
        for (int i = 0; i < numRows; i++) {
            csvWriter.writeNext(emptyRow);
        }
    }

    /**
     * <b>HELPER METHOD</b>
     * This method is used to write a row in the CSV file that represents a count of a specific category of students.
     *
     * @param csvWriter The CSVWriter object that is used to write to the CSV file.
     * @param label The label for the count. This is a string that describes the category of students that are being counted.
     *              For example, "Nombre F" for the count of female students, "Nombre M" for the count of male students,
     *              or "Nombre B" for the count of bachelor students.
     * @param count The count of students in the specified category. This is an integer.
     * @param rowLength The length of the row in the CSV file. This is an integer.
     */
    public void writeCountRow(CSVWriter csvWriter, String label, int count, int rowLength) {
        String[] row = new String[rowLength];
        Arrays.fill(row, "");
        row[1] = label;
        row[2] = String.valueOf(count);
        csvWriter.writeNext(row);
    }

    public Bonus getStudentBonus(String token, Integer idStudent, Boolean limited, Integer sprintId) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "readBonuses"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }

        return bonusRepository.findStudentBonus(idStudent, limited, sprintId);
    }

    public List<Bonus> getStudentBonuses(String token, Integer idStudent, Integer sprintId) {

        return bonusRepository.findAllStudentBonuses(idStudent, sprintId);
    }

    public Double getIndividualTotalGrade(String token, Integer id, Integer sprintId) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "readGrades"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }

        Integer teamId = userService.getTeamByMemberId(token, id, getStudentById(token, id).projectId()).id();

        Double studentGradedTeamGrade = gradeRepository.findAverageByGradeTypeForTeam(teamId, sprintId, GradeTypeName.GLOBAL_TEAM_PERFORMANCE.displayName());

        Double individualGrade = gradeRepository.findAverageByGradeTypeForStudent(id, sprintId, GradeTypeName.INDIVIDUAL_PERFORMANCE.displayName());

        return (2*individualGrade + studentGradedTeamGrade)/3;
    }

    public Double getSprintGrade(String token, Integer studentId, Integer sprintId) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "readGrade"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }

        Integer teamId = userService.getTeamByMemberId(token, studentId, getStudentById(token, studentId).projectId()).id();

        double teamGrade = teamService.getTeamTotalGrade(token, teamId, sprintId);

        List<Bonus> studentBonuses = getStudentBonuses(token, studentId, sprintId);

        return 0.7*(Math.min(teamGrade + studentBonuses.stream().mapToDouble(Bonus::value).sum(), 20.0)) + 0.3*(getIndividualTotalGrade(token, studentId, sprintId));
    }

    public Grade getGradeByTypeAndAuthor(String token, Integer id, Integer gradeTypeId, Integer authorId, Integer sprintId) {
        if (!Boolean.TRUE.equals(authService.checkAuth(token, "readGrade"))) {
            throw new SecurityException(GlobalExceptionHandler.UNAUTHORIZED_ACTION);
        }

        return gradeRepository.findByStudentAndGradeTypeAndAuthor(id, gradeTypeId, authorId, sprintId);
    }

}
