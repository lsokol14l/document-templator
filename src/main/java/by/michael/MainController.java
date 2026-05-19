package by.michael;

import by.michael.formatters.DateFormatter;
import by.michael.formatters.PhoneFormatter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainController {
  @FXML private TextField templatePathField;

  // Реквизиты письма
  @FXML private TextField letterDateField;
  @FXML private TextField letterNumberField;

  // Адресат
  @FXML private TextField recipientPostField;
  @FXML private TextField recipientOrganizationField;
  @FXML private TextField recipientNameField;
  @FXML private TextField greetingField;
  @FXML private Label recipientNameErrorLabel;

  // Содержание
  @FXML private TextArea letterBodyArea;
  @FXML private Label letterBodyErrorLabel;

  // Приложения
  @FXML private TextField appendixTitleField;
  @FXML private TextArea appendixContentArea;
  @FXML private Spinner<Integer> appendixPageCountSpinner;
  @FXML private ListView<Appendix> appendixListView;

  // Отправитель
  @FXML private TextField senderPostField;
  @FXML private TextField senderNameField;

  // Исполнитель
  @FXML private TextField executorNameField;
  @FXML private TextField executorPhoneField;

  // Прочее
  @FXML private TextField outputPathField;
  @FXML private Label statusLabel;
  @FXML private TextArea previewArea;

  private DocumentService documentService;
  private ObservableList<Appendix> appendices;

  @FXML
  public void initialize() {
    documentService = new DocumentService();

    // Инициализируем список приложений
    appendices = FXCollections.observableArrayList();
    appendixListView.setItems(appendices);

    // Инициализируем Spinner для количества страниц
    SpinnerValueFactory<Integer> valueFactory =
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1);
    appendixPageCountSpinner.setValueFactory(valueFactory);

    // Добавляем кнопку удаления при двойном клике
    appendixListView.setOnMouseClicked(
        event -> {
          if (event.getClickCount() == 2) {
            Appendix selected = appendixListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
              appendices.remove(selected);
              updatePreview();
              updateStatus("Приложение удалено", false);
            }
          }
        });

    // Слушатели для обновления предпросмотра в реальном времени
    letterBodyArea.textProperty().addListener((obs, old, newVal) -> updatePreview());
    recipientNameField.textProperty().addListener((obs, old, newVal) -> updatePreview());
    letterDateField.textProperty().addListener((obs, old, newVal) -> updatePreview());
    letterNumberField.textProperty().addListener((obs, old, newVal) -> updatePreview());

    // Применяем форматеры
    letterDateField.setTextFormatter(DateFormatter.createDateFormatter());
    executorPhoneField.setTextFormatter(PhoneFormatter.createPhoneFormatter());
  }

  @FXML
  private void onChooseTemplate() {
    File file = openFileChooser("Выберите шаблон", "*.docx", "DOCX Files");
    if (file != null) {
      templatePathField.setText(file.getAbsolutePath());
      updateStatus("Шаблон выбран: " + file.getName(), false);
    }
  }

  @FXML
  private void onChooseOutputPath() {
    File file = saveFileChooser("Сохранить как", "*.docx", "DOCX Files");
    if (file != null) {
      outputPathField.setText(file.getAbsolutePath());
    }
  }

  @FXML
  private void onAddAppendix() {
    String title = appendixTitleField.getText().trim();
    String content = appendixContentArea.getText().trim();
    int pageCount = appendixPageCountSpinner.getValue();

    if (title.isEmpty()) {
      updateStatus("Ошибка: введите заголовок приложения", true);
      return;
    }

    if (content.isEmpty()) {
      updateStatus("Ошибка: введите текст приложения", true);
      return;
    }

    Appendix appendix = new Appendix(title, content, pageCount);
    appendices.add(appendix);

    // Очищаем поля
    appendixTitleField.clear();
    appendixContentArea.clear();
    appendixPageCountSpinner.getValueFactory().setValue(1);

    updatePreview();
    updateStatus("Приложение добавлено", false);
  }

  /** Валидирует все обязательные поля */
  private boolean validateFields() {
    boolean isValid = true;

    // Очищаем предыдущие ошибки
    recipientNameErrorLabel.setText("");
    letterBodyErrorLabel.setText("");

    // Проверка шаблона
    if (templatePathField.getText().isEmpty()) {
      updateStatus("✗ Ошибка: выберите шаблон", true);
      return false;
    }

    // Проверка ФИО адресата
    if (recipientNameField.getText().trim().isEmpty()) {
      recipientNameErrorLabel.setText("⚠ Обязательное поле");
      updateStatus("✗ Ошибка: введите ФИО адресата", true);
      isValid = false;
    }

    // Проверка текста письма
    if (letterBodyArea.getText().trim().isEmpty()) {
      letterBodyErrorLabel.setText("⚠ Обязательное поле");
      updateStatus("✗ Ошибка: введите текст письма", true);
      isValid = false;
    }

    // Проверка пути сохранения
    if (outputPathField.getText().isEmpty()) {
      updateStatus("✗ Ошибка: выберите путь для сохранения", true);
      return false;
    }

    return isValid;
  }

  @FXML
  private void onGenerateDocument() {
    // Валидируем поля
    if (!validateFields()) {
      return;
    }

    try {
      // Подготавливаем словарь замен
      java.util.Map<String, String> replacements = new java.util.HashMap<>();

      // Реквизиты письма
      replacements.put("{LETTER_DATE}", letterDateField.getText());
      replacements.put("{LETTER_NUMBER}", letterNumberField.getText());

      // Адресат
      replacements.put("{RECIPIENT_POST}", recipientPostField.getText());
      replacements.put("{RECIPIENT_ORGANIZATION}", recipientOrganizationField.getText());
      replacements.put("{RECIPIENT_NAME}", recipientNameField.getText());
      replacements.put("{GREETING}", greetingField.getText());

      // Содержание
      replacements.put("{LETTER_BODY}", letterBodyArea.getText());

      // Отправитель
      replacements.put("{SENDER_POST}", senderPostField.getText());
      replacements.put("{SENDER_NAME}", senderNameField.getText());

      // Исполнитель
      replacements.put("{EXECUTOR_NAME}", executorNameField.getText());
      replacements.put("{EXECUTOR_PHONE}", executorPhoneField.getText());

      // Генерируем документ с приложениями
      String templatePath = templatePathField.getText();
      String outputPath = outputPathField.getText();

      documentService.generateDocument(
          templatePath, outputPath, replacements, new ArrayList<>(appendices));

      updateStatus("✓ Документ успешно создан: " + new File(outputPath).getName(), false);

      // Показываем диалог об успехе
      showInfo("Успех", "Документ создан успешно!\n" + outputPath);

    } catch (Exception e) {
      updateStatus("✗ Ошибка: " + e.getMessage(), true);
      showError("Ошибка", "Не удалось создать документ:\n" + e.getMessage());
    }
  }

  /** Обновляет предпросмотр письма в реальном времени с форматированием */
  private void updatePreview() {
    StringBuilder preview = new StringBuilder();

    preview.append("═══════════════════════════════════════════════════════════════\n");
    preview.append("ПРЕДПРОСМОТР ПИСЬМА\n");
    preview.append("═══════════════════════════════════════════════════════════════\n\n");

    // Реквизиты письма (дата справа, номер слева)
    if (!letterDateField.getText().isEmpty() || !letterNumberField.getText().isEmpty()) {
      String dateStr = letterDateField.getText().isEmpty() ? "" : letterDateField.getText();
      String numberStr = letterNumberField.getText().isEmpty() ? "" : letterNumberField.getText();

      // Форматируем дату справа, номер слева
      String line = String.format("%-30s%30s", "№ " + numberStr, dateStr);
      preview.append(line).append("\n\n");
    }

    // Адресат
    if (!recipientPostField.getText().isEmpty()
        || !recipientOrganizationField.getText().isEmpty()
        || !recipientNameField.getText().isEmpty()) {

      if (!recipientPostField.getText().isEmpty()) {
        preview.append(recipientPostField.getText()).append("\n");
      }
      if (!recipientOrganizationField.getText().isEmpty()) {
        preview.append(recipientOrganizationField.getText()).append("\n");
      }
      if (!recipientNameField.getText().isEmpty()) {
        preview.append(recipientNameField.getText()).append("\n");
      }
      preview.append("\n");
    }

    // Приветствие
    if (!greetingField.getText().isEmpty()) {
      preview.append(greetingField.getText()).append("!\n\n");
    }

    // Основной текст
    preview.append(letterBodyArea.getText()).append("\n\n");

    // Приложения с форматированием
    if (!appendices.isEmpty()) {
      preview.append("───────────────────────────────────────────────────────────────\n");
      preview.append("ПРИЛОЖЕНИЯ:\n");
      preview.append("───────────────────────────────────────────────────────────────\n");
      for (int i = 0; i < appendices.size(); i++) {
        Appendix app = appendices.get(i);
        if (appendices.size() == 1) {
          preview.append("\nПриложение").append(" (").append(app.getPageCount()).append(" стр.)\n");
        } else {
          preview
              .append("\nПриложение ")
              .append(i + 1)
              .append(" (")
              .append(app.getPageCount())
              .append(" стр.)\n");
        }
        preview.append(app.getTitle()).append("\n");
      }
      preview.append("\n");
    }

    // Подпись и отправитель (с табуляцией)
    if (!senderNameField.getText().isEmpty() || !senderPostField.getText().isEmpty()) {
      preview.append("───────────────────────────────────────────────────────────────\n");
      preview.append("\n");
      preview.append("                    ____________          ");
      if (!senderNameField.getText().isEmpty()) {
        preview.append(senderNameField.getText());
      }
      preview.append("\n");
      preview.append("                    (Подпись)             (ФИО)\n");
      if (!senderPostField.getText().isEmpty()) {
        preview.append("\n").append(senderPostField.getText()).append("\n");
      }
      preview.append("\n");
    }

    // Исполнитель (внизу страницы)
    if (!executorNameField.getText().isEmpty() || !executorPhoneField.getText().isEmpty()) {
      preview.append("───────────────────────────────────────────────────────────────\n");
      preview.append("Исполнитель:\n");
      if (!executorNameField.getText().isEmpty()) {
        preview.append("  ").append(executorNameField.getText()).append("\n");
      }
      if (!executorPhoneField.getText().isEmpty()) {
        preview.append("  Тел: ").append(executorPhoneField.getText()).append("\n");
      }
    }

    preview.append("\n═══════════════════════════════════════════════════════════════");

    previewArea.setText(preview.toString());
  }

  @FXML
  private void onClear() {
    templatePathField.clear();
    letterDateField.clear();
    letterNumberField.clear();
    recipientPostField.clear();
    recipientOrganizationField.clear();
    recipientNameField.clear();
    greetingField.clear();
    letterBodyArea.clear();
    appendixTitleField.clear();
    appendixContentArea.clear();
    appendixPageCountSpinner.getValueFactory().setValue(1);
    appendices.clear();
    senderPostField.clear();
    senderNameField.clear();
    executorNameField.clear();
    executorPhoneField.clear();
    outputPathField.clear();

    // Очищаем ошибки
    recipientNameErrorLabel.setText("");
    letterBodyErrorLabel.setText("");

    updatePreview();
    updateStatus("Поля очищены", false);
  }

  private File openFileChooser(String title, String extension, String description) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(title);
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
    return fileChooser.showOpenDialog(new Stage());
  }

  private File saveFileChooser(String title, String extension, String description) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle(title);
    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(description, extension));
    fileChooser.setInitialFileName("письмо.docx");
    return fileChooser.showSaveDialog(new Stage());
  }

  private void updateStatus(String message, boolean isError) {
    statusLabel.setText(message);
    statusLabel.setStyle(isError ? "-fx-text-fill: #cc0000;" : "-fx-text-fill: #0066cc;");
  }

  private void showInfo(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }

  private void showError(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
