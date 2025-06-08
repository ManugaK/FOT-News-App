package lk.cmb.fot_news_app;

public class NewsItem {
    private String title;
    private String date;
    private String image;
    private String category; // Add this!

    public NewsItem() {}

    public NewsItem(String title, String date, String image, String category) {
        this.title = title;
        this.date = date;
        this.image = image;
        this.category = category;
    }

    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getImage() { return image; }
    public String getCategory() { return category; }
}

