package com.codecool.krk.dao;

import com.codecool.krk.model.Account;

import java.util.NoSuchElementException;

public interface DAOLogin {
    public Account getAccountByNicknameAndPassword(String nickname, String password) throws NoSuchElementException;
    public Account getAccountBySessionId(String sessionId) throws NoSuchElementException;
    void updateAccount(int id, Account newAccount);
}
