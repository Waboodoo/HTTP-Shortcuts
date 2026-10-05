# Tasker integration
-keep class com.joaomgcd.taskerpluginlibrary.** { *; }
-keep class net.dinglisch.android.tasker.** { *; }

# Cryptography and such
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE

# Room specific rules
-keep class androidx.room.RoomDatabase { *; }
-keep class androidx.room.Room { *; }
-keep class android.arch.** { *; }
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Persistent CookieJar library (whose own rules are way too broad and are thus ignored)
-keep class com.franmontiel.persistentcookiejar.persistence.SerializableCookie { *; }

# For signing APKs. TODO: This rule should be optimized
-keep class com.android.apksig.internal.** { *; }

# For MQTT, because Paho doesn't provide useful rules of its own # TODO: These rules should be optimized
-keep class org.eclipse.paho.clent.mqttv3.** {*;}
-keep class org.eclipse.paho.client.mqttv3.*$* { *; }
-keep class org.eclipse.paho.client.mqttv3.logging.JSR47Logger { *; }