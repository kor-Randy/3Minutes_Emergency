package com.example.wlgusdn.iot

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.support.v4.content.ContextCompat

abstract class PermissionsActivity : Activity()
{
    internal val MY_PERMISSIONS_MANAGE_WRITE_SETTINGS = 100
    internal val MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 69

    private var mLocationPermission = false
    private var mSettingPermission = true

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        settingPermission()
        /**
         * Locations permission done in onActrivityResult
         */
        locationsPermission()

        if (mLocationPermission && mSettingPermission) onPermissionsOkay()
    }


    private fun settingPermission() {
        mSettingPermission = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(getApplicationContext())) {
                mSettingPermission = false
                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()))
                startActivityForResult(intent, MY_PERMISSIONS_MANAGE_WRITE_SETTINGS)
            }
        }
    }


    private fun locationsPermission() {
        mLocationPermission = true
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            mLocationPermission = false
            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION
                )

                // MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // Check which request we're responding to
        if (requestCode == MY_PERMISSIONS_MANAGE_WRITE_SETTINGS) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                mSettingPermission = true
                if (!mLocationPermission) locationsPermission()
            } else {
                settingPermission()
            }
        }

        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                mLocationPermission = true
                if (!mSettingPermission) settingPermission()
            } else {
                locationsPermission()
            }
        }

        if (mLocationPermission && mSettingPermission) onPermissionsOkay()

    }


    internal abstract fun onPermissionsOkay()
}