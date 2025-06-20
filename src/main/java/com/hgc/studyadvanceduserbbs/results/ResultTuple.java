package com.hgc.studyadvanceduserbbs.results;

import lombok.*;

@Builder // 체이닝기버
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ResultTuple<T> {
    private Result result;
    private T payload;
}
