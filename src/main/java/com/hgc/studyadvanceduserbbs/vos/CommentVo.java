package com.hgc.studyadvanceduserbbs.vos;

import com.hgc.studyadvanceduserbbs.entities.CommentEntity;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CommentVo extends CommentEntity {
    private String userNickname;
    private boolean isMine;
}
