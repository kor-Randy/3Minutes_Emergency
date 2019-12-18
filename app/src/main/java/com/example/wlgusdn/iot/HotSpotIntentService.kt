package com.example.wlgusdn.iot

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.content.ContentValues.TAG

class HotSpotIntentService : IntentService("HotSpotIntentService")
{
    /**
     * Id for running service in foreground
     */
    private val FOREGROUND_ID = 1338
    private val CHANNEL_ID = "control_app"

    // Action names...assigned in manifest.
    private var ACTION_TURNON: String? = null
    private var ACTION_TURNOFF: String? = null
    private var DATAURI_TURNON: String? = null
    private var DATAURI_TURNOFF: String? = null
    private var mStartIntent: Intent? = null

    @RequiresApi(api = Build.VERSION_CODES.O)
    internal var mMyOreoWifiManager: MyOreoWifiManager? = null


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     *
     */


    /**
     * Helper method to start this intent from [HotSpotIntentReceiver]
     * @param context
     * @param intent
     */
    fun start(context: Context, intent: Intent) {
        val i = Intent(context, HotSpotIntentService::class.java)
        i.action = intent.action
        i.data = intent.data
        context.startService(i)
    }

    override fun onHandleIntent(intent: Intent?) {
        ACTION_TURNON = getString(R.string.intent_action_turnon)
        ACTION_TURNOFF = getString(R.string.intent_action_turnoff)

        DATAURI_TURNON = getString(R.string.intent_data_host_turnon)
        DATAURI_TURNOFF = getString(R.string.intent_data_host_turnoff)

        // Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Received start intent")

        mStartIntent = intent

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

        } else {
            carryOn()
        }

    }


    private fun carryOn() {
        var turnOn = true
        if (mStartIntent != null) {
            val action = mStartIntent!!.action
            val data = mStartIntent!!.dataString
            if (ACTION_TURNON == action || data != null && data.contains(DATAURI_TURNON!!)) {
                turnOn = true
                Log.i(TAG, "Action/data to turn on hotspot")
            } else if (ACTION_TURNOFF == action || data != null && data.contains(DATAURI_TURNOFF!!)) {
                turnOn = false
                Log.i(TAG, "Action/data to turn off hotspot")
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                hotspotOreo(turnOn)
            } else {
                turnOnHotspotPreOreo(turnOn)
            }
        }
    }


    private fun turnOnHotspotPreOreo(turnOn: Boolean): Boolean {
        run {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val methods = wifiManager.javaClass.declaredMethods
            for (method in methods) {
                if (method.name == "setWifiApEnabled") {
                    try {
                        if (turnOn) {
                            wifiManager.isWifiEnabled =
                                false //Turning off wifi because tethering requires wifi to be off
                            method.invoke(wifiManager, null, true) //Activating tethering
                            return true
                        } else {
                            method.invoke(wifiManager, null, false) //Deactivating tethering
                            wifiManager.isWifiEnabled =
                                true //Turning on wifi ...should probably be done from a saved setting
                            return true
                        }
                    } catch (e: Exception) {
                        return false
                    }

                }
            }

            //Error setWifiApEnabled not found
            return false
        }


    }

    /**
     *
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun hotspotOreo(turnOn: Boolean) {

        if (mMyOreoWifiManager == null) {
            mMyOreoWifiManager = MyOreoWifiManager(this)
        }

        if (turnOn) {

            //this dont work
            val callback = object : MyOnStartTetheringCallback() {
                override fun onTetheringStarted() {
                    startForeground(
                        FOREGROUND_ID,
                        buildForegroundNotification()
                    )
                }

                override fun onTetheringFailed() {

                }
            }

            mMyOreoWifiManager!!.startTethering(callback)
        } else {
            mMyOreoWifiManager!!.stopTethering()
            stopForeground(true)
            stopSelf()
        }

    }

    //****************************************************************************************


    /**
     * Build low priority notification for running this service as a foreground service.
     * @return
     */
    private fun buildForegroundNotification(): Notification {
        registerNotifChnnl(this)

        val stopIntent = Intent(this, HotSpotIntentService::class.java)
        stopIntent.action = getString(R.string.intent_action_turnoff)

        val pendingIntent = PendingIntent.getService(this, 0, stopIntent, 0)

        val b = NotificationCompat.Builder(this, CHANNEL_ID)
        b.setOngoing(true)
            .setContentTitle("WifiHotSpot is On")
            .addAction(
                NotificationCompat.Action(
                    R.drawable.turn_off,
                    "TURN OFF HOTSPOT",
                    pendingIntent
                )
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setSmallIcon(R.drawable.notif_hotspot_black_24dp)


        return b.build()
    }


    private fun registerNotifChnnl(context: Context) {
        if (Build.VERSION.SDK_INT >= 26) {
            val mngr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mngr.getNotificationChannel(CHANNEL_ID) != null) {
                return
            }
            //
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_chnnl),
                NotificationManager.IMPORTANCE_LOW
            )
            // Configure the notification channel.
            channel.description = context.getString(R.string.notification_chnnl_location_descr)
            channel.enableLights(false)
            channel.enableVibration(false)
            mngr.createNotificationChannel(channel)
        }
    }
}