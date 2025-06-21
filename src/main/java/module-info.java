module com.example.uaslabpbo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires java.net.http;
    requires jdk.compiler;
    requires jbcrypt;
    requires com.google.gson;

    opens com.example.uaslabpbo to javafx.graphics;
    opens com.example.uaslabpbo.controller to javafx.fxml;
    opens com.example.uaslabpbo.model to javafx.base;
    exports com.example.uaslabpbo.model;
    exports com.example.uaslabpbo;
}