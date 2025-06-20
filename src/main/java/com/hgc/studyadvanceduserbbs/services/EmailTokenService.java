package com.hgc.studyadvanceduserbbs.services;

import com.hgc.studyadvanceduserbbs.entities.EmailTokenEntity;
import com.hgc.studyadvanceduserbbs.mappers.EmailTokenMapper;
import com.hgc.studyadvanceduserbbs.results.CommonResult;
import com.hgc.studyadvanceduserbbs.results.Result;
import com.hgc.studyadvanceduserbbs.results.email_token.VerifyEmailTokenResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class EmailTokenService {
    public static boolean isCodeValid(String code) {
        return code != null && code.matches("^(\\d{6})$");

    }public static boolean isSaltValid(String salt) {
        return salt != null && salt.matches("^([\\da-zA-Z]{128})$");
    }
    private final EmailTokenMapper emailTokenMapper;

    @Autowired
    public EmailTokenService(EmailTokenMapper emailTokenMapper) {
        this.emailTokenMapper = emailTokenMapper;
    }

    public Result verifyEmailToken(EmailTokenEntity emailToken){
        //0. Result 인터페이스를 구현하는 ...results.email_token.verifyEmailTokenResult 열거형 만들고 인자로 FAILURE_EXPIRED 추가
        //1. 전달 받은 emailToken 매개변수 및 이의 email,code,salt 정규화 실패시 commonResult.FAILURE 반환
        if (emailToken == null ||
                !UserService.isEmailVaild(emailToken.getEmail()) ||
                !EmailTokenService.isCodeValid(emailToken.getCode()) ||
                !EmailTokenService.isSaltValid(emailToken.getSalt())) {
            return CommonResult.FAILURE;
        }
        //2. EmailTokenMapper의 selectByEmailAndCodeAndSalt 호출하여 EmailTokenEntity 반환 받기
        EmailTokenEntity dbEmailToken = this.emailTokenMapper.selectByEmailAndCodeAndSalt(emailToken.getEmail(), emailToken.getCode(), emailToken.getSalt());
        //3. <2>가 null일 경우 CommonResult.FAILURE  반환
        //4. <2>가 가지고 있는 isUsed가 true일 경우 CommonResult.FAILURE  반환
        if(dbEmailToken==null && !emailToken.isUsed()){
            return CommonResult.FAILURE;
        }
        //5. <2>가 가지고 있는 expiresAt이 현재 시간보다 과거일 경우(만료) VerifyEmailTokenResult.FAILURE_EXPIRED  반환
        if (dbEmailToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            //              ⬆만료 시간이 .이전인가(현재)
            return VerifyEmailTokenResult.FAILURE_EXPIRED;
        }
        //6. <2>가 가지고 잇는 useAgent가 전달 받은 emailToekn의 userAgent와 다를 경우 CommonResult.FAILURE  반환
        if (!dbEmailToken.getUserAgent().equals(emailToken.getUserAgent())) {
            return CommonResult.FAILURE;
        }
        //7. <2>의 isUsed는 true로 지정하고 EmailTokenMapper의 update 호출(인자는 <2>)
        dbEmailToken.setUsed(true);
        //8. <7>의 결과가 0보다 크면 CommonResult.SUCCESS를, 아니면 FAILURE를 반환.
        return this.emailTokenMapper.update(dbEmailToken)>0? CommonResult.SUCCESS: CommonResult.FAILURE;

    }
}
