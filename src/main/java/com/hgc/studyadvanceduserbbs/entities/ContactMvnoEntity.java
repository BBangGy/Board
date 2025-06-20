package com.hgc.studyadvanceduserbbs.entities;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "code")
public class ContactMvnoEntity {
    private String code;
    private String displayText;
}
