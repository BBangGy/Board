package com.hgc.studyadvanceduserbbs.services;

import com.hgc.studyadvanceduserbbs.entities.EmailTokenEntity;
import com.hgc.studyadvanceduserbbs.entities.UserEntity;
import com.hgc.studyadvanceduserbbs.mappers.EmailTokenMapper;
import com.hgc.studyadvanceduserbbs.mappers.UserMapper;
import com.hgc.studyadvanceduserbbs.results.CommonResult;
import com.hgc.studyadvanceduserbbs.results.Result;
import com.hgc.studyadvanceduserbbs.results.ResultTuple;
import com.hgc.studyadvanceduserbbs.results.user.LoginResult;
import com.hgc.studyadvanceduserbbs.results.user.RegisterResult;
import com.hgc.studyadvanceduserbbs.utils.CryptoUtils;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.time.LocalDateTime;

@Service
public class UserService {
    private static EmailTokenEntity generateEmailToken(String email, String userAgent, int expMin) {
        String code = RandomStringUtils.randomNumeric(6);// 000000 - 999999 까지 준다. -> 반환타입은 String
        String salt = RandomStringUtils.randomAlphabetic(128);//a~z+ A~Z +0-9 -> 랜덤하게 128개를 반환한다.
        return UserService.generateEmailToken(email, userAgent, code, salt, expMin);
    }

    private static EmailTokenEntity generateEmailToken(String email, String userAgent, String code, String salt, int expMin) {
        EmailTokenEntity emailToken = new EmailTokenEntity();
        emailToken.setEmail(email);
        emailToken.setCode(code);
        emailToken.setSalt(salt);
        emailToken.setUserAgent(userAgent);
        emailToken.setUsed(false);
        emailToken.setCreateAt(LocalDateTime.now());
        emailToken.setExpiresAt(LocalDateTime.now().plusMinutes(expMin));
        //                          ⬆현재 시간에서        ⬆10분 추가(미래로)
        // > 현재 시간보다 10분 뒤
        return emailToken;
    }

    private final UserMapper userMapper;
    private final EmailTokenMapper emailTokenMapper;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine springTemplateEngine;

    @Autowired
    public UserService(UserMapper userMapper, EmailTokenMapper emailTokenMapper, JavaMailSender javaMailSender, SpringTemplateEngine springTemplateEngine) {
        this.userMapper = userMapper;
        this.emailTokenMapper = emailTokenMapper;
        this.javaMailSender = javaMailSender;
        this.springTemplateEngine = springTemplateEngine;
    }

    public static boolean isEmailVaild(String input) {
        return input != null && input.matches("^(?=.{8,50}$)([\\da-z\\-_.]{4,})@([\\da-z][\\da-z\\-]*[\\da-z]\\.)?([\\da-z][\\da-z\\-]*[\\da-z])\\.([a-z]{2,15})(\\.[a-z]{2,3})?$");
    }

    public static boolean isContactSecondValid(String input) {
        return input != null && input.matches("^(\\d{3,4})$");
    }

    public static boolean isContactThirdValid(String input) {
        return input != null && input.matches("^(\\d{4})$");
    }

    public static boolean isNameValid(String input) {
        return input != null && input.matches("^([가-힝]{2,5})$");
    }

    public static boolean isPasswordVaild(String input) {
        return input != null && input.matches("^([\\da-zA-Z`~!@#$%^&*()\\-_=+\\[{\\]}\\\\|;:'\",<.>/?]{8,50})$");
    }

    public static boolean isNicknameVaild(String input) {
        return input != null && input.matches("^([\\da-zA-Z가-힣]{2,10})$");
    }

    public static boolean isBirthVaild(String input) {
        return input != null && input.matches("^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[1-2][0-9]|3[0-1])$");
    }

