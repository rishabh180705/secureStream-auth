package com.securestream.auth.controller;

import com.securestream.auth.dto.UserDto;
import com.securestream.auth.service.UserService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/api/users")
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    private static final Logger logger =
            LoggerFactory.getLogger(UserController.class);
   @GetMapping("/getuser")
   public UserDto getUser(@RequestParam("email") String email){

       return userService.getUser(email);

   }

   @GetMapping("/getall")
   public List<UserDto> getUsers(){
     List<UserDto> users = userService.getUsers();

       if(users.isEmpty()){
           return null;
       }
     return users;
   }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable("email") String email){
         String resp= userService.deleteUser(email);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resp);
    }

}
