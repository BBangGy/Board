package com.hgc.studyadvanceduserbbs.mappers;

import com.hgc.studyadvanceduserbbs.entities.ArticleEntity;
import com.hgc.studyadvanceduserbbs.vos.ArticleVo;
import com.hgc.studyadvanceduserbbs.vos.PageVo;
import com.hgc.studyadvanceduserbbs.vos.SearchVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ArticleMapper {
    int insert(@Param(value = "article") ArticleEntity article);

    int selectCountByBoardId(@Param(value = "boardId") String boardId);

    ArticleVo[] selectByBoardId(@Param(value = "pageVo") PageVo pageVo, @Param(value = "boardId") String boardId);

    ArticleVo[] selectBySearch(@Param(value = "pageVo") PageVo pageVo, @Param(value = "searchVo") SearchVo searchVo);

    ArticleEntity selectById(@Param(value = "id") int id);

    ArticleEntity selectPrevious(@Param(value = "id") int id);

    ArticleEntity selectNext(@Param(value = "id") int id);

    int update(@Param(value = "article") ArticleEntity article);

    int selectCountBySearch(@Param(value = "searchVo") SearchVo searchVo);

}
