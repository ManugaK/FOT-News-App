package lk.cmb.fot_news_app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewsDetailsActivity extends AppCompatActivity {

    private boolean isLiked = false;
    private int likes = 0;
    private String newsId = ""; // The node ID in your DB ("news1" etc.)
    private String username = ""; // <-- NEW: Store username

    private static final String PREFS_NAME = "likes_prefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_details);

        // Find views
        ImageView backButton = findViewById(R.id.backButton);
        ImageView profileButton = findViewById(R.id.profileButton);
        TextView newsTitle = findViewById(R.id.newsTitle);
        ImageView newsImage = findViewById(R.id.newsImage);
        TextView newsDate = findViewById(R.id.newsDate);
        TextView newsLikes = findViewById(R.id.newsLikes);
        TextView newsTimeAgo = findViewById(R.id.newsTimeAgo);
        TextView newsDescription = findViewById(R.id.newsDescription);
        TextView newsFooter = findViewById(R.id.newsFooter);
        TextView newsHashtags = findViewById(R.id.newsHashtags);
        ImageView likeIcon = findViewById(R.id.likeIcon);

        // Handle back button
        backButton.setOnClickListener(v -> finish());

        // --- Get data from Intent ---
        newsId = getIntent().getStringExtra("newsId"); // You'll need to send this with the intent!
        String title = getIntent().getStringExtra("title");
        String date = getIntent().getStringExtra("date");
        String image = getIntent().getStringExtra("image");
        String description = getIntent().getStringExtra("description");
        String footer = getIntent().getStringExtra("footer");
        String hashtags = getIntent().getStringExtra("hashtags");
        likes = getIntent().getIntExtra("likes", 0);
        long timestamp = getIntent().getLongExtra("timestamp", System.currentTimeMillis() / 1000);
        username = getIntent().getStringExtra("username"); // <-- NEW: Get logged-in username!

        // Set data
        newsTitle.setText(title);
        newsDate.setText(date);
        newsLikes.setText(String.valueOf(likes));
        newsFooter.setText(footer);
        newsHashtags.setText(hashtags);
        newsTimeAgo.setText(getTimeAgo(timestamp));
        newsDescription.setText(description);

        Glide.with(this).load(image)
                .placeholder(R.drawable.news1)
                .error(R.drawable.news1)
                .into(newsImage);

        // --- LIKE BUTTON TOGGLE LOGIC BELOW ---

        // 1. Check local session if this news is liked
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isLiked = prefs.getBoolean(newsId, false);
        updateLikeIcon(likeIcon);

        // 2. Like button click
        likeIcon.setOnClickListener(v -> {
            if (!isLiked) {
                isLiked = true;
                likes++;
                updateLikeInFirebase(likes);
            } else {
                isLiked = false;
                likes = Math.max(0, likes - 1);
                updateLikeInFirebase(likes);
            }
            // Save in SharedPreferences
            prefs.edit().putBoolean(newsId, isLiked).apply();
            // Update UI
            newsLikes.setText(String.valueOf(likes));
            updateLikeIcon(likeIcon);
        });

        // --- PROFILE BUTTON: navigate to ProfileActivity with username ---
        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(NewsDetailsActivity.this, ProfileActivity.class);
            intent.putExtra("username", username); // <-- Pass username!
            startActivity(intent);
        });
    }

    // Sets the icon for liked/unliked state
    private void updateLikeIcon(ImageView likeIcon) {
        if (isLiked) {
            likeIcon.setImageResource(R.drawable.filledlikeicon); // Use your filled icon
        } else {
            likeIcon.setImageResource(R.drawable.likeicon); // Outline icon
        }
    }

    // Updates the likes value in Firebase
    private void updateLikeInFirebase(int newLikes) {
        if (newsId == null || newsId.isEmpty()) return;
        DatabaseReference newsRef = FirebaseDatabase.getInstance().getReference("news").child(newsId);
        newsRef.child("likes").setValue(newLikes);
    }

    // Utility: Convert Unix timestamp to "12m" style
    private String getTimeAgo(long timestamp) {
        if (timestamp < 1000000000000L) {
            timestamp = timestamp * 1000;
        }
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        if (diff < 60 * 1000) return "just now";
        else if (diff < 60 * 60 * 1000) return (diff / (60 * 1000)) + "m";
        else if (diff < 24 * 60 * 60 * 1000) return (diff / (60 * 60 * 1000)) + "h";
        else return (diff / (24 * 60 * 60 * 1000)) + "d";
    }
}
