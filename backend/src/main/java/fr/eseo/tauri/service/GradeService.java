package fr.eseo.tauri.service;

import fr.eseo.tauri.model.*;
import fr.eseo.tauri.repository.GradeRepository;
import fr.eseo.tauri.repository.*;
import fr.eseo.tauri.util.CustomLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GradeService {

	private final StudentRepository studentRepository;
	private final GradeRepository gradeRepository;
	private final TeamRepository teamRepository;
	private final GradeTypeRepository gradeTypeRepository;


	/**
	 * Constructor for TeamService.
	 *
	 * @param teamRepository      the team repository
	 * @param gradeRepository     the grade repository
	 * @param studentRepository   the student repository
	 * @param gradeTypeRepository the gradeTypeRepository
	 */
	@Autowired
	public GradeService(TeamRepository teamRepository, GradeRepository gradeRepository, StudentRepository studentRepository, GradeTypeRepository gradeTypeRepository) {
		this.teamRepository = teamRepository;
		this.gradeRepository = gradeRepository;
		this.studentRepository = studentRepository;
		this.gradeTypeRepository = gradeTypeRepository;
	}

	/**
	 * This method is used to update the mean of imported grades for each student.
	 */
	public void updateImportedMean() {
		var students = studentRepository.findAll();
		var grades = gradeRepository.findAll().stream().filter(grade -> grade.student() != null).toList();

		for (var student : students) {
			if (Boolean.TRUE.equals(student.bachelor())) continue;

			var studentGrades = grades.stream().filter(
					grade -> grade.student().id().equals(student.id())
							&& grade.gradeType().imported()
							&& !grade.gradeType().name().equalsIgnoreCase("mean")
							&& !grade.gradeType().name().equalsIgnoreCase("average")
			).toList();

			var mean = mean(studentGrades);

			gradeRepository.updateImportedMeanByStudentId(mean, student.id());
		}
		CustomLogger.logInfo("Updated imported mean for all students.");
	}

	/**
	 * This method calculates the mean of a list of grades.
	 *
	 * @param grades the list of Grade objects for which the mean is to be calculated
	 * @return the mean of the grades, or 0 if there are no grades or all grades have a factor of 0
	 */
	private float mean(List<Grade> grades) {
		var total = 0f;
		var factors = 0f;

		for (var grade : grades) {
			total += grade.value() * grade.gradeType().factor();
			factors += grade.gradeType().factor();
		}

		if (factors == 0) return 0;
		return total / factors;
	}

	/**
	 * This method is used to assign a grade to a team.
	 *
	 * @param teamName  the name of the team to whom the grade is assigned
	 * @param value     the value of the grade to be assigned
	 * @param gradeName the name of the grade type to be assigned
	 */
	public void assignGradeToTeam(String teamName, Integer value, String gradeName/*,User author, String comment*/) {
		Team team = teamRepository.findByName(teamName);
		GradeType gradeType = gradeTypeRepository.findByName(gradeName);
		if (team != null) {
			Grade grade = new Grade();
			grade.value(Float.valueOf(value));
			grade.gradeType(gradeType);
			grade.team(team);
			gradeTypeRepository.save(gradeType);
			gradeRepository.save(grade);
		} else {
			throw new IllegalArgumentException("L'équipe avec le nom fourni n'a pas été trouvée.");
		}
	}

	/**
	 * This method is used to assign a grade to a student.
	 *
	 * @param studentName the name of the student to whom the grade is assigned
	 * @param value       the value of the grade to be assigned
	 * @param gradeName   the name of the grade type to be assigned
	 */
	public void assignGradeToStudent(String studentName, Integer value, String gradeName) {
		Student student = studentRepository.findByName(studentName);
		GradeType gradeType = gradeTypeRepository.findByName(gradeName);
		if (student != null) {
			Grade grade = new Grade();
			grade.value(Float.valueOf(value));
			grade.gradeType(gradeType);
			grade.student(student);
			gradeTypeRepository.save(gradeType);
			gradeRepository.save(grade);
		}
	}


	/**
	 * This method is used to create a new Grade object and save it to the database.
	 *
	 * @param author    the author of the grade
	 * @param gradeType the type of the grade
	 * @param student   the student who received the grade
	 * @param value     the value of the grade
	 * @param comment   the comment for the grade
	 * @return the created Grade object that has been saved to the database
	 */
	public Grade createGrade(User author, GradeType gradeType, Student student, float value, String comment) {
		Grade grade = new Grade();
		grade.value(value);
		grade.comment(comment);
		grade.author(author);
		grade.gradeType(gradeType);
		grade.student(student);
		return gradeRepository.save(grade);
	}


	/**
	 * This method is used to create grades for a student from the provided grade types and values.
	 *
	 * @param student      the student for whom the grades are created
	 * @param valuesString the list of grade values as strings
	 * @param gradeTypes   the list of grade types
	 * @param comment      the comment for the grades
	 */
	public void createGradesFromGradeTypesAndValues(Student student, List<String> valuesString, List<GradeType> gradeTypes, String comment) {
		List<Grade> grades = gradeRepository.findAll();
		for (int i = 0; i < valuesString.size(); i++) {
			String gradeValue = valuesString.get(i).trim();
			if (!gradeValue.isEmpty()) {
				if (i == 0) {
					float gradeAsFloat = Float.parseFloat(gradeValue);
					grades.add(createGrade(null, gradeTypes.get(0), student, gradeAsFloat, comment));  // First grade is the average grade
				} else {
					try {
						float gradeAsFloat = Float.parseFloat(gradeValue);
						grades.add(createGrade(null, gradeTypes.get(i), student, gradeAsFloat, comment));
					} catch (NumberFormatException ignored) {
						// Do nothing
						// If the grade is not a number, it is ignored
					}
				}
			}
		}
		CustomLogger.logInfo("Successfully created grades for student " + student.name() + " from grade types and values contained in the provided CSV file.");
	}

}
