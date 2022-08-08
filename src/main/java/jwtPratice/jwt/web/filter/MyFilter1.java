package jwtPratice.jwt.web.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MyFilter1 implements Filter {


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest)request;
        HttpServletResponse rep = (HttpServletResponse)response;

        System.out.println("Test 필터 1");
//        if(req.getMethod().equals("POST")) {
//            String headerAuth = req.getHeader("Authorization");
//            System.out.println("myFilter1: " + headerAuth);
//
//        }

        chain.doFilter(request, response);
    }
}
