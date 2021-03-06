package com.brentpanther.bitcoinwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import java.text.DecimalFormat;
import java.text.NumberFormat;

class WidgetViews {

    private static final double TEXT_HEIGHT = .70;

    static void setText(Context context, RemoteViews views, Currency currency, String amount, int widgetId) {
        Unit unit = Prefs.getUnit(context, widgetId);
        boolean showDecimals = Prefs.getShowDecimals(context, widgetId);
        String text = buildText(currency, amount, showDecimals, unit);
        Prefs.setLastValue(context, widgetId, text);
        putValue(context, views, text, widgetId);
    }

    static void setLastText(Context context, RemoteViews views, int widgetId) {
        String lastValue = Prefs.getLastValue(context, widgetId);
        if (!TextUtils.isEmpty(lastValue)) {
            putValue(context, views, lastValue, widgetId);
        }
    }

    private static void putValue(Context context, RemoteViews views, String text, int widgetId) {
        setImageVisibility(context, views, widgetId);
        Pair<Integer, Integer> availableSize = getTextAvailableSize(context, widgetId);
        if (availableSize == null) return;
        float textSize = TextSizer.getTextSize(context, text, availableSize);
        views.setTextViewText(R.id.price, text);
        views.setTextViewTextSize(R.id.price, TypedValue.COMPLEX_UNIT_DIP, textSize);
        if (Prefs.getLabel(context, widgetId)) {
            int providerInt = Prefs.getProvider(context, widgetId);
            BTCProvider provider = BTCProvider.values()[providerInt];
            availableSize = getLabelAvailableSize(context, widgetId);
            float labelSize = TextSizer.getLabelSize(context, provider.getLabel(), availableSize);
            views.setTextViewText(R.id.provider, provider.getLabel());
            views.setTextViewTextSize(R.id.provider, TypedValue.COMPLEX_UNIT_DIP, labelSize);
            show(views, R.id.provider, R.id.top_space);
        } else {
            hide(views, R.id.provider, R.id.top_space);
        }
        show(views, R.id.price);
        hide(views, R.id.loading);
    }

    private static void setImageVisibility(Context context, RemoteViews views, int widgetId) {
        boolean hideIcon = Prefs.getIcon(context, widgetId);
        if (hideIcon) {
            hide(views, R.id.bitcoinImageBW);
            hide(views, R.id.bitcoinImage);
        } else {
            hide(views, R.id.bitcoinImageBW);
            show(views, R.id.bitcoinImage);
        }
    }

    private static Pair<Integer, Integer> getTextAvailableSize(Context context, int widgetId) {
        Pair<Integer, Integer> size = getWidgetSize(context, widgetId);
        if (size == null) {
            return null;
        }
        int width = size.first;
        int height = size.second;

        if (Prefs.getThemeLayout(context, widgetId) != R.layout.widget_layout_transparent) {
            // light and dark themes have 5dp padding all around
            width -= 10;
            height -= 10;
        }

        if (!Prefs.getIcon(context, widgetId)) {
            // icon is 25% of width
            width *= .75;
        }
        if (Prefs.getLabel(context, widgetId)) {
            height *= TEXT_HEIGHT;
        }
        return Pair.create((int)(width * .9), (int)(height * .85));
    }

    private static Pair<Integer, Integer> getLabelAvailableSize(Context context, int widgetId) {
        Pair<Integer, Integer> size = getWidgetSize(context, widgetId);
        if (size == null) {
            return null;
        }
        int height = size.second;
        int width = size.first;
        if (Prefs.getThemeLayout(context, widgetId) != R.layout.widget_layout_transparent) {
            // light and dark themes have 5dp padding all around
            height -= 10;
        }
        if (!Prefs.getIcon(context, widgetId)) {
            // icon is 25% of width
            width *= .75;
        }
        height *= ((1 - TEXT_HEIGHT) / 2);
        return Pair.create((int)(width * .9), (int)(height * .9));
    }

    private static Pair<Integer, Integer> getWidgetSize(Context context, int widgetId) {
        boolean portrait = context.getResources().getConfiguration().orientation == 1;
        String w = portrait ? AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH : AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH;
        String h = portrait ? AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT : AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int width = appWidgetManager.getAppWidgetOptions(widgetId).getInt(w);
        int height = appWidgetManager.getAppWidgetOptions(widgetId).getInt(h);
        return Pair.create(width, height);
    }

    private static String buildText(Currency currency, String amount, boolean showDecimals, Unit unit) {
        String format = currency.getFormat();
        if (!showDecimals) {
            format = format.replaceAll("\\.00", "");
        }
        NumberFormat nf = new DecimalFormat(format);
        double adjustedAmount = unit.adjust(amount);
        return nf.format(adjustedAmount);
    }

    static void setLoading(RemoteViews views, Context context, int widgetId) {
        show(views, R.id.loading);
        views.setViewVisibility(R.id.price, View.INVISIBLE);
        views.setViewVisibility(R.id.bitcoinImageBW, View.GONE);
        if (!Prefs.getIcon(context, widgetId)) {
            views.setViewVisibility(R.id.bitcoinImage, View.INVISIBLE);
        }
    }

    static void show(RemoteViews views, int... ids) {
        for (int id : ids) views.setViewVisibility(id, View.VISIBLE);
    }

    static void hide(RemoteViews views, int... ids) {
        for (int id : ids) views.setViewVisibility(id, View.GONE);
    }

    static void setOld(RemoteViews views, boolean isOld, boolean hideIcon) {
        if (!hideIcon && isOld) {
            hide(views, R.id.bitcoinImage);
            show(views, R.id.bitcoinImageBW);
        } else if(!hideIcon) {
            show(views, R.id.bitcoinImage);
        }
        show(views, R.id.price);
        hide(views, R.id.loading);
    }

}
