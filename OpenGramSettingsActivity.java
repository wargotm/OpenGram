package org.telegram.ui;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
public class OpenGramSettingsActivity extends OpenGramPage {
    static TextView text(Context c, String s, int size, int color) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(Theme.getColor(color));
        return t;
    }
    @Override
    public View createView(Context context) {
        bar(LocaleController.getString(R.string.OpenGramSettings));
        LinearLayout box = new LinearLayout(context);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(0, AndroidUtilities.dp(12), 0, 0);
        TextView a = text(context, LocaleController.getString(R.string.OpenGramGlassSettings), 16, Theme.key_windowBackgroundWhiteBlackText);
        a.setGravity(Gravity.CENTER_VERTICAL);
        a.setPadding(AndroidUtilities.dp(22), 0, AndroidUtilities.dp(22), 0);
        a.setOnClickListener(v -> presentFragment(new GlassPage()));
        box.addView(a, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 56));
        TextView b = text(context, LocaleController.getString(R.string.OpenGramVerifiedSettings), 16, Theme.key_windowBackgroundWhiteBlackText);
        b.setGravity(Gravity.CENTER_VERTICAL);
        b.setPadding(AndroidUtilities.dp(22), 0, AndroidUtilities.dp(22), 0);
        b.setOnClickListener(v -> presentFragment(new VerifiedPage()));
        box.addView(b, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 56));
        fragmentView = box;
        return box;
    }
    public static class GlassPage extends OpenGramPage {
        @Override
        public View createView(Context context) {
            bar(LocaleController.getString(R.string.OpenGramGlassSettings));
            LinearLayout box = new LinearLayout(context);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(28), AndroidUtilities.dp(22), 0);
            int v0 = MessagesController.getGlobalMainSettings().getInt("opengram_glass_opacity", 78);
            box.addView(text(context, LocaleController.getString(R.string.OpenGramGlassOpacity), 17, Theme.key_windowBackgroundWhiteBlackText));
            TextView val = text(context, v0 + "%", 28, Theme.key_windowBackgroundWhiteBlueText);
            val.setGravity(Gravity.CENTER);
            box.addView(val, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 54, 0, 12, 0, 4));
            SeekBar sb = new SeekBar(context);
            sb.setMax(100);
            sb.setProgress(v0);
            box.addView(sb, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44));
            box.addView(text(context, LocaleController.getString(R.string.OpenGramGlassScale), 13, Theme.key_windowBackgroundWhiteGrayText));
            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar s, int p, boolean user) {
                    val.setText(p + "%");
                    if (user) MessagesController.getGlobalMainSettings().edit().putInt("opengram_glass_opacity", p).apply();
                }
                @Override public void onStartTrackingTouch(SeekBar s) { }
                @Override public void onStopTrackingTouch(SeekBar s) { }
            });
            fragmentView = box;
            return box;
        }
    }
    public static class VerifiedPage extends OpenGramPage {
        @Override
        public View createView(Context context) {
            bar(LocaleController.getString(R.string.OpenGramVerifiedSettings));
            String key = "opengram_major_badge_" + currentAccount;
            LinearLayout box = new LinearLayout(context);
            box.setOrientation(LinearLayout.VERTICAL);
            box.setPadding(AndroidUtilities.dp(22), AndroidUtilities.dp(20), AndroidUtilities.dp(22), 0);
            LinearLayout row = new LinearLayout(context);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.addView(text(context, LocaleController.getString(R.string.OpenGramVerifiedMajor), 16, Theme.key_windowBackgroundWhiteBlackText), LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1f));
            Switch sw = new Switch(context);
            sw.setChecked(MessagesController.getGlobalMainSettings().getBoolean(key, false));
            row.addView(sw);
            box.addView(row, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 56));
            box.addView(text(context, LocaleController.getString(R.string.OpenGramVerifiedInfo), 13, Theme.key_windowBackgroundWhiteGrayText));
            sw.setOnCheckedChangeListener((x, on) -> {
                MessagesController.getGlobalMainSettings().edit().putBoolean(key, on).apply();
                TLRPC.User me = UserConfig.getInstance(currentAccount).getCurrentUser();
                if (me != null) {
                    me.verified = on;
                    TLRPC.User c = getMessagesController().getUser(me.id);
                    if (c != null) c.verified = on;
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_NAME);
            });
            fragmentView = box;
            return box;
        }
    }
}
abstract class OpenGramPage extends BaseFragment {
    @Override
    protected boolean useSimpleActionBar() {
        return true;
    }
    void bar(String title) {
        setHasOwnBackground(true);
        simpleActionBar.setBackButtonImage(R.drawable.ic_ab_back);
        simpleActionBar.setTitle(title);
        simpleActionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) finishFragment();
            }
        });
    }
}
