package com.codecool.krk;

import com.codecool.krk.connectors.SQLConnector;
import com.codecool.krk.dao.DAOLogin;
import com.codecool.krk.dao.daoSQL.DAOLoginSQL;
import com.codecool.krk.handlerHTTP.LoginHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.Connection;

public class LoginApp {

    public void run() throws IOException {
        DAOLogin daoLogin = getDaoLogin();

        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/", new LoginHandler(daoLogin));
        server.start();
    }

    private DAOLogin getDaoLogin() {
        Connection connection = SQLConnector.getConnection();
        return new DAOLoginSQL(connection);
    }
}
