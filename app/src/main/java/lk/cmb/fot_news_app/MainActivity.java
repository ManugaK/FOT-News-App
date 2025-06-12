package lk.cmb.fot_news_app;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.view.View;
import android.widget.TextView;
import android.app.Activity;
import android.view.inputmethod.InputMethodManager;
import android.view.MotionEvent;
import android.content.Intent;
import android.widget.ImageButton;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private List<NewsItem> newsList;     // Filtered list for the RecyclerView
    private List<NewsItem> allNewsList;  // Master list with all news items

    // ---- Track Firebase keys ----
    private List<String> newsIdList;     // Filtered IDs
    private List<String> allNewsIdList;  // All IDs

    private String selectedCategory = "sports"; // Default category
    private String currentSearchQuery = "";     // Store current search

    private String username = "User";

    private TextView greetingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- GET THE USERNAME from SharedPreferences (not just Intent) ---
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "User");

        greetingText = findViewById(R.id.greetingText);
        greetingText.setText("Hi, " + username);

        // --- Handle Info Button to open Developer Info Activity ---
        View infoButton = findViewById(R.id.infoButton);
        if (infoButton != null) {
            infoButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, DeveloperInfoActivity.class);
                startActivity(intent);
            });
        }

        // --- Handle Profile Button to navigate to Profile Activity ---
        ImageButton profileButton = findViewById(R.id.profileButton);
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                // No need to pass username; ProfileActivity will read from SharedPreferences
                startActivity(intent);
            });
        }

        // 1. Initialize RecyclerView
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsList = new ArrayList<>();
        allNewsList = new ArrayList<>();
        newsIdList = new ArrayList<>();
        allNewsIdList = new ArrayList<>();
        newsAdapter = new NewsAdapter(newsList, newsIdList, username);
        newsRecyclerView.setAdapter(newsAdapter);

        // 2. Initialize Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_sports) {
                selectedCategory = "sports";
            } else if (itemId == R.id.nav_academic) {
                selectedCategory = "academic";
            } else if (itemId == R.id.nav_events) {
                selectedCategory = "events";
            }
            filterNews();
            return true;
        });

        // 3. Setup Search Bar
        EditText searchEditText = findViewById(R.id.searchEditText);
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                filterNews();
            }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        // 4. Fetch News Data from Firebase
        DatabaseReference newsRef = FirebaseDatabase.getInstance().getReference("news");
        newsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allNewsList.clear();
                allNewsIdList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NewsItem newsItem = dataSnapshot.getValue(NewsItem.class);
                    if (newsItem != null) {
                        allNewsList.add(newsItem);
                        allNewsIdList.add(dataSnapshot.getKey());
                    }
                }
                filterNews();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Optionally show an error (e.g., Toast)
            }
        });
    }

    // Refresh username greeting every time MainActivity resumes (after returning from ProfileActivity)
    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        username = prefs.getString("username", "User");
        if (greetingText != null) {
            greetingText.setText("Hi, " + username);
        }
        // Also, update NewsAdapter's username if needed (if you use username in adapter)
        if (newsAdapter != null) {
            newsAdapter.setUsername(username);
            newsAdapter.notifyDataSetChanged();
        }
    }

    // 5. Filter by category AND search text
    private void filterNews() {
        newsList.clear();
        newsIdList.clear();
        for (int i = 0; i < allNewsList.size(); i++) {
            NewsItem item = allNewsList.get(i);
            boolean matchesCategory = item.getCategory() != null &&
                    item.getCategory().equalsIgnoreCase(selectedCategory);
            boolean matchesSearch = item.getTitle() != null &&
                    item.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase());
            if (matchesCategory && matchesSearch) {
                newsList.add(item);
                newsIdList.add(allNewsIdList.get(i));
            }
        }
        newsAdapter.notifyDataSetChanged();

        // Show/hide 'no results' message and RecyclerView
        TextView noResultsText = findViewById(R.id.noResultsText);
        if (newsList.isEmpty()) {
            noResultsText.setVisibility(View.VISIBLE);
            newsRecyclerView.setVisibility(View.GONE);
        } else {
            noResultsText.setVisibility(View.GONE);
            newsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void hideKeyboardAndClearFocus() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            view.clearFocus();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                int[] scrcoords = new int[2];
                v.getLocationOnScreen(scrcoords);
                float x = ev.getRawX() + v.getLeft() - scrcoords[0];
                float y = ev.getRawY() + v.getTop() - scrcoords[1];
                if (x < v.getLeft() || x > v.getRight() || y < v.getTop() || y > v.getBottom()) {
                    hideKeyboardAndClearFocus();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
