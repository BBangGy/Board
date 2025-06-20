package com.hgc.studyadvanceduserbbs.entities;

import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class BoardEntity {
    private String id;
    private String displayText;
    private boolean isAdminOnly;
}
