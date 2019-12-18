package com.example.wlgusdn.iot

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast

class MagicActivity : AppCompatActivity()
{
    fun useMagicActivityToTurnOn(c: Context) {
        val uri = Uri.Builder().scheme(c.getString(R.string.intent_data_scheme))
            .authority(c.getString(R.string.intent_data_host_turnon)).build()
        Toast.makeText(c, "Turn on. Uri: $uri", Toast.LENGTH_LONG).show()
        val i = Intent(Intent.ACTION_VIEW)
        i.data = uri
        c.startActivity(i)
    }

    fun useMagicActivityToTurnOff(c: Context) {
        val uri = Uri.Builder().scheme(c.getString(R.string.intent_data_scheme))
            .authority(c.getString(R.string.intent_data_host_turnoff)).build()
        Toast.makeText(c, "Turn off. Uri: $uri", Toast.LENGTH_LONG).show()
        val i = Intent(Intent.ACTION_VIEW)
        i.data = uri
        c.startActivity(i)
    }

    private val TAG = MagicActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate")
    }


    internal fun onPermissionsOkay() {
        carryOnWithHotSpotting()
    }


    /**
     * The whole purpose of this activity - to start [HotSpotIntentService]
     * This may be called straright away in `onCreate` or after permissions granted.
     */
    private fun carryOnWithHotSpotting() {
        val intent = intent
        val hotSpotIntentService = HotSpotIntentService()
         hotSpotIntentService.start(this@MagicActivity, intent)
        finish()
    }

}