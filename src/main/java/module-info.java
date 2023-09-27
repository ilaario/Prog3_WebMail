module com.prog3.email.prog3_webmail {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.prog3.email.prog3_webmail to javafx.fxml;
    exports com.prog3.email.prog3_webmail;
}