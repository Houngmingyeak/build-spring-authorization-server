package co.istad.springauthorizationserver.service;

import co.istad.springauthorizationserver.dto.RegisterRequest;
import co.istad.springauthorizationserver.dto.UserResponse;
import co.istad.springauthorizationserver.entity.Role;
import co.istad.springauthorizationserver.entity.User;
import co.istad.springauthorizationserver.exception.DuplicateResourceException;
import co.istad.springauthorizationserver.repository.RoleRepository;
import co.istad.springauthorizationserver.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {

        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(username)) {
            throw new DuplicateResourceException("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists: " + email);
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException(
                        "ROLE_USER not found. Check the Flyway V1 migration."));

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);

        List<String> roleNames = new ArrayList<>();
        for (Role role : savedUser.getRoles()) {
            roleNames.add(role.getName());
        }

        UserResponse response = new UserResponse();
        response.setId(savedUser.getId());
        response.setUsername(savedUser.getUsername());
        response.setEmail(savedUser.getEmail());
        response.setEnabled(savedUser.isEnabled());
        response.setRoles(roleNames);
        response.setCreatedAt(savedUser.getCreatedAt());

        return response;
    }
}