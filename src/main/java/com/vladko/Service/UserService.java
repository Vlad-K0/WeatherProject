package com.vladko.Service;

import com.vladko.Entity.Users;
import com.vladko.Repositories.UserRepository;
import com.vladko.DTO.RegisterUserDTO;
import org.springframework.stereotype.Component;

@Component
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void registerUser(RegisterUserDTO userDTO) {
        if (userDTO.getUsername().isEmpty() || userDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Login or password is empty");
        }
        Users user = Users.builder()
                .login(userDTO.getUsername())
                .password(userDTO.getPassword()).build();
        userRepository.save(user);
    }
}
