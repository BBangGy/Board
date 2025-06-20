package com.hgc.studyadvanceduserbbs.mappers;

import com.hgc.studyadvanceduserbbs.entities.CommentEntity;
import com.hgc.studyadvanceduserbbs.vos.CommentVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentMapper {
    int insert(@Param(value = "comment") CommentEntity comment);

    CommentVo[] selectByArticleId(@Param(value = "articleId") int articleId);

    CommentEntity selectById(@Param(value = "id") int id);

    int update(@Param(value = "comment") CommentEntity comment);


}
