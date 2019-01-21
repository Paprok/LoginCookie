package com.codecool.krk.handlerHTTP;

import com.codecool.krk.dao.DAOLogin;
import com.codecool.krk.helpersHTTP.CookieHelper;
import com.codecool.krk.model.Account;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.util.NoSuchElementException;
import java.util.Optional;

public class WelcomeHandler implements HttpHandler {
    private DAOLogin daoLogin;
    private CookieHelper cookieHelper;
    private HttpExchange httpExchange;
    private String response;
    private int code;
    private String location;

    public WelcomeHandler(DAOLogin daoLogin, CookieHelper cookieHelper) {
        this.daoLogin = daoLogin;
        this.cookieHelper = cookieHelper;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        this.httpExchange = httpExchange;
        String method = httpExchange.getRequestMethod();
        resetFields();
        Optional<HttpCookie> cookie = cookieHelper.getSessionIdCookie(httpExchange);
        if (cookie.isPresent()) {
            String sessionId = cookieHelper.getSessionId(cookie);
            if (method.equals("GET")) {
                handleRequestGET(sessionId);
            } else if (method.equals("POST")){
                handleRequestPOST(sessionId);
            }
        }
        redirect();
    }



    private void resetFields() {
            this.response = "";
            this.location = "/";
            this.code = 404;
        }

        private void handleRequestGET (String sessionId) throws IOException {
            try{
                Account account = daoLogin.getAccountBySessionId(sessionId);
                this.response = "<html><head></head><body>" +
                        "<h> Welcome </h>" +
                        "<form action=\"\" method=\"post\">\n" +
                        "    <input type=\"submit\" name=\"logout\" value=\"Logout\"/>\n" +
                        "</form>\n" +
                        "</body></html>";
                this.code = 200;
                this.location = "/welcome";
                sendExchange();
            } catch (NoSuchElementException e){
                redirect();
            }
        }

        private void redirect () throws IOException {
            this.httpExchange.getResponseHeaders().add("Location", "/");
            this.httpExchange.sendResponseHeaders(303, 0);
        }

        private void sendExchange () throws IOException {
            this.httpExchange.getResponseHeaders().add("Location", this.location);
            this.httpExchange.sendResponseHeaders(this.code, this.response.length());
            OutputStream os = this.httpExchange.getResponseBody();
            os.write(this.response.getBytes());
            os.close();
        }

        private void handleRequestPOST(String sessionId){
        Account account = daoLogin.getAccountBySessionId(sessionId);
        account.setSession_id(null);
        daoLogin.updateAccount(account.getUser_id(), account);
        }
    }
