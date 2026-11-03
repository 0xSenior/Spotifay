# Spotifay — keep Retrofit/kotlinx.serialization metadata
-keepattributes *Annotation*, InnerClasses, Signature
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class com.example.spotifay.data.** { *; }
-keepclassmembers class kotlinx.serialization.json.** { *; }