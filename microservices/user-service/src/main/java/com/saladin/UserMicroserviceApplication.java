package com.saladin;

import com.saladin.dto.UserRequest;
import com.saladin.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableDiscoveryClient
public class UserMicroserviceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserMicroserviceApplication.class, args);
    }

    @Bean
    CommandLineRunner init(UserService userService) {
        return args -> {
            userService.promoteToAdminIfExists("saladin");
            // Crée un admin si pas déjà présent
            try {
                UserRequest admin = new UserRequest();
                admin.setUsername("admin");
                admin.setEmail("admin@example.com");
                admin.setPassword("123456");
                userService.createUser(admin);
            } catch (Exception e) {
                System.out.println("Admin already exists.");
            }
            // createUser() always assigns USER; elevate the bootstrap account for local frontend login
            userService.promoteToAdminIfExists("admin");
        };
    }
}
