package com.hgc.studyadvanceduserbbs.services;

import com.hgc.studyadvanceduserbbs.entities.ArticleEntity;
import com.hgc.studyadvanceduserbbs.entities.BoardEntity;
import com.hgc.studyadvanceduserbbs.entities.UserEntity;
import com.hgc.studyadvanceduserbbs.mappers.ArticleMapper;
import com.hgc.studyadvanceduserbbs.mappers.BoardMapper;
import com.hgc.studyadvanceduserbbs.mappers.UserMapper;
import com.hgc.studyadvanceduserbbs.results.CommonResult;
import com.hgc.studyadvanceduserbbs.results.Result;
import com.hgc.studyadvanceduserbbs.vos.ArticleVo;
import com.hgc.studyadvanceduserbbs.vos.PageVo;
import com.hgc.studyadvanceduserbbs.vos.SearchVo;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ArticleService {
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final BoardMapper boardMapper;

    public static boolean isContentValid(String input) {
        return input != null && input.matches("^(.{1,100000})$");
    }

    public static boolean isTitleValid(String input) {
        return input != null && input.matches("^(.{1,100})$");
    }

    @Autowired
    public ArticleService(ArticleMapper articleMapper, UserMapper userMapper, BoardMapper boardMapper) {
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.boardMapper = boardMapper;
    }

    public Pair<ArticleVo[], PageVo> getByBoardId(String boardId, int page) {
        if (page < 1) {
            page = 1;
        }
        int totalcount = this.articleMapper.selectCountByBoardId(boardId);
        PageVo pageVo = new PageVo(10, page, totalcount);
        ArticleVo[] articles = this.articleMapper.selectByBoardId(pageVo, boardId);
        return Pair.of(articles, pageVo);
    }

    public Pair<ArticleVo[], PageVo> getBySearch(SearchVo searchVo, int page) {
        if (page < 1) {
            page = 1;
        }
        int totalcount = this.articleMapper.selectCountBySearch(searchVo);
        PageVo pageVo = new PageVo(10, page, totalcount);
        ArticleVo[] articles = this.articleMapper.selectBySearch(pageVo, searchVo);
        return Pair.of(articles, pageVo);
    }

    public ArticleEntity getById(int id) {
        if (id < 1) {
            return null;
        }
        return this.articleMapper.selectById(id);
    }

    public ArticleEntity getNext(int id) {
        if (id < 1) {
            return null;
        }
        return this.articleMapper.selectNext(id);
    }

    public ArticleEntity getPrevious(int id) {
        if (id < 1) {
            return null;
        }
        return this.articleMapper.selectPrevious(id);
    }

    public Result incrementView(ArticleEntity article) {
        if (article == null || article.getId() < 1) {
            return CommonResult.FAILURE;
        }
        article.setView(article.getView() + 1);
        return this.articleMapper.update(article) > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE;
    }

    public Result delete(UserEntity user, int id) {
        if (user == null ||
                user.isDeleted() ||
                user.isSuspended()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        ArticleEntity dbArticle = this.articleMapper.selectById(id);
        if (dbArticle == null || dbArticle.isDeleted()) {
            return CommonResult.FAILURE_ABSENT;
        }
        if (!dbArticle.getUserEmail().equals(user.getEmail()) && !user.isAdmin()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        dbArticle.setDeleted(true);
        return this.articleMapper.update(dbArticle) > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE;
    }

    public Result modify(UserEntity user, ArticleEntity article) {
        if (user == null ||
                user.isDeleted() ||
                user.isSuspended()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        if (article == null ||
                article.getId() < 1 ||
                !ArticleService.isTitleValid(article.getTitle()) ||
                !ArticleService.isContentValid(article.getContent())) {
            return CommonResult.FAILURE;
        }
        ArticleEntity dbArticle = this.articleMapper.selectById(article.getId());
        if (dbArticle == null || dbArticle.isDeleted()) {
            return CommonResult.FAILURE_ABSENT;
        }
        if (!dbArticle.getUserEmail().equals(user.getEmail()) && !user.isAdmin()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        dbArticle.setTitle(article.getTitle());
        dbArticle.setContent(article.getContent());
        dbArticle.setModifiedAt(LocalDateTime.now());
        return this.articleMapper.update(dbArticle) > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE;
    }

    public Result write(UserEntity user, ArticleEntity article) {
        if (user == null ||
                user.isDeleted() || user.isSuspended()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        if (article == null ||
                !BoardService.isIdValid(article.getBoardId()) ||
                !ArticleService.isTitleValid(article.getTitle()) ||
                !ArticleService.isContentValid(article.getContent())) {
            return CommonResult.FAILURE_ABSENT;
        }
        BoardEntity dbBoard = this.boardMapper.selectById(article.getBoardId());
        if (dbBoard == null) {
            return CommonResult.FAILURE;
        }
        if (dbBoard.isAdminOnly() && !user.isAdmin()) {
            //관리자 전용 게시판인데 && 사용자가 관리자가 아니면
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }

        article.setUserEmail(user.getEmail());
        article.setView(0);
        article.setCreatedAt(LocalDateTime.now());
        article.setModifiedAt(null);
        article.setDeleted(false);
        return this.articleMapper.insert(article) > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE;
    }
}
