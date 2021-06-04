# mysso
模拟淘宝单点登录SSO

主要运用加载静态资源浏览器不进行同源策略限制的原理，后期有时间会进一步完善这个项目。
现在这个项目是简单的演示逻辑，证明这种方案的可行性。

# 淘宝、天猫的单点登录-SSO
今天梳理了下单点登录，这个很早之前有接触过。单点登录的方案有很多，不同的方案也有不同的用户体验，最终实现的是集中管理登录，登录状态在所有允许的系统中通用。
最简单的是所有系统都在同源<协议://域名{子域名}:端口>，直接通过cookie就可以解决。非同源的常见方案有用地址栏加token实现，有过通过域名转发映射解决的、也有通过部署多套系统解决的😂。
兴起之下想看下阿里巴巴他们SSO是怎么玩的。

## 分析淘宝和天猫SSO逻辑
淘宝地址：https://www.taobao.com/
天猫地址：https://www.tmall.com/

<!-- more -->

1. 查看淘宝域名下的所有cookie信息以及数据请求信息：
![cookie](http://xieahui.com/uploads/sso/淘宝cookie信息.jpeg)

2. 查看淘宝控制台警告信息
![警告](http://xieahui.com/uploads/sso/淘宝警告信息.jpeg)

3. 其中的一个请求连接
![请求连接](http://xieahui.com/uploads/sso/其中的一个请求连接.jpeg)

3. 查看淘宝控制台接口请求信息
![网络请求信息](http://xieahui.com/uploads/sso/淘宝网络请求.jpeg)

看这个应该容易想起来淘宝是怎么玩的SSO了。没错，他是利用的浏览器同源策略加载静态资源时的非同源限制的问题。下面模仿者玩一下，确认下是不是这么回事。

## 验证猜测是否正确：
三个项目分别是：mysso<真正的登录入口>、myservice1<使用单点登录的服务1>、myservice2<使用单点登录的服务2>，我用maven继承关系把他们放到一个项目里了。
1. 首先确定三个测试使用的域名：
mysso.com : sso登录的项目； myservice1.com : 使用sso服务的系统1； myservice2.com : 使用sso服务的系统2；

### 配置hosts
2. 配置hosts
```javascript
127.0.0.1 mysso.com  myservice1.com myservice2.com
```

### 配置nginx
3. 配置nginx反向代理
3.1. mysso.com.conf:
```javascript
server {
        listen       80;
        server_name  mysso.com;

        #charset koi8-r;

        access_log  logs/mysso.access.log  main;
        error_log logs/mysso.error.log;
        
        #root /Users/brucexie/Documents/work/myspace/retail/retail-wap;
        
        location / {
                    add_header 'Access-Control-Allow-Origin' "$http_origin";
                    add_header 'Access-Control-Allow-Credentials' 'true';
                    add_header 'Access-Control-Allow-Methods' 'GET, POST';
                    add_header 'Access-Control-Allow-Headers' 'X-Requested-With';
                    proxy_pass         http://localhost:8068;
                    proxy_set_header   Host             $host;
                    proxy_set_header   X-Real-IP        $remote_addr;
                    proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
    
            }	
    
            error_page   500 502 503 504  /50x.html;
            location = /50x.html {
                root   html;
            }

    }
```

3.2. myservice1.com.conf
```javascript
server {
        listen       80;
        server_name  myservice1.com;

        #charset koi8-r;

        access_log  logs/myservice1.access.log  main;
        error_log logs/myservice1.error.log;
        
        #root /Users/brucexie/Documents/work/myspace/retail/retail-wap;
        
        location / {
                    add_header 'Access-Control-Allow-Origin' "$http_origin";
                    add_header 'Access-Control-Allow-Credentials' 'true';
                    add_header 'Access-Control-Allow-Methods' 'GET, POST';
                    add_header 'Access-Control-Allow-Headers' 'X-Requested-With';
                    proxy_pass         http://localhost:8066;
                    proxy_set_header   Host             $host;
                    proxy_set_header   X-Real-IP        $remote_addr;
                    proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
    
            }	
    
            error_page   500 502 503 504  /50x.html;
            location = /50x.html {
                root   html;
            }

    }
```

3.3. myservice2.com.conf
```javascript
server {
        listen       80;
        server_name  myservice2.com;

        #charset koi8-r;

        access_log  logs/myservice2.access.log  main;
        error_log logs/myservice2.error.log;
        
        #root /Users/brucexie/Documents/work/myspace/retail/retail-wap;
        
        location / {
                    add_header 'Access-Control-Allow-Origin' "$http_origin";
                    add_header 'Access-Control-Allow-Credentials' 'true';
                    add_header 'Access-Control-Allow-Methods' 'GET, POST';
                    add_header 'Access-Control-Allow-Headers' 'X-Requested-With';
                    proxy_pass         http://localhost:8067;
                    proxy_set_header   Host             $host;
                    proxy_set_header   X-Real-IP        $remote_addr;
                    proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
    
            }	
    
            error_page   500 502 503 504  /50x.html;
            location = /50x.html {
                root   html;
            }

    }
```

### 搭建测试代码
工程目录结构：
![目录结构](http://xieahui.com/uploads/sso/代码目录结构.jpeg)

考虑了下，代码太多。选择一些有意义的贴出来，后面将整个代码都上传到github上。这个项目代码本身是为了验证阿里sso的一种思路，将来有时间的话再来整理一下。
mysso中的LoginController.java
```javascript
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
```

mysso中的index.html
```javascript
<!DOCTYPE html>
<html lang="en" xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>main</title>
    <script src="https://cdn.bootcss.com/jquery/1.10.2/jquery.js"></script>
    <script type="application/javascript">
        let script = document.createElement('script');
        script.setAttribute('src', 'http://myservice1.com/login?token=[[${abc}]]');
        document.getElementsByTagName('head')[0].appendChild(script);

        script = document.createElement('script');
        script.setAttribute('src', 'http://myservice2.com/login?token=[[${abc}]]');
        document.getElementsByTagName('head')[0].appendChild(script);
    </script>
</head>
<body>
sso-[[${msg}]]
<br>
<span th:text="${msg}"></span>
</body>
</html>
```
由于只是测试，这里请求地址写死了。

myservice1中的LoginController.java
```javascript
package com.sso.mysso.myservice1;

import org.springframework.web.bind.annotation.GetMapping;
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
            cookie.setDomain(".myservice1.com");
            response.addCookie(cookie);
            return "first-login-success";
        }

        response.sendRedirect("http://mysso.com/login");
        return "error";
    }

}

```

myservice2中的LoginController.java
```javascript
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

```

### 跑一下看结果
sso登录后的结果
![sso](http://xieahui.com/uploads/sso/测试用例sso.jpeg)

sso异常警告信息，发现这个异常和淘宝控制台包的异常有些相似
![异常警告](http://xieahui.com/uploads/sso/sso异常信息.jpeg)

service1服务已经登录了
![service1](http://xieahui.com/uploads/sso/service1.jpeg)

service2服务已经登录了
![service2](http://xieahui.com/uploads/sso/service2.jpeg)

退出登录也是这样的。

总结：在一个服务上通过加载资源的形式去调用其他服务的登录接口，处理请求接口的服务根据标识去写入客户端cookie。
实际的项目中要考虑的问题远不止这些，像安全策略、埋点分析这个demo中都没有涉及。仅验证了一种友好的SSO登录实现方式，不会像有些项目一样走到哪里屁股后面都跟着一个token。