package com.project256.leetcode.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.project256.leetcode.common.Error;
import com.project256.leetcode.common.PageReqBody;
import com.project256.leetcode.common.ResponseStatus;
import com.project256.leetcode.model.discuss.DiscussTopics;
import com.project256.leetcode.model.discuss.Topic;
import com.project256.leetcode.model.discuss.TopicReqBody;
import com.project256.leetcode.service.DiscussService;
import com.project256.leetcode.util.ImageUtil;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static com.project256.leetcode.util.CommonUtil.*;
import static com.project256.leetcode.util.HttpUtil.getErrorIfFailed;
import static com.project256.leetcode.util.HttpUtil.post;
import static com.project256.leetcode.util.RequestParamUtil.*;

@Service
public class DiscussServiceImpl implements DiscussService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscussServiceImpl.class);
    private static final String UPDATE_TOPIC_OPERATION = "updateTopic";
    private static final String CREATE_TOPIC_OPERATION = "createTopicForQuestion";
    private static final String DELETE_TOPIC_OPERATION = "deleteTopic";
    private static final String UPLOAD_IMAGE_URL = "https://leetcode.com/storage/upload/image";
    private static final String PROBLEM_REFER = "https://leetcode.com/problems/";

    @Override
    public DiscussTopics getTopics(PageReqBody req) {
        Error errorStatus = checkPageParam(req);
        if (errorStatus != null) {
            return null;
        }
        StringEntity requestBody = buildDiscussReqBody(req);
//        CookieStore cookieStore = getCookies(req.getUri());
        String res = post(requestBody);
        Error error = getErrorIfFailed(res);
        if (error != null) {
            LOGGER.error(error.getMessage());
            return null;
        }
        JSONObject j = JSONObject.parseObject(res).getJSONObject("data");
        j = j.getJSONObject("questionTopicsList");
        return JSON.parseObject(j.toString(), DiscussTopics.class);
    }

    @Override
    public Topic getTopic(int topicId, String cookies) {
//        CookieStore cookieStore = getCookies(problemUri);
        StringEntity body = buildDiscussTopicsReqBody(topicId);
        String res = post(body, cookies);
        Error error = getErrorIfFailed(res);
        if (error != null) {
            LOGGER.error(error.getMessage());
            return null;
        }
        JSONObject j = JSONObject.parseObject(res);
        j = j.getJSONObject("data").getJSONObject("topic");
        return JSON.parseObject(j.toString(), Topic.class);
    }

    @Override
    public ResponseStatus createTopic(TopicReqBody req) {
        Error error = checkParams(req, CREATE_TOPIC_OPERATION);
        if (error != null) {
            LOGGER.error(error.getMessage());
            return null;
        }
        StringEntity entity = buildCreateTopicReqBody(req);
        return topicPost(req, entity, CREATE_TOPIC_OPERATION);
    }

    @Override
    public ResponseStatus updateTopic(TopicReqBody req) {
        Error error = checkParams(req, UPDATE_TOPIC_OPERATION);
        if (error != null) {
            LOGGER.error(error.getMessage());
            return null;
        }
        StringEntity entity = buildUpdateTopicReqBody(req);
        return topicPost(req, entity, UPDATE_TOPIC_OPERATION);
    }

    @Override
    public ResponseStatus deleteTopic(TopicReqBody req) {
        Error error = checkParams(req, DELETE_TOPIC_OPERATION);
        if (error != null) {
            LOGGER.error(error.getMessage());
            return null;
        }
        StringEntity requestBody = buildDeleteTopicReqBody(req);
        return topicPost(req, requestBody, DELETE_TOPIC_OPERATION);
    }

    @Override
    public String uploadImage(String cookies, MultipartFile file) {
        Error cookieStatus = checkCookie(cookies);
        if (cookieStatus != null) {
            LOGGER.error(cookieStatus.getMessage());
            return null;
        }
        try {
            return ImageUtil.upload(UPLOAD_IMAGE_URL, PROBLEM_REFER, cookies, file);
        } catch (IOException e) {
            LOGGER.error("IOException ", e);
        }
        LOGGER.error("Upload failure. Please try again");
        return null;
    }

    private ResponseStatus topicPost(TopicReqBody req, StringEntity entity, String operation) {
        String res = post(req.getUri(), req.getCookies(), entity);
        Error e;
        if ((e = getErrorIfFailed(res)) != null) {
            LOGGER.error(e.getMessage());
            return null;
        }
        return getResponseStatus(operation, res);
    }

    private Error checkParams(TopicReqBody req, String operation) {
        if (!isCookieValid(req.getCookies())) {
            return new Error("User cookie is invalid");
        }
        if (req.getId() == null) {
            return new Error("Id is required");
        }
        if (StringUtils.isEmpty(req.getTitle()) && !operation.equals(DELETE_TOPIC_OPERATION)) {
            return new Error("Title is required");
        }
        if (StringUtils.isEmpty(req.getContent()) && !operation.equals(DELETE_TOPIC_OPERATION)) {
            return new Error("Content is required");
        }
        return null;
    }
}
