package lk.cmb.fot_news_app;

import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class DeveloperInfoActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_developer_info);

        Button exitButton = findViewById(R.id.exitButton);
        exitButton.setOnClickListener(v -> finish());
    }
}