    public ResultTuple<EmailTokenEntity> sendRecoverPasswordEmail(String email, String userAgent) throws MessagingException {
        if (!UserService.isEmailVaild(email) || userAgent == null) {
            System.out.println(1);
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build(); // 객체를 마무리 짓는 트리거
        }
        UserEntity dbUser = this.userMapper.selectByEmail(email);
        if (dbUser == null || dbUser.isDeleted()) {
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE_ABSENT)
                    .build();
        }
        EmailTokenEntity emailToken = UserService.generateEmailToken(email, userAgent, 10);
        //                          ⬆현재 시간에서        ⬆10분 추가(미래로)
        // > 현재 시간보다 10분 뒤
        if (this.emailTokenMapper.insert(emailToken) < 1) {
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }
        //todo 이메일 보내기
        //여기부터 메일 보내는 내용⬇
        Context context = new Context();
        //model과 같다고 생각하면 된다. context-> 이거 thymeleaf에 있는 context를 사용
        context.setVariable("code", emailToken.getCode());
        String mailText = this.springTemplateEngine.process("user/recoverPasswordEmail", context); //template 안에 user->registerEmail에 context를 같이 처리한다.
        //springTemplateEngine.process는 처리한걸 문자열로 만들어서 mailText에 집어 넣는다.
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setFrom("hyeongyu98@gmail.com");//이메일 보내는 사람의 주소는 applicaiton.property에 있는 username과 같아야한다.
        mimeMessageHelper.setTo(emailToken.getEmail()); //보내는 사람
        mimeMessageHelper.setSubject("[SAUB] 비밀번호 재설정 인증번호");
        mimeMessageHelper.setText(mailText, true);
        this.javaMailSender.send(mimeMessage);
        //여기까지⬆
        return ResultTuple.<EmailTokenEntity>builder()
                .result(CommonResult.SUCCESS)
                .payload(emailToken)
                .build();
    }


    public ResultTuple<EmailTokenEntity> sendRegisterEmail(String email, String userAgent) throws MessagingException {

        if (!UserService.isEmailVaild(email) || userAgent == null) {
            System.out.println(1);
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build(); // 객체를 마무리 짓는 트리거
        }
        if (this.userMapper.selectCountByEmail(email) > 0) {
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE_DUPLICATE)
                    .build();
        }

        EmailTokenEntity emailToken = UserService.generateEmailToken(email, userAgent, 10);
        if (this.emailTokenMapper.insert(emailToken) < 1) {
            return ResultTuple.<EmailTokenEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }
        //todo 이메일 보내기
        //여기부터 메일 보내는 내용⬇
        Context context = new Context();
        //model과 같다고 생각혐 된다. context-> 이거 thymeleaf에 있는 context를 사용
        context.setVariable("code", emailToken.getCode());
        String mailText = this.springTemplateEngine.process("user/registerEmail", context); //template 안에 user->registerEmail에 context를 같이 처리한다.
        //springTemplateEngne.process는 처리한걸 문자열로 만들어서 mailText에 집어 넣는다.
        MimeMessage mimeMessage = this.javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
        mimeMessageHelper.setFrom("hyeongyu98@gmail.com");//이메일 보내는 사람의 주소는 applicaiton.property에 있는 username과 같아야한다.
        mimeMessageHelper.setTo(emailToken.getEmail()); //보내는 사람
        mimeMessageHelper.setSubject("[SAUB] 회원가입 인증번호");
        mimeMessageHelper.setText(mailText, true);
        this.javaMailSender.send(mimeMessage);
        //여기까지⬆
        return ResultTuple.<EmailTokenEntity>builder()
                .result(CommonResult.SUCCESS)
                .payload(emailToken)
                .build();
    }
    public Result changeMyInfo(UserEntity signedUser,UserEntity userEntity) {
        if (signedUser == null  || signedUser.isSuspended()) {
            return CommonResult.FAILURE;
        }
        UserEntity dbUser = this.userMapper.selectByEmail(signedUser.getEmail());
        if (dbUser == null || dbUser.isDeleted()) {
            return CommonResult.FAILURE_ABSENT;
        }
        signedUser.setEmail(userEntity.getEmail());
        signedUser.setName(userEntity.getName());
        signedUser.setNickname(userEntity.getNickname());
        signedUser.setBirth(userEntity.getBirth());
        signedUser.setContactFirst(userEntity.getContactFirst());
        signedUser.setContactSecond(userEntity.getContactSecond());
        signedUser.setContactThird(userEntity.getContactThird());
        signedUser.setAddressPostal(userEntity.getAddressPostal());
        signedUser.setAddressPrimary(userEntity.getAddressPrimary());
        signedUser.setAddressSecondary(userEntity.getAddressSecondary());
        signedUser.setModifiedAt(LocalDateTime.now());
        return this.userMapper.update(signedUser)>0? CommonResult.SUCCESS: CommonResult.FAILURE;
    }

    public ResultTuple<UserEntity> login(String email, String password) {
        //1.전달받은 eamil과 password에 대해 정규화 실패시 commonresult.failure를 가지는 resultTuple 반환
        if (email == null || password == null || !UserService.isEmailVaild(email) || !UserService.isPasswordVaild(password)) {
            return ResultTuple.<UserEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }
        //2. userMapper의 selectByEmail을 통해 전달 받은 email을 전달하여 userentity select
        UserEntity dbUserEntity = this.userMapper.selectByEmail(email);
        //3. <2>가 null이거나, isDeleted가 true일 경우 result로 commonResult.failure를 가지는 resultTuple 반환
        if (dbUserEntity == null || dbUserEntity.isDeleted()) {
            return ResultTuple.<UserEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }
        //4. 전달받은 password를 sha-512로 해싱하여 <2>가 가지는 passsword와 비교하여 일치하지 않을 경우 commonResult.failure를 가지는 resultTuple 반환
        String hashedPassword = CryptoUtils.hashSha512(password);
        if (!dbUserEntity.getPassword().equals(hashedPassword)) {
            return ResultTuple.<UserEntity>builder()
                    .result(CommonResult.FAILURE)
                    .build();
        }
        //5.<2>가 가지는 isSuspended가 true일 경우 result로 loginresult.failure_suspended를 가지는 resultTuple 반환
        if (dbUserEntity.isSuspended()) {
            return ResultTuple.<UserEntity>builder()
                    .result(LoginResult.FAILURE_SUSPENDED).build();
        }
        //6. result로 commonResult.success를, payload로 <2>를 가지는 resulttuple로 반환

        return ResultTuple.<UserEntity>builder()
                .result(CommonResult.SUCCESS)
                .payload(dbUserEntity).build();
    }

    public Result recoverPassword(EmailTokenEntity emailToken, String newPassword) {
        //1. emailtoken 및 emailtoken이 가지고 있는 email,code,salt에 대해 정규화. 실패시 commonresult.failure 반환
        if (emailToken == null ||
                !UserService.isEmailVaild(emailToken.getEmail()) ||
                !EmailTokenService.isCodeValid(emailToken.getCode()) ||
                !EmailTokenService.isSaltValid(emailToken.getSalt())) {
            return CommonResult.FAILURE;
        }
        //2. newPassword에 대해 정규화. 실패시 commonresult.failure 반환.
        if (!UserService.isPasswordVaild(newPassword)) {
            return CommonResult.FAILURE;
        }
        //3. 주입된 의존성 emailtokenmapper의 selectbyemailandcodeandsalt를 호출하여 db에 저장되어 있는 emailtokenenitity 반환받기
        EmailTokenEntity dbEmailToken = this.emailTokenMapper.selectByEmailAndCodeAndSalt(emailToken.getEmail(), emailToken.getCode(), emailToken.getSalt());
        //4. 아래중 하나라도 만족할 경우 commonresult.failure반환
        //  -<3>이 null
        //  -<3>이 가지고 있는 userAgent가 매개변수인 emailtoken의 useragent와 일치하지 않음.
        //  -<3>이 가지고 있는 isUsed가false임
        if (dbEmailToken == null ||
                !dbEmailToken.getUserAgent().equals(emailToken.getUserAgent()) ||
                !dbEmailToken.isUsed()) {
            System.out.println(2);
            System.out.println(dbEmailToken==null);
            System.out.println(dbEmailToken.isUsed());
            return CommonResult.FAILURE;
        }
        // 5. 매개 변수인 emailtoken이 가지고 있는 email을 전달 인자로 전달하여 주입된 읜존성 usermapper의 selectbyemail 호출.
        UserEntity dBUserEntity = this.userMapper.selectByEmail(emailToken.getEmail());
        // 6. 아래 항목 중 하나라도 만족할 경우 commonresult.failure 반환
        //   -<5>가 null
        //   -<5>가 가지고 있는 isDeleted 혹은 isSuspended가 true
        if (dBUserEntity == null ||
                dBUserEntity.isDeleted() ||
                dBUserEntity.isSuspended()) {
            return CommonResult.FAILURE;
        }
        //7. 매개 변수인 newPassword를 sha-512 알고리즈믕ㄹ 통해 해싱
        String hashedPassword = CryptoUtils.hashSha512(newPassword);
        //8. <6>이 가지고 있는 password를 <7>로 갈음하고 주입된 의존성 usermapper의 update의 인자로 전달하여 호출
        //9. <8>의 결과가 0보다 클 경우 commonresult.success를 아닐 경우, commonresult.failure를 반환.
        dBUserEntity.setPassword(hashedPassword);
        return this.userMapper.update(dBUserEntity) > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE;
    }

    public Result checkNickname(String nickname) {
        if (!UserService.isNicknameVaild(nickname)) {
            return CommonResult.FAILURE;
        }

        return this.userMapper.selectCountByNickname(nickname) > 0 ?
                CommonResult.FAILURE_DUPLICATE : CommonResult.SUCCESS;
    }

    public Result register(UserEntity user, EmailTokenEntity emailToken) {
        //1. 전달 받은 emailToeken및 user에 대한 정규화 실시. 실패시 commonresult.failure
        if (emailToken == null ||
                user == null ||
                !UserService.isEmailVaild(emailToken.getEmail()) ||
                !UserService.isEmailVaild(user.getEmail()) ||
                !EmailTokenService.isCodeValid(emailToken.getCode()) ||
                !EmailTokenService.isSaltValid(emailToken.getSalt()) ||
                !UserService.isNicknameVaild(user.getNickname()) ||
                !UserService.isPasswordVaild(user.getPassword()) ||
                !UserService.isNameValid(user.getName()) ||
                !UserService.isContactSecondValid(user.getContactSecond()) ||
                !UserService.isContactThirdValid(user.getContactThird()) ||
                user.getGender() == null ||
                (!user.getGender().equals("M") && !user.getGender().equals("F")) ||
                user.getContactMvnoCode() == null ||
                user.getBirth() == null ||
                user.getContactFirst() == null ||
                user.getAddressPostal() == null ||
                user.getAddressPrimary() == null ||
                user.getAddressSecondary() == null) {
            System.out.println(3);
            return CommonResult.FAILURE;
        }
        //2.의존성 emailtokenmapper를 통해 전달 받은 emailtoken이 가지는 email,code,salt를 기준으로 emailtokenentity select.
        EmailTokenEntity dbEmailToken = this.emailTokenMapper.selectByEmailAndCodeAndSalt(emailToken.getEmail(), emailToken.getCode(), emailToken.getSalt());
        //3. 아래의 경우 중 한가지라도 만족한다면, 이메일 인증이 정상적으로 이루어지지 않은 것 임으로 commonresult.failure
        // - <2>가 Null일 경우
        //-<2>가 isUsed가 false일 경우
        //-<2>의 userAgent가 전달 받은 emailtoken의 useragent 값과 다를 경우
        if (dbEmailToken == null || !dbEmailToken.isUsed() || !dbEmailToken.getUserAgent().equals(emailToken.getUserAgent())) {
            return CommonResult.FAILURE;
        }
        //4.의존성 userMapper를 통해 전달 받은 user가 가지는 email을 전달, 그 결과가 0보다 크다면 RegisterResult.failure_duplicate_email 반환.
        if (this.userMapper.selectCountByEmail(user.getEmail()) > 0) {
            return RegisterResult.FAILURE_DUPLICATE_EMAIL;
        }
        //5.의존성 userMapper를 통해 전달 받은 user가 가지는 nickname을 전달, 그 결과가 0보다 크다면 RegisterResult.failure_duplicate_nickname 반환.
        if (this.userMapper.selectCountByNickname(user.getNickname()) > 0) {
            return RegisterResult.FAILURE_DUPLICATE_NICKNAME;
        }
        //6.의존성 userMapper를 통해 전달 받은 user가 가지는 contact을 전달, 그 결과가 0보다 크다면 RegisterResult.failure_duplicate_contact 반환.
        if (this.userMapper.selectCountByContact(user.getContactFirst(), user.getContactSecond(), user.getContactThird()) > 0) {
            return RegisterResult.FAILURE_DUPLICATE_CONTACT;
        }
        //7. CryptoUtils의 hashsha512를 통해 전달 받은 user가 가지는 password를 해시 암호화.
        String hashPassword = CryptoUtils.hashSha512(user.getPassword());
        //8. 전달 받은 user의 아래 각 멤버 변수를 해당하는 값으로 할당.
        //  -lastSignedAt: 현재 일시
        //  -lastSignedUa: 전달 받은 emailtoken가 가지는 useragent
        //  -isAdmin/isDeleted/isSuspended: false
        //  -createdAt/modifiedAt: 현재일시
        user.setPassword(hashPassword);
        user.setAdmin(false);
        user.setDeleted(false);
        user.setSuspended(false);
        user.setLastSignedAt(LocalDateTime.now());
        user.setLastSignedUa(emailToken.getUserAgent());
        user.setCreatedAt(LocalDateTime.now());
        user.setModifiedAt(LocalDateTime.now());
        //9. 의존성 userMapper를 통해 전달 받은 user를 insert하고 그 결과 값이 0보다 클 경우 commonresult.success를 , 아닐 경우 commonresult.failure를 반환
        System.out.println(5);
        return this.userMapper.insert(user) > 0 ? CommonResult.SUCCESS : CommonResult.FAILURE;
    }
}