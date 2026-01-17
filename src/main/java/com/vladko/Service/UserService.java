package com.vladko.Service;

import com.vladko.DTO.UserDTO;
import com.vladko.Entity.User;
import com.vladko.Exceptions.AuthException;
import com.vladko.Repositories.UserRepository;
import com.vladko.DTO.AuthRequestDTO;
import com.vladko.Utils.Crypt.CryptoUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO findByUsername(String username) {
        Optional<User> findUser = userRepository.findByUsername(username);
        if (findUser.isPresent()) {
            UserDTO userDTO = new UserDTO();
            userDTO.setLogin(findUser.get().getLogin());
            return userDTO;
        }
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

        // Получаем пользователя из базы и создаем DTO
        Optional<User> user = userRepository.findByUsername(loginUserDTO.getUsername());
        if (user.isPresent()) {
            UserDTO userDTO = new UserDTO();
            userDTO.setLogin(user.get().getLogin());
            return userDTO;
        }

        throw new AuthException("User not found");
    }

    public boolean isPasswordValid(AuthRequestDTO loginUserDTO) {
        return userRepository.getPasswordByLogin(loginUserDTO.getUsername())
                .map(dbHash -> CryptoUtils.checkPassword(loginUserDTO.getPassword(), dbHash))
                .orElse(false);
    }
}
