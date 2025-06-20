package com.hgc.studyadvanceduserbbs.vos;

import com.hgc.studyadvanceduserbbs.entities.ArticleEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ArticleVo extends ArticleEntity {
    private String userNickname;
}
