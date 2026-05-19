package by.michael.formatters;

import javafx.scene.control.TextFormatter;

public class PhoneFormatter {

  /**
   * Вспомогательный метод для форматирования телефона в маску +7(999)999 99-99 Безопасно
   * обрабатывает ввод любой длины до 11 цифр.
   */
  private static String formatPhone(String digits) {
    int length = digits.length();
    if (length == 0) {
      return "";
    }

    StringBuilder result = new StringBuilder("+");

    // 1. Первая цифра (например, 7) -> +7
    if (length <= 1) {
      return result.append(digits).toString();
    }
    result.append(digits.substring(0, 1)).append("(");

    // 2. Код оператора (до 3 цифр) -> +7(999
    if (length <= 4) {
      return result.append(digits.substring(1)).toString();
    }
    result.append(digits.substring(1, 4)).append(")");

    // 3. Первая часть номера (до 3 цифр) -> +7(999)999
    if (length <= 7) {
      return result.append(digits.substring(4)).toString();
    }
    result.append(digits.substring(4, 7)).append(" ");

    // 4. Вторая часть номера (до 2 цифр) -> +7(999)999 99
    if (length <= 9) {
      return result.append(digits.substring(7)).toString();
    }
    result.append(digits.substring(7, 9)).append("-");

    // 5. Хвост номера (оставшиеся 2 цифры) -> +7(999)999 99-99
    result.append(digits.substring(9));

    return result.toString();
  }

  /**
   * Создает TextFormatter для ввода телефона в формате +7(999)999 99-99 Корректно обрабатывает
   * ввод, удаление, навигацию стрелочками и курсор.
   */
  public static TextFormatter<String> createPhoneFormatter() {
    return new TextFormatter<>(
        change -> {
          // 1. Если текст не менялся (нажаты стрелки, выделение, мышка)
          // — отдаем управление JavaFX стандартно
          if (!change.isContentChange()) {
            return change;
          }

          // Если текст полностью удалили — разрешаем это действие
          if (change.isDeleted() && change.getControlNewText().isEmpty()) {
            return change;
          }

          // Запоминаем режим удаления и позицию, где стирали
          boolean isDeletion = change.isDeleted();
          int deletionStart = change.getRangeStart();

          // Извлекаем только цифры из нового состояния поля
          String text = change.getControlNewText();
          String digitsOnly = text.replaceAll("[^0-9]", "");

          // Ограничиваем ввод до 11 цифр (например, для номеров РФ/РБ через 7 или 8)
          if (digitsOnly.length() > 11) {
            digitsOnly = digitsOnly.substring(0, 11);
          }

          // Применяем маску
          String formatted = formatPhone(digitsOnly);

          // Перезаписываем абсолютно весь текст в поле
          change.setRange(0, change.getControlText().length());
          change.setText(formatted);

          // 2. УПРАВЛЕНИЕ КУРСОРОМ ПО ТВОЕМУ АЛГОРИТМУ
          int targetCaretPos;
          if (isDeletion) {
            // Если стирали — оставляем курсор на месте удаления
            targetCaretPos = deletionStart;
          } else {
            // Если вводили — переносим курсор в самый конец новой строки
            targetCaretPos = formatted.length();
          }

          // Защита от выхода за границы строки
          if (targetCaretPos > formatted.length()) {
            targetCaretPos = formatted.length();
          }

          change.setCaretPosition(targetCaretPos);
          change.setAnchor(targetCaretPos);

          return change;
        });
  }
}
