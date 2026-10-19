package sevenward.nandofit

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class NandoFitApp : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("nandofit")
                    .setApplicationId("1:1000360723205:android:3d3fce886b34290a15ad6c")
                    .setApiKey("AIzaSyC7N46190MU0ZwAexywxgXhRTWViByG8yA")
                    .setGcmSenderId("1000360723205")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
