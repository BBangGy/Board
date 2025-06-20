package com.hgc.studyadvanceduserbbs.services;

import com.hgc.studyadvanceduserbbs.entities.BoardEntity;
import com.hgc.studyadvanceduserbbs.mappers.BoardMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoardService {
    private final BoardMapper boardMapper;

    public static boolean isIdValid(String input) {
        return input != null && input.matches("^([a-z_]{1,10})$");
    }

    @Autowired
    public BoardService(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    public BoardEntity getById(String id) {
        if(!BoardService.isIdValid(id)){
            return null;
        }
        return this.boardMapper.selectById(id);
    }

    public BoardEntity[] getAll() {
        return this.boardMapper.selectAll();
    }
}
