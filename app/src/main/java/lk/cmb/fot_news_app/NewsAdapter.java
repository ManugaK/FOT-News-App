package lk.cmb.fot_news_app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private List<NewsItem> newsList;
    private List<String> newsIdList; // Store Firebase keys
    private String username; // <-- NEW: Store logged-in username

    // Updated constructor accepts username
    public NewsAdapter(List<NewsItem> newsList, List<String> newsIdList, String username) {
        this.newsList = newsList;
        this.newsIdList = newsIdList;
        this.username = username;
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_news_card, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NewsViewHolder holder, int position) {
        NewsItem item = newsList.get(position);
        holder.newsTitle.setText(item.getTitle());
        holder.newsDate.setText(item.getDate());
        Glide.with(holder.itemView.getContext())
                .load(item.getImage())
                .placeholder(R.drawable.news1)
                .error(R.drawable.news1)
                .into(holder.newsImage);

        // Get the newsId for this card
        String newsId = newsIdList.get(position);

        // Card click opens details activity, passes all fields + newsId + username
        holder.itemView.setOnClickListener(v -> {
            Context context = holder.itemView.getContext();
            Intent intent = new Intent(context, NewsDetailsActivity.class);
            intent.putExtra("title", item.getTitle());
            intent.putExtra("date", item.getDate());
            intent.putExtra("image", item.getImage());
            intent.putExtra("description", item.getDescription());
            intent.putExtra("footer", item.getFooter());
            intent.putExtra("hashtags", item.getHashtags());
            intent.putExtra("likes", item.getLikes());
            intent.putExtra("timestamp", item.getTimestamp());
            intent.putExtra("newsId", newsId); // << key for DB updates!
            intent.putExtra("username", username); // <-- NEW: Pass logged-in user!
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage;
        TextView newsTitle, newsDate;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.newsImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsDate = itemView.findViewById(R.id.newsDate);
        }
    }
}
