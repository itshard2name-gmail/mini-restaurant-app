package com.example.auth;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import com.example.auth.model.Role;
import com.example.auth.model.User;
import com.example.auth.repository.RoleRepository;
import com.example.auth.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RsaUtil rsaUtil;
    private final StringRedisTemplate redisTemplate;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, RsaUtil rsaUtil,
            StringRedisTemplate redisTemplate, UserRepository userRepository, RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.rsaUtil = rsaUtil;
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/quick-login")
    public ResponseEntity<?> quickLogin(@RequestBody QuickLoginRequest request) {
        try {
            String phone = request.getPhone();
            if (phone == null || phone.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Phone number is required");
            }

            // Check if user exists
            User user = userRepository.findByUsername(phone).orElse(null);

            if (user == null) {
                // Register new user
                user = new User();
                user.setUsername(phone);
                // Set a random/dummy password (users can reset later if implemented, but
                // primarily use phone)
                user.setPassword(passwordEncoder.encode("{noop}" + java.util.UUID.randomUUID().toString()));

                Role userRole = roleRepository.findByName("ROLE_USER")
                        .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                Set<Role> roles = new HashSet<>();
                roles.add(userRole);
                user.setRoles(roles);

                userRepository.save(user);
            }

            // Generate Token directly (Bypassing AuthManager since we trust the phone input
            // for MVP)
            List<String> roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toList());

            String token = jwtUtil.generateToken(user.getUsername(), roles);
            redisTemplate.opsForValue().set("token:" + user.getUsername(), token, 24, TimeUnit.HOURS);

            return ResponseEntity.ok(Map.of("token", token, "roles", roles));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing quick login");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Decrypt password
            String decryptedPassword;
            try {
                decryptedPassword = rsaUtil.decrypt(request.getPassword());
            } catch (Exception e) {
                return ResponseEntity.status(401).body("Invalid credentials (decryption error)");
            }

            // Authenticate using Spring Security
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), decryptedPassword));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get Roles
            List<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            // Generate Token
            if (request.getUsername() == null) {
                return ResponseEntity.status(400).body("Username cannot be null");
            }
            String token = jwtUtil.generateToken(request.getUsername(), roles);

            // Store in Redis
            redisTemplate.opsForValue().set("token:" + request.getUsername(), token, 24, TimeUnit.HOURS);

            return ResponseEntity.ok(Map.of("token", token, "roles", roles));

        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error processing login");
        }
    }

    @GetMapping("/publicKey")
    public ResponseEntity<String> getPublicKey() {
        return ResponseEntity.ok(rsaUtil.getPublicKey());
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class QuickLoginRequest {
        private String phone;

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }
    }
}
