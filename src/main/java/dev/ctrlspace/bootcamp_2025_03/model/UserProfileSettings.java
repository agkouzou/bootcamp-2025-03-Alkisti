package dev.ctrlspace.bootcamp_2025_03.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserProfileSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Optional nickname for the assistant to refer to user
    private String nickname;

    // A brief introduction the user writes to "introduce" themselves to ChatGPT
    @Column(length = 1000)
    private String introduction;

    // What the user does (job or identity)
    private String job;

    // A list of traits the assistant should embody (stored as comma-separated values)
    @ElementCollection
    private List<String> traits;

    // Any additional free-form notes
    @Column(length = 2000)
    private String notes;

    // Link to the actual User
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @JsonBackReference
    private User user;
}
