package com.hgc.studyadvanceduserbbs.mappers;

import com.hgc.studyadvanceduserbbs.entities.UserEntity;
import org.apache.catalina.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insert(@Param(value = "user") UserEntity user);

    UserEntity selectByEmail(@Param(value = "email") String email);
    int selectCountByContact(@Param(value = "contactFirst") String contactFirst,
                            @Param(value = "contactSecond") String contactSecond,
                            @Param(value = "contactThird") String contactThird);

    int selectCountByEmail(@Param(value = "email") String email);

    int selectCountByNickname(@Param(value = "nickname") String nickname);

    int update(@Param(value = "user") UserEntity user);
}
