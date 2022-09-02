# Getting started

Init player in your application class

class MagicalExoPlayerApp : Application() {

    override fun onCreate() {
        super.onCreate()
        MagicalExoPlayerProvider.init(this)
    }
}
