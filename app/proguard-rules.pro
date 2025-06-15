# app/proguard-rules.pro

-keep class com.system.update.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# --- AÑADE ESTO ---
# Reglas para OkHttp y su dependencia Okio
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**

# Esto es necesario si usas MockWebServer de OkHttp, es bueno tenerlo por si acaso.
-dontwarn org.conscrypt.**
