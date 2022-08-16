package com.imemalta.api.gourmetSnApp.controllers.authentication;

import com.imemalta.api.gourmetSnApp.services.authentication.TokenManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/")
@ResponseBody
public class TokenController {
    private final TokenManagement tokenManagement;

    @Autowired
    public TokenController(TokenManagement tokenManagement) {
        this.tokenManagement = tokenManagement;
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_USERS')")
    @RequestMapping(value = "/users/{username}/requestFirstPasswordToken", method = RequestMethod.PUT)
    public void requestFirstPasswordToken(@PathVariable("username") String username){
        tokenManagement.requestFirstPasswordToken(username);
    }

    @RequestMapping(value = "/requestPasswordReset", method = RequestMethod.PUT)
    public void requestPasswordResetToken(@RequestParam("username") String username){
        tokenManagement.requestPasswordResetToken(username);
    }

    @RequestMapping(value = "/sendActivationToken", method = RequestMethod.PUT)
    public void sendEmailActivationToken(@RequestParam("username") String username){
        tokenManagement.sendEmailActivationToken(username);
    }

    @RequestMapping(value = "/activate", method = RequestMethod.PUT)
    public void activateUser(@RequestParam("token") String tokenStr){
        tokenManagement.activateUser(tokenStr);
    }

    @RequestMapping(value = "/activateWithPassword", method = RequestMethod.PUT)
    public void activateUserWithPassword(@RequestParam("token") String tokenStr,
                                                           @RequestParam("password") String password){
        tokenManagement.activateUserWithPassword(tokenStr, password);
    }

    @RequestMapping(value = "/resetPassword", method = RequestMethod.PUT)
    public void resetPassword(@RequestParam("token") String tokenStr,
                                                @RequestParam("password") String password){
        tokenManagement.resetPassword(tokenStr, password);
    }
}
