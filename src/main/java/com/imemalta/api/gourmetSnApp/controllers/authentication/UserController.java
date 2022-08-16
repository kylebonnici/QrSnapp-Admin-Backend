package com.imemalta.api.gourmetSnApp.controllers.authentication;

import com.imemalta.api.gourmetSnApp.entities.authentication.User;
import com.imemalta.api.gourmetSnApp.services.authentication.UserManagement;
import com.imemalta.api.gourmetSnApp.utils.VariantUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/")
@ResponseBody
public class UserController {

    private final UserManagement userManagement;

    @Autowired
    public UserController(UserManagement userManagement) {
        this.userManagement = userManagement;
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpSession session) {
        session.invalidate();
    }


    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public void login() {
    }


    @PreAuthorize("hasRole('ROLE_MANAGE_USERS')")
    @RequestMapping(value = "/users/", method = RequestMethod.GET)
    public Iterable<User> getAll(){
        return userManagement.getUsers();
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_USERS')")
    @RequestMapping(value = "/users/{username}", method = RequestMethod.GET)
    public User get(@PathVariable("username") String username){
        return userManagement.getUser(username);
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_USERS')")
    @RequestMapping(value = "/users/", method = RequestMethod.POST)
    public User add(@RequestBody User userDto){
        return userManagement.addUser(userDto);
    }


    @PreAuthorize("hasRole('ROLE_MANAGE_USERS')")
    @RequestMapping(value = "/users/{username}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("username") String username) {
        userManagement.delete(username);
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_USERS')")
    @RequestMapping(value = "/users/{username}", method = RequestMethod.PUT)
    public User update(@PathVariable("username") String username,
                       @RequestBody User userIn){
        return userManagement.updateUser(username, userIn);
    }

    @PreAuthorize("hasRole('ROLE_MANAGE_USERS')")
    @RequestMapping(value = "/users/{username}/changePassword", method = RequestMethod.PUT)
    public void changePassword(@PathVariable("username") String username,
                               @RequestParam("currentPassword") String currentPassword,
                               @RequestParam("newPassword") String newPassword ){
        userManagement.changePassword(username,
                currentPassword,
                newPassword);
    }

    @RequestMapping(value = "/user/changePassword", method = RequestMethod.PUT)
    public void changePasswordSession(@RequestParam("currentPassword") String currentPassword,
                                      @RequestParam("newPassword") String newPassword ){
        userManagement.changePassword(VariantUtils.currentSessionUsername(),
                currentPassword, newPassword);
    }

    @RequestMapping(value = "/user/", method = RequestMethod.GET)
    public User getSessionUser() {
        return userManagement.getUser(VariantUtils.currentSessionUsername());
    }

    @RequestMapping(value = "/user/", method = RequestMethod.PUT)
    public User updateSessionUser(@RequestBody User userIn) {
        return userManagement.updateUser(VariantUtils.currentSessionUsername(), userIn);
    }

    @RequestMapping(value = "/signup/", method = RequestMethod.POST)
    public void signUp(@RequestBody User userIn, @RequestParam("password") String password){
        userManagement.signUp(userIn, password);
    }

    @RequestMapping(value = "/isUsernameTaken", method = RequestMethod.GET)
    @Transactional(readOnly = true)
    public boolean isUsernameTaken(@RequestParam("username") String username){
        return userManagement.isUsernameTaken(username);
    }
}
