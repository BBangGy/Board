package com.hgc.studyadvanceduserbbs.entities;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Enumeration;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "email")
public class UserEntity {
    private String email;
    private String password;
    private String nickname;
    private String name;
    private LocalDate birth;
    private String gender;
    private String contactMvnoCode;
    private String contactFirst;
    private String contactSecond;
    private String contactThird;
    private String addressPostal;
    private String addressPrimary;
    private String addressSecondary;
    private LocalDateTime lastSignedAt;
    private String lastSignedUa;
    private boolean isAdmin;
    private boolean isDeleted;
    private boolean isSuspended;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
