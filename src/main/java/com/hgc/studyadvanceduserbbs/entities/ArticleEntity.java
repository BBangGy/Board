package com.hgc.studyadvanceduserbbs.entities;

import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class ArticleEntity {
    private int id;
    private String boardId;
    private String userEmail;
    private String title;
    private String content;
    private int view;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private boolean isDeleted;

}
