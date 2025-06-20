package com.hgc.studyadvanceduserbbs.entities;

import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = {"email", "code", "salt"})
public class EmailTokenEntity {
    private String email;
    private String code;
    private String salt;
    private String userAgent;
    private boolean isUsed;
    private LocalDateTime createAt;
    private LocalDateTime expiresAt;
}
