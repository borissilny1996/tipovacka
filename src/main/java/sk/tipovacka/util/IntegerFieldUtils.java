package sk.tipovacka.util;

import com.vaadin.flow.component.textfield.IntegerField;

public class IntegerFieldUtils {

    public static int getIntegerFieldValueOrDefault(IntegerField integerField) {
        return integerField.getValue() == null ? 0 : integerField.getValue();
    }
}
