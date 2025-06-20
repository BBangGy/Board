package com.hgc.studyadvanceduserbbs.controller;

import com.hgc.studyadvanceduserbbs.entities.EmailTokenEntity;
import com.hgc.studyadvanceduserbbs.entities.UserEntity;
import com.hgc.studyadvanceduserbbs.results.CommonResult;
import com.hgc.studyadvanceduserbbs.results.Result;
import com.hgc.studyadvanceduserbbs.results.ResultTuple;
import com.hgc.studyadvanceduserbbs.services.EmailTokenService;
import com.hgc.studyadvanceduserbbs.services.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.json.JSONObject;
import org.springframework.aop.framework.AbstractAdvisingBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/user")
public class UserController {
    private final UserService userService;
    private final EmailTokenService emailTokenService;
    private final AbstractAdvisingBeanPostProcessor abstractAdvisingBeanPostProcessor;

    @Autowired
    public UserController(UserService userService, EmailTokenService emailTokenService, AbstractAdvisingBeanPostProcessor abstractAdvisingBeanPostProcessor) {
        this.userService = userService;
        this.emailTokenService = emailTokenService;
        this.abstractAdvisingBeanPostProcessor = abstractAdvisingBeanPostProcessor;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getLogin() {
        return "user/login";
    }

    @RequestMapping(value = "/recover",method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getRecover(@SessionAttribute(value="signedUser",required = false) UserEntity signedUser){
        if (signedUser != null) {
            return "redirect:/";
        }
        return "user/recover";
    }

    @RequestMapping(value = "/my", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getMy(@SessionAttribute(value = "signedUser") UserEntity signedUser, Model model) {
        if (signedUser != null) {
            model.addAttribute("signedUser", signedUser);
            return "user/my";
        }
        return "redirect:/user/login";
    }

    @RequestMapping(value="/my",method = RequestMethod.PATCH,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchMy(@SessionAttribute(value="signedUser")UserEntity signedUser,UserEntity userEntity){
        if (signedUser == null) {
            return "redirect:/user/login";
        }
        Result result = this.userService.changeMyInfo(signedUser,userEntity);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/recover-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRecoverPassword(EmailTokenEntity emailToken, HttpServletRequest request, @RequestParam(value = "password", required = false) String newPassword) {
        emailToken.setUserAgent(request.getHeader("User-Agent"));
        Result result = this.userService.recoverPassword(emailToken, newPassword);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/recover-password-email",method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRecoverPasswordEmail(@RequestParam(value = "email")String email,
                                           HttpServletRequest request) throws MessagingException {
        String userAgent = request.getHeader("User-Agent");
        ResultTuple<EmailTokenEntity> result = this.userService.sendRecoverPasswordEmail(email, userAgent);
        JSONObject response = new JSONObject();
        response.put("result", result.getResult().toStringLower());
        if (result.getResult() == CommonResult.SUCCESS) {
            response.put("salt", result.getPayload().getSalt());
        }
        return response.toString();
    }

    @RequestMapping(value = "/recover-password-email", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String patchRecoverPasswordEmail(EmailTokenEntity emailToken, HttpServletRequest request) {
        emailToken.setUserAgent(request.getHeader("User-Agent"));
        Result result = this.emailTokenService.verifyEmailToken(emailToken);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String getRegister(@SessionAttribute(value="signedUser",required = false) UserEntity signedUser)
    //HTTPServletReqeust 는 요청과 관련된 모든 정보가 다 들어있다.
//        System.out.println(request.getHeader("User-Agent")); // request.getHeader()중에 "User-Agent"라는 이름의 값을 가지고 오고 싶다.
    {
        if (signedUser != null) {
            return "redirec:/";
        }
        return "user/register";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postLogin(@RequestParam(value = "email", required = false) String email, @RequestParam(value = "password", required = false) String password, HttpSession session) {
        //1. 의존성인 userservice의 login을 통해 전달 받은 email과 password를 전달하여 resulttuple 반환 받기.
        ResultTuple<UserEntity> resultTuple = this.userService.login(email, password);
        //2. <1>의 result가 success일 경우 전달 받은 session 객체에 "signedUser"라는 이름으로 <1>의 payload 할당.
        if (resultTuple.getResult() == CommonResult.SUCCESS) {
            session.setAttribute("signedUser", resultTuple.getPayload());
        }

        //3. 평소 하던데로 JSONObject를 사용하여 응답 반환.
        JSONObject response = new JSONObject();
        response.put("result", resultTuple.getResult().toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String postLogout(@SessionAttribute(value = "signedUser", required = false) UserEntity signedUser, HttpSession session) {
        session.setAttribute("signedUser", null);
        return "redirect:/user/login";
    }

    @RequestMapping(value = "/nickname-check", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postNicknameCheck(@RequestParam(value = "nickname") String nickname) {

        Result result = this.userService.checkNickname(nickname);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRegister(EmailTokenEntity emailToken, UserEntity user, HttpServletRequest request) {

        emailToken.setUserAgent(request.getHeader("User-Agent"));
        Result result = this.userService.register(user, emailToken);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/register-email", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String pathRegisterEmail(EmailTokenEntity emailToken, HttpServletRequest request) {
        emailToken.setUserAgent(request.getHeader("User-Agent"));
        Result result = this.emailTokenService.verifyEmailToken(emailToken);
        JSONObject response = new JSONObject();
        response.put("result", result.toStringLower());
        return response.toString();
    }

    @RequestMapping(value = "/register-email", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String postRegisterEmail(@RequestParam(value = "email") String email, HttpServletRequest request) throws MessagingException {
        String userAgent = request.getHeader("User-Agent");
        ResultTuple<EmailTokenEntity> result = this.userService.sendRegisterEmail(email, userAgent);
        JSONObject response = new JSONObject();
        response.put("result", result.getResult().toStringLower());
        if (result.getResult() == CommonResult.SUCCESS) {
            response.put("salt", result.getPayload().getSalt());
        }
        return response.toString();
    }
}
