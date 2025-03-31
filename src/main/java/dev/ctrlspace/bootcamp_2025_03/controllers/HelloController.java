package dev.ctrlspace.bootcamp_2025_03.controllers;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {



    @GetMapping("/hello")
    public String hello(){

        return """
                <h1>
                Bootcamp 2025.03 Repo
                </h1>
                                
                <p>
                This is a repository for the Bootcamp 2025.03. It contains all the code and documentation for the bootcamp.
                </p>
                                
                <h2>
                Getting Started
                </h2>
                                
                <p>
                To use this code you need to:
                <ul>
                <li>Intall IntelliJ or any other IDE</li>
                <li>Clone this repository</li>
                <li>Open the project in your IDE</li>
                <li>Install Java 21 and above</li>
                <li>Run the project</li>
                </ul>
                """;
    }

}
