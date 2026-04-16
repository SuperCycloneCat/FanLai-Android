package you.you.utils;

import android.content.Context;
import android.util.TypedValue;
import android.widget.LinearLayout;
import android.widget.TextView;

import you.you.R;

public class BadgeUtils {

    private static int dp2px(Context context, float dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics());
    }

    public static TextView createBadge(Context context, String text) {
        TextView badge = new TextView(context);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.rightMargin = dp2px(context, 8);
        params.bottomMargin = dp2px(context, 8);
        badge.setLayoutParams(params);
        badge.setBackgroundResource(R.drawable.example_button_selector);
        badge.setMinWidth(dp2px(context, 40));
        badge.setPadding(
                dp2px(context, 12),
                dp2px(context, 6),
                dp2px(context, 12),
                dp2px(context, 6)
        );
        badge.setText(text);
        badge.setTextColor(0xff5e5345);
        badge.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        badge.setSingleLine(true);
        badge.setGravity(android.view.Gravity.CENTER);
        return badge;
    }
}