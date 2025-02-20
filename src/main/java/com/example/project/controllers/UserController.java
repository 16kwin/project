package com.example.project.controllers;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.project.model.Moment;
import com.example.project.service.MomentService;
import com.example.project.dto.MomentDTO; // Импортируем DTO

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private MomentService momentService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        System.out.println("HERE");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();

        Moment user = momentService.findByUsername(currentUserName).orElse(null);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        // Преобразуем Moment в MomentDTO
        MomentDTO userDTO = new MomentDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setFirstName(user.getFirstName());
        userDTO.setLastName(user.getLastName());
        userDTO.setRoleName(user.getRole().getName()); // Получаем только имя роли

        return ResponseEntity.ok(userDTO); // Возвращаем DTO
    }
}