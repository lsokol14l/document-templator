package by.michael;

public class Appendix {
  private String title; // Заголовок приложения
  private String content; // Текст приложения
  private int pageCount; // Количество страниц

  public Appendix(String title, String content, int pageCount) {
    this.title = title;
    this.content = content;
    this.pageCount = pageCount;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public int getPageCount() {
    return pageCount;
  }

  public void setPageCount(int pageCount) {
    this.pageCount = pageCount;
  }

  @Override
  public String toString() {
    return title + " (" + pageCount + " стр.)";
  }
}
