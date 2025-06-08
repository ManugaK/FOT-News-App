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

    private String selectedCategory = "sports"; // Default category (use lowercase for consistency)
    private String currentSearchQuery = "";     // <-- Store current search

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- GET THE USERNAME from Intent and show greeting ---
        String username = getIntent().getStringExtra("username");
        if (username == null || username.isEmpty()) {
            username = "User"; // fallback if not set
        }
        TextView greetingText = findViewById(R.id.greetingText);
        greetingText.setText("Hi, " + username);

        // 1. Initialize RecyclerView
        newsRecyclerView = findViewById(R.id.newsRecyclerView);
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsList = new ArrayList<>();
        allNewsList = new ArrayList<>();
        newsAdapter = new NewsAdapter(newsList);
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
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    NewsItem newsItem = dataSnapshot.getValue(NewsItem.class);
                    if (newsItem != null) {
                        allNewsList.add(newsItem);
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

    // 5. Filter by category AND search text
    private void filterNews() {
        newsList.clear();
        for (NewsItem item : allNewsList) {
            boolean matchesCategory = item.getCategory() != null &&
                    item.getCategory().equalsIgnoreCase(selectedCategory);
            boolean matchesSearch = item.getTitle() != null &&
                    item.getTitle().toLowerCase().contains(currentSearchQuery.toLowerCase());
            if (matchesCategory && matchesSearch) {
                newsList.add(item);
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

    // --- Add these methods below ---

    // Hide keyboard and clear focus if needed
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
