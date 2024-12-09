# Исключения для классов из библиотек, используемых в коде
-keep class com.pavlov.MyShadowGallery.data.model.** { *; }
-keep class com.pavlov.MyShadowGallery.file.** { *; }
-keep class com.pavlov.MyShadowGallery.navigation.** { *; }
-keep class com.pavlov.MyShadowGallery.ui.settings.** { *; }

# Исключения для всех аннотированных классов @Keep
-keep @interface androidx.annotation.Keep
-keep class ** {
    @androidx.annotation.Keep *;
}

# Сохранение указанных верхнеуровневых функций (для Kotlin)
-keep class *AuthScreenKt { *; }
-keep class *BottomNavBarKt { *; }
-keep class *NavGraphKt { *; }

# Сохранение классов gRPC
-keep class io.grpc.** { *; }
-dontwarn io.grpc.**

# Сохранение классов BouncyCastle
-keep class org.bouncycastle.** { *; }
-dontwarn org.bouncycastle.**

# Сохранение классов Conscrypt
-keep class org.conscrypt.** { *; }
-dontwarn org.conscrypt.**

# Сохранение классов OpenJSSE
-keep class org.openjsse.** { *; }
-dontwarn org.openjsse.**

-keepclassmembers class * extends android.app.Activity {
   public void *(android.os.Bundle);
   public void *(android.view.Menu);
}

-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.lifecycle.viewmodel.** { *; }
-keepclassmembers class androidx.lifecycle.savedstate.** { *; }

# Исключить классы с рефлексией из обфускации
# Сужаем область до необходимых классов
-keepclassmembers class java.lang.reflect.Field { *; }
-keepclassmembers class java.lang.reflect.Method { *; }

-keepattributes Exceptions

 # Правила для Google Tink
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# Правила для Google API Client
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**

# Правила для Joda-Time
-keep class org.joda.time.** { *; }
-dontwarn org.joda.time.**

# Правила для Error Prone Annotations (если они не нужны в рантайме)
-dontwarn com.google.errorprone.annotations.**