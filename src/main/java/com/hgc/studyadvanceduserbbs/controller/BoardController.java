package com.hgc.studyadvanceduserbbs.controller;

import com.hgc.studyadvanceduserbbs.entities.ArticleEntity;
import com.hgc.studyadvanceduserbbs.entities.BoardEntity;
import com.hgc.studyadvanceduserbbs.services.ArticleService;
import com.hgc.studyadvanceduserbbs.services.BoardService;
import com.hgc.studyadvanceduserbbs.vos.ArticleVo;
import com.hgc.studyadvanceduserbbs.vos.PageVo;
import com.hgc.studyadvanceduserbbs.vos.SearchVo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(value = "/board")
public class BoardController {
    private final BoardService boardService;
    private final ArticleService articleService;

    @Autowired
    public BoardController(BoardService boardService, ArticleService articleService) {
        this.boardService = boardService;
        this.articleService = articleService;
    }

    @RequestMapping(value = "/list", produces = MediaType.TEXT_HTML_VALUE, method = RequestMethod.GET)
    public String getList(@RequestParam(value = "id", required = false, defaultValue = "") String id,
                          @RequestParam(value = "page", required = false, defaultValue = "1") int page
            , Model model, SearchVo searchVo) {
        System.out.println(searchVo.getId());
        System.out.println(searchVo.getBy());
        System.out.println(searchVo.getKeyword());
        BoardEntity board = this.boardService.getById(id);
        model.addAttribute("board", board);
        if (board != null) {
            Pair<ArticleVo[],PageVo> result;
            if (searchVo.getBy() != null && searchVo.getKeyword() != null) {
                // 검색 방식, 검색어가 있음(검색 목록 조회)
                result=this.articleService.getBySearch(searchVo,page);
                model.addAttribute("searchVo", searchVo);
            }else{
                // 검색 방식 혹은 검색어가 없음(일반 목록 조회)
                result = this.articleService.getByBoardId(board.getId(), page);
            }
            model.addAttribute("articles", result.getLeft());//articlevo[]
            model.addAttribute("pageVo", result.getRight()); //pagevo
            model.addAttribute("boards", this.boardService.getAll());
        }
        return "board/list";
    }


}
