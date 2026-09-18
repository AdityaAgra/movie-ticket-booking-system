package com.aditya.movieticketbooking.user;

import com.aditya.movieticketbooking.common.enums.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Configuration
public class UserBootstrapDataInitializer {

    @Bean
    ApplicationRunner bootstrapUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.enabled:true}") boolean enabled,
            @Value("${app.bootstrap.admin.email:admin@moviebooking.local}") String adminEmail,
            @Value("${app.bootstrap.admin.password:admin123}") String adminPassword,
            @Value("${app.bootstrap.customer.email:customer@moviebooking.local}") String customerEmail,
            @Value("${app.bootstrap.customer.password:customer123}") String customerPassword) {
        return arguments -> createInitialUsers(
                userRepository,
                passwordEncoder,
                enabled,
                adminEmail,
                adminPassword,
                customerEmail,
                customerPassword);
    }

    @Transactional
    void createInitialUsers(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            boolean enabled,
            String adminEmail,
            String adminPassword,
            String customerEmail,
            String customerPassword) {
        if (!enabled) {
            return;
        }

        createIfMissing(userRepository, passwordEncoder, "Local Admin", adminEmail, adminPassword, Role.ADMIN);
        createIfMissing(userRepository, passwordEncoder, "Local Customer", customerEmail, customerPassword, Role.CUSTOMER);
    }

    private void createIfMissing(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String name,
            String email,
            String password,
            Role role) {
        if (!userRepository.existsByEmail(email)) {
            userRepository.save(User.create(name, email, passwordEncoder.encode(password), role));
        }
    }
}
