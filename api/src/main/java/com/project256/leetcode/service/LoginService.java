package com.project256.leetcode.service;

import org.apache.http.client.CookieStore;

public interface LoginService {
    CookieStore login(String username, String pwd);

    CookieStore signUp(String username, String email, String password1, String password2);

    CookieStore resetPassword(String email);
}
