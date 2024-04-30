package fr.eseo.tauri.service;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;
import fr.eseo.tauri.model.GradeType;
import fr.eseo.tauri.model.Student;
import fr.eseo.tauri.model.Team;
import fr.eseo.tauri.model.enumeration.Gender;
import fr.eseo.tauri.repository.StudentRepository;
import fr.eseo.tauri.util.CustomLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

/**
 * Service class for handling student-related operations.
 */
@Service
@RequiredArgsConstructor
public class StudentService {

    static final String MAP_KEY_NAMES = "names";
    static final String MAP_KEY_GENDERS = "genders";

    static final String MAP_KEY_BACHELORS = "bachelors";
    static final String MAP_KEY_GRADES = "grades";

    private final StudentRepository studentRepository;
    private final TeamService teamService;
    private final GradeTypeService gradeTypeService;
    private final GradeService gradeService;

    /**
     * This method is used to create a new student and save it to the repository.
     *
     * @param student the Student object to be saved
     * @throws IllegalArgumentException if the student's name is null or empty
     */
    public void createStudent(Student student) {
        if (student.name() == null || student.name().trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        studentRepository.save(student);
    }

    /**
     * Retrieves the quantity of students.
     *
     * @return the quantity of students
     */
    public Integer getStudentQuantity() {
        List<Student> students = studentRepository.findAll();
        return students.size();
    }

    /**
     * Retrieves students form a team.
     *
     * @param teamId The id of the team
     * @return a list of students
     */
    public List<Student> getStudentsByTeamId(Integer teamId) {
        Team team = teamService.getTeamById(teamId);
        return studentRepository.findStudentsByTeam(team);
    }

    public List<Student> getStudents() {
        return studentRepository.findAll();
    }

    public int getNumberWomen() {
        return studentRepository.countWomen();
    }

    public int getNumberStudents() {
        return studentRepository.countTotal();
    }

    public int getNumberBachelor() {
        return studentRepository.countBachelor();
    }

    /**
     * <b>HELPER METHOD</b>
     * This method is used to extract student data from a CSV file.
     * The data includes the student's name, gender, bachelor status, and grades.
     *
     * @param inputStream The input stream of the CSV file.
     * @return A map containing lists of names, genders, bachelor statuses, and grades.
     */
    public Map<String, Object> extractNamesGenderBachelorAndGrades(InputStream inputStream) {
        Map<String, Object> result = new HashMap<>();
        List<String> names = new ArrayList<>();
        List<String> genders = new ArrayList<>();
        List<String> bachelors = new ArrayList<>();
        List<List<String>> grades = new ArrayList<>();

        boolean namesStarted = false;

        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
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
        } catch (IOException | CsvValidationException e) {
            CustomLogger.logError("An error occurred in extractNamesGenderAndBachelor", e);
        }

        CustomLogger.logInfo("Successfully extracted student data (names, genders, bachelors and grades ) from the CSV file.");

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
    static boolean hasNonEmptyValue(String[] line, int index) {
        return line.length > index && !line[index].trim().isEmpty();
    }


    /**
     * <b>HELPER  METHOD</b>
     * This method is used to create a Student object from the provided data.
     * The data includes the student's name, gender, and bachelor status.
     *
     * @param name     the name of the student
     * @param gender   the gender of the student
     * @param bachelor the bachelor status of the student
     * @return the created Student object
     * @throws IllegalArgumentException if the name or gender is null or empty, or if the bachelor status is null
     */
    Student createStudentFromData(String name, String gender, String bachelor) {
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
        student.teamRole("Not assigned");
        student.project(null);
        student.team(null); // Team is not assigned yet
        student.password("password");
        student.privateKey("privateKey");
        student.email(name.toLowerCase().replace(" ", ".") + "@reseau.eseo.fr");
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
     *
     * @param file The CSV file containing the student data.
     */
    @SuppressWarnings("unchecked")
    public void populateDatabaseFromCSV(MultipartFile file) {
        if (file.isEmpty()) {
            CustomLogger.logError("Uploaded file is empty");
            return;
        }

        try {
            List<GradeType> gradeTypes = gradeTypeService.createGradeTypesFromCSV(file.getInputStream());
            CustomLogger.logInfo("Successfully created GradeType objects from the CSV file.");
            Map<String, Object> extractedData = extractNamesGenderBachelorAndGrades(file.getInputStream());

            List<String> names = (List<String>) extractedData.get(MAP_KEY_NAMES);
            List<String> genders = (List<String>) extractedData.get(MAP_KEY_GENDERS);
            List<String> bachelors = (List<String>) extractedData.get(MAP_KEY_BACHELORS);
            List<List<String>> gradesList = (List<List<String>>) extractedData.get(MAP_KEY_GRADES);

            for (int i = 0; i < names.size(); i++) {
                Student student = createStudentFromData(names.get(i), genders.get(i), bachelors.get(i));
                createStudent(student);
                gradeService.createGradesFromGradeTypesAndValues(student, gradesList.get(i), gradeTypes, "Imported grades");
            }

            CustomLogger.logInfo(String.format("Successfully populated database with %d students and their associated grades contained in the CSV file.", names.size()));
        } catch (Exception e) {
            CustomLogger.logError("An error occurred while handling the uploaded file", e);
            throw new RuntimeException("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * This method is used to delete all students that have been imported into the database.
     */
    public void deleteAllImportedStudentsAndGradeTypes() {
        try {
            studentRepository.deleteAll();
            CustomLogger.logInfo("Successfully deleted all imported students from the database.");
        } catch (Exception e) {
            CustomLogger.logError("An error occurred while deleting imported students", e);
        }
        gradeTypeService.deleteAllImportedGradeTypes();
        CustomLogger.logInfo("Successfully deleted all imported grade types from the database.");
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
    public byte[] createStudentsCSV() {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(byteArrayOutputStream);
        List<GradeType> importedGrades = gradeTypeService.getImportedGradeTypes();
        List<Student> students = getStudents();

        try (CSVWriter csvWriter = new CSVWriter(writer)) {
            writeHeaders(csvWriter, importedGrades);
            writeStudentData(csvWriter, students, importedGrades);
            writeSummaryData(csvWriter, importedGrades.size());
            writer.flush();
        } catch (IOException e) {
            CustomLogger.logError("An error occurred while creating the CSV file", e);
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * <b>HELPER METHOD</b>
     * This method is used to write headers to the CSV file.
     *
     * @param csvWriter The CSVWriter object that is used to write to the CSV file.
     * @param importedGrades The list of imported grade types.
     */
    void writeHeaders(CSVWriter csvWriter, List<GradeType> importedGrades) {
        String[] factors = new String[importedGrades.size() + 4];
        String[] headers = new String[importedGrades.size() + 4];
        Arrays.fill(headers, "");
        Arrays.fill(factors, "");
        headers[2] = "sexe M / F";
        int index = 5;
        for (GradeType gradeType : importedGrades) {
            if(gradeType.name().equals("AVERAGE")){
                continue;
            }
            headers[index] = gradeType.name();
            factors[index++] = gradeType.factor().toString();
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
     * @param importedGrades The list of imported grade types.
     */
    void writeStudentData(CSVWriter csvWriter, List<Student> students, List<GradeType> importedGrades) {
        int studentIndex = 1;
        for (Student student : students) {
            String[] studentInfo = new String[importedGrades.size() + 4];
            Arrays.fill(studentInfo, "");
            studentInfo[0] = String.valueOf(studentIndex++);
            studentInfo[1] = student.name();
            studentInfo[2] = student.gender().toString().equals("MAN") ? "M" : "F";
            studentInfo[3] = student.bachelor() ? "B" : "";

            int gradeIndex = 4;
            for (GradeType gradeType : importedGrades) {
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
    void writeSummaryData(CSVWriter csvWriter, int numberOfGrades) {
        writeEmptyRows(csvWriter, 4, numberOfGrades + 4);
        writeCountRow(csvWriter, "Nombre F", getNumberWomen(), numberOfGrades + 4);
        writeCountRow(csvWriter, "Nombre M", getNumberStudents() - getNumberWomen(), numberOfGrades + 4);
        String[] row = new String[numberOfGrades + 4];
        Arrays.fill(row, "");
        row[1] = "Nombre B";
        row[3] = String.valueOf(getNumberBachelor());
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
    void writeEmptyRows(CSVWriter csvWriter, int numRows, int rowLength) {
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
    void writeCountRow(CSVWriter csvWriter, String label, int count, int rowLength) {
        String[] row = new String[rowLength];
        Arrays.fill(row, "");
        row[1] = label;
        row[2] = String.valueOf(count);
        csvWriter.writeNext(row);
    }

}
