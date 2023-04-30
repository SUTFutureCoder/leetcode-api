package com.project256.leetcode.service;

import com.project256.leetcode.model.problem.list.ProblemStatusList;

public interface TopService {

    ProblemStatusList getTopLikedProblems();

    ProblemStatusList getInterviewProblems();
}
