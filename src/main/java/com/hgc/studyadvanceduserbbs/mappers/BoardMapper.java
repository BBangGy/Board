package com.hgc.studyadvanceduserbbs.mappers;

import com.hgc.studyadvanceduserbbs.entities.BoardEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface BoardMapper {
    BoardEntity selectById(@Param(value = "id") String id);

    BoardEntity[] selectAll();
}
