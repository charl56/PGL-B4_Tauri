package fr.eseo.tauri.controller;

import fr.eseo.tauri.model.User;
import fr.eseo.tauri.util.CustomLogger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public Boolean login(@RequestBody User user) {
        CustomLogger.logInfo(user.email() + " is trying to log in");

        return true;
    }

    @PostMapping("/logon")
    public Boolean logon(@RequestBody User user) {
        CustomLogger.logInfo(user.email() + " is trying to log on");


        return true;
    }

    @PostMapping("/logout")
    public Boolean logout(@RequestBody User user) {
        CustomLogger.logInfo(user.email() + " is trying to log out");


        return true;
    }


}
