package com.example.songsfirebase


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat


open class NotificationReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        var intent1 = Intent(context, MusicService::class.java)
        intent1.putExtra("musicnoti", ""+ intent?.action)

        if (context != null) {
            Log.d("zxc",""+intent?.action)
            ContextCompat.startForegroundService(context, intent1)
        }


    }
 }