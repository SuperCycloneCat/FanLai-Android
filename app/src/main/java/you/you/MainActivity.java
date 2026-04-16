package you.you;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.List;

import you.you.data.LunchRecommender;
import you.you.data.RuleManager;
import you.you.model.RecommendationResult;
import you.you.utils.BadgeUtils;

public class MainActivity extends AppCompatActivity {

    private EditText userInput;
    private Button recommendBtn;
    private TextView foodResult;
    private LinearLayout tagContainer;
    private LunchRecommender recommender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recommender = new LunchRecommender(this);
        initViews();
        setupListeners();
        updateUI();
    }

    private void initViews() {
        userInput = findViewById(R.id.userInput);
        recommendBtn = findViewById(R.id.recommendBtn);
        foodResult = findViewById(R.id.foodResult);
        tagContainer = findViewById(R.id.tagContainer);
    }

    private void setupListeners() {
        recommendBtn.setOnClickListener(v -> {
            v.animate().scaleX(0.9f).scaleY(0.9f).setDuration(150).withEndAction(() -> {
                v.animate().scaleX(1f).scaleY(1f).setDuration(150).start();
            }).start();
            updateUI();
        });

        userInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updateUI();
                return true;
            }
            return false;
        });

        View.OnClickListener exampleClickListener = v -> {
            Button btn = (Button) v;
            String text = btn.getText().toString();
            userInput.setText(text);
            updateUI();
            userInput.clearFocus();
        };

        findViewById(R.id.exampleBtn1).setOnClickListener(exampleClickListener);
        findViewById(R.id.exampleBtn2).setOnClickListener(exampleClickListener);
        findViewById(R.id.exampleBtn3).setOnClickListener(exampleClickListener);
        findViewById(R.id.exampleBtn4).setOnClickListener(exampleClickListener);
        findViewById(R.id.exampleBtn5).setOnClickListener(exampleClickListener);
        findViewById(R.id.tagWorkshopBtn).setOnClickListener(v -> {
            startActivity(new Intent(this, TagWorkshopActivity.class));
        });
    }

    private void updateUI() {
        String userText = userInput.getText().toString().trim();
        RecommendationResult result = recommender.recommend(userText);

        String food = result.getFood();
        foodResult.setText(food != null ? food : "🍚 黄焖鸡");

        tagContainer.removeAllViews();

        List<String> tags = result.getTags();
        String matchedVia = result.getMatchedVia();

        android.widget.HorizontalScrollView scrollView = new android.widget.HorizontalScrollView(this);
        scrollView.setHorizontalScrollBarEnabled(false);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout tagsRow = new LinearLayout(this);
        tagsRow.setOrientation(LinearLayout.HORIZONTAL);
        scrollView.addView(tagsRow);

        if (tags != null && !tags.isEmpty()) {
            List<String> uniqueTags = new ArrayList<>();
            for (String tag : tags) {
                if (!uniqueTags.contains(tag)) {
                    uniqueTags.add(tag);
                }
            }
            if (uniqueTags.size() > 4) {
                uniqueTags = uniqueTags.subList(0, 4);
            }

            for (String tag : uniqueTags) {
                TextView badge = BadgeUtils.createBadge(this, "🏷️ " + tag);
                tagsRow.addView(badge);
            }
        }

        if ("fallback".equals(matchedVia)) {
            TextView fallbackBadge = BadgeUtils.createBadge(this, "🎲 今日随机灵感");
            tagsRow.addView(fallbackBadge);
        } else if (tags == null || tags.isEmpty()) {
            TextView randomBadge = BadgeUtils.createBadge(this, "✨ 随性之选");
            tagsRow.addView(randomBadge);
        }

        tagContainer.addView(scrollView);

        TextView hintText = new TextView(this);
        LinearLayout.LayoutParams hintParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        hintParams.topMargin = 12;
        hintText.setLayoutParams(hintParams);
        hintText.setTextColor(0xffa28d74);
        hintText.setTextSize(14);
        hintText.setTypeface(null, android.graphics.Typeface.ITALIC);

        if ("fallback".equals(matchedVia)) {
            hintText.setText(" 没有命中关键词，为你随机选一个～");
        } else {
            hintText.setText(" 根据你的心情匹配");
        }

        tagContainer.addView(hintText);
    }

    @Override
    protected void onResume() {
        super.onResume();
        RuleManager.getInstance(this).invalidateCache();
        recommender = new LunchRecommender(this);
    }
}