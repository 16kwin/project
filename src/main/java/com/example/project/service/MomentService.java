package com.example.project.service;

import com.example.project.model.Moment;
import com.example.project.repository.MomentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MomentService {

    private final MomentRepository momentRepository;

    @Autowired
    public MomentService(MomentRepository momentRepository) {
        this.momentRepository = momentRepository;
    }

    public Optional<Moment> findByUsername(String username) {
        return momentRepository.findByUsername(username);
    }

    public Moment save(Moment moment) {
        return momentRepository.save(moment);
    }

}