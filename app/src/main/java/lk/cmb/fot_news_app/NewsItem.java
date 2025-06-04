package lk.cmb.fot_news_app;
public class NewsItem {
    private String title;
    private String date;
    private int imageResId; // For local drawable, use int; for URL, use String

    public NewsItem(String title, String date, int imageResId) {
        this.title = title;
        this.date = date;
        this.imageResId = imageResId;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public int getImageResId() { return imageResId; }
}
