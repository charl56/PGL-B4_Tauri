package fr.eseo.tauri.config;

import fr.eseo.tauri.seeder.*;
import net.datafaker.Faker;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class SeedConfig implements ApplicationListener<ContextRefreshedEvent> {

	private final Faker faker;

	private final UserSeeder userSeeder;
	private final TeamSeeder teamSeeder;
	private final StudentSeeder studentSeeder;
	private final GradeTypeSeeder gradeTypeSeeder;
	private final GradeSeeder gradeSeeder;
	private final PermissionSeeder permissionSeeder;
	private final RoleSeeder roleSeeder;

	@Autowired
	public SeedConfig(
			UserSeeder userSeeder, TeamSeeder teamSeeder, StudentSeeder studentSeeder,
			GradeTypeSeeder gradeTypeSeeder, GradeSeeder gradeSeeder, PermissionSeeder permissionSeeder,
			RoleSeeder roleSeeder
	) {
		this.faker = new Faker(new Locale("fr-FR"));

		this.userSeeder = userSeeder;
        this.teamSeeder = teamSeeder;
		this.studentSeeder = studentSeeder;
		this.gradeTypeSeeder = gradeTypeSeeder;
		this.gradeSeeder = gradeSeeder;
		this.permissionSeeder = permissionSeeder;
		this.roleSeeder = roleSeeder;
	}

	@Override
	public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
		userSeeder.seed(faker);
		// teamSeeder.seed(faker);
		// studentSeeder.seed(faker);
		// gradeTypeSeeder.seed(faker);
		// gradeSeeder.seed(faker);
		permissionSeeder.seed();
		// gradeTypeSeeder.seedTeamGradeType();
		roleSeeder.seed(faker);
	}

}
