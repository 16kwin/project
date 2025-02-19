package com.example.project.controllers;

import com.example.project.model.Moment;
import com.example.project.model.Role;
import com.example.project.repository.RoleRepository;
import com.example.project.service.MomentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MomentService momentService;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(MomentService momentService, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder) {
        this.momentService = momentService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Moment registrationRequest) {
        // 1. Проверка, существует ли пользователь с таким именем
        if (momentService.findByUsername(registrationRequest.getUsername()).isPresent()) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        // 2. Создание нового пользователя
        Moment moment = new Moment();
        moment.setUsername(registrationRequest.getUsername());
        moment.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        moment.setFirstName(registrationRequest.getFirstName());
        moment.setLastName(registrationRequest.getLastName());

        // 3. Назначение роли USER по умолчанию
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role USER not found"));
        moment.setRole(userRole); //  <--  Теперь устанавливаем роль напрямую

        // 4. Сохранение пользователя в БД
        momentService.save(moment);

        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }
}