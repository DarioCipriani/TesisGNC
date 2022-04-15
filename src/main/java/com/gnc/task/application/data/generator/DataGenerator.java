package com.gnc.task.application.data.generator;

import com.vaadin.flow.spring.annotation.SpringComponent;

import com.gnc.task.application.data.service.UserRepository;
import com.gnc.task.application.data.entity.User;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.gnc.task.application.data.Role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

@SpringComponent
public class DataGenerator {

	@Value("${users.admin-username}")
	private String adminUserName;
	@Value("${users.admin-password}")
	private String adminPassword;
	@Value("${users.admin-mail}")
	private String adminMail;

	@Bean
	public CommandLineRunner loadData(PasswordEncoder passwordEncoder, UserRepository userRepository) {
		return args -> {
			Logger logger = LoggerFactory.getLogger(getClass());
			if (userRepository.count() != 0L) {
				logger.info("Using existing database");
				return;
			}

			logger.info("Generating initial data");

			logger.info("... generating 1 User entities...");
			User admin = new User();
			admin.setName(adminUserName);
			admin.setUsername(adminMail);
			admin.setHashedPassword(passwordEncoder.encode(adminPassword));
			admin.setRoles(Collections.singleton(Role.ADMIN));
			userRepository.save(admin);

			logger.info("Generated initial data");
		};
	}

}
