package com.hgc.studyadvanceduserbbs.vos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchVo {
    private String id;// 검색할 게시판 식별자
    private String by; // 검색할 방식(제목, 제목+내용,작성자)
    private String keyword;//검색할 키워드
}
