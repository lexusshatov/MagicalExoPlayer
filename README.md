# Getting started

### 1.Init player in your application class:

```
class MagicalExoPlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MagicalExoPlayerProvider.init(this)
    }
}
```

### 2. Add player view in XML file 

```
<com.potyvideo.library.newplayer.MagicalExoPlayer
        android:id="@+id/andExoPlayerView"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        app:andexo_show_controller="true"
        app:andexo_show_full_screen="true" />
```
