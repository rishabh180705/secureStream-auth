package com.securestream.auth.service;


import java.util.Optional;


import com.securestream.auth.dto.LoginRequest;
import com.securestream.auth.dto.LoginResponse;
import com.securestream.auth.entity.Subscription;
import com.securestream.auth.exception.EmailAlreadyExistsException;
import com.securestream.auth.security.JwtService;
import lombok.Data;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.securestream.auth.dto.RegisterRequest;
import com.securestream.auth.entity.Role;
import com.securestream.auth.entity.User;
import com.securestream.auth.repository.UserRepository;


@Data
@Service
public class AuthServiceImpl implements AuthService {

    private final  UserRepository userRepository;
    private  final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public String register(RegisterRequest request){

        Optional<User> existingUser =userRepository.findByEmail(request.getEmail());

        if(existingUser.isPresent()) {
            throw new EmailAlreadyExistsException( "Email already exists");
        }
        User userD = new User();
        userD.setEmail(request.getEmail());
        userD.setName(request.getName());
        userD.setPassword(
                passwordEncoder.encode(request.getPassword())
        );
        userD.setRole(Role.USER);
        userD.setEnabled(true);
        userD.setSubscription(Subscription.REGULAR);

        userRepository.save(userD);
        return "User Registered Successfully";

    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        ));


        UserDetails userDetails = (UserDetails) authentication.getPrincipal();


        String token = jwtService.generateToken(userDetails);


        return new LoginResponse(token);
    }




}
