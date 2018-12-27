package com.codecool.krk.handlerHTTP;

import com.codecool.krk.dao.DAOLogin;
import com.codecool.krk.helpersHTTP.CookieHelper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
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
        String sessionId = cookie.get().getValue();
        sessionId = sessionId.replace("\"", "");
        if(method.equals("GET")) {
            handleRequestGET(sessionId);
        }
        sendExchange();
    }

    private void resetFields() {
        this.response = "";
        this.location = "/";
        this.code = 404;
    }

    private void handleRequestGET(String sessionId){
        if(daoLogin.getAccountBySessionId(sessionId) != null) {
            this.response = "<html><head></head><body>" +
                    "<h> Welcome </h>" +
                    "</body></html>";
            this.code = 200;
            this.location = "/welcome";
        } else {
            this.code = 303;
        }
    }

    private void sendExchange() throws IOException{
        this.httpExchange.getResponseHeaders().add("Location", this.location);
        this.httpExchange.sendResponseHeaders(this.code, this.response.length());
        OutputStream os = this.httpExchange.getResponseBody();
        os.write(this.response.getBytes());
        os.close();
    }
}
