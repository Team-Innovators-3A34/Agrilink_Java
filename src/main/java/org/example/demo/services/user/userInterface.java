package org.example.demo.services.user;

import org.example.demo.HelloApplication;
import org.example.demo.models.User;
import org.example.demo.utils.EmailSender;
import org.example.demo.utils.dataBaseHelper;
import org.example.demo.utils.sessionManager;
import org.mindrot.jbcrypt.BCrypt;

import javax.mail.MessagingException;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public interface userInterface {

    boolean login(String username, String password) ;

    boolean loginGoogle(String email) ;

    boolean Register(User user) ;

    boolean checkUniqueEmail(String email) ;

    boolean checkVerifyUser(String email) ;

    boolean checkBannedUser(String email) ;

    void SendCodeValidationAccount(String email) ;

    Boolean ConfirmCodeValidationAccount(String email, String code) ;

    void SendCodeResetPassword(String email) ;

    Boolean ResetPassword(String email, String code, String password) ;

    List<User> getAllUsers() ;

    void DeleteUser(String email) ;

    void updateUser(User user) ;

    boolean verifyPassword(User user, String oldPassword) ;

    boolean updatePassword(User user, String newPassword) ;

}