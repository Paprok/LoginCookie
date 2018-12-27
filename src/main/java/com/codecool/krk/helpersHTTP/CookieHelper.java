package com.codecool.krk.helpersHTTP;

import com.sun.net.httpserver.HttpExchange;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CookieHelper {
    private final String SESSION_COOKIE_NAME;

    public CookieHelper(String SESSION_COOKIE_NAME) {
        this.SESSION_COOKIE_NAME = SESSION_COOKIE_NAME;
    }

    public String getSESSION_COOKIE_NAME() {
        return SESSION_COOKIE_NAME;
    }

    public Optional<HttpCookie> getSessionIdCookie(HttpExchange httpExchange){
        String cookieStr = httpExchange.getRequestHeaders().getFirst("Cookie"); //
        List<HttpCookie> cookies = parseCookies(cookieStr); // gets all cookies
        return findCookieByName(SESSION_COOKIE_NAME, cookies);
    }

    private List<HttpCookie> parseCookies(String cookieString){
        List<HttpCookie> cookies = new ArrayList<>();
        if(cookieString == null || cookieString.isEmpty()){ // what happens if cookieString = null?
            return cookies;
        }
        for(String cookie : cookieString.split(";")){
            int indexOfEq = cookie.indexOf('=');
            String cookieName = cookie.substring(0, indexOfEq);
            String cookieValue = cookie.substring(indexOfEq + 1, cookie.length());
            cookies.add(new HttpCookie(cookieName, cookieValue));
        }
        return cookies;
    }

    private Optional<HttpCookie> findCookieByName(String name, List<HttpCookie> cookies){
        for(HttpCookie cookie : cookies){
            if(cookie.getName().equals(name))
                return Optional.ofNullable(cookie);
        }
        return Optional.empty();
    }
}
