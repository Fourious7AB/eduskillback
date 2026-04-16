package com.example.eduskill.service;

import com.example.eduskill.dto.UserDto;
import org.springframework.data.domain.Page;

public interface UserService {


    UserDto updateUser(UserDto userDto,String userId);

    UserDto getUserByEmail(String email);

    UserDto getUserById(String userId);

    void deleteUser(String userId);

    UserDto createUser(UserDto userDto);

    Iterable<UserDto>getAllUser();

    Page<UserDto> getEnabledUsers(int page, int size);

    Page<UserDto> getDisabledUsers(int page, int size);



}