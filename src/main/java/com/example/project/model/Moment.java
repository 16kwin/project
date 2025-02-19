package com.example.project.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "moment")
@Data
public class Moment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id")  // Указываем имя колонки внешнего ключа
    private Role role;
}