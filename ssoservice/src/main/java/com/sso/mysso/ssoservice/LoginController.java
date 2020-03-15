package com.sso.mysso.ssoservice;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by xiehui1956(@)gmail.com on 2020/3/13
 */
@Controller
public class LoginController {

    final String LC = "loginCookie", MG = "msg", TK = "abc";

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, Model model
            , @RequestParam(required = false, defaultValue = "1") String name
            , @RequestParam(required = false, defaultValue = "2") String pwd) {

        Cookie[] cookies = request.getCookies();
        if (null != cookies)
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(LC) && cookie.getValue().equals("121")) {
                    model.addAttribute(MG, "already-login");
                    model.addAttribute(TK, "login-success");
                    return "/index";

                }
            }

        if (name.equals(pwd)) {
            Cookie cookie = new Cookie(LC, "121");
            cookie.setPath("/");
            cookie.setDomain(".mysso.com");
            response.addCookie(cookie);
            model.addAttribute(MG, "first-login-success");
            model.addAttribute(TK, "login-success");
            return "/index";
        }
        return "/index";
    }

}
