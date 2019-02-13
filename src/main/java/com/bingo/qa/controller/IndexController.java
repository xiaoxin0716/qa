package com.bingo.qa.controller;

import com.bingo.qa.model.*;
import com.bingo.qa.service.CommentService;
import com.bingo.qa.service.FollowService;
import com.bingo.qa.service.LikeService;
import com.bingo.qa.service.QuestionService;
import com.bingo.qa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bingo
 */
@Controller
public class IndexController {

    private final UserService userService;

    private final QuestionService questionService;

    private final HostHolder hostHolder;

    private final FollowService followService;

    private final CommentService commentService;
    
    private final LikeService likeService;

    @Autowired
    public IndexController(UserService userService, QuestionService questionService, HostHolder hostHolder, FollowService followService, CommentService commentService,LikeService likeService) {
        this.userService = userService;
        this.questionService = questionService;
        this.hostHolder = hostHolder;
        this.followService = followService;
        this.commentService = commentService;
        this.likeService = likeService;
    }

    @GetMapping(value = {"/", "/index"})
    public String index(Model model) {
        List<ViewObject> vos = getQuestions(0, 0, 10);
        model.addAttribute("vos", vos);
        return "index";
    }

    private List<ViewObject> getQuestions(int userId, int offset, int limit) {
        List<Question> questionList = questionService.selectLatestQuestions(userId, offset, limit);

        List<ViewObject> vos = new ArrayList<>();

        for (Question question : questionList) {
            ViewObject vo = new ViewObject();
            vo.set("question", question);
            vo.set("user", userService.selectById(question.getUserId()));
            vo.set("followCount", followService.getFollowerCount(EntityType.ENTITY_QUESTION,  question.getId()));
            vos.add(vo);
        }
        return vos;
    }

    @GetMapping(value = "/user/{userId}")
    public String userIndex(Model model,
                            @PathVariable("userId") int userId) {
        model.addAttribute("vos", getQuestions(userId, 0, 10));

        User user = userService.selectById(userId);
        ViewObject vo = new ViewObject();
        vo.set("user", user);
        vo.set("commentCount", commentService.getUserCommentCount(userId));
        vo.set("followerCount", followService.getFollowerCount(EntityType.ENTITY_USER, userId));
        vo.set("followeeCount", followService.getFolloweeCount(userId, EntityType.ENTITY_USER));
        
        
        List<Comment> commentList = commentService.getCommentByUserId(userId);
      //统计用户所有评论获得的赞同数
        long likeeCount = 0;
        for(Comment comment:commentList) {
        	likeeCount += likeService.getLikeCount(EntityType.ENTITY_COMMENT,comment.getId());
        }
        vo.set("likeeeCount",likeeCount);
        if (hostHolder.getUser() != null) {
            vo.set("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_USER, userId));
        } else {
            vo.set("followed", false);
        }
        model.addAttribute("profileUser", vo);
        return "profile";
    }

}
