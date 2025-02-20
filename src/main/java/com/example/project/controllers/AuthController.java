package com.example.project.controllers;

import com.example.project.model.Moment;
import com.example.project.model.Role;
import com.example.project.repository.RoleRepository;
import com.example.project.service.CustomUserDetailsService;
import com.example.project.service.MomentService;
import com.example.project.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final MomentService momentService;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthController(MomentService momentService, RoleRepository roleRepository, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, CustomUserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.momentService = momentService;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, Object> registrationRequest) {
        // 1. Проверка, существует ли пользователь с таким именем
        if (momentService.findByUsername((String) registrationRequest.get("username")).isPresent()) {
            return new ResponseEntity<>("Username is already taken!", HttpStatus.BAD_REQUEST);
        }

        // 2. Создание нового пользователя
        Moment moment = new Moment();
        moment.setUsername((String) registrationRequest.get("username"));
        moment.setPassword(passwordEncoder.encode((String) registrationRequest.get("password")));
        moment.setFirstName((String) registrationRequest.get("firstName"));
        moment.setLastName((String) registrationRequest.get("lastName"));

        // 3. Назначение роли
        String roleName = (String) registrationRequest.get("role"); // Получаем выбранную роль из запроса
        Role userRole = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role " + roleName + " not found"));
        moment.setRole(userRole); //  Теперь устанавливаем роль

        // 4. Сохранение пользователя в БД
        momentService.save(moment);

        return new ResponseEntity<>("User registered successfully", HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
        } catch (AuthenticationException e) {
            return new ResponseEntity<>("Invalid username or password", HttpStatus.UNAUTHORIZED);
        }

        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(username);

        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(Map.of("token", jwt));
    }
    @GetMapping("/user/me")
public ResponseEntity<?> getMe(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Not authenticated");
    }

    // Получаем имя пользователя из объекта Authentication
    String username = authentication.getName();

    // Находим пользователя в базе данных по имени пользователя
    Moment moment = momentService.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found")); // Обрабатываем случай, если пользователь не найден
    // Создаем объект, содержащий только нужные данные профиля
    Map<String, String> profileData = Map.of(
            "username", moment.getUsername(),
            "firstName", moment.getFirstName(),
            "lastName", moment.getLastName()
    );

    // Возвращаем данные профиля
    return ResponseEntity.ok(profileData);
}
}