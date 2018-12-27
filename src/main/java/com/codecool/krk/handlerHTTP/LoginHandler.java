package com.codecool.krk.handlerHTTP;

import com.codecool.krk.dao.DAOLogin;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class LoginHandler implements HttpHandler {
    private DAOLogin daoLogin;

    public LoginHandler(DAOLogin daoLogin) {
        this.daoLogin = daoLogin;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {

    }
}
