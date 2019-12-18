package com.example.wlgusdn.iot

import android.Manifest
import android.app.Service
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.content.pm.PackageManager
import android.Manifest.permission
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.support.v4.content.ContextCompat
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.support.v4.content.ContextCompat.getSystemService
import android.util.Log


class GpsTracker : Service,LocationListener
{
    private val mContext: Context
    var location: Location? = null
    var latitude: Double = 0.toDouble()
    var longitude: Double = 0.toDouble()

    private val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = 10
    private val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong()
    protected var locationManager: LocationManager? = null


   constructor(context: Context) {
        this.mContext = context
        aa()
    }


   fun aa(): Location? {
        try {
            locationManager = mContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager?

            val isGPSEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (!isGPSEnabled && !isNetworkEnabled) {

            } else {

                val hasFineLocationPermission = ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                val hasCoarseLocationPermission = ContextCompat.checkSelfPermission(
                    mContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )


                if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
                } else
                    return null


                if (isNetworkEnabled) {


                    locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                        this
                    )

                    if (locationManager != null) {
                        location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (location != null) {
                            latitude = location!!.latitude
                            longitude = location!!.longitude
                        }
                    }
                }


                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager!!.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                            this
                        )
                        if (locationManager != null) {
                            location = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (location != null) {
                                latitude = location!!.latitude
                                longitude = location!!.longitude
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("@@@", "" + e.toString())
        }

        return location
    }




    override fun onLocationChanged(location: Location) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }


    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@GpsTracker)
        }
    }


}