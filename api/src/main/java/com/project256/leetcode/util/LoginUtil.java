package com.project256.leetcode.util;

import com.alibaba.fastjson.JSON;
import com.project256.leetcode.common.Error;
import com.project256.leetcode.model.login.LoginResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

import static com.project256.leetcode.util.HttpUtil.getCookies;
import static com.project256.leetcode.util.HttpUtil.getCsrfToken;

public class LoginUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoginUtil.class);
    static final String LOGIN_URL = "https://leetcode.com/accounts/login/";

    static HttpUriRequest buildLoginRequest(String uri, String refer, HttpEntity params) {
        HttpUriRequest req = RequestBuilder.post(uri)
                .setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 6.1; Win64; x64) " +
                        "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36")
                //boundary is necessary
                .setHeader(HttpHeaders.CONTENT_TYPE, "multipart/form-data;boundary=----WbKitFormBoundarysfevHGSzVFcFIb9e")
                .setHeader(HttpHeaders.REFERER, refer)
                .setHeader("x-requested-with", "XMLHttpRequest")
                .setEntity(params)
                .build();
        LOGGER.info("Executing request {}", req.getRequestLine());
        return req;
    }

    private static HttpEntity buildMultiFormData(String token, String user, String pwd) {
        return MultipartEntityBuilder.create()
                .setBoundary("----WbKitFormBoundarysfevHGSzVFcFIb9e") //boundary is necessary
                .addTextBody("csrfmiddlewaretoken", token)
                .addTextBody("login", user)
                .addTextBody("password", pwd)
//                .addTextBody("next", "/problem")
                .build();
    }

    public static CookieStore login(String user, String pwd) {
        CookieStore cookieStore = getCookies(LOGIN_URL);
        String token = getCsrfToken(cookieStore);
        HttpEntity formData = buildMultiFormData(token, user, pwd);
        HttpUriRequest request = buildLoginRequest(LOGIN_URL, LOGIN_URL, formData);
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore).build();
             CloseableHttpResponse res = httpClient.execute(request)) {
            return processLogin(cookieStore, res);
        } catch (Exception e) {
            LOGGER.error("Exception occurs.", e);
        }
        LOGGER.error("Exception occurs. Please try again");
        return null;
    }

    private static CookieStore processLogin(CookieStore cookieStore, CloseableHttpResponse res) throws IOException {
        int statusCode = res.getStatusLine().getStatusCode();
        String content = EntityUtils.toString(res.getEntity(), "UTF-8");
        LOGGER.info("Response content: {}", content);
        LOGGER.info("Login status:{}, message:{}", statusCode, res.getStatusLine().getReasonPhrase());
        if (statusCode != 200) {
            LOGGER.error(getErrorIfLoginFailed(statusCode, content).getMessage());
            return null;
        }
        return getSession(cookieStore);
    }

    private static Error getErrorIfLoginFailed(int statusCode, String content) {
        try {
            LoginResponse loginRes = JSON.parseObject(content, LoginResponse.class);
            List<String> formErrors = loginRes.getForm().getErrors();
            String error = !CollectionUtils.isEmpty(formErrors) ? formErrors.get(0) : "Username or password is incorrect.";
            return new Error(error);
        } catch (Exception e) { // in case of non-conformity result
            LOGGER.error("Exception happens. {}", e);
            return new Error("Request failed");
        }
    }

    static CookieStore getSession(CookieStore cookieStore) {
        String session = null;
        for (Cookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals("LEETCODE_SESSION")) {
                session = cookie.getValue();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                df.setTimeZone(TimeZone.getDefault());
                LOGGER.info("Session expired time : {}", df.format(cookie.getExpiryDate()));
            }

        }
        if (session == null) {
            LOGGER.error("Login failed. Please try again");
            return null;
        }
        return cookieStore;
    }

}
