package com.codecool.krk.dao.daoSQL;

import com.codecool.krk.dao.DAOLogin;
import com.codecool.krk.model.Account;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.NoSuchElementException;

public class DAOLoginSQL implements DAOLogin {
    private Connection connection;

    public DAOLoginSQL(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Account getAccountByNicknameAndPassword(String nickname, String password) throws NoSuchElementException {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM accounts WHERE nick=? AND password=?");
            ps.setString(1, nickname);
            ps.setString(2, password);
            ResultSet resultSet = ps.executeQuery();

            if (resultSet.next()) {
                return extractAccountFromResultSet(resultSet);
            }

            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new NoSuchElementException();
    }

    private Account extractAccountFromResultSet(ResultSet resultSet){
        return null;
    }
}
