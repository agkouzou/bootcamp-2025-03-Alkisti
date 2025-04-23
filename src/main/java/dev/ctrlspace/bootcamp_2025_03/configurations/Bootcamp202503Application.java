package dev.ctrlspace.bootcamp_2025_03.configurations;

import dev.ctrlspace.bootcamp_2025_03.controllers.UserController;
import dev.ctrlspace.bootcamp_2025_03.controllers.HelloController;
import dev.ctrlspace.bootcamp_2025_03.exceptions.BootcampControllerAdvice;
import dev.ctrlspace.bootcamp_2025_03.model.User;
import dev.ctrlspace.bootcamp_2025_03.repository.UserRepository;
import dev.ctrlspace.bootcamp_2025_03.services.UserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackageClasses = {
		UserController.class,
		UserService.class,
		BootcampControllerAdvice.class
})
@EnableJpaRepositories(basePackageClasses = {
		UserRepository.class
})
@EntityScan(basePackageClasses = {
		User.class
})
public class Bootcamp202503Application {

	public static void main(String[] args) {
		SpringApplication.run(Bootcamp202503Application.class, args);
	}

}
