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

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RsaUtil rsaUtil;
    private final StringRedisTemplate redisTemplate;

    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, RsaUtil rsaUtil,
            StringRedisTemplate redisTemplate) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.rsaUtil = rsaUtil;
        this.redisTemplate = redisTemplate;
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
}
