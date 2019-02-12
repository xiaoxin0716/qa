package com.bingo.qa.controller;

import com.bingo.qa.async.EventModel;
import com.bingo.qa.async.EventProducer;
import com.bingo.qa.async.EventType;
import com.bingo.qa.model.Comment;
import com.bingo.qa.model.EntityType;
import com.bingo.qa.model.HostHolder;
import com.bingo.qa.model.User;
import com.bingo.qa.service.CommentService;
import com.bingo.qa.service.LikeService;
import com.bingo.qa.util.QaUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author bingo
 *
 */
@Controller
public class LikeController {

    private final LikeService likeService;

    private final HostHolder hostHolder;

    private final EventProducer eventProducer;

    private final CommentService commentService;

    @Autowired
    public LikeController(LikeService likeService, HostHolder hostHolder, EventProducer eventProducer, CommentService commentService) {
        this.likeService = likeService;
        this.hostHolder = hostHolder;
        this.eventProducer = eventProducer;
        this.commentService = commentService;
    }

    @PostMapping(value = {"/like"})
    @ResponseBody
    public String like(@RequestParam("commentId") String id) {
        int commentId = Integer.parseInt(id.replaceAll(",", ""));

        User user = hostHolder.getUser();
        if (user == null) {
            // 用户未登录，直接返回
            return QaUtil.getJSONString(999);
        }

        Comment comment = commentService.getCommentById(commentId);

        // 用户点了个赞，那么就发送一个event出去，通知被点赞的评论的owner
        eventProducer.fireEvent(new EventModel(EventType.LIKE)
                .setActorId(user.getId())
                .setEntityId(commentId)
                .setEntityOwnerId(comment.getUserId())
                .setEntityType(EntityType.ENTITY_COMMENT)
                .setExt("questionId", comment.getEntityId() + "")
        );

        long likeCount = likeService.like(user.getId(), EntityType.ENTITY_COMMENT, commentId);
        return QaUtil.getJSONString(0, likeCount + "");
    }


    @PostMapping(value = {"/dislike"})
    @ResponseBody
    public String dislike(@RequestParam("commentId") String id) {
        int commentId = Integer.parseInt(id.replaceAll(",", ""));

        User user = hostHolder.getUser();
        if (user == null) {
            // 用户未登录，直接返回
            return QaUtil.getJSONString(999);
        }

        long likeCount = likeService.disLike(user.getId(), EntityType.ENTITY_COMMENT, commentId);
        return QaUtil.getJSONString(0, likeCount + "");
    }
}
