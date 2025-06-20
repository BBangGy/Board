package com.hgc.studyadvanceduserbbs.services;

import com.hgc.studyadvanceduserbbs.entities.ArticleEntity;
import com.hgc.studyadvanceduserbbs.entities.CommentEntity;
import com.hgc.studyadvanceduserbbs.entities.UserEntity;
import com.hgc.studyadvanceduserbbs.mappers.ArticleMapper;
import com.hgc.studyadvanceduserbbs.mappers.CommentMapper;
import com.hgc.studyadvanceduserbbs.results.CommonResult;
import com.hgc.studyadvanceduserbbs.results.Result;
import com.hgc.studyadvanceduserbbs.results.ResultTuple;
import com.hgc.studyadvanceduserbbs.vos.CommentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentService {
    private final CommentMapper commentMapper;
    private final ArticleMapper articleMapper;

    @Autowired
    public CommentService(CommentMapper commentMapper, ArticleMapper articleMapper) {
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
    }

    public Result deleteById(UserEntity user, int id) {
        //전달 받은 id가 pk에서 나올수 없는 값이진 않은지
        if (id < 1) {
            return CommonResult.FAILURE;
        }

        //사용자는 로그인을 했는지, 탈퇴/정지를 당하지 않았는지
        if (user == null || user.isDeleted() || user.isSuspended()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        //전달받은 id를 id로 가지는 댓글이 db에 있는지
        CommentEntity dbComment = this.commentMapper.selectById(id);
        if (dbComment == null) {
            return CommonResult.FAILURE_ABSENT;
        }
        //전달 받은 사용자가 해당 댓글을 삭제할 권한이 있는지
        if (!user.getEmail().equals(dbComment.getUserEmail()) && !user.isAdmin()) {
            return CommonResult.FAILURE;
        }
        //실제 삭제하지는 말고 isDelete만 update하는걸로
        dbComment.setDeleted(true);
        return this.commentMapper.update(dbComment) > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE;
    }

    public ResultTuple<CommentVo[]> getByArticleId(int articleId) {
        if (articleId < 1) {
            return ResultTuple.<CommentVo[]>builder()
                    .result(CommonResult.FAILURE).build();
        }
        ArticleEntity dbArticle = this.articleMapper.selectById(articleId);
        if (dbArticle == null || dbArticle.isDeleted()) {
            return ResultTuple.<CommentVo[]>builder()
                    .result(CommonResult.FAILURE_ABSENT).build();
        }
        return ResultTuple.<CommentVo[]>builder()
                .result(CommonResult.SUCCESS)
                .payload(this.commentMapper.selectByArticleId(articleId))
                .build();
    }

    public Result modify(UserEntity user, CommentEntity comment) {
        //전달받은 comment의 id,content가 아구가 맞는지,
        if (comment == null || comment.getId() < 1 || comment.getContent() == null || comment.getContent().isEmpty()) {
            return CommonResult.FAILURE;
        }
        //사용자는 로그인을 했는지, 탈퇴/정지를 당하진 않았는지
        if (user == null || user.isDeleted() || user.isSuspended()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        //전달받은 id를 id로 가지는 댓글이 db에 있는지
        CommentEntity dbComment = this.commentMapper.selectById(comment.getId());
        if (dbComment == null ||
                dbComment.isDeleted()) {
            return CommonResult.FAILURE_ABSENT;
        }
        //전달 받은 사용자가 해당 댓글을 수정할 권한이 있는지
        if (!user.getEmail().equals(dbComment.getUserEmail()) && !user.isAdmin()) {
            return CommonResult.FAILURE;
        }
        dbComment.setContent(comment.getContent());
        return this.commentMapper.update(dbComment) > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE;
    }

    public Result write(UserEntity user, CommentEntity comment) {
        if (user == null || user.isSuspended() || user.isDeleted()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        if (comment == null || comment.getArticleId() < 1 || comment.getContent() == null || comment.getContent().isEmpty()) {
            return CommonResult.FAILURE;
        }
        ArticleEntity dbArticle = this.articleMapper.selectById(comment.getArticleId());
        if (dbArticle == null || dbArticle.isDeleted()) {
            return CommonResult.FAILURE_SESSION_EXPIRED;
        }
        comment.setUserEmail(user.getEmail());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setDeleted(false);
        return this.commentMapper.insert(comment) > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE;
    }
}
