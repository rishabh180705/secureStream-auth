package com.securestream.auth.service;

import com.securestream.auth.dto.UserDto;

import java.util.List;

public interface UserService {

    String deleteUser(String email);

    List<UserDto> getUsers();


    UserDto getUser(String email);
}
