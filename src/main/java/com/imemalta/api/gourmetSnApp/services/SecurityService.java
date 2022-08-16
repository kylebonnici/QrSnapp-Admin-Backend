package com.imemalta.api.gourmetSnApp.services;

public interface SecurityService {
    String findLoggedInUsername();

    void autoLogin(String username, String password);
}
