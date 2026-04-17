package you.you;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import you.you.data.RuleManager;
import you.you.model.MenuItem;

public class TagWorkshopActivity extends AppCompatActivity {

    private RecyclerView rulesRecyclerView;
    private RulesAdapter adapter;
    private List<Object> items;
    private RuleManager ruleManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag_workshop);

        ruleManager = RuleManager.getInstance(this);
        initViews();
        loadData();
    }

    private void initViews() {
        TextView backBtn = findViewById(R.id.backBtn);
        rulesRecyclerView = findViewById(R.id.rulesRecyclerView);

        backBtn.setOnClickListener(v -> finish());
        TextView menuBtn = findViewById(R.id.menuBtn);
        menuBtn.setOnClickListener(v -> {
            android.widget.PopupMenu popup = new android.widget.PopupMenu(this, v);
            popup.getMenuInflater().inflate(R.menu.workshop_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == R.id.action_add) {
                    showEditDialog(-1, null);
                } else if (id == R.id.action_reset) {
                    new AlertDialog.Builder(this)
                            .setMessage("确定重置为默认规则吗？\n您自定义的所有规则将会丢失")
                            .setPositiveButton("重置", (dialog, which) -> {
                                ruleManager.resetToDefault();
                                ruleManager.invalidateCache();  // 全局清除缓存
                                loadData();
                                Toast.makeText(this, "已重置为默认规则", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                } else if (id == R.id.action_clear) {
                    new AlertDialog.Builder(this)
                            .setMessage("确定清空所有规则吗？")
                            .setPositiveButton("清空", (dialog, which) -> {
                                for (int i = ruleManager.getAllRules().size() - 1; i >= 0; i--) {
                                    ruleManager.deleteRule(i);
                                }
                                ruleManager.invalidateCache();  // 全局清除缓存
                                loadData();
                                Toast.makeText(this, "已清空所有规则", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }
                return true;
            });
            popup.show();
        });

        rulesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadData() {
        items = new ArrayList<>();
        items.addAll(ruleManager.getAllRules());
        items.add("FALLBACK_POOL");

        adapter = new RulesAdapter(items);
        rulesRecyclerView.setAdapter(adapter);
    }

    private void showEditDialog(final int position, final MenuItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_rule, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final TextView titleText = view.findViewById(R.id.dialogTitle);
        final EditText emojiNameInput = view.findViewById(R.id.editEmojiName);
        final EditText tagsInput = view.findViewById(R.id.editTags);
        final Button cancelBtn = view.findViewById(R.id.cancelBtn);
        final Button saveBtn = view.findViewById(R.id.saveBtn);

        titleText.setText(position == -1 ? "新增规则" : "编辑规则");

        if (item != null) {
            emojiNameInput.setText(item.getEmoji() + " " + item.getName());
            tagsInput.setText(String.join(", ", item.getTags()));
        }

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        saveBtn.setOnClickListener(v -> {
            String emojiName = emojiNameInput.getText().toString().trim();
            String tagsStr = tagsInput.getText().toString().trim();

            if (emojiName.isEmpty() || tagsStr.isEmpty()) {
                Toast.makeText(this, "请填写完整信息", Toast.LENGTH_SHORT).show();
                return;
            }

            String emoji = "🍽️";
            String name = emojiName;
            if (emojiName.length() >= 2 && Character.isSurrogate(emojiName.charAt(0))) {
                emoji = emojiName.substring(0, 2);
                name = emojiName.substring(2).trim();
            }

            if (name.isEmpty()) {
                name = "新食物";
            }

            List<String> tags = Arrays.asList(tagsStr.replace("，", ",").split(","));
            for (int i = 0; i < tags.size(); i++) {
                tags.set(i, tags.get(i).trim());
            }

            MenuItem newItem = new MenuItem(name, tags, emoji);
            if (position == -1) {
                ruleManager.addRule(newItem);
            } else {
                ruleManager.updateRule(position, newItem);
            }
            ruleManager.invalidateCache();  // 全局清除缓存
            loadData();
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showFallbackEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_rule, null);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        final TextView titleText = view.findViewById(R.id.dialogTitle);
        final EditText emojiNameInput = view.findViewById(R.id.editEmojiName);
        final EditText tagsInput = view.findViewById(R.id.editTags);
        final Button cancelBtn = view.findViewById(R.id.cancelBtn);
        final Button saveBtn = view.findViewById(R.id.saveBtn);

        titleText.setText("编辑兜底随机池");
        emojiNameInput.setVisibility(View.GONE);
        tagsInput.setHint("食物名称，逗号分隔");
        tagsInput.setText(String.join(", ", ruleManager.getFallbackOptions()));

        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        saveBtn.setOnClickListener(v -> {
            String text = tagsInput.getText().toString().trim();
            if (!text.isEmpty()) {
                List<String> options = Arrays.asList(text.replace("，", ",").split(","));
                for (int i = 0; i < options.size(); i++) {
                    options.set(i, options.get(i).trim());
                }
                ruleManager.saveFallbackOptions(options);
                ruleManager.invalidateCache();  // 全局清除缓存
                loadData();
                Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private static class RulesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_RULE = 0;
        private static final int TYPE_FALLBACK = 1;

        private final List<Object> items;

        RulesAdapter(List<Object> items) {
            this.items = items;
        }

        @Override
        public int getItemViewType(int position) {
            if (items.get(position) instanceof MenuItem) {
                return TYPE_RULE;
            }
            return TYPE_FALLBACK;
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            if (viewType == TYPE_RULE) {
                View view = inflater.inflate(R.layout.item_tag_rule, parent, false);
                return new RuleViewHolder(view);
            } else {
                View view = inflater.inflate(R.layout.item_fallback_pool, parent, false);
                return new FallbackViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
            if (holder instanceof RuleViewHolder) {
                final MenuItem item = (MenuItem) items.get(position);
                final RuleViewHolder ruleHolder = (RuleViewHolder) holder;
                ruleHolder.title.setText(item.getEmoji() + " " + item.getName());
                ruleHolder.tags.setText(String.join(", ", item.getTags()));
                ruleHolder.foods.setText(item.getName());

                ruleHolder.editBtn.setOnClickListener(v -> {
                    ((TagWorkshopActivity) ruleHolder.itemView.getContext()).showEditDialog(position, item);
                });

                ruleHolder.deleteBtn.setOnClickListener(v -> {
                    new AlertDialog.Builder(ruleHolder.itemView.getContext())
                            .setMessage("确定删除此规则？")
                            .setPositiveButton("删除", (dialog, which) -> {
                                RuleManager.getInstance(ruleHolder.itemView.getContext()).deleteRule(position);
                                RuleManager.getInstance(ruleHolder.itemView.getContext()).invalidateCache();  // 全局清除缓存
                                ((TagWorkshopActivity) ruleHolder.itemView.getContext()).loadData();
                                Toast.makeText(ruleHolder.itemView.getContext(), "已删除", Toast.LENGTH_SHORT).show();
                            })
                            .setNegativeButton("取消", null)
                            .show();
                });
            } else if (holder instanceof FallbackViewHolder) {
                final FallbackViewHolder fallbackHolder = (FallbackViewHolder) holder;
                fallbackHolder.foods.setText(String.join(", ", RuleManager.getInstance(fallbackHolder.itemView.getContext()).getFallbackOptions()));

                fallbackHolder.editBtn.setOnClickListener(v -> {
                    ((TagWorkshopActivity) fallbackHolder.itemView.getContext()).showFallbackEditDialog();
                });
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

    private static class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView tags;
        TextView foods;
        Button editBtn;
        Button deleteBtn;

        RuleViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.ruleTitle);
            tags = itemView.findViewById(R.id.tagsText);
            foods = itemView.findViewById(R.id.foodsText);
            editBtn = itemView.findViewById(R.id.editBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }

    private static class FallbackViewHolder extends RecyclerView.ViewHolder {
        TextView foods;
        Button editBtn;

        FallbackViewHolder(View itemView) {
            super(itemView);
            foods = itemView.findViewById(R.id.fallbackFoodsText);
            editBtn = itemView.findViewById(R.id.editFallbackBtn);
        }
    }
}