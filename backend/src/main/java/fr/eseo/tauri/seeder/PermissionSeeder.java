package fr.eseo.tauri.seeder;

import fr.eseo.tauri.model.Permission;
import fr.eseo.tauri.model.enumeration.PermissionType;
import fr.eseo.tauri.model.enumeration.RoleType;
import fr.eseo.tauri.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static fr.eseo.tauri.model.enumeration.PermissionType.*;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PermissionSeeder {

	private static final Map<RoleType, List<PermissionType>> PERMISSIONS = Map.of(
			RoleType.IDENTIFIED_USER, List.of(LOGIN_OUT, VIEW_GRADE_SCALE, IMPORT_GENERATED_KEY),

			RoleType.SYSTEM_ADMINISTRATOR, List.of(IMPORT_GENERATED_KEY),

			RoleType.TECHNICAL_COACH, List.of(GRADE_SUPPORT_MATERIAL, GRADE_PRESENTATION_CONTENT, ADD_COMMENT_WG, ADD_ALL_TEAMS_COMMENT, ADD_ALL_TEAMS_FEEDBACK, SPRINTS_PAGE, COMMENT_TECHNICAL_SOLUTION, COMMENT_PROJECT_MANAGEMENT, GRADES_PAGE, RATING_PAGE),

			RoleType.SUPERVISING_STAFF, List.of(PREVIEW_TEAM, VALIDATION_TEAM_BEFORE_PREPUBLISH, FLAG_TEAM_WITHOUT_STUDENTS, FLAG_TEAM_WITH_STUDENTS, VIEW_TEAM_CHANGES, VIEW_TEAMS, VIEW_OWN_TEAM, VIEW_SPRINT_PROTOCOL, VIEW_OWN_TEAM_WITH_CRITERIA, VIEW_TEAMS_INFORMATIONS, GRADE_PRESENTATION_CONTENT, GRADE_SUPPORT_MATERIAL, GRADE_TECHNICAL_SOLUTION, GRADE_SPRINT_CONFORMITY, GRADE_PROJECT_MANAGEMENT, GRADE_INDIVIDUAL_PERFORMANCE, GRADE_CONFIRMATION, VIEW_ALL_ORAL_GRADES, VIEW_OWN_TEAM_COMMENT, GIVE_UNLIMITED_BONUS_MALUS, VALIDATION_LIMITED_BONUS_MALUS, VALIDATION_OWN_TEAM_GRADES, USE_KEY_OWN_TEAM, VERIFY_OWN_TEAM_KEY_USED, USE_KEY_ALL_TEAMS, VIEW_ALL_SPRINTS_GRADES, ADD_ALL_TEAMS_FEEDBACK, ADD_ALL_TEAMS_COMMENT, VIEW_FEEDBACK, VIEW_COMMENT, TEAMS_PAGE, MY_TEAM_PAGE, SPRINTS_PAGE, GRADES_PAGE, RATING_PAGE),

			RoleType.OPTION_LEADER, List.of(IMPORT, MODIFICATION_STUDENT_LIST, MANAGE_SPRINT, VIEW_ALL_WRITING_GRADES, EXPORT_INDIVIDUAL_GRADES, STUDENTS_PAGE, TEAMS_PAGE, SPRINTS_PAGE, GRADES_PAGE),

			RoleType.PROJECT_LEADER, List.of(IMPORT, MODIFICATION_STUDENT_LIST, EDIT_IMPORTED_GRADE_TYPES, EXPORT_STUDENT_LIST, TEAM_CREATION, TEAM_MANAGEMENT, PUBLISH_TEAMS, MANAGE_SPRINT, MANAGE_GRADE_SCALE, VIEW_ALL_WRITING_GRADES, VERIFY_ALL_KEYS_USED, EXPORT_INDIVIDUAL_GRADES, STUDENTS_PAGE, TEAMS_PAGE, SPRINTS_PAGE, GRADES_PAGE, GRADE_SCALES_PAGE),

			RoleType.OPTION_STUDENT, List.of(FLAG_TEAM_WITH_STUDENTS, VIEW_TEAM_CHANGES, VALIDATION_OWN_TEAM, VIEW_TEAMS, VIEW_OWN_TEAM, VIEW_SPRINT_PROTOCOL, VIEW_OWN_SPRINT_GRADE, TEAMS_PAGE, MY_TEAM_PAGE, SPRINTS_PAGE, GRADES_PAGE, RATING_PAGE),

			RoleType.TEAM_MEMBER, List.of(GRADE_GLOBAL_PERFORMANCE, PUBLISH_RUNNING_ORDER, VIEW_OWN_GRADE_COMMENT, VIEW_OWN_TEAM_GRADE, VIEW_TEAM_GRADE, LIMITED_BONUS_MALUS, VIEW_OWN_GRADES_WG, USE_KEY_OWN_TEAM, VIEW_OWN_SPRINT_GRADE, TEAMS_PAGE, MY_TEAM_PAGE, SPRINTS_PAGE, GRADES_PAGE, RATING_PAGE),

			RoleType.ESEO_ADMINISTRATION, List.of(),

			RoleType.JURY_MEMBER, List.of()
	);

	private final PermissionRepository permissionRepository;

	public void seed() {
		RoleType role;
		for (var entry : PERMISSIONS.entrySet()) {
			role = entry.getKey();
			var permissions = entry.getValue();

			for (var permission : permissions) {
				attributePermission(role, permission);
			}
		}
	}

	private void attributePermission(RoleType role, PermissionType permission) {
		var permissionEntity = new Permission();

		permissionEntity.role(role);
		permissionEntity.type(permission);

		permissionRepository.save(permissionEntity);
	}

}
