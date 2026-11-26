package com.rafael.tasasve;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TasasWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_REFRESH = "com.rafael.tasasve.ACTION_WIDGET_REFRESH";
    private static final String API_URL = "https://tasas-ve-api.rafael-f60.workers.dev/api";
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(Context context, AppWidgetManager mgr, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            bindActionsAndLoading(context, mgr, id);
            fetchAndRender(context, mgr, id);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (ACTION_REFRESH.equals(intent.getAction())) {
            int id = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            );
            AppWidgetManager mgr = AppWidgetManager.getInstance(context);
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                bindActionsAndLoading(context, mgr, id);
                fetchAndRender(context, mgr, id);
            }
        }
    }

    private void bindActionsAndLoading(Context context, AppWidgetManager mgr, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tasas);
        views.setTextViewText(R.id.w_time, "Actualizando…");

        Intent openIntent = new Intent(context, MainActivity.class);
        PendingIntent openPi = PendingIntent.getActivity(
            context, id, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.w_root, openPi);

        Intent refIntent = new Intent(context, TasasWidgetProvider.class);
        refIntent.setAction(ACTION_REFRESH);
        refIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        PendingIntent refPi = PendingIntent.getBroadcast(
            context, id + 1000, refIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.w_refresh, refPi);

        mgr.updateAppWidget(id, views);
    }

    private void fetchAndRender(Context context, AppWidgetManager mgr, int id) {
        IO.execute(() -> {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tasas);
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int code = conn.getResponseCode();
                if (code < 200 || code >= 300) {
                    throw new Exception("HTTP " + code);
                }

                StringBuilder sb = new StringBuilder();
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String line;
                    while ((line = br.readLine()) != null) sb.append(line);
                }
                conn.disconnect();

                JSONObject root = new JSONObject(sb.toString());
                Double bcvUsd = extractNumber(root.optJSONObject("bcv"), "usd");
                Double parUsd = extractNumber(root, "paralelo");

                List<Double> p2p = new ArrayList<>();
                for (String k : new String[]{"binance", "okx"}) {
                    JSONObject ex = root.optJSONObject(k);
                    if (ex != null) {
                        if (ex.has("buy") && !ex.isNull("buy")) p2p.add(ex.getDouble("buy"));
                        if (ex.has("sell") && !ex.isNull("sell")) p2p.add(ex.getDouble("sell"));
                    }
                }

                String bcvStr = (bcvUsd != null) ? "Bs " + fmt(bcvUsd) : "Bs —";
                String parStr = (parUsd != null) ? "Bs " + fmt(parUsd) : "Bs —";
                String usdtStr = "Bs —";
                if (!p2p.isEmpty()) {
                    double sum = 0;
                    for (double v : p2p) sum += v;
                    usdtStr = "Bs " + fmt(sum / p2p.size());
                }

                String hora = new SimpleDateFormat("hh:mm a", new Locale("es", "VE")).format(new Date());
                views.setTextViewText(R.id.w_bcv, "BCV: " + bcvStr);
                views.setTextViewText(R.id.w_par, "Par: " + parStr + " · USDT: " + usdtStr);
                views.setTextViewText(R.id.w_time, "Act. " + hora);
            } catch (Exception e) {
                views.setTextViewText(R.id.w_time, "Sin conexión al actualizar");
            }
            mgr.updateAppWidget(id, views);
        });
    }

    private static Double extractNumber(JSONObject parent, String key) {
        if (parent == null || !parent.has(key) || parent.isNull(key)) return null;
        Object val = parent.opt(key);
        if (val instanceof Number) {
            return ((Number) val).doubleValue();
        }
        if (val instanceof JSONObject) {
            JSONObject obj = (JSONObject) val;
            if (obj.has("value") && !obj.isNull("value")) {
                return obj.optDouble("value", Double.NaN);
            }
        }
        return null;
    }

    private static String fmt(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return "—";
        return String.format(Locale.forLanguageTag("es-VE"), "%,.2f", val);
    }
}
