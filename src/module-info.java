module org.melvdlin.chat_app_kt {
    requires javafx.graphics;
    requires javafx.controls;
    requires kotlin.stdlib;
    requires kotlin.test;
    requires kotlin.reflect;

    opens org.melvdlin.chat_app_kt.core.client to javafx.graphics;
//    opens org.melvdlin.chat_app_kt to kotlin.reflect;
}