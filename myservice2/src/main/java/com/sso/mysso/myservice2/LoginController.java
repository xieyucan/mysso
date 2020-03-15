package com.sso.mysso.myservice2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by xiehui1956(@)gmail.com on 2020/3/13
 */
@RestController
public class LoginController {

    final String LC = "loginCookie";

    @GetMapping("/login")
    public String login(HttpServletRequest request, HttpServletResponse response, String token) throws IOException {

        Cookie[] cookies = request.getCookies();
        if (null != cookies)
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(LC) && cookie.getValue().equals("121")) {
                    return "already-login";
                }
            }

        if ("login-success".equals(token)) {
            Cookie cookie = new Cookie(LC, "121");
            cookie.setPath("/");
            cookie.setDomain(".myservice2.com");
            response.addCookie(cookie);
            return "first-login-success";
        }

        response.sendRedirect("http://mysso.com/login");
        return "error";
    }
    
}
