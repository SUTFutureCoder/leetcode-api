package com.project256.leetcode.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.project256.leetcode.common.Error;
import com.project256.leetcode.common.PageReqBody;
import com.project256.leetcode.common.ResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;

public class CommonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

    public static Boolean isCookieValid(String cookies) {
        cookies = decode(cookies);
        if (cookies == null) {
            return false;
        }
        boolean containToken = false, containSession = false;
        String[] values = cookies.split(";");
        for (String val : values) {
            String[] data = val.split("=");
            if (data.length != 2) {
                return false; // incorrect cookie
            }
            //remove blank space
            if (data[0].replace(" ", "").equals("csrftoken")) {
                containToken = true;
            }
            if (data[0].replace(" ", "").equals("LEETCODE_SESSION")) {
                containSession = true;
            }
        }
        return containToken && containSession;
    }

    public static Error checkCookie(String cookies) {
        if (cookies == null) {
            return new Error("Cookie cannot be empty");
        }
        if (!isCookieValid(cookies)) {
            return new Error("User cookie is invalid");
        }
        return null;
    }

    public static String decode(String cookies) {
        if (cookies == null) {
            return null;
        }
        try {
            return URLDecoder.decode(cookies, "UTF-8");
        } catch (Exception e) {
            LOGGER.error("Decode failure. ", e);
        }
        return null;
    }

    public static Error checkPageParam(PageReqBody req) {
        if (req.getId() == null) {
            return new Error("Question id is required");
        }
        if (req.getPage() < 0) {
            return new Error("Negative page index is not supported");
        }
        if (req.getPage() == 0) {
            req.setPage(1);
        }
        if (req.getPageSize() <= 0 || req.getPageSize() > 512) {
            req.setPageSize(15);
        }
        return null;
    }

    public static ResponseStatus getResponseStatus(String operationName, String res) {
        JSONObject data = JSONObject.parseObject(res).getJSONObject("data");
        data = data.getJSONObject(operationName);
        ResponseStatus status = JSON.parseObject(data.toString(), ResponseStatus.class);
        if (status.getOk() != null && !status.getOk()) {
            LOGGER.error(status.getError());
            return null;
        }
        return status;
    }
}
