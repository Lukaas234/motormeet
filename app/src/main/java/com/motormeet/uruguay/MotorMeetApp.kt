package com.motormeet.uruguay

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.appwrite.Client
import org.osmdroid.config.Configuration
import javax.inject.Inject

@HiltAndroidApp
class MotorMeetApp : Application() {

    @Inject
    lateinit var client: Client

    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().userAgentValue = packageName
    }
}
