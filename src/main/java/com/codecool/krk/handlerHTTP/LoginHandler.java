package com.codecool.krk.handlerHTTP;

import com.codecool.krk.dao.DAOLogin;
import com.codecool.krk.helpersHTTP.CookieHelper;
import com.codecool.krk.model.Account;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.util.*;

public class LoginHandler implements HttpHandler {
    private final String LOGIN_LOCATION = "/";
    private DAOLogin daoLogin;
    private CookieHelper cookieHelper;
    private final String WELCOME_PAGE_LOCATION = "/welcome";


    public LoginHandler(DAOLogin daoLogin, CookieHelper cookieHelper) {
        this.daoLogin = daoLogin;
        this.cookieHelper = cookieHelper;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        routeByMethodType(method, httpExchange);
    }



    private void routeByMethodType(String method, HttpExchange httpExchange) throws IOException {
        if (method.equals("GET")) {
            handleRequestGET(httpExchange);
        } else if (method.equals("POST")) {
            logBasedOnForm(httpExchange);
        }
    }

    private void handleRequestGET(HttpExchange httpExchange) throws IOException {
        Optional<HttpCookie> cookie = this.cookieHelper.getSessionIdCookie(httpExchange);
        if (cookie.isPresent()) {
            handlePresentCookie(httpExchange, cookie);
        } else {
            sendLoginPage(httpExchange);
        }
    }

    private void handlePresentCookie(HttpExchange httpExchange, Optional<HttpCookie> cookie) throws IOException {
        String sessionId = this.cookieHelper.getSessionId(cookie);
        try {
            Account account = daoLogin.getAccountBySessionId(sessionId);
            redirect(httpExchange, this.WELCOME_PAGE_LOCATION);
        } catch (NoSuchElementException e) {
            sendLoginPage(httpExchange);
        }
    }

    private void sendLoginPage(HttpExchange httpExchange) throws IOException {
        String response = "<html><head></head><body>" +
                "<form method=\"POST\" >\n " +
                "  Login:<br>\n" +
                "  <input type=\"text\" name=\"name\" value=\"admin\">\n" +
                "  <br>\n" +
                "  Password:<br>\n" +
                "  <input type=\"text\" name=\"password\">\n" +
                "  <br>\n" +
                "  <input type=\"submit\" value=\"Submit\">\n" +
                "</form> " +
                "</body></html>";
        int code = 200;
        sendExchange(httpExchange, code, response);
    }

    private void logBasedOnForm(HttpExchange httpExchange) throws IOException {
        Map<String, String> inputs = getParsedData(httpExchange);
        String name = inputs.get("name");
        String password = inputs.get("password");

        try {
            Account account = daoLogin.getAccountByNicknameAndPassword(name, password);
            handleValidUser(httpExchange, account);
        } catch (NoSuchElementException e) {
            handleInvalidUser(httpExchange);
        }
    }

    private Map<String, String> getParsedData(HttpExchange httpExchange) throws IOException {
        String formData = getFormData(httpExchange);
        System.out.println(formData);
        return parseFormData(formData);
    }

    private String getFormData(HttpExchange httpExchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        return br.readLine();
    }

    private void handleValidUser(HttpExchange httpExchange, Account account) throws  IOException{
        String sessionId = UUID.randomUUID().toString();
        account.setSession_id(sessionId);
        daoLogin.updateAccount(account.getUser_id(), account);
        createCookie(sessionId, httpExchange);
        redirect(httpExchange, WELCOME_PAGE_LOCATION);
    }

    private void createCookie(String sessionId, HttpExchange httpExchange) {
        Optional<HttpCookie> cookie = Optional.of(new HttpCookie(cookieHelper.getSESSION_COOKIE_NAME(), sessionId));
        httpExchange.getResponseHeaders().add("Set-Cookie", cookie.get().toString());
    }

    private void handleInvalidUser(HttpExchange httpExchange) throws IOException {
        redirect(httpExchange, LOGIN_LOCATION);
        System.out.println("Wrong login credentials");
    }

    private static Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            // We have to decode the value because it's urlencoded. see: https://en.wikipedia.org/wiki/POST_(HTTP)#Use_for_submitting_web_forms
            String value = new URLDecoder().decode(keyValue[1], "UTF-8");
            map.put(keyValue[0], value);
        }
        return map;
    }

    private void sendExchange(HttpExchange httpExchange, int code, String response) throws IOException {
        httpExchange.sendResponseHeaders(code, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void redirect(HttpExchange httpExchange, String location) throws IOException {
        httpExchange.getResponseHeaders().add("Location", location);
        httpExchange.sendResponseHeaders(303, 0);
    }
}
