package com.project256.leetcode.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.project256.leetcode.common.Error;
import com.project256.leetcode.common.PageReqBody;
import com.project256.leetcode.common.ResponseStatus;
import com.project256.leetcode.model.comment.Comment;
import com.project256.leetcode.model.comment.CommentReqBody;
import com.project256.leetcode.service.CommentService;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.project256.leetcode.util.CommonUtil.*;
import static com.project256.leetcode.util.HttpUtil.*;
import static com.project256.leetcode.util.RequestParamUtil.buildCommentReqBody;


@Service
public class CommentServiceImpl implements CommentService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommentServiceImpl.class);

    private static final String CREATE_COMMENT_OPERATION = "createComment";
    private static final String UPDATE_COMMENT_OPERATION = "updateComment";
    private static final String DELETE_COMMENT_OPERATION = "deleteComment";

    @Override
    public List<Comment> getComments(PageReqBody req) {
        Error errorStatus = checkPageParam(req);
        if (errorStatus != null) {
            LOGGER.error(errorStatus.getMessage());
            return null;
        }
//        CookieStore cookieStore = getCookies(req.getUri());
        StringEntity requestBody = buildCommentReqBody(req);
        String res = post(requestBody, req.getCookies());
        Error error = getErrorIfFailed(res);
        if (error != null) {
            LOGGER.error(error.getMessage());
            return null;
        }
        JSONArray j = JSONObject.parseObject(res).getJSONObject("data")
                .getJSONObject("topicComments").getJSONArray("data");
        if (j == null || j.size() == 0) {
            return new ArrayList<>();
        }
        List<Comment> comments = JSON.parseArray(j.toString(), Comment.class);
        return comments;
    }

    @Override
    public ResponseStatus createComment(CommentReqBody req) {
        Error error = checkParams(req, CREATE_COMMENT_OPERATION);
        if (error != null) {
            LOGGER.error(error.getMessage());
            return null;
        }
        StringEntity requestBody = buildCommentReqBody(req.getTopicId(), req.getParentCommentId(), req.getContent());
        return commentPost(req, requestBody, CREATE_COMMENT_OPERATION);
    }

    @Override
    public ResponseStatus updateComment(CommentReqBody req) {
        Error error = checkParams(req, UPDATE_COMMENT_OPERATION);
        if (error != null) {
            LOGGER.error(error.getMessage());
            return null;
        }
        StringEntity requestBody = buildCommentReqBody(req.getCommentId(), req.getContent());
        return commentPost(req, requestBody, UPDATE_COMMENT_OPERATION);
    }

    @Override
    public ResponseStatus deleteComment(CommentReqBody req) {
        Error error = checkParams(req, DELETE_COMMENT_OPERATION);
        if (error != null) {
            LOGGER.error(error.getMessage());
            return null;
        }
        StringEntity requestBody = buildCommentReqBody(req.getCommentId());
        return commentPost(req, requestBody, DELETE_COMMENT_OPERATION);
    }

    private ResponseStatus commentPost(CommentReqBody req, HttpEntity entity, String operationName) {
        String cookies = req.getCookies();
        String res = post(req.getUri(), cookies, entity);
        Error e = getErrorIfFailed(res);
        if (e != null) {
            LOGGER.error(e.getMessage());
            return null;
        }
        return getResponseStatus(operationName, res);
    }

    private Error checkParams(CommentReqBody req, String operation) {
        if (!isCookieValid(req.getCookies())) {
            return new Error("User cookie is invalid");
        }
        if (StringUtils.isEmpty(req.getUri())) {
            return new Error("Refer uri is required");
        }
        if (CREATE_COMMENT_OPERATION.equals(operation) && req.getTopicId() == null) {
            return new Error("Topic id is required");
        }
        if ((UPDATE_COMMENT_OPERATION.equals(operation)
                || DELETE_COMMENT_OPERATION.equals(operation))
                && req.getCommentId() == null) {
            return new Error("Comment id is required");
        }
        if (StringUtils.isEmpty(req.getContent()) && !DELETE_COMMENT_OPERATION.equals(operation)) {
            return new Error("Comment content is required");
        }
        return null;
    }
}
