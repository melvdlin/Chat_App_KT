module org.melvdlin.chat_app_kt {
    requires javafx.graphics;
    requires javafx.controls;
    requires kotlin.stdlib;
    requires kotlin.test;

    opens org.melvdlin.chat_app_kt.client to javafx.graphics;
}