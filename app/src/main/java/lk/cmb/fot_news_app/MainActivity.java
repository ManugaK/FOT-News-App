package lk.cmb.fot_news_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Initialize RecyclerView
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 2. Sample data (replace with real data later)
        newsList = new ArrayList<>();
        newsList.add(new NewsItem("Clash of the Titans: Faculty Football Faceoff", "30/04/2025", R.drawable.news1));
        newsList.add(new NewsItem("Soccer Showdown Begins – Feel the Rush!", "29/04/2025", R.drawable.news2));
        newsList.add(new NewsItem("Final Countdown: The Ultimate Battle for Glory", "28/04/2025", R.drawable.news3));
        newsList.add(new NewsItem("Final Countdown: The Ultimate Battle for Glory", "28/04/2025", R.drawable.news3));
        newsList.add(new NewsItem("Final Countdown: The Ultimate Battle for Glory", "28/04/2025", R.drawable.news3));

        // 3. Set adapter
        newsAdapter = new NewsAdapter(newsList);
        newsRecyclerView.setAdapter(newsAdapter);
    }
}
