package you.you.data;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import you.you.model.MenuItem;
import you.you.model.RecommendationResult;

public class LunchRecommender {

    private static final List<String> LUCKY_TEXTS = Arrays.asList(
            "🍀 命中注定就是它了",
            "✨ 老天让你吃这个",
            "🎲 骰子已掷出",
            "🌙 今天的缘分是这个",
            "⚡ 灵感击中了你",
            "🎪 闭眼选都好吃",
            "🌟 这就是宿命",
            "🎯 命运的选择",
            "🍻 跟着感觉走"
    );

    private final List<MenuItem> menuList;
    private final List<String> fallbackOptions;
    private final Random random;

    private final SynonymManager synonymManager;

    public LunchRecommender(Context context) {
        RuleManager manager = RuleManager.getInstance(context);
        synonymManager = SynonymManager.getInstance(context);
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

        Set<String> expandedWords = synonymManager.expand(userText);
        String lowerText = userText.toLowerCase();

        MenuItem bestMatch = null;
        int maxScore = 0;
        List<String> matchedTags = new ArrayList<>();

        for (MenuItem item : menuList) {
            int score = 0;
            List<String> hitTags = new ArrayList<>();
            String foodName = item.getName().toLowerCase();

            // ========== 1. 食物名称匹配（仅内部加分，不显示标签）==========
            for (String word : expandedWords) {
                if (foodName.contains(word)) {
                    score += 3;
                    break;
                }
            }

            // ========== 2. 标签匹配 ==========
            for (String tag : item.getTags()) {
                String lowerTag = tag.toLowerCase();
                if (lowerText.contains(lowerTag)) {
                    int weight = tag.length() >= 3 ? 2 : 1;
                    score += weight;
                    hitTags.add(tag);
                    continue;
                }
                for (String word : expandedWords) {
                    if (word.contains(lowerTag) || lowerTag.contains(word)) {
                        score += 1;
                        hitTags.add(tag);
                        break;
                    }
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

    public RecommendationResult pureRandom() {
        List<String> allFoods = new ArrayList<>();
        for (MenuItem item : menuList) {
            allFoods.add(item.getEmoji() + " " + item.getName());
        }
        allFoods.addAll(fallbackOptions);

        String randomFood = allFoods.get(random.nextInt(allFoods.size()));
        String luckyText = LUCKY_TEXTS.get(random.nextInt(LUCKY_TEXTS.size()));

        return new RecommendationResult(
                "🎰 " + randomFood,
                Arrays.asList(luckyText),
                "lucky",
                "🎲",
                null
        );
    }
}