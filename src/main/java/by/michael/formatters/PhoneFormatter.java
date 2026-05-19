package by.michael.formatters;

import javafx.scene.control.TextFormatter;

public class PhoneFormatter {
  /** Форматирует номер телефона в формате +7(999)999 99-99 */
  public static TextFormatter<String> createPhoneFormatter() {
    return new TextFormatter<>(
        change -> {
          String text = change.getControlNewText();

          if (text.isEmpty()) {
            return change;
          }

          // Извлекаем только цифры
          String digitsOnly = text.replaceAll("[^0-9]", "");

          // Ограничиваем до 11 цифр
          if (digitsOnly.length() > 11) {
            return null; // Отклоняем изменение
          }

          // Форматируем номер
          String formatted = formatPhone(digitsOnly);
          change.setText(formatted);
          change.setRange(0, change.getRangeEnd());

          return change;
        });
  }



  /** Вспомогательный метод для форматирования телефона */
  private static String formatPhone(String digits) {
    if (digits.isEmpty()) {
      return "";
    }

    StringBuilder result = new StringBuilder("+");

    if (digits.length() <= 1) {
      result.append(digits);
    } else if (digits.length() <= 4) {
      result.append(digits.substring(0, 1)).append("(").append(digits.substring(1));
    } else if (digits.length() <= 7) {
      result
          .append(digits.substring(0, 1))
          .append("(")
          .append(digits.substring(1, 4))
          .append(")")
          .append(digits.substring(4));
    } else if (digits.length() <= 9) {
      result
          .append(digits.substring(0, 1))
          .append("(")
          .append(digits.substring(1, 4))
          .append(")")
          .append(digits.substring(4, 7))
          .append(" ")
          .append(digits.substring(7));
    } else {
      result
          .append(digits.substring(0, 1))
          .append("(")
          .append(digits.substring(1, 4))
          .append(")")
          .append(digits.substring(4, 7))
          .append(" ")
          .append(digits.substring(7, 9))
          .append("-")
          .append(digits.substring(9));
    }

    return result.toString();
  }


}
