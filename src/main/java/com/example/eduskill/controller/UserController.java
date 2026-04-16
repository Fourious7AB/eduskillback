package com.example.eduskill.controller;


import com.example.eduskill.dto.UserDto;
import com.example.eduskill.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;



    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserDto userDto){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userDto));
    }

    @GetMapping
    public ResponseEntity<Iterable<UserDto>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUser());
    }

    @GetMapping("/email/{emailId}")
    public ResponseEntity<UserDto> getUserByEmail(@PathVariable String emailId){
        return ResponseEntity.ok(userService.getUserByEmail(emailId));
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId){
        userService.deleteUser(userId);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(@RequestBody UserDto userDto,@PathVariable String userId){
        return ResponseEntity.ok(userService.updateUser(userDto,userId));
    }
    @GetMapping("/{userId}")
    public ResponseEntity<UserDto> getUserById(@PathVariable String userId){
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/enabled")
    public ResponseEntity<Page<UserDto>> getEnabledUsers(
            @RequestParam int page

    ){
        return ResponseEntity.ok(userService.getEnabledUsers(page, 10));
    }

    @GetMapping("/disabled")
    public ResponseEntity<Page<UserDto>> getDisabledUsers(
            @RequestParam int page
    ){
        return ResponseEntity.ok(userService.getDisabledUsers(page, 10));
    }



}
