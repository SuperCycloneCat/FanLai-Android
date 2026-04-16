package you.you.model;

import java.util.List;

public class MenuItem {
    private final String name;
    private final List<String> tags;
    private final String emoji;

    public MenuItem(String name, List<String> tags, String emoji) {
        this.name = name;
        this.tags = tags;
        this.emoji = emoji;
    }

    public String getName() {
        return name;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getEmoji() {
        return emoji;
    }
}