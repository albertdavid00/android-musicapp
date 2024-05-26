package com.unibuc.musicapp

import android.app.Application
import android.util.Log
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.InitParams
import com.unibuc.musicapp.utils.Constants
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MusicApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        SendbirdChat.init(
            InitParams(Constants.SENDBIRD_APP_ID, applicationContext, useCaching = false),
            object : InitResultHandler {
                override fun onMigrationStarted() {
                    Log.i("Application", "Called when there's an update in Sendbird server.")
                }

                override fun onInitFailed(e: SendbirdException) {
                    Log.i("Application", "Called when initialization failed. SDK will still operate properly as if useLocalCaching is set to false.")
                }

                override fun onInitSucceed() {
                    Log.i("Application", "Called when initialization is completed.")
                }
            }
        )
    }
}