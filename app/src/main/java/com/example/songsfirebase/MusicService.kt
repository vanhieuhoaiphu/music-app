package com.example.songsfirebase



import android.app.*
import android.content.ComponentName

import android.content.Intent

import android.media.MediaPlayer
import android.net.Uri

import android.os.Binder
import android.os.Build

import android.os.IBinder
import android.util.Log

import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


class MusicService: Service() {
    var media: MediaPlayer=MediaPlayer()
    var mediaoff: MediaPlayer=MediaPlayer()
    var medialist: MediaPlayer=MediaPlayer()
    var mediahome: MediaPlayer=MediaPlayer()
    var check=""
   var timenum:Int=-1
    private val myBinder:IBinder = MyLocalBinder()
    override fun onBind(intent: Intent?): IBinder? {
        return myBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        var time= intent?.getStringExtra("time")
        if(time!=null){
            timenum= time?.toInt()!!
        }

        check= intent?.getStringExtra("musicnoti").toString()

        Thread{
            while(true){
                if(timenum!=null){
                    while (timenum >=0){
                        if(timenum==0){
                            stop()
                            timenum=-1
                        }
                        else{
                            timenum--
                        }
                        Thread.sleep(1000)
                    }
                }
            }
        }.start()


        return super.onStartCommand(intent, flags, startId)
    }
fun stop(){
    mediahome.pause()
    media.pause()
    medialist.pause()
    mediaoff.pause()
}
    fun playmusic( title:String, uri:String, id:Int){
        if(id==1){
           stop()
            mediahome= MediaPlayer()
            mediahome.setDataSource(uri)
            mediahome.prepare()
            mediahome.start()
        }

        else if(id==3)
        {
           stop()
            mediaoff= MediaPlayer()
            mediaoff.setDataSource(uri)

            mediaoff.prepare()
            mediaoff.start()
        } else if(id==4)
        {
            stop()
            if(uri.indexOf("/storage/")==-1){

                medialist= MediaPlayer.create(this, Uri.parse(uri))
                Log.d("zxc",""+uri)
                medialist.start()

            }
           else{
                medialist= MediaPlayer()
                medialist.setDataSource(uri)
                medialist.prepare()
                medialist.start()
            }


        }

    }
    fun notifi(title:String,name:String){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel("as", "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT)
            val manager = getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(serviceChannel)
        }
        var contentView= RemoteViews(packageName, R.layout.notihome2)

            contentView.setTextViewText(R.id.titlenoti,title)
            contentView.setOnClickPendingIntent(R.id.playBtnnotihome, Stop(name))
            contentView.setOnClickPendingIntent(R.id.nextBtnnotihome, Next(name))
    
        if(check.indexOf("stop")!=-1){
            contentView= RemoteViews(packageName, R.layout.notihome1)
            contentView.setTextViewText(R.id.titlenoti,title)
            contentView.setOnClickPendingIntent(R.id.playBtnnotihome, Start(name))
            contentView.setOnClickPendingIntent(R.id.nextBtnnotihome, Next(name))
        }


        val intent = Intent(this, HomeFragment::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        var builder = NotificationCompat.Builder(this, "channelId")
                .setContent(contentView)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(pendingIntent)
                .build()
        startForeground(1,builder)
    }
    fun Stop(name:String): PendingIntent {
        val intentbtn=Intent("stop"+name)


        return PendingIntent.getBroadcast(this, 1, intentbtn, 0)
    }
    fun Start(name:String): PendingIntent {
        val intentbtn=Intent("play"+name)

        return PendingIntent.getBroadcast(this, 1, intentbtn, 0)
    }
    fun Next(name:String): PendingIntent {
        val intentbtn=Intent("next"+name)

        return PendingIntent.getBroadcast(this, 1, intentbtn, 0)
    }



    override fun startService(service: Intent?): ComponentName? {
        return super.startService(service)
    }

    inner class MyLocalBinder : Binder() {
        fun getService(): MusicService {
            return this@MusicService
        }

    }
}
