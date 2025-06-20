package com.hgc.studyadvanceduserbbs.controller;

import com.hgc.studyadvanceduserbbs.entities.*;
import com.hgc.studyadvanceduserbbs.results.CommonResult;
import com.hgc.studyadvanceduserbbs.results.Result;
import com.hgc.studyadvanceduserbbs.results.ResultTuple;
import com.hgc.studyadvanceduserbbs.services.ArticleService;
import com.hgc.studyadvanceduserbbs.services.BoardService;
import com.hgc.studyadvanceduserbbs.services.CommentService;
import com.hgc.studyadvanceduserbbs.services.ImageService;
import com.hgc.studyadvanceduserbbs.vos.CommentVo;
import org.apache.catalina.User;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.IOException;

@Controller
@RequestMapping(value = "/article")
public class ArticleController {
    private final BoardService boardService;
    private final ArticleService articleService;
    private final ImageService imageService;
    private final CommentService commentService;

    @Autowired
    public ArticleController(BoardService boardService, ArticleService articleService, ImageService imageService, CommentService commentService) {
        this.boardService = boardService;
        this.articleService = articleService;
        this.imageService = imageService;
        this.commentService = commentService;
    }

    @RequestMapping(value = "/comment", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public CommentVo[] getComment(@RequestParam(value = "id", required = false) int id,
                                  @SessionAttribute(value = "signedUser", required = false) UserEntity signedUser) {
        ResultTuple<CommentVo[]> result = this.commentService.getByArticleId(id);
        if (result.getResult() == CommonResult.SUCCESS) {
            CommentVo[] comments = result.getPayload();
            String signedUserEmail = signedUser == null ? null : signedUser.getEmail();
            for (CommentVo comment : comments) {
                if (comment.isDeleted()) {
                    comment.setContent(null);
                }
                comment.setMine(comment.getUserEmail().equals(signedUserEmail));
            }
            return comments;
        }
        return new CommentVo[0];
    }

    @RequestMapping(value = "/comment", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postComment(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, CommentEntity comment) {

        Result result = this.commentService.write(signedUser, comment);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/comment", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteComment(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, @RequestParam(value = "id", required = false) int id) {
        Result result = this.commentService.deleteById(signedUser, id);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/comment", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchComment(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, CommentEntity comment) {
        Result result = this.commentService.modify(signedUser, comment);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/image", method = RequestMethod.GET)
    public ResponseEntity<byte[]> getImage(@RequestParam(value = "id", required = false) int id) {
        //responseentity는 응답을 돌려주기위한 상태 타입.
        ImageEntity image = this.imageService.getById(id);
        if (image == null) {
            return ResponseEntity.notFound().build();
            //notFound()는 404를 날린다.
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachMent; filename=" + image.getName())
                .contentLength(image.getData().length)
                .contentType(MediaType.parseMediaType(image.getContentType())) //문자열이라서 mediatype으로 바꿔주어야한다.
                .body(image.getData());//ok는 상태코드 200;
    }

    @RequestMapping(value = "/image", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postImage(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, @RequestParam(value = "upload", required = false) MultipartFile multipartFile) throws IOException {
//        binary 파일을 받기 위해서는 multipartfile을 사용한다. -> 이미지 파일
        ImageEntity image = ImageEntity.builder()
                .name(multipartFile.getOriginalFilename())
                .contentType(multipartFile.getContentType())
                .data(multipartFile.getBytes())
                .build();
        Result result = this.imageService.add(signedUser, image);
        JSONObject response = new JSONObject();
        if (result == CommonResult.SUCCESS) {
            response.put("url", "/article/image?id=" + image.getId());

        } else if (result == CommonResult.FAILURE_SESSION_EXPIRED) {
            JSONObject error = new JSONObject();
            error.put("message", "세션이 만료되었거나 게시글을 작성할 권하이 없습니다. 관리자에게 문의해 주세요.");
            response.put("error", error);
        } else {
            JSONObject error = new JSONObject();
            error.put("message", "알 수 없는 이유로 이미지를 업로드하지 못하였습니다. 잠시 후 다시 시도해 주세요.");
            response.put("error", error);
        }
        return response.toString();
    }

    @RequestMapping(value = "/modify", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getModify(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, @RequestParam(value = "id", required = false) int id, Model model) {
        ArticleEntity article = this.articleService.getById(id);
        if (article != null) {
            model.addAttribute("board", this.boardService.getById(article.getBoardId()));
        }
        if (article != null && (signedUser == null || !article.getUserEmail().equals(signedUser.getEmail()) && !signedUser.isAdmin()))
        //게시글이 존재 && (로그인 안했거나 || 게시글의 작성자가 로그인한 사람과 다르고 &&관리자도 아니면)
        {
            article = null;
        }
        model.addAttribute("article", article);
        return "article/modify";
    }


    @RequestMapping(value = "/write", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getWrite(@RequestParam(value = "boardId", required = false) String boardId, Model model) {
        BoardEntity board = this.boardService.getById(boardId);
        model.addAttribute("board", board);
        return "article/write";
    }

    @RequestMapping(value = "/", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String deleteIndex(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, @RequestParam(value = "id") int id) {
        Result result = this.articleService.delete(signedUser, id);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getIndex(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, @RequestParam(value = "id", required = false) int id, Model model) {

        ArticleEntity article = this.articleService.getById(id);
        model.addAttribute("article", article);
        model.addAttribute("allowed", article != null && signedUser != null && (article.getUserEmail().equals(signedUser.getEmail()) || signedUser.isAdmin()));
        // 게시글이 존재 && 로그인한 상태 && (게시글 작성자가 로그인한 사용자와 동일 하거나 || 로그인한 사용자가 관리자)인가.
        if (article != null) {
            this.articleService.incrementView(article);
            model.addAttribute("previousArticle", this.articleService.getPrevious(id));
            model.addAttribute("nextArticle", this.articleService.getNext(id));
        }
        return "article/index";
    }

    @RequestMapping(value = "/", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchIndex(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, ArticleEntity article) {
        Result result = this.articleService.modify(signedUser, article);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postIndex(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, ArticleEntity article) {
        Result result = this.articleService.write(signedUser, article);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        if (result == CommonResult.SUCCESS) {
            response.put("id", article.getId());
        }
        return response.toString();
    }
}
