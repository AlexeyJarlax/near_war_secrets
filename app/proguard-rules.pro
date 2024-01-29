-keepattributes Signature

-keep class com.example.package.** { *; }
-dontwarn org.bouncycastle.jsse.BCSSLParameters**
-dontwarn org.bouncycastle.jsse.BCSSLSocket**
-dontwarn org.bouncycastle.jsse.provider.BouncyCastleJsseProvider**
-dontwarn org.conscrypt.Conscrypt$Version**
-dontwarn org.conscrypt.Conscrypt**
-dontwarn org.conscrypt.ConscryptHostnameVerifier**
-dontwarn org.openjsse.javax.net.ssl.SSLParameters**
-dontwarn org.openjsse.javax.net.ssl.SSLSocket**
-dontwarn org.openjsse.net.ssl.OpenJSSE**

# Сохранение generic signatures
-keepattributes Signature,InnerClasses

# Сохранение всех методов и полей в классе FileProviderAdapter
-keep class com.pavlov.MyShadowGallery.file.FileProviderAdapter {
    *;
}

# Сохранение всех методов и полей во вложенных классах FileProviderAdapter.Companion
-keep class com.pavlov.MyShadowGallery.file.FileProviderAdapter$Companion {
    *;
}


# Сохранение всех методов и полей во вложенных классах FileProviderAdapter$Companion$rotateImageByKorutin
-keep class com.pavlov.MyShadowGallery.file.FileProviderAdapter$Companion$rotateImageByKorutin$* {
    *;
}

# Сохранение generic signatures
-keepattributes Signature,InnerClasses

# Сохранение всех методов и полей в классе Encryption
-keep class com.pavlov.MyShadowGallery.util.Encryption {
    *;
}

# Сохранение всех методов и полей во вложенных классах Encryption.Companion
-keep class com.pavlov.MyShadowGallery.util.Encryption$Companion {
    *;
}

# Сохранение всех методов и полей в классе Encryption$Companion$*
-keep class com.pavlov.MyShadowGallery.util.Encryption$Companion$* {
    *;
}

# Сохранение generic signatures в классе APK
-keepclassmembers class com.pavlov.MyShadowGallery.util.APK {
    <fields>;
    <methods>;
}

# Сохранение generic signatures в классе APKM
-keepclassmembers class com.pavlov.MyShadowGallery.util.APKM {
    <fields>;
    <methods>;
}