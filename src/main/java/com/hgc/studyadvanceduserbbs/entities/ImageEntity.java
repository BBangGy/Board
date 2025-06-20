package com.hgc.studyadvanceduserbbs.entities;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of="id")
public class ImageEntity {
    private int id;
    private String name;
    private String contentType;
    private byte[] data; //longblob은 byte 배열로 받아야한다.
    private LocalDateTime createdAt;
}
