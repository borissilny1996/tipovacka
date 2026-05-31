package sk.tipovacka.util;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

public class NotificationUtil {

    private static final int DURATION = 3000;

    public static void showSuccess(String message) {
        Notification n = Notification.show(message);
        n.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        n.setDuration(DURATION);
    }

    public static void showError(String message) {
        Notification n = Notification.show(message);
        n.addThemeVariants(NotificationVariant.LUMO_ERROR);
        n.setDuration(DURATION);
    }
}
