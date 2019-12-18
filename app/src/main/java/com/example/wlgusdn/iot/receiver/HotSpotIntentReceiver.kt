package com.example.wlgusdn.iot.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.wlgusdn.iot.MagicActivity
import com.example.wlgusdn.iot.R

class HotSpotIntentReceiver : BroadcastReceiver()
{
    private val TAG = HotSpotIntentReceiver::class.java.simpleName

    override fun onReceive(context: Context, intent: Intent?) {
        val ACTION_TURNON = context.getString(R.string.intent_action_turnon)
        val ACTION_TURNOFF = context.getString(R.string.intent_action_turnoff)
        Log.i(TAG, "Received intent")
        if (intent != null) {
            val action = intent.action

            if (ACTION_TURNON == action) {
                val magicActivity = MagicActivity()
                magicActivity.useMagicActivityToTurnOn(context)
            } else if (ACTION_TURNOFF == action) {
                val magicActivity = MagicActivity()
                magicActivity.useMagicActivityToTurnOff(context)
            }
        }

    }
}