package com.example.wlgusdn.iot

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.widget.RemoteViews

class locationService : Service(),LocationListener
{
    var i=0
    override fun onLocationChanged(location: Location?) {


    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

     }

    override fun onProviderDisabled(provider: String?) {

    }

    var isGPSEnable = false

    var isNetWorkEnable = false

    var isGetLocation = false

    var location : Location?=null





    @SuppressLint("MissingPermission")
    fun lo(): Location {
        //위치 찾기

        var locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager


        var isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        var isNetWorkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)



        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0f, this)

        location=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

        return location!!
    }





    override fun onBind(intent: Intent?): IBinder? {

        return null

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)



    }
    override fun onCreate() {
        super.onCreate()
        lo()

        startForeground()
    }


    fun startForeground()
    {
        Log.d("asdasd","StartForeground")
        sem=0
        val notificationIntent = Intent(this,HospitalActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)

        val remoteViews = RemoteViews(packageName, R.layout.notification_service)

        val builder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= 26) {

            val CHANNEL_ID = "snwodeer_service_channel"
            val channel = NotificationChannel(CHANNEL_ID,
                "SnowDeer Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT)

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)

            builder = NotificationCompat.Builder(this, CHANNEL_ID)
        } else {
            builder = NotificationCompat.Builder(this)
        }
        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setContent(remoteViews)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        startForeground(1, builder.build())

        with(NotificationManagerCompat.from(this)) {
            //Notification생성 <-ForeGround
            // notificationId is a unique int for each notification that you must define
            sercon=this@locationService
            notify(1, builder.build())
        }


        i += 1

        Thread(Runnable {

            run {
                while (true) {

                    Log.d("asdasd","갑니다")
                    val location = lo()
                    Log.d("asdasd","lati"+location.latitude.toString()+"/"+"long"+location.longitude.toString()+"\n")
                    MainActivity.bt!!.send(location.latitude.toString()+"\n", true);
                    MainActivity.bt!!.send(location.longitude.toString()+"\n", true);


                    Thread.sleep(2000)

                    if(sem==1)
                        break;
                }
            }
        }).start()



    }

    override fun onDestroy() {
        super.onDestroy()
        sem=1
        Log.d("wlgusdn1","des")
        with(NotificationManagerCompat.from(locationService.sercon!!)) {
            // notificationId is a unique int for each notification that you must define
            cancel(1)
        }

    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        with(NotificationManagerCompat.from(locationService.sercon!!)) {
            // notificationId is a unique int for each notification that you must define
            cancel(1)
        }

        stopSelf()
    }
    companion object
    {
        var sercon : Context?=null
        var  sem : Int?=null
    }

}