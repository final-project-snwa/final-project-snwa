package com.team.snwa.snwabackend.domain.comment.service;

import com.team.snwa.snwabackend.domain.comment.dto.request.CommentRequestDto;
import com.team.snwa.snwabackend.domain.comment.dto.response.CommentResponseDto;
import com.team.snwa.snwabackend.domain.comment.entity.Comment;
import com.team.snwa.snwabackend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    CommentResponseDto createComment(Long articleId, CommentRequestDto commentRequestDto,
                                     User user);

    Page<CommentResponseDto> getComments(Long articleId, Pageable pageable);

    CommentResponseDto updateComment(Long commentId, CommentRequestDto commentRequestDto,
                                     User user);

    void deleteComment(Long commentId, User user);
}
