package com.project256.leetcode.service;

import com.project256.leetcode.common.PageReqBody;
import com.project256.leetcode.common.ResponseStatus;
import com.project256.leetcode.model.discuss.DiscussTopics;
import com.project256.leetcode.model.discuss.Topic;
import com.project256.leetcode.model.discuss.TopicReqBody;
import org.springframework.web.multipart.MultipartFile;

public interface DiscussService {
    DiscussTopics getTopics(PageReqBody req);

    Topic getTopic(int topicId, String cookies);

    ResponseStatus createTopic(TopicReqBody req);

    ResponseStatus updateTopic(TopicReqBody req);

    ResponseStatus deleteTopic(TopicReqBody req);

    String uploadImage(String cookies, MultipartFile file);
}
