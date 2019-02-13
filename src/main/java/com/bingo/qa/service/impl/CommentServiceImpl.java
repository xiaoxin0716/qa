package com.bingo.qa.service.impl;

import com.bingo.qa.dao.CommentDAO;
import com.bingo.qa.model.Comment;
import com.bingo.qa.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author bingo
 */
@Service
public class CommentServiceImpl implements CommentService {
    private final CommentDAO commentDAO;

    @Autowired
    public CommentServiceImpl(CommentDAO commentDAO) {
        this.commentDAO = commentDAO;
    }

    @Override
    public List<Comment> getCommentsByEntity(int entityId, int entityType) {
        return commentDAO.selectCommentByEntity(entityId, entityType);
    }

    @Override
    public int addComment(Comment comment) {
        return commentDAO.addComment(comment) > 0 ? comment.getId() : 0;
    }

    @Override
    public int getCommentCount(int entityId, int entityType) {
        return commentDAO.getCommentCount(entityId, entityType);
    }

    @Override
    public Comment getCommentById(int id) {
        return commentDAO.getCommentById(id);
    }

    @Override
    public int getUserCommentCount(int userId) {
        return commentDAO.getUserCommentCount(userId);
    }

	@Override
	public List<Comment> getCommentByUserId(int userId) {
		return commentDAO.getCommentByUserId(userId);
	}

}
