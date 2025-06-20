- create schema `saub`;
- create table `saub`.`email_tokens`
(
`email`      varchar(50)  not null,
`code`       varchar(6)   not null comment '인증번호 6자리',
`salt`       varchar(128) not null comment '요청자 검증 키',
`user_agent` varchar(512) not null comment '요청자 브라우저/운영체제 정보',
`is_used`    boolean      not null default false comment '토큰(인증 정보) 사용 여부',
`create_at`  datetime     not null default now(),
`expires_at` datetime     not null comment '토큰(인증 정보) 만료 일시',
constraint primary key (`email`, `code`, `salt`)
);
- create table `saub`.`contact_mvno`
(
`code` varchar(3) not null,
`display_text` varchar(25) not null,
constraint primary key (`code`)
);/*연락처 통신사를 담기 위한 테이블*/
- create table `saub`.`users`
(
`email`             varchar(50)    not null,
`password`          varchar(128)   not null,
`nickname`          varchar(10)    not null,
`name`              varchar(5)     not null comment '실명',
`birth`             date           not null comment '생년월일',
`gender`            ENUM ('M','F') not null comment '성별코드 f|m',
`contact_mvno_code` varchar(3)     not null comment '연락처 통신사 코드 FK',
`contact_first`     varchar(4)     not null comment '연락처 앞',
`contact_second`    varchar(4)     not null comment '연락처 중간',
`contact_third`     varchar(4)     not null comment '연락처 끝',
`address_postal`    varchar(4)     not null comment '주소 우편번호',
`address_primary`   varchar(100)   not null comment '주소 기본',
`address_secondary` varchar(100)   not null comment '주소 상세',
`last_signed_at`    datetime       not null comment '마지막 로그인 일시',
`last_signed_ua`    varchar(512)   not null comment '마지막 로그인 유저 에이전트',
`is_admin` boolean not null default false comment '관리자 여부',
`is_deleted` boolean not null default false comment '탈퇴 여부',
`is_suspended` boolean not null default false comment '정지 여부',
`created_at`        DATETIME       not null default now() comment '가입 일시',
`modified_at`       datetime       not null default now() comment '최종 수정 일시',
constraint primary key (`email`),
constraint unique (`nickname`),
constraint unique (`contact_first`, `contact_second`, `contact_third`),
constraint foreign key (`contact_mvno_code`) references `saub`.`contact_mvno` (`code`)
on delete cascade
on update cascade
);



