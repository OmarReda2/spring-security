package com.eazybytes.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RequestValidationBeforeFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String header = req.getHeader(HttpHeaders.AUTHORIZATION);
        if(null != req){
            header = header.trim();
            if(StringUtils.startsWithIgnoreCase(header,"Basic")){
                byte[] base64Token = header.substring(6).getBytes(StandardCharsets.UTF_8);
                byte[] decode;

                try {
                    decode = Base64.getDecoder().decode(base64Token);
                    String token = new String(decode, StandardCharsets.UTF_8); // un:pw
                    int delim = token.indexOf(":");
                    if(delim == -1){
                        throw new BadCredentialsException("Invalid Basic Authentication Token");
                    }

                    String email = token.substring(0,delim);
                    if(email == "test"){
                        res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        return;
                    }
                }catch (IllegalArgumentException exception){
                    throw new BadCredentialsException("Failed to Decode basic Authentication Token");
                }
            }

            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
