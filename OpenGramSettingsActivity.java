package org.telegram.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
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
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.LayoutHelper;

public class OpenGramSettingsActivity extends OpenGramPage {

    private LinearLayout createList(Context context) {
        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        return list;
    }

    private void addHeader(LinearLayout list, Context context, String title) {
        HeaderCell header = new HeaderCell(context);
        header.setText(title);
        list.addView(header, new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(40)));
    }

    private TextCell addRow(LinearLayout list, Context context, String title, boolean divider, Runnable action) {
        TextCell cell = new TextCell(context);
        cell.setText(title, divider);
        cell.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector)));
        cell.setOnClickListener(v -> action.run());
        list.addView(cell, new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(50)));
        return cell;
    }

    private ScrollView wrap(Context context, LinearLayout list) {
        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.addView(list, new ScrollView.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        fragmentView = scroll;
        return scroll;
    }

    @Override
    public View createView(Context context) {
        bar(LocaleController.getString(R.string.OpenGramSettings));
        LinearLayout list = createList(context);

        addHeader(list, context, "ОСНОВНЫЕ ФУНКЦИИ");
        addRow(list, context, "OpenGram Premium", true, () -> showComingSoon(context));
        addRow(list, context, "Удалённые сообщения", true, () -> showComingSoon(context));
        addRow(list, context, "История редактирования", true, () -> showComingSoon(context));
        addRow(list, context, "Плагины", false, () -> showComingSoon(context));

        addHeader(list, context, "КАСТОМИЗАЦИЯ");
        addRow(list, context, "Liquid Glass", true, () -> presentFragment(new GlassPage()));
        addRow(list, context, "Значок верификации", true, () -> presentFragment(new VerifiedPage()));
        addRow(list, context, "Секунды во времени", true, () -> showComingSoon(context));
        addRow(list, context, "Нижняя панель", true, () -> showComingSoon(context));
        addRow(list, context, "Анимации", false, () -> showComingSoon(context));

        TextView developer = new TextView(context);
        developer.setText("Разработчик: @opengl41");
        developer.setTextSize(12);
        developer.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
        developer.setGravity(Gravity.CENTER_VERTICAL);
        developer.setPadding(AndroidUtilities.dp(21), 0, AndroidUtilities.dp(21), 0);
        developer.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector)));
        developer.setOnClickListener(v -> {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/opengl41")));
            } catch (Exception ignored) { }
        });
        list.addView(developer, new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(48)));

        return wrap(context, list);
    }

    private void showComingSoon(Context context) {
        org.telegram.ui.ActionBar.AlertDialog.Builder builder = new org.telegram.ui.ActionBar.AlertDialog.Builder(context);
        builder.setMessage("Эта функция пока находится в разработке.");
        builder.setPositiveButton(LocaleController.getString(R.string.OK), null);
        showDialog(builder.create());
    }

    public static class GlassPage extends OpenGramPage {
        @Override
        public View createView(Context context) {
            bar(LocaleController.getString(R.string.OpenGramGlassSettings));
            LinearLayout list = new LinearLayout(context);
            list.setOrientation(LinearLayout.VERTICAL);
            list.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            HeaderCell header = new HeaderCell(context);
            header.setText(LocaleController.getString(R.string.OpenGramGlassSettings));
            list.addView(header, new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(40)));

            TextCell description = new TextCell(context);
            description.setText(LocaleController.getString(R.string.OpenGramGlassOpacity), false);
            list.addView(description, new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(50)));

            int initial = MessagesController.getGlobalMainSettings().getInt("opengram_glass_opacity", 78);
            TextView value = new TextView(context);
            value.setText(initial + "%");
            value.setTextSize(16);
            value.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            value.setGravity(Gravity.CENTER_VERTICAL);
            value.setPadding(AndroidUtilities.dp(21), 0, AndroidUtilities.dp(21), 0);
            list.addView(value, new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(42)));

            SeekBar seekBar = new SeekBar(context);
            seekBar.setMax(100);
            seekBar.setProgress(initial);
            LinearLayout.LayoutParams seekParams = new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(48));
            seekParams.leftMargin = AndroidUtilities.dp(16);
            seekParams.rightMargin = AndroidUtilities.dp(16);
            list.addView(seekBar, seekParams);
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                    value.setText(progress + "%");
                    if (fromUser) MessagesController.getGlobalMainSettings().edit().putInt("opengram_glass_opacity", progress).apply();
                }
                @Override public void onStartTrackingTouch(SeekBar bar) { }
                @Override public void onStopTrackingTouch(SeekBar bar) { }
            });

            ScrollView scroll = new ScrollView(context);
            scroll.addView(list, new ScrollView.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            fragmentView = scroll;
            return scroll;
        }
    }

    public static class VerifiedPage extends OpenGramPage {
        @Override
        public View createView(Context context) {
            bar(LocaleController.getString(R.string.OpenGramVerifiedSettings));
            LinearLayout list = new LinearLayout(context);
            list.setOrientation(LinearLayout.VERTICAL);
            list.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

            HeaderCell header = new HeaderCell(context);
            header.setText(LocaleController.getString(R.string.OpenGramVerifiedSettings));
            list.addView(header, new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(40)));

            String key = "opengram_major_badge_" + currentAccount;
            TextCheckCell toggle = new TextCheckCell(context);
            toggle.setTextAndCheck(LocaleController.getString(R.string.OpenGramVerifiedMajor),
                    MessagesController.getGlobalMainSettings().getBoolean(key, false), true);
            toggle.setOnClickListener(v -> {
                boolean enabled = !toggle.getCheckBox().isChecked();
                toggle.getCheckBox().setChecked(enabled, true);
                MessagesController.getGlobalMainSettings().edit().putBoolean(key, enabled).apply();
                TLRPC.User me = UserConfig.getInstance(currentAccount).getCurrentUser();
                if (me != null) {
                    me.verified = enabled;
                    TLRPC.User cached = getMessagesController().getUser(me.id);
                    if (cached != null) cached.verified = enabled;
                }
                getNotificationCenter().postNotificationName(NotificationCenter.mainUserInfoChanged);
                getNotificationCenter().postNotificationName(NotificationCenter.updateInterfaces, MessagesController.UPDATE_MASK_NAME);
            });
            list.addView(toggle, new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(50)));

            TextCell info = new TextCell(context);
            info.setText(LocaleController.getString(R.string.OpenGramVerifiedInfo), false);
            info.setEnabled(false);
            list.addView(info, new LinearLayout.LayoutParams(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(50)));

            ScrollView scroll = new ScrollView(context);
            scroll.addView(list, new ScrollView.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
            fragmentView = scroll;
            return scroll;
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
