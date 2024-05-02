package fr.eseo.tauri.config;

import fr.eseo.tauri.seeder.*;
import fr.eseo.tauri.util.ListUtil;
import net.datafaker.Faker;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class SeedConfig implements ApplicationListener<ContextRefreshedEvent> {

	private static final String FAKER_LANGUAGE = "fr-FR";
	private static final String[] HIBERNATE_MODES = {"create", "create-drop"};

	private final Faker faker;

	private final UserSeeder userSeeder;
	private final GradeTypeSeeder gradeTypeSeeder;
	private final PermissionSeeder permissionSeeder;
	private final RoleSeeder roleSeeder;
	private final ProjectSeeder projectSeeder;

	@Value("${spring.jpa.hibernate.ddl-auto}")
	private String hibernateMode;

	private final NotificationSeeder notificationSeeder;

	@Autowired
	public SeedConfig(UserSeeder userSeeder, GradeTypeSeeder gradeTypeSeeder, PermissionSeeder permissionSeeder, RoleSeeder roleSeeder, ProjectSeeder projectSeeder, NotificationSeeder notificationSeeder) {
		this.faker = new Faker(new Locale(FAKER_LANGUAGE));

        this.faker = new Faker(new Locale("fr-FR"));

		this.userSeeder = userSeeder;
		this.gradeTypeSeeder = gradeTypeSeeder;
		this.permissionSeeder = permissionSeeder;
		this.roleSeeder = roleSeeder;
		this.projectSeeder = projectSeeder;
		this.notificationSeeder = notificationSeeder;
	}

	@Override
	public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
		if (!ListUtil.contains(List.of(HIBERNATE_MODES), hibernateMode)) return;

		userSeeder.seed(faker);
		gradeTypeSeeder.seed();
		permissionSeeder.seed();
		roleSeeder.seed(faker);
		notificationSeeder.seed(faker);
		projectSeeder.seed();
	}

}
