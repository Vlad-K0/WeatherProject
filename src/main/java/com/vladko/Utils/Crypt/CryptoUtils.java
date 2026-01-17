package com.vladko.Utils.Crypt;

import org.mindrot.jbcrypt.BCrypt;

public class CryptoUtils {

    public static String encryptPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public static Boolean checkPassword(String userPassword, String passwordFromDB) {
        return BCrypt.checkpw(userPassword, passwordFromDB);
    }
}
