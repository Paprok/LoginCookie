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
    private final String LOGIN_LOCATION = "/";
    private final String WELCOME_PAGE_LOCATION = "/welcome";


    public LoginHandler(DAOLogin daoLogin, CookieHelper cookieHelper) {
        this.daoLogin = daoLogin;
        this.cookieHelper = cookieHelper;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        this.httpExchange = httpExchange;
        String method = httpExchange.getRequestMethod();
        routeByMethodType(method);
    }


    private void routeByMethodType(String method) throws IOException {
        if (method.equals("GET")) {
            handleRequestGET();
        } else if (method.equals("POST")) {
            logBasedOnForm();
        }
    }

    private void handleRequestGET() throws IOException {
        Optional<HttpCookie> cookie = this.cookieHelper.getSessionIdCookie(httpExchange);
        if (cookie.isPresent()) {
            handlePresentCookie(cookie);
        } else {
            sendLoginPage();
        }
    }

    private void handlePresentCookie(Optional<HttpCookie> cookie) throws IOException {
        String sessionId = this.cookieHelper.getSessionId(cookie);
        try {
            Account account = daoLogin.getAccountBySessionId(sessionId);
            redirect(this.WELCOME_PAGE_LOCATION);
        } catch (NoSuchElementException e) {
            sendLoginPage();
        }
    }

    private void sendLoginPage() throws IOException {
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
        sendExchange(code, response);
    }

    private void logBasedOnForm() throws IOException {
        try {
            Map<String, String> inputs = getParsedData(httpExchange);
            String name = inputs.get("name");
            String password = inputs.get("password");
            Account account = daoLogin.getAccountByNicknameAndPassword(name, password);
            handleValidUser(account);
        } catch (NoSuchElementException | UnsupportedEncodingException | IllegalArgumentException e) {
            handleInvalidUser();
        }
    }

    private Map<String, String> getParsedData(HttpExchange httpExchange) throws IOException, IllegalArgumentException {
        String formData = getFormData(httpExchange);
        System.out.println(formData);
        Map<String, String> parsedData = parseFormData(formData);
        return parsedData;
    }

    private String getFormData(HttpExchange httpExchange) throws IOException {
        InputStreamReader isr = new InputStreamReader(httpExchange.getRequestBody(), "utf-8");
        BufferedReader br = new BufferedReader(isr);
        return br.readLine();
    }

    private void handleValidUser(Account account) throws IOException {
        String sessionId = UUID.randomUUID().toString();
        account.setSession_id(sessionId);
        daoLogin.updateAccount(account.getUser_id(), account);
        createCookie(sessionId);
        redirect(WELCOME_PAGE_LOCATION);
    }

    private void createCookie(String sessionId) {
        Optional<HttpCookie> cookie = Optional.of(new HttpCookie(cookieHelper.getSESSION_COOKIE_NAME(), sessionId));
        httpExchange.getResponseHeaders().add("Set-Cookie", cookie.get().toString());
    }

    private void handleInvalidUser() throws IOException {
        redirect(LOGIN_LOCATION);
        System.out.println("Wrong login credentials");
    }

    private static Map<String, String> parseFormData(String formData) throws UnsupportedEncodingException, IllegalArgumentException {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            // We have to decode the value because it's urlencoded. see: https://en.wikipedia.org/wiki/POST_(HTTP)#Use_for_submitting_web_forms
            String value;
            if(keyValue.length == 2) {
                value = new URLDecoder().decode(keyValue[1], "UTF-8");
            } else {
                throw new IllegalArgumentException("missing data");
            }
            map.put(keyValue[0], value);
        }
        return map;
    }

    private void sendExchange(int code, String response) throws IOException {
        httpExchange.sendResponseHeaders(code, response.length());
        OutputStream os = httpExchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private void redirect(String location) throws IOException {
        httpExchange.getResponseHeaders().add("Location", location);
        httpExchange.sendResponseHeaders(303, 0);
    }
}
