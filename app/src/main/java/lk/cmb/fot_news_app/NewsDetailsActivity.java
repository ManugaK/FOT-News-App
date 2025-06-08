package lk.cmb.fot_news_app;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;

public class NewsDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);

        // Get data from Intent
        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String image = getIntent().getStringExtra("image");
        String description = getIntent().getStringExtra("description");
        String footer = getIntent().getStringExtra("footer");
        String hashtags = getIntent().getStringExtra("hashtags");
        int likes = getIntent().getIntExtra("likes", 0);
        long timestamp = getIntent().getLongExtra("timestamp", 0);

        // Bind views
        TextView titleText = findViewById(R.id.detailsTitle);
        TextView dateText = findViewById(R.id.detailsDate);
        ImageView imageView = findViewById(R.id.detailsImage);
        TextView descriptionText = findViewById(R.id.detailsDescription);
        TextView footerText = findViewById(R.id.detailsFooter);
        TextView hashtagsText = findViewById(R.id.detailsHashtags);
        TextView likesText = findViewById(R.id.detailsLikes);
        TextView timestampText = findViewById(R.id.detailsTimestamp);

        // Set data to views
        titleText.setText(title);
        dateText.setText(date);
        descriptionText.setText(description);
        footerText.setText(footer);
        hashtagsText.setText(hashtags);
        likesText.setText("Likes: " + likes);
        timestampText.setText("Timestamp: " + timestamp);

        Glide.with(this)
                .load(image)
                .placeholder(R.drawable.news1)
                .into(imageView);
    }
}
