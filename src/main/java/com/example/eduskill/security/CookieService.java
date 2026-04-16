package com.example.eduskill.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
@Getter
public class CookieService {

    private final String refreshTokenCookieName;
    private final boolean cookiesHttpOnly;
    private final boolean cookieSecure;
    private final String cookieDomain;
    private final String cookieSameSite;

    public CookieService(
            @Value("${security.jwt.refresh-token-cookie-name}") String refreshTokenCookieName,
            @Value("${security.jwt.cookie-http-only}") boolean cookiesHttpOnly,
            @Value("${security.jwt.cookie-secure}") boolean cookieSecure,
            @Value("${security.jwt.cookie-domain}") String cookieDomain,
            @Value("${security.jwt.cookie-same-site}") String cookieSameSite) {
        this.refreshTokenCookieName = refreshTokenCookieName;
        this.cookiesHttpOnly = cookiesHttpOnly;
        this.cookieSecure = cookieSecure;
        this.cookieDomain = cookieDomain;
        this.cookieSameSite = cookieSameSite;
    }

    //create method to attach cookie to response
    public void attachRefreshCookie(HttpServletResponse response, String value, int maxAge){
        var responseCookieBuilder= ResponseCookie.from(refreshTokenCookieName,value)
                .httpOnly(cookiesHttpOnly)
                .secure(cookieSecure)
                .path("/")//path set all
                .sameSite(cookieSameSite)
                .maxAge(maxAge);

        if (cookieDomain!=null && !cookieDomain.isBlank()){
            responseCookieBuilder.domain(cookieDomain);
        }
        ResponseCookie responseCookie=responseCookieBuilder.build();
        response.addHeader(HttpHeaders.SET_COOKIE,responseCookie.toString());

    }

    //clear the refresh cookie
    public void clearRefreshCookie(HttpServletResponse response){
        var builder=ResponseCookie.from(refreshTokenCookieName,"")
                .maxAge(0)
                .httpOnly(cookiesHttpOnly)
                .path("/")
                .sameSite(cookieSameSite)
                .secure(cookieSecure);

        if (cookieDomain!=null && !cookieDomain.isBlank()){
            builder.domain(cookieDomain);
        }
        ResponseCookie responseCookie=builder.build();
        response.addHeader(HttpHeaders.SET_COOKIE,responseCookie.toString());
    }

    //anti-store header
    public void addNoStoreHeader(HttpServletResponse response){
        response.setHeader(HttpHeaders.CACHE_CONTROL,"no-store");
        response.setHeader("Pragma","no-cache");
    }

}
