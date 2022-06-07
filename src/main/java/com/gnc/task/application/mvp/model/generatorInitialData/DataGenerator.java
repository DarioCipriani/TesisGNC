package com.gnc.task.application.mvp.model.generatorInitialData;

import com.gnc.task.application.mvp.model.entity.Role;
import com.gnc.task.application.mvp.model.entity.User;
import com.gnc.task.application.mvp.model.repository.UserRepository;
import com.vaadin.flow.spring.annotation.SpringComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;

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
