package com.rafael.tasasve;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;

public class TasasWidgetProvider extends AppWidgetProvider {
    public static final String ACTION_REFRESH = "com.rafael.tasasve.ACTION_WIDGET_REFRESH";
    public static final String ACTION_SYNC_FROM_APP = "com.rafael.tasasve.ACTION_SYNC_FROM_APP";
    public static final String PREFS = "tasas_widget_cache";

    private static final String HOST = "tasas-ve-api.rafael-f60.workers.dev";
    private static final String API_URL = "https://" + HOST + "/api";
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(Context context, AppWidgetManager mgr, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            bindActions(context, mgr, id);
            renderCachedIfAny(context, mgr, id, "Actualizando…");
            fetchAndRender(context, mgr, id);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        ComponentName cn = new ComponentName(context, TasasWidgetProvider.class);
        int[] allIds = mgr.getAppWidgetIds(cn);

        if (ACTION_SYNC_FROM_APP.equals(action)) {
            for (int id : allIds) {
                renderCachedIfAny(context, mgr, id, "App");
            }
            return;
        }

        if (ACTION_REFRESH.equals(action)) {
            int id = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            );
            if (id != AppWidgetManager.INVALID_APPWIDGET_ID) {
                bindActions(context, mgr, id);
                renderCachedIfAny(context, mgr, id, "Actualizando…");
                fetchAndRender(context, mgr, id);
            } else {
                for (int wId : allIds) {
                    bindActions(context, mgr, wId);
                    renderCachedIfAny(context, mgr, wId, "Actualizando…");
                    fetchAndRender(context, mgr, wId);
                }
            }
        }
    }

    public static void saveFromApp(Context context, double bcvUsd, double parUsd, Double usdtAvg) {
        String bcvLine = "BCV: " + ((bcvUsd > 0) ? "Bs " + fmt(bcvUsd) : "Bs —");
        String parVal = (parUsd > 0) ? "Bs " + fmt(parUsd) : "Bs —";
        String usdtVal = (usdtAvg != null && usdtAvg > 0) ? "Bs " + fmt(usdtAvg) : "Bs —";
        String parLine = "Par: " + parVal + " · USDT: " + usdtVal;
        String hora = new SimpleDateFormat("hh:mm a", new Locale("es", "VE")).format(new Date());

        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("bcv", bcvLine)
            .putString("par", parLine)
            .putString("time", hora)
            .apply();

        Intent i = new Intent(context, TasasWidgetProvider.class);
        i.setAction(ACTION_SYNC_FROM_APP);
        context.sendBroadcast(i);
    }

    private void bindActions(Context context, AppWidgetManager mgr, int id) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tasas);

        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent openPi = PendingIntent.getActivity(
            context, id, openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.w_root, openPi);

        Intent cfgIntent = new Intent(context, MainActivity.class);
        cfgIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent cfgPi = PendingIntent.getActivity(
            context, id + 2000, cfgIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.w_cfg, cfgPi);

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

    private void renderCachedIfAny(Context context, AppWidgetManager mgr, int id, String statusText) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String bcv = sp.getString("bcv", null);
        String par = sp.getString("par", null);
        String lastTime = sp.getString("time", null);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tasas);
        if (bcv != null && par != null) {
            views.setTextViewText(R.id.w_bcv, bcv);
            views.setTextViewText(R.id.w_par, par);
            views.setTextViewText(R.id.w_time, (lastTime != null ? "Act. " + lastTime : statusText));
        } else {
            views.setTextViewText(R.id.w_time, statusText);
        }
        bindActions(context, mgr, id);
        mgr.updateAppWidget(id, views);
    }

    private String readStream(InputStream stream) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (stream != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) sb.append(line);
            }
        }
        return sb.toString();
    }

    private String resolveIpViaGoogleDoh() throws Exception {
        HttpsURLConnection doh = null;
        try {
            URL u = new URL("https://dns.google/resolve?name=" + HOST + "&type=A");
            doh = (HttpsURLConnection) u.openConnection();
            doh.setConnectTimeout(10000);
            doh.setReadTimeout(10000);
            doh.setRequestProperty("Accept", "application/json");
            if (doh.getResponseCode() != 200) throw new Exception("DoH HTTP " + doh.getResponseCode());
            JSONObject ans = new JSONObject(readStream(doh.getInputStream()));
            JSONArray arr = ans.optJSONArray("Answer");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject item = arr.optJSONObject(i);
                    if (item != null && item.optInt("type") == 1 && item.has("data")) {
                        return item.getString("data");
                    }
                }
            }
            throw new Exception("Sin IP DoH");
        } finally {
            if (doh != null) doh.disconnect();
        }
    }

    private String fetchJsonDirect(String targetUrl, String hostHeader) throws Exception {
        HttpsURLConnection conn = null;
        try {
            URL url = new URL(targetUrl);
            conn = (HttpsURLConnection) url.openConnection();
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);
            conn.setRequestMethod("GET");
            if (hostHeader != null) {
                conn.setRequestProperty("Host", hostHeader);
            }
            conn.setRequestProperty("User-Agent", "TasasVE-Android/1.0");
            conn.setRequestProperty("Accept", "application/json");

            int code = conn.getResponseCode();
            InputStream stream = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
            String body = readStream(stream);
            if (code < 200 || code >= 300) {
                throw new Exception("HTTP " + code);
            }
            return body;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private String fetchJsonSmart() throws Exception {
        try {
            InetAddress.getByName(HOST);
            return fetchJsonDirect(API_URL, null);
        } catch (UnknownHostException uhe) {
            String ip = resolveIpViaGoogleDoh();
            return fetchJsonDirect("https://" + ip + "/api", HOST);
        }
    }

    private void fetchAndRender(Context context, AppWidgetManager mgr, int id) {
        IO.execute(() -> {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tasas);
            try {
                String rawJson = fetchJsonSmart();
                JSONObject root = new JSONObject(rawJson);

                Double bcvUsd = extractNumber(root.optJSONObject("bcv"), "usd");
                Double parUsd = extractNumber(root, "paralelo");

                List<Double> p2p = new ArrayList<>();
                for (String k : new String[]{"binance", "okx"}) {
                    JSONObject ex = root.optJSONObject(k);
                    if (ex != null) {
                        Double buy = extractNumber(ex, "buy");
                        Double sell = extractNumber(ex, "sell");
                        if (buy != null && !Double.isNaN(buy)) p2p.add(buy);
                        if (sell != null && !Double.isNaN(sell)) p2p.add(sell);
                    }
                }

                String bcvLine = "BCV: " + ((bcvUsd != null && !Double.isNaN(bcvUsd)) ? "Bs " + fmt(bcvUsd) : "Bs —");
                String parVal = (parUsd != null && !Double.isNaN(parUsd)) ? "Bs " + fmt(parUsd) : "Bs —";
                String usdtVal = "Bs —";
                if (!p2p.isEmpty()) {
                    double sum = 0;
                    for (double v : p2p) sum += v;
                    usdtVal = "Bs " + fmt(sum / p2p.size());
                }
                String parLine = "Par: " + parVal + " · USDT: " + usdtVal;
                String hora = new SimpleDateFormat("hh:mm a", new Locale("es", "VE")).format(new Date());
                String timeLine = "Act. " + hora;

                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .edit()
                    .putString("bcv", bcvLine)
                    .putString("par", parLine)
                    .putString("time", hora)
                    .apply();

                views.setTextViewText(R.id.w_bcv, bcvLine);
                views.setTextViewText(R.id.w_par, parLine);
                views.setTextViewText(R.id.w_time, timeLine);
            } catch (Exception e) {
                SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
                String cachedBcv = sp.getString("bcv", null);
                String cachedPar = sp.getString("par", null);
                String cachedTime = sp.getString("time", null);

                if (cachedBcv != null && cachedPar != null) {
                    views.setTextViewText(R.id.w_bcv, cachedBcv);
                    views.setTextViewText(R.id.w_par, cachedPar);
                    views.setTextViewText(R.id.w_time, "Guardado" + (cachedTime != null ? " · " + cachedTime : ""));
                } else {
                    views.setTextViewText(R.id.w_time, "Abre la app para cargar");
                }
            }

            bindActions(context, mgr, id);
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
        if (val instanceof String) {
            try {
                return Double.parseDouble(((String) val).replace(",", "."));
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private static String fmt(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return "—";
        return String.format(Locale.forLanguageTag("es-VE"), "%,.2f", val);
    }
}
