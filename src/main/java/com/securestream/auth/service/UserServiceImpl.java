package com.securestream.auth.service;

import com.securestream.auth.dto.UserDto;
import com.securestream.auth.entity.User;
import com.securestream.auth.exception.UserDoes_notExit;
import com.securestream.auth.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    @Override
    public String deleteUser(String email) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new UserDoes_notExit("User does not exist"));

        userRepository.deleteByEmail(email);


        return "User deleted successfully"+user;
    }

    @Override
    public List<UserDto> getUsers() {

      List<User> users= userRepository.findAll();
        ModelMapper modelMapper = new ModelMapper();
        return users.stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .toList();
    }

    @Override
    public UserDto getUser(String email) {
        User user=userRepository.findByEmailIgnoreCase(email).orElseThrow(
                ()->new UserDoes_notExit("User does not exist"));
        ModelMapper modelMapper = new ModelMapper();
        return modelMapper.map(user, UserDto.class);
    }
}
