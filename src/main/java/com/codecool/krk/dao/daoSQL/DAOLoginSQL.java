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
    public Account getAccountByNicknameAndPassword(String name, String password) throws NoSuchElementException {
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM accounts WHERE name=? AND password=?");
            ps.setString(1, name);
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

    @Override
    public Account getAccountBySessionId(String sessionId) throws NoSuchElementException{
        try{
            PreparedStatement ps = connection.prepareStatement("SELECT * FROM accounts WHERE session_id=?");
            ps.setString(1, sessionId);
            ResultSet resultSet = ps.executeQuery();
            if (resultSet.next()){
                return extractAccountFromResultSet(resultSet);
            }
            ps.close();
        } catch (SQLException e){
            e.printStackTrace();
        }
        throw new NoSuchElementException();
    }

    @Override
    public void updateAccount(int id, Account newAccount) {
        String sql = "UPDATE accounts SET name=?, password=?, session_id=? WHERE user_id=?";
        try{
            PreparedStatement ps = connection.prepareStatement(sql);
            ps.setString(1, newAccount.getName());
            ps.setString(2, newAccount.getPassword());
            ps.setString(3, newAccount.getSession_id());
            ps.setInt(4, id);
            ps.execute();
            ps.close();
        } catch (SQLException e){
            System.out.println(String.format("Couldn't update user with %s id", id));
        }
    }

    private Account extractAccountFromResultSet(ResultSet resultSet) throws SQLException{
        Account account = new Account();
        account.setName(resultSet.getString("name"));
        account.setPassword(resultSet.getString("password"));
        account.setSession_id(resultSet.getString("session_id"));
        account.setUser_id(resultSet.getInt("user_id"));
        return account;
    }
}
