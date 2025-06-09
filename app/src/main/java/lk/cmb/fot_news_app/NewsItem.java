package lk.cmb.fot_news_app;

public class NewsItem {
    private String title;
    private String date;
    private String image;
    private String category;
    private String description;
    private String footer;
    private String hashtags;
    private int likes;        // int type
    private long timestamp;   // long type

    public NewsItem() {}

    // Getters
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getImage() { return image; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getFooter() { return footer; }
    public String getHashtags() { return hashtags; }
    public int getLikes() { return likes; }           // int type
    public long getTimestamp() { return timestamp; }  // long type
}
