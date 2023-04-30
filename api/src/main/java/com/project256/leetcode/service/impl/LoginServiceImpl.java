package com.project256.leetcode.service.impl;

import com.project256.leetcode.service.LoginService;
import com.project256.leetcode.util.LoginUtil;
import com.project256.leetcode.util.SignUpUtil;
import org.apache.http.client.CookieStore;
import org.springframework.stereotype.Service;

@Service
public class LoginServiceImpl implements LoginService {
    @Override
    public CookieStore login(String username, String pwd) {
        return LoginUtil.login(username, pwd);
    }

    @Override
    public CookieStore signUp(String username, String email, String password1, String password2) {
        return SignUpUtil.signUp(username, email, password1, password2);
    }

    @Override
    public CookieStore resetPassword(String email) {
        return SignUpUtil.resetPassword(email);
    }
}
