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
    public static final String EXTRA_OPEN_CONFIG = "extra_open_widget_config";
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

    public static void saveFromApp(
        Context context,
        double bcvUsd,
        double bcvEur,
        double parUsd,
        Double usdtAvg,
        Double binAvg,
        Double okxAvg
    ) {
        String hora = new SimpleDateFormat("hh:mm a", new Locale("es", "VE")).format(new Date());
        SharedPreferences.Editor ed = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
        ed.putFloat("v_bcv", (float) bcvUsd);
        ed.putFloat("v_eur", (float) bcvEur);
        ed.putFloat("v_par", (float) parUsd);
        ed.putFloat("v_usdt", (usdtAvg != null && usdtAvg > 0) ? usdtAvg.floatValue() : -1f);
        ed.putFloat("v_bin", (binAvg != null && binAvg > 0) ? binAvg.floatValue() : -1f);
        ed.putFloat("v_okx", (okxAvg != null && okxAvg > 0) ? okxAvg.floatValue() : -1f);
        ed.putString("time", hora);
        ed.apply();

        Intent i = new Intent(context, TasasWidgetProvider.class);
        i.setAction(ACTION_SYNC_FROM_APP);
        context.sendBroadcast(i);
    }

    public static void setWidgetMode(Context context, String mode) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("mode", mode)
            .apply();

        Intent i = new Intent(context, TasasWidgetProvider.class);
        i.setAction(ACTION_SYNC_FROM_APP);
        context.sendBroadcast(i);
    }

    public static String getWidgetMode(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("mode", "all");
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
        cfgIntent.putExtra(EXTRA_OPEN_CONFIG, true);
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

    private static void applyModeToViews(Context context, RemoteViews views, String fallbackTime) {
        SharedPreferences sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        float vBcv = sp.getFloat("v_bcv", -1f);
        float vEur = sp.getFloat("v_eur", -1f);
        float vPar = sp.getFloat("v_par", -1f);
        float vUsdt = sp.getFloat("v_usdt", -1f);
        float vBin = sp.getFloat("v_bin", -1f);
        float vOkx = sp.getFloat("v_okx", -1f);
        String lastTime = sp.getString("time", null);
        String mode = sp.getString("mode", "all");

        String sBcv = (vBcv > 0) ? "Bs " + fmt(vBcv) : "Bs —";
        String sEur = (vEur > 0) ? "Bs " + fmt(vEur) : "Bs —";
        String sPar = (vPar > 0) ? "Bs " + fmt(vPar) : "Bs —";
        String sUsdt = (vUsdt > 0) ? "Bs " + fmt(vUsdt) : "Bs —";
        String sBin = (vBin > 0) ? "Bs " + fmt(vBin) : "Bs —";
        String sOkx = (vOkx > 0) ? "Bs " + fmt(vOkx) : "Bs —";

        String mainText;
        String subText;

        switch (mode) {
            case "bcv":
                mainText = "BCV: " + sBcv;
                subText = "Euro: " + sEur + " · Par: " + sPar;
                break;
            case "eur":
                mainText = "Euro: " + sEur;
                subText = "BCV: " + sBcv + " · USDT: " + sUsdt;
                break;
            case "par":
                mainText = "Par: " + sPar;
                subText = "BCV: " + sBcv + " · USDT: " + sUsdt;
                break;
            case "usdt":
                mainText = "USDT: " + sUsdt;
                subText = "Bin: " + sBin + " · OKX: " + sOkx;
                break;
            case "bin":
                mainText = "Binance: " + sBin;
                subText = "OKX: " + sOkx + " · BCV: " + sBcv;
                break;
            case "okx":
                mainText = "OKX: " + sOkx;
                subText = "Bin: " + sBin + " · BCV: " + sBcv;
                break;
            case "all":
            default:
                mainText = "BCV: " + sBcv;
                subText = "Par: " + sPar + " · USDT: " + sUsdt;
                break;
        }

        views.setTextViewText(R.id.w_bcv, mainText);
        views.setTextViewText(R.id.w_par, subText);
        if (lastTime != null) {
            views.setTextViewText(R.id.w_time, "Act. " + lastTime);
        } else {
            views.setTextViewText(R.id.w_time, fallbackTime);
        }
    }

    private void renderCachedIfAny(Context context, AppWidgetManager mgr, int id, String statusText) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tasas);
        applyModeToViews(context, views, statusText);
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

    private Double exchangeAvg(JSONObject root, String key) {
        JSONObject ex = root.optJSONObject(key);
        if (ex == null) return null;
        List<Double> vals = new ArrayList<>();
        Double buy = extractNumber(ex, "buy");
        Double sell = extractNumber(ex, "sell");
        if (buy != null && !Double.isNaN(buy)) vals.add(buy);
        if (sell != null && !Double.isNaN(sell)) vals.add(sell);
        if (vals.isEmpty()) return null;
        double sum = 0;
        for (double v : vals) sum += v;
        return sum / vals.size();
    }

    private void fetchAndRender(Context context, AppWidgetManager mgr, int id) {
        IO.execute(() -> {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tasas);
            try {
                String rawJson = fetchJsonSmart();
                JSONObject root = new JSONObject(rawJson);

                JSONObject bcvObj = root.optJSONObject("bcv");
                Double bcvUsd = extractNumber(bcvObj, "usd");
                Double bcvEur = extractNumber(bcvObj, "eur");
                Double parUsd = extractNumber(root, "paralelo");
                Double binAvg = exchangeAvg(root, "binance");
                Double okxAvg = exchangeAvg(root, "okx");

                List<Double> p2p = new ArrayList<>();
                if (binAvg != null && !Double.isNaN(binAvg)) p2p.add(binAvg);
                if (okxAvg != null && !Double.isNaN(okxAvg)) p2p.add(okxAvg);

                Double usdtAvg = null;
                if (!p2p.isEmpty()) {
                    double sum = 0;
                    for (double v : p2p) sum += v;
                    usdtAvg = sum / p2p.size();
                }

                String hora = new SimpleDateFormat("hh:mm a", new Locale("es", "VE")).format(new Date());
                SharedPreferences.Editor ed = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
                if (bcvUsd != null && !Double.isNaN(bcvUsd)) ed.putFloat("v_bcv", bcvUsd.floatValue());
                if (bcvEur != null && !Double.isNaN(bcvEur)) ed.putFloat("v_eur", bcvEur.floatValue());
                if (parUsd != null && !Double.isNaN(parUsd)) ed.putFloat("v_par", parUsd.floatValue());
                ed.putFloat("v_usdt", (usdtAvg != null && !Double.isNaN(usdtAvg)) ? usdtAvg.floatValue() : -1f);
                ed.putFloat("v_bin", (binAvg != null && !Double.isNaN(binAvg)) ? binAvg.floatValue() : -1f);
                ed.putFloat("v_okx", (okxAvg != null && !Double.isNaN(okxAvg)) ? okxAvg.floatValue() : -1f);
                ed.putString("time", hora);
                ed.apply();

                applyModeToViews(context, views, "Act. " + hora);
            } catch (Exception e) {
                applyModeToViews(context, views, "Abre la app para cargar");
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
