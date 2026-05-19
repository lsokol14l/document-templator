package by.michael.formatters;

import javafx.scene.control.TextFormatter;

public class DateFormatter {

  /**
   * Форматирует строку из цифр в маску дд-мм-yyyy. Безопасно обрабатывает любую длину строки до 8
   * символов.
   */
  private static String formatDate(String digits) {
    int length = digits.length();
    if (length == 0) {
      return "";
    }

    StringBuilder result = new StringBuilder();

    // Форматируем День (первые 2 цифры)
    if (length <= 2) {
      return digits;
    }
    result.append(digits.substring(0, 2)).append("-");

    // Форматируем Месяц (следующие 2 цифры)
    if (length <= 4) {
      result.append(digits.substring(2));
      return result.toString();
    }
    result.append(digits.substring(2, 4)).append("-");

    // Форматируем Год (все оставшиеся цифры)
    result.append(digits.substring(4));

    return result.toString();
  }

  /**
   * Создает TextFormatter для ввода даты (дд-мм-yyyy). Каретка всегда следует за текстом в самый
   * конец при вводе.
   */
  public static TextFormatter<String> createDateFormatter() {
    return new TextFormatter<>(
        change -> {
          // 1. Если текст не менялся (нажаты стрелки, Home, End, клик мыши)
          // — просто отдаем управление JavaFX, ничего не трогая
          if (!change.isContentChange()) {
            return change;
          }

          // Если текст полностью удалили — разрешаем
          if (change.isDeleted() && change.getControlNewText().isEmpty()) {
            return change;
          }

          // Запоминаем, удалял ли пользователь что-то (чтобы курсор не прыгал при Backspace)
          boolean isDeletion = change.isDeleted();
          int deletionStart = change.getRangeStart();

          // Извлекаем только цифры из нового ввода
          String currentNewText = change.getControlNewText();
          String digitsOnly = currentNewText.replaceAll("[^0-9]", "");

          if (digitsOnly.length() > 8) {
            digitsOnly = digitsOnly.substring(0, 8);
          }

          // Получаем итоговую красивую строку с дефисами
          String formattedText = formatDate(digitsOnly);

          // Перезаписываем весь текст в текстовом поле
          change.setRange(0, change.getControlText().length());
          change.setText(formattedText);

          // 2. УПРАВЛЕНИЕ КУРСОРОМ ПО ТВОЕМУ АЛГОРИТМУ
          int targetCaretPos;
          if (isDeletion) {
            // Если стирали — оставляем курсор в месте удаления (чтобы удобно было стирать)
            targetCaretPos = deletionStart;
          } else {
            // Если вводили символы — жестко шлем курсор в САМЫЙ КОНЕЦ строки
            targetCaretPos = formattedText.length();
          }

          // Корректируем границы на всякий случай
          if (targetCaretPos > formattedText.length()) {
            targetCaretPos = formattedText.length();
          }

          change.setCaretPosition(targetCaretPos);
          change.setAnchor(targetCaretPos);

          return change;
        });
  }
}
