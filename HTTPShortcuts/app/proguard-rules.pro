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
