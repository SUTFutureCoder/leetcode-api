package com.project256.leetcode.service;

import com.project256.leetcode.common.PageReqBody;
import com.project256.leetcode.common.ResponseStatus;
import com.project256.leetcode.model.comment.Comment;
import com.project256.leetcode.model.comment.CommentReqBody;

import java.util.List;

public interface CommentService {
    List<Comment> getComments(PageReqBody req);

    ResponseStatus createComment(CommentReqBody request);

    ResponseStatus updateComment(CommentReqBody request);

    ResponseStatus deleteComment(CommentReqBody request);
}
