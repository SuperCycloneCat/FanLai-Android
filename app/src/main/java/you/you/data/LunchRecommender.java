package you.you.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import you.you.model.MenuItem;
import you.you.model.RecommendationResult;

public class LunchRecommender {

    private final List<MenuItem> menuList;
    private final List<String> fallbackOptions;
    private final Random random;

    public LunchRecommender(Context context) {
        RuleManager manager = RuleManager.getInstance(context);
        this.menuList = manager.getAllRules();
        this.fallbackOptions = manager.getFallbackOptions();
        this.random = new Random();
    }

    public RecommendationResult recommend(String userText) {

        if (userText == null || userText.trim().isEmpty()) {
            return new RecommendationResult(
                "今天想吃什么？随便说说～",
                new ArrayList<String>(),
                "empty",
                null,
                null
            );
        }

        String lowerText = userText.toLowerCase();

        MenuItem bestMatch = null;
        int maxScore = 0;
        List<String> matchedTags = new ArrayList<>();

        for (MenuItem item : menuList) {
            int score = 0;
            List<String> hitTags = new ArrayList<>();

            for (String tag : item.getTags()) {
                if (lowerText.contains(tag.toLowerCase())) {
                    int weight = tag.length() >= 3 ? 2 : 1;
                    score += weight;
                    hitTags.add(tag);
                }
            }

            if (score > maxScore) {
                maxScore = score;
                bestMatch = item;
                matchedTags = new ArrayList<>(hitTags);
            }
        }

        if (maxScore == 0 || bestMatch == null) {
            int randomIndex = random.nextInt(fallbackOptions.size());
            String randomFood = fallbackOptions.get(randomIndex);
            return new RecommendationResult(
                "✨ " + randomFood,
                Arrays.asList("灵感随机", "随便吃"),
                "fallback",
                "🎲",
                null
            );
        }

        String displayFood = bestMatch.getName();
        if (bestMatch.getEmoji() != null && !displayFood.contains(bestMatch.getEmoji())) {
            displayFood = bestMatch.getEmoji() + " " + displayFood;
        }

        return new RecommendationResult(
            displayFood,
            matchedTags.isEmpty() ? Arrays.asList(bestMatch.getTags().get(0)) : matchedTags,
            "tags",
            bestMatch.getEmoji(),
            bestMatch
        );
    }
}