package you.you.model;

import java.util.List;

public class RecommendationResult {
    private final String food;
    private final List<String> tags;
    private final String matchedVia;
    private final String emoji;
    private final MenuItem rawItem;

    public RecommendationResult(String food, List<String> tags, String matchedVia, String emoji, MenuItem rawItem) {
        this.food = food;
        this.tags = tags;
        this.matchedVia = matchedVia;
        this.emoji = emoji;
        this.rawItem = rawItem;
    }

    public String getFood() { return food; }
    public List<String> getTags() { return tags; }
    public String getMatchedVia() { return matchedVia; }
    public String getEmoji() { return emoji; }
    public MenuItem getRawItem() { return rawItem; }
}