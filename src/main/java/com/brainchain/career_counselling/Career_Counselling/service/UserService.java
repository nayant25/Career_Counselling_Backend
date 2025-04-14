package com.brainchain.career_counselling.Career_Counselling.service;

import org.springframework.stereotype.Service;
import com.brainchain.career_counselling.Career_Counselling.entity.User;
import com.brainchain.career_counselling.Career_Counselling.exceptions.NotFoundException;
import com.brainchain.career_counselling.Career_Counselling.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Service
public class UserService {
    UserRepository repository;
    
    public User getUserByEmail(String email) {
        return repository.findByEmail(email)
          .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }
}
