package by.michael;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;
import org.apache.poi.xwpf.usermodel.*;

public class DocumentService {

  /** Генерирует документ на основе шаблона с заменой плейсхолдеров */
  public void generateDocument(
      String templatePath, String outputPath, Map<String, String> replacements) throws Exception {

    try (FileInputStream inputStream = new FileInputStream(templatePath)) {
      // Загружаем исходный шаблон
      XWPFDocument document = new XWPFDocument(inputStream);

      // Заменяем плейсхолдеры в абзацах
      for (XWPFParagraph paragraph : document.getParagraphs()) {
        replacePlaceholdersInParagraph(paragraph, replacements);
      }

      // Заменяем плейсхолдеры в таблицах
      for (XWPFTable table : document.getTables()) {
        replacePlaceholdersInTable(table, replacements);
      }

      // Заменяем плейсхолдеры в колонтитулах
      for (XWPFHeader header : document.getHeaderList()) {
        for (XWPFParagraph paragraph : header.getParagraphs()) {
          replacePlaceholdersInParagraph(paragraph, replacements);
        }
      }

      for (XWPFFooter footer : document.getFooterList()) {
        for (XWPFParagraph paragraph : footer.getParagraphs()) {
          replacePlaceholdersInParagraph(paragraph, replacements);
        }
      }

      // Сохраняем результат
      try (FileOutputStream outputStream = new FileOutputStream(outputPath)) {
        document.write(outputStream);
      }

      document.close();
    }
  }

  /** Заменяет плейсхолдеры в абзаце */
  private void replacePlaceholdersInParagraph(
      XWPFParagraph paragraph, Map<String, String> replacements) {
    List<XWPFRun> runs = paragraph.getRuns();

    for (int i = 0; i < runs.size(); i++) {
      XWPFRun run = runs.get(i);
      String text = run.getText(0);

      if (text != null) {
        String newText = text;

        // Заменяем все плейсхолдеры
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
          newText = newText.replace(entry.getKey(), entry.getValue());
        }

        if (!text.equals(newText)) {
          run.setText(newText, 0);
        }
      }
    }
  }

  /** Заменяет плейсхолдеры в таблице */
  private void replacePlaceholdersInTable(XWPFTable table, Map<String, String> replacements) {
    for (XWPFTableRow row : table.getRows()) {
      for (XWPFTableCell cell : row.getTableCells()) {
        for (XWPFParagraph paragraph : cell.getParagraphs()) {
          replacePlaceholdersInParagraph(paragraph, replacements);
        }
      }
    }
  }
}
