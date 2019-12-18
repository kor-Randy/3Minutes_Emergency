package com.example.wlgusdn.iot

import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Handler
import android.util.Log
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import com.android.dx.stock.ProxyBuilder

class MyOreoWifiManager {
    private val TAG = MyOreoWifiManager::class.java.simpleName

    private var mContext: Context?=null
    private var mWifiManager: WifiManager?=null
    private var mConnectivityManager: ConnectivityManager?=null

   constructor(c: Context){
        mContext = c
        mWifiManager = mContext!!.getSystemService(Context.WIFI_SERVICE) as WifiManager
        mConnectivityManager = mContext!!.getSystemService(ConnectivityManager::class.java) as ConnectivityManager
    }

    /**
     * This sets the Wifi SSID and password
     * Call this before `startTethering` if app is a system/privileged app
     * Requires: android.permission.TETHER_PRIVILEGED which is only granted to system apps
     */
    fun configureHotspot(name: String, password: String) {
        val apConfig = WifiConfiguration()
        apConfig.SSID = name
        apConfig.preSharedKey = password
        apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        try {
            val setConfigMethod =
                mWifiManager!!.javaClass.getMethod("setWifiApConfiguration", WifiConfiguration::class.java)
            val status = setConfigMethod.invoke(mWifiManager, apConfig) as Boolean
            Log.d(TAG, "setWifiApConfiguration - success? $status")
        } catch (e: Exception) {
            Log.e(TAG, "Error in configureHotspot")
            e.printStackTrace()
        }

    }

    /**
     * This enables tethering using the ssid/password defined in Settings App>Hotspot & tethering
     * Does not require app to have system/privileged access
     * Credit: Vishal Sharma - https://stackoverflow.com/a/52219887
     */
    fun startTethering(callback: MyOnStartTetheringCallback): Boolean {
        val outputDir = mContext!!.codeCacheDir
        val proxy: Any
        try {
            proxy = ProxyBuilder.forClass(OnStartTetheringCallbackClass())
                .dexCache(outputDir).handler(InvocationHandler { proxy, method, args ->
                    when (method.name) {
                        "onTetheringStarted" -> callback.onTetheringStarted()
                        "onTetheringFailed" -> callback.onTetheringFailed()
                        else -> ProxyBuilder.callSuper(proxy, method, args)
                    }
                    null
                }).build()
        } catch (e: Exception) {
            Log.e(TAG, "Error in enableTethering ProxyBuilder")
            e.printStackTrace()
            return false
        }

        var method: Method? = null
        try {
            method = mConnectivityManager!!.javaClass.getDeclaredMethod(
                "startTethering",
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                OnStartTetheringCallbackClass(),
                Handler::class.java
            )
            if (method == null) {
                Log.e(TAG, "startTetheringMethod is null")
            } else {
                method.invoke(mConnectivityManager, ConnectivityManager.TYPE_MOBILE, false, proxy, null)
                Log.d(TAG, "startTethering invoked")
            }
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error in enableTethering")
            e.printStackTrace()
        }

        return false
    }

    fun stopTethering() {
        try {
            val method =
                mConnectivityManager!!.javaClass.getDeclaredMethod("stopTethering", Int::class.javaPrimitiveType!!)
            if (method == null) {
                Log.e(TAG, "stopTetheringMethod is null")
            } else {
                method.invoke(mConnectivityManager, ConnectivityManager.TYPE_MOBILE)
                Log.d(TAG, "stopTethering invoked")
            }
        } catch (e: Exception) {
            Log.e(TAG, "stopTethering error: $e")
            e.printStackTrace()
        }

    }

    private fun OnStartTetheringCallbackClass(): Class<*>? {
        try {
            return Class.forName("android.net.ConnectivityManager\$OnStartTetheringCallback")
        } catch (e: ClassNotFoundException) {
            Log.e(TAG, "OnStartTetheringCallbackClass error: $e")
            e.printStackTrace()
        }

        return null
    }
}