package com.example.project.service;

import com.example.project.model.Moment;
import com.example.project.repository.MomentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private MomentRepository momentRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Moment> moment = momentRepository.findByUsername(username);
        if (moment.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        Moment foundedMoment = moment.get();
        //return new User(foundedMoment.getUsername(), foundedMoment.getPassword(), new ArrayList<>()); //Старый вариант без ролей
        return new User(
                foundedMoment.getUsername(),
                foundedMoment.getPassword(),
                java.util.Collections.singletonList(new org.springframework.security.core.GrantedAuthority() {
                    @Override
                    public String getAuthority() {
                        return foundedMoment.getRole().getName();
                    }
                })
        );

    }
}
