package com.hgc.studyadvanceduserbbs.controlleradvices;

import com.hgc.studyadvanceduserbbs.services.BoardService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
//공통 로직을 사용할수있도록 도와주는 기능을 제공.
//이건 header에서 사용하기 위해서 만들은것이다.
public class CommonControllerAdvice {
    private final BoardService boardService;

    @Autowired
    public CommonControllerAdvice(BoardService boardService) {
        this.boardService = boardService;
    }

    @ModelAttribute
//    공통 데이터 제공을 위한 어노테이션
    //board를 가지고 와서 header에 사용하기 위해서.
    public void addBoardAttribute(HttpServletRequest request, Model model) {
        System.out.println(request.getMethod());
        if (request.getMethod().equals("GET")) {
            model.addAttribute("boards", this.boardService.getAll());
        }
        System.out.println("컨트롤러 어드바이스 실행됨");
    }
}
