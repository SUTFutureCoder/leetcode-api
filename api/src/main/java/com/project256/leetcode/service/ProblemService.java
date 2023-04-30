package com.project256.leetcode.service;

import com.project256.leetcode.model.problem.detail.Problem;
import com.project256.leetcode.model.problem.list.ProblemStatusList;

import java.util.List;
import java.util.Map;

public interface ProblemService {
    Problem getProblem(String titleSlug);

    ProblemStatusList getAllProblems();

    Map<String, Object> getTags();

    List<Integer> filterProblems(String key);
}
