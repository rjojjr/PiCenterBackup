package com.kirchnersolutions.picenter.backup.rest;

import com.kirchnersolutions.picenter.backup.comps.Auth;
import com.kirchnersolutions.picenter.backup.comps.Status;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
public class MainController {

    private Auth auth;
    private Status status;

    @Autowired
    public MainController(Auth auth, Status status){
        this.auth = auth;
        this.status = status;
    }

    @GetMapping("/status")
    public String initClient(@RequestHeader("token") String token,
                             @RequestHeader HttpHeaders headers,  HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        if(token == null || !auth.validateToken(token)){
            return "invalid token";
        }
        if(!status.getStatus().equals("idle")){
            return "processing " + status.currentTask();
        }
        return "idle";
    }

    @GetMapping("/backup")
    public String backup(@RequestHeader("token") String token,
                             @RequestHeader HttpHeaders headers,  HttpServletResponse response) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
        HttpSession httpSession = cookie(request, response);
        if(token == null || !auth.validateToken(token)){
            return "invalid token";
        }
        status.add("backup");
        return status.currentTask();
    }

    private static HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true); // true == allow create
    }

    private static HttpSession cookie(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session != null && request.getParameter("JSESSIONID") != null) {
            Cookie userCookie = new Cookie("JSESSIONID", request.getParameter("JSESSIONID"));
            userCookie.setMaxAge(30 * 60 + 1);
            response.addCookie(userCookie);
            return session;
        } else if (session != null) {
            String sessionId = session.getId();
            Cookie userCookie = new Cookie("JSESSIONID", sessionId);
            userCookie.setMaxAge(30 * 60 + 1);
            response.addCookie(userCookie);
            return session;
        } else {
            session = getSession();
            String sessionId = session.getId();
            Cookie userCookie = new Cookie("JSESSIONID", sessionId);
            userCookie.setMaxAge(30 * 60 + 1);
            response.addCookie(userCookie);
            return session;
        }
    }

}
