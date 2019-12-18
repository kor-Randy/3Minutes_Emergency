package com.example.wlgusdn.iot



import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.util.Log
import java.lang.reflect.Method
import android.content.Context.WIFI_SERVICE
import android.os.Build
import android.os.Handler
import android.support.v4.content.ContextCompat.getSystemService
import android.support.v4.util.Preconditions
import android.text.Html
import android.text.method.LinkMovementMethod
import java.lang.reflect.InvocationTargetException
import java.util.UUID.randomUUID
import android.net.wifi.WifiConfiguration.KeyMgmt
import java.util.*


class ApManager {
    val TAG = "WiFiHotspotActivity";

    fun getWifiApState():Int
    {
        try
        {
            var method = mWifiManager!!.javaClass.getMethod( "getWifiApState");
            return method.invoke(mWifiManager) as Int
        }
        catch ( e:Exception)
        {
            Log.e(TAG, "", e);
            return WIFI_AP_STATE_FAILED;
        }
    }

   val WIFI_AP_STATE_FAILED = 4;
    var mWifiManager : WifiManager?=null

   constructor( context:Context)
    {
        mWifiManager =  context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }



    @SuppressLint("NewApi")
    fun setWifiApEnabled(config:WifiConfiguration, enabled:Boolean) : Boolean
    {

      /*  config.SSID = "\"" + "look" + "\"";
        config.preSharedKey = "\"" + "wl850930" + "\"";
        config.hiddenSSID = true;
        config.status = WifiConfiguration.Status.ENABLED;
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        val res = mWifiManager!!.addNetwork(config);
        mWifiManager!!.enableNetwork(res, true);

        mWifiManager!!.saveConfiguration()

        mWifiManager!!.setWifiEnabled(true);*/
        config.SSID="look"
        config.status=WifiConfiguration.Status.DISABLED
        config.priority=40
        mWifiManager!!.enableNetwork(mWifiManager!!.addNetwork(config),true)
        try
        {
            if (enabled)
            {
                mWifiManager!!.startLocalOnlyHotspot(object : WifiManager.LocalOnlyHotspotCallback()
                {
                    override fun onStarted(reservation: WifiManager.LocalOnlyHotspotReservation?) {
                        super.onStarted(reservation)
                        Log.d("gkttm","Start")


                        config.allowedAuthAlgorithms.clear()
                        config.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                        config.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)

                        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)

                        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)


                        Log.d("gkttm","이거다"+reservation!!.wifiConfiguration.SSID+"/"+reservation!!.wifiConfiguration.preSharedKey)

                    }

                    override fun onStopped() {
                        super.onStopped()

                        Log.d("gkttm","Stop")
                    }

                    override fun onFailed(reason: Int) {
                        super.onFailed(reason)
                        Log.d("gkttm","Fail")
                    }
                }, Handler())
                // disable WiFi in any case
              //  mWifiManager!!.setWifiEnabled(true);


            }
            var method = mWifiManager!!.javaClass.getMethod( "setWifiApEnabled", WifiConfiguration::class.java, Boolean::class.java);
            return method.invoke(mWifiManager, config, enabled) as Boolean
        }
        catch ( e:Exception)
        {
            Log.e(TAG, "", e);
            return false;
        }
    }









    //check whether wifi hotspot on or off
    fun isApOn(context: Context): Boolean {
        val wifimanager = context.getSystemService(WIFI_SERVICE) as WifiManager
        try {
            val method = wifimanager::class.java.getDeclaredMethod("isWifiApEnabled")
            method.isAccessible = true
            Log.d("hothot","isApOn : "+method.invoke(wifimanager) as Boolean)
            return method.invoke(wifimanager) as Boolean
        } catch (ignored: Throwable) {
        }

        return false
    }

    // toggle wifi hotspot on or off
    fun configApState(context: Context): Boolean {
        val wifimanager = context.getSystemService(WIFI_SERVICE) as WifiManager

        val wificonfiguration: WifiConfiguration? = WifiConfiguration()
        wificonfiguration!!.SSID="\""+"look"+"\""
        wificonfiguration.preSharedKey = "\"" + "wl850930" + "\"";
        wificonfiguration.hiddenSSID = true;
        wificonfiguration.status = WifiConfiguration.Status.ENABLED;
        try {
            // if WiFi is on, turn it off
            if (isApOn(context)) {
                wifimanager.isWifiEnabled = false
            }
            //wifimanager.setWifiEnabled(true)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                wifimanager.addNetwork(wificonfiguration)
                wifimanager.startLocalOnlyHotspot(WifiManager.LocalOnlyHotspotCallback(),Handler())
            }
            val method = wifimanager::class.java.getMethod(
                "setWifiApEnabled",
                WifiConfiguration::class.java,
                Boolean::class.java
            )
            method.invoke(wifimanager, wificonfiguration, !isApOn(context))
            Log.d("hothot","true")
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d("hothot","false")
        return false
    }
} // end of class
