package com.example.travelappbe.service;

import com.example.travelappbe.dto.RegisterRequestDto;
import com.example.travelappbe.dto.RegisterResponseDto;
import com.example.travelappbe.entity.User;
import com.example.travelappbe.entity.UserRole;
import com.example.travelappbe.exception.UserAlreadyExistsException;
import com.example.travelappbe.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with the provided email and password.
     *
     * @param registerRequestDto the registration request containing email and password
     * @return RegisterResponseDto with user details
     * @throws UserAlreadyExistsException if a user with the same email already exists
     */
    @Transactional
    public RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto) {
        // Check if user already exists
        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new UserAlreadyExistsException(
                    "User with email " + registerRequestDto.getEmail() + " already exists"
            );
        }

        // Hash the password
        String hashedPassword = passwordEncoder.encode(registerRequestDto.getPassword());

        // Create new user
        User user = new User(
                registerRequestDto.getEmail(),
                hashedPassword,
                UserRole.TOURIST
        );

        // Save user to database
        User savedUser = userRepository.save(user);

        // Return response DTO
        return new RegisterResponseDto(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getRole().toString(),
                savedUser.getCreatedAt()
        );
    }

    /**
     * Checks if a user with the given email exists.
     *
     * @param email the email to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Retrieves a user by email.
     *
     * @param email the email to search for
     * @return User if found, null otherwise
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
