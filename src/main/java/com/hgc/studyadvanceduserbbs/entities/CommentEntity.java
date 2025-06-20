package com.hgc.studyadvanceduserbbs.entities;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class CommentEntity {
    private int id;
    private int articleId;
    private String userEmail;
    private Integer commentId;
    private String content;
    private LocalDateTime createdAt;
    private boolean isDeleted;
}
