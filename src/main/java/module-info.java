module com.prog3.email.prog3_webmail {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.prog3.email.prog3_webmail.Client to javafx.fxml;
    exports com.prog3.email.prog3_webmail.Client;
    opens com.prog3.email.prog3_webmail.Server to javafx.fxml;
    exports com.prog3.email.prog3_webmail.Server;
}