# Add project specific ProGuard rules here.
# You can find general rules for popular librariesproguard-android-optimize.txt

# Keep interfaces and annotations required by Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    public static final androidx.room.RoomDatabase$Callback sRoomDatabaseCallback;
}
-keep class **.*_Impl {
    <init>(...);
}
-keep class androidx.room.paging.LimitOffsetDataSource<T> {
    <init>(...);
}

# Keep Kotlin Coroutines metadata
-keep class kotlin.coroutines.jvm.internal.DebugMetadataKt
-keep class kotlin.coroutines.Continuation
-keepclasseswithmembers class * {
    @kotlin.coroutines.jvm.internal.DebugMetadata <methods>;
}
-keep class kotlin.Metadata { *; }
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep application classes used in the manifest
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep activities and their constructors
-keep public class * extends android.app.Activity { public <init>(); }

# Keep custom views and their constructors
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
} 