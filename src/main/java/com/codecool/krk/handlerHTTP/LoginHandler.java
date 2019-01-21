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
    private DAOLogin daoLogin;
    private CookieHelper cookieHelper;
    private HttpExchange httpExchange;
    private String response;
    private String location;
    private int code;

    public LoginHandler(DAOLogin daoLogin, CookieHelper cookieHelper) {
        this.daoLogin = daoLogin;
        this.cookieHelper = cookieHelper;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        resetFields();
        this.httpExchange = httpExchange;
        routeByMethodType(method);
    }

    private void resetFields() {
        this.response = "";
        this.location = "/";
        this.code = 404;
    }

    private void routeByMethodType(String method) throws IOException {
        if (method.equals("GET")) {
            handleRequestGET();
        } else if (method.equals("POST")) {
            handleRequestPOST();
            sendExchange();
        }
    }

    private void handleRequestGET() throws IOException{
        Optional<HttpCookie> cookie = this.cookieHelper.getSessionIdCookie(this.httpExchange);
        if (cookie.isPresent()) {
            handlePresentCookie(cookie);
        } else {
            sendResponseGet();
        }
    }

    private void handlePresentCookie(Optional<HttpCookie> cookie) throws IOException {
        String sessionId = this.cookieHelper.getSessionId(cookie);
        try {
            Account account = daoLogin.getAccountBySessionId(sessionId);
            this.location = "/welcome";
            this.code = 303;
            redirect();
        } catch (NoSuchElementException e){
            sendResponseGet();
        }
    }

    private void sendResponseGet() throws IOException {
        this.response = "<html><head></head><body>" +
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
        this.code = 200;
        sendExchange();
    }

    private void handleRequestPOST() throws IOException {
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        String formData = br.readLine();
        System.out.println(formData);
        Map inputs = parseFormData(formData);
        String name = (String) inputs.get("name");
        String password = (String) inputs.get("password");
        System.out.println(name + " " + password);

        try {
            Account account = daoLogin.getAccountByNicknameAndPassword(name, password);
            handleValidUser(account);

        } catch (NoSuchElementException e) {
            handleInvalidUser();
        }
    }

    private void handleValidUser(Account account) {
        String sessionId = UUID.randomUUID().toString();
        account.setSession_id(sessionId);
        daoLogin.updateAccount(account.getUser_id(), account);
        createCookie(sessionId);
        this.location = "/welcome";
        this.code = 303;
    }

    private void createCookie(String sessionId) {
        Optional<HttpCookie> cookie = Optional.of(new HttpCookie(cookieHelper.getSESSION_COOKIE_NAME(), sessionId));
        httpExchange.getResponseHeaders().add("Set-Cookie", cookie.get().toString());
    }

    private void handleInvalidUser() {
        this.code = 303;
        this.location = "/";
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

    private void sendExchange() throws IOException {
        this.httpExchange.getResponseHeaders().add("Location", this.location);
        this.httpExchange.sendResponseHeaders(this.code, this.response.length());
        OutputStream os = this.httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }

    private void redirect() throws IOException {
        this.httpExchange.getResponseHeaders().add("Location", this.location);
        this.httpExchange.sendResponseHeaders(303, 0);
    }
}
