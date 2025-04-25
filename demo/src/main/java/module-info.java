module org.example.demo {
    requires javafx.fxml;
    requires java.management;
    requires org.controlsfx.controls;
    requires jbcrypt;
    requires java.mail;
    requires com.google.auth.oauth2;
    requires com.google.gson;
    requires java.sql;
    requires javafx.web;
    requires javafx.graphics;

    requires java.net.http;
    requires mysql.connector.j;
    requires jdk.jsobject;
    requires org.apache.httpcomponents.core5.httpcore5.h2;
    requires java.scripting;
    requires org.json;
    requires java.desktop;
    requires com.google.common;
    requires itextpdf;
    requires org.apache.commons.io;


    opens org.example.demo.controller to javafx.fxml;
    opens org.example.demo.models to javafx.base;
    exports org.example.demo;
    opens org.example.demo.controller.backOffice.user to javafx.fxml;
    opens org.example.demo.controller.backOffice.event to javafx.fxml;
    opens org.example.demo.controller.backOffice.claim to javafx.fxml;
    opens org.example.demo.controller.backOffice.ressource to javafx.fxml;
    opens org.example.demo.controller.backOffice.posts to javafx.fxml;
    opens org.example.demo.controller.backOffice.recyclingPoint to javafx.fxml;
    opens org.example.demo.controller.backOffice to javafx.fxml;
    opens org.example.demo.controller.frontOffice to javafx.fxml;
    opens org.example.demo.controller.frontOffice.settings to javafx.fxml;
    opens org.example.demo.controller.frontOffice.event to javafx.fxml;
    opens org.example.demo.controller.frontOffice.profile to javafx.fxml;
    opens org.example.demo.controller.frontOffice.posts to javafx.fxml;

    opens org.example.demo.controller.frontOffice.ressource to javafx.fxml;
    opens org.example.demo.controller.frontOffice.claim to javafx.fxml;


}