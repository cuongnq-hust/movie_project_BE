package dev.cuongnq.moives.service;

import dev.cuongnq.moives.auth.AuthenticationRequest;
import dev.cuongnq.moives.auth.AuthenticationResponse;
import dev.cuongnq.moives.auth.RegisterRequest;
import dev.cuongnq.moives.model.Role;
import dev.cuongnq.moives.model.User;
import dev.cuongnq.moives.repository.RoleCustomRepo;
import dev.cuongnq.moives.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleCustomRepo roleCustomRepo;
    private final UserService userService;

    public ResponseEntity<?> register(RegisterRequest registerRequest) {
        try {
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new IllegalArgumentException("User with email " + registerRequest.getEmail() + " already exists");
            }

            User user = userService.saveUser(new User(
                    registerRequest.getMobile_number(),
                    registerRequest.getUser_name(),
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    new HashSet<>()
            ));

            userService.addToUser(registerRequest.getEmail(), "ROLE_USER"); // default role

            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    public ResponseEntity<?> authenticate(AuthenticationRequest authenticationRequest) {
        try {
            User user = userRepository.findByEmail(authenticationRequest.getEmail())
                    .orElseThrow(() -> new NoSuchElementException("User not found"));

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getEmail(),
                    authenticationRequest.getPassword()
            ));

            List<Role> roles = roleCustomRepo.getRole(user);

            Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
            Set<Role> set = new HashSet<>();

            roles.forEach(role -> {
                set.add(new Role(role.getName()));
                authorities.add(new SimpleGrantedAuthority(role.getName()));
            });

            user.setRoles(set);
            set.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));

            var jwtAccessToken = jwtService.generateToken(user, authorities);
            var jwtRefreshToken = jwtService.generateRefreshToken(user, authorities);

            return ResponseEntity.ok(AuthenticationResponse.builder()
                    .access_token(jwtAccessToken)
                    .refresh_token(jwtRefreshToken)
                    .email(user.getEmail())
                    .user_name(user.getUser_name())
                    .build());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body("Invalid Credential");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }
}