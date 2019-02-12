package com.bingo.qa.controller;


import com.bingo.qa.async.EventModel;
import com.bingo.qa.async.EventProducer;
import com.bingo.qa.async.EventType;
import com.bingo.qa.model.*;
import com.bingo.qa.service.*;
import com.bingo.qa.util.QaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.HtmlUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author bingo
 */
@Controller
public class QuestionController {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuestionController.class);

    private final QuestionService questionService;

    private final SensitiveService sensitiveService;

    private final HostHolder hostHolder;

    private final UserService userService;

    private final CommentService commentService;

    private final LikeService likeService;

    private final FollowService followService;

    private final EventProducer eventProducer;

    @Autowired
    public QuestionController(QuestionService questionService, SensitiveService sensitiveService, HostHolder hostHolder, UserService userService, CommentService commentService, LikeService likeService, FollowService followService, EventProducer eventProducer) {
        this.questionService = questionService;
        this.sensitiveService = sensitiveService;
        this.hostHolder = hostHolder;
        this.userService = userService;
        this.commentService = commentService;
        this.likeService = likeService;
        this.followService = followService;
        this.eventProducer = eventProducer;
    }

    @PostMapping(value = {"/question/add"})
    @ResponseBody
    public String addQuestion(@RequestParam("title") String title,
                              @RequestParam("content") String content) {

        try {
            // 从hostHolder中取出用户
            User user = hostHolder.getUser();
            if (user ==  null) {
                // 未登录，直接返回
                return QaUtil.getJSONString(999);
            }

            Question question = new Question();
            question.setUserId(user.getId());

            // 过滤title和content中的html标签和敏感词
            title = sensitiveService.filter(HtmlUtils.htmlEscape(title));
            content = sensitiveService.filter(HtmlUtils.htmlEscape(content));

            question.setTitle(title);
            question.setContent(content);
            question.setCreatedDate(new Date());
            question.setCommentCount(0);

            int res = questionService.addQuestion(question);

            if (res > 0) {
                // 添加问题成功之后，发送一个事件
                eventProducer.fireEvent(new EventModel(EventType.ADD_QUESTION)
                        .setActorId(question.getUserId()).setEntityId(question.getId())
                        .setExt("title", question.getTitle()).setExt("content", question.getContent()));

                return QaUtil.getJSONString(0);
            }

        } catch (Exception e) {
            LOGGER.error("增加问题失败" + e.getMessage());
        }

        return QaUtil.getJSONString(1, "失败");
    }


    @GetMapping(value = "/question/{qid}")
    public String questionDetail(Model model,
                                 @PathVariable("qid") int qid) {
        Question question = questionService.getQuestionById(qid);
        model.addAttribute("question", question);

        List<Comment> commentList = commentService.getCommentsByEntity(qid, EntityType.ENTITY_QUESTION);
        List<ViewObject> vos = new ArrayList<>();
        for (Comment comment : commentList) {
            ViewObject vo = new ViewObject();
            vo.set("comment", comment);
            if (hostHolder.getUser() == null) {
                vo.set("liked", 0);
            } else {
                vo.set("liked", likeService.getLikeStatus(hostHolder.getUser().getId(), EntityType.ENTITY_COMMENT, comment.getId()));
            }

            vo.set("likeCount", likeService.getLikeCount(EntityType.ENTITY_COMMENT, comment.getId()));
            vo.set("user", userService.selectById(comment.getUserId()));
            vos.add(vo);
        }

        model.addAttribute("vos", vos);

        List<ViewObject> followUsers = new ArrayList<ViewObject>();
        // 获取关注的用户信息
        List<Integer> users = followService.getFollowers(EntityType.ENTITY_QUESTION, qid, 20);
        for (Integer userId : users) {
            ViewObject vo = new ViewObject();
            User u = userService.selectById(userId);
            if (u == null) {
                continue;
            }
            vo.set("name", u.getName());
            vo.set("headUrl", u.getHeadUrl());
            vo.set("id", u.getId());
            followUsers.add(vo);
        }
        model.addAttribute("followUsers", followUsers);
        if (hostHolder.getUser() != null) {
            model.addAttribute("followed", followService.isFollower(hostHolder.getUser().getId(), EntityType.ENTITY_QUESTION, qid));
        } else {
            model.addAttribute("followed", false);
        }
        return "detail";
    }

}
