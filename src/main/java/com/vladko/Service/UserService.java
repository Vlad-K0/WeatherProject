package com.vladko.Service;

import com.vladko.DTO.UserDTO;
import com.vladko.Entity.User;
import com.vladko.Exceptions.AuthException;
import com.vladko.Exceptions.NotFoundPassword;
import com.vladko.Repositories.UserRepository;
import com.vladko.DTO.AuthRequestDTO;
import com.vladko.Utils.Crypt.CryptoUtils;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.stereotype.Component;

import javax.naming.AuthenticationException;

@Component
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO findByUsername(String username) {
        userRepository.findByUsername(username);
        return new UserDTO();
    }

    public void registerUser(AuthRequestDTO registerUserDTO) {
        if (registerUserDTO.getUsername().isEmpty() || registerUserDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Login or password is empty");
        } // сделать глубокую проверку через класс валидации

        String hashedPassword = CryptoUtils.encryptPassword(registerUserDTO.getPassword());

        User user = User.builder()
                .login(registerUserDTO.getUsername())
                .password(hashedPassword)
                .build();

        userRepository.save(user);
    }

    public User loginUser(AuthRequestDTO loginUserDTO) {
        if (loginUserDTO.getUsername().isEmpty() || loginUserDTO.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Login or password is empty");
        }
        if (!isPasswordValid(loginUserDTO)) {
            throw new AuthException("Invalid login or password");
        }
        return userRepository.findByUsername(loginUserDTO.getUsername())
                .orElseThrow(() -> new AuthException("User not found"));
    }

    public boolean isPasswordValid(AuthRequestDTO loginUserDTO) {
        return userRepository.getPasswordByLogin(loginUserDTO.getUsername())
                .map(dbHash -> CryptoUtils.checkPassword(loginUserDTO.getPassword(), dbHash))
                .orElse(false);
    }
}
