-keep class org.liquidplayer.javascript.** { *; }
-keep class ch.rmy.android.http_shortcuts.data.models.** { *; }
-keep class ch.rmy.android.http_shortcuts.activities.contact.MetaData { *; }
-keep class ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams { *; }
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }
-keep class com.joaomgcd.taskerpluginlibrary.** { *; }
-keep class net.dinglisch.android.tasker.** { *; }
-keep class androidx.compose.material3.TabRowKt { *; } # here because I had to resort to reflection to change a hard-coded value
-dontwarn org.apache.harmony.xnet.provider.jsse.SSLParametersImpl
-dontwarn org.bouncycastle.jsse.BCSSLParameters
-dontwarn org.bouncycastle.jsse.BCSSLSocket
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
-dontwarn org.openjsse.javax.net.ssl.SSLParameters
-dontwarn org.openjsse.javax.net.ssl.SSLSocket
-dontwarn org.openjsse.net.ssl.OpenJSSE