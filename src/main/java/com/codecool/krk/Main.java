package com.codecool.krk;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        try {
            LoginApp loginApp = new LoginApp();
            loginApp.run();
        } catch (IOException e) {
            System.out.println("Couldn't run server");
            e.printStackTrace();
        }
    }
}
