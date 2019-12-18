package com.example.wlgusdn.iot

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.TextView

class SecondActivity : PermissionsActivity
{
   var thiscontext : Context?=null
    override fun onPermissionsOkay() {


    }
    constructor(context : Context)
    {
        thiscontext=context
    }

    private val TAG = MainActivity::class.java.simpleName
    private val SHOW_ICON = "show_icon"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val instructTv = findViewById<TextView>(R.id.instructions_tv)
        instructTv.setMovementMethod(LinkMovementMethod.getInstance())

        val actionsNoteTv = findViewById<TextView>(R.id.actions_note_tv)
        actionsNoteTv.setMovementMethod(LinkMovementMethod.getInstance())

        val linkonTv = findViewById<TextView>(R.id.linkon_tv)
        linkonTv.setMovementMethod(LinkMovementMethod.getInstance())

        val linkoffTv = findViewById<TextView>(R.id.linkoff_tv)
        linkoffTv.setMovementMethod(LinkMovementMethod.getInstance())
    }



    fun HotSpotOnOff(i : Int?)
    {

        if(i==1)
        {
            val intent = Intent(getString(R.string.intent_action_turnon))
            sendImplicitBroadcast(thiscontext!!, intent)
        }
        else
        {
            val intent = Intent(getString(R.string.intent_action_turnoff))
            sendImplicitBroadcast(thiscontext!!, intent)
        }

    }

    fun onClickTurnOnAction(v: View) {
        val intent = Intent(getString(R.string.intent_action_turnon))
        sendImplicitBroadcast(this, intent)
    }

    fun onClickTurnOffAction(v: View) {
        val intent = Intent(getString(R.string.intent_action_turnoff))
        sendImplicitBroadcast(this, intent)
    }

    fun onClickTurnOnData(v: View) {
       val magicActivity= MagicActivity()
        magicActivity.useMagicActivityToTurnOn(this)
    }

    fun onClickTurnOffData(v: View) {
        val magicActivity= MagicActivity()
        magicActivity.useMagicActivityToTurnOff(this)
    }

    private fun sendImplicitBroadcast(ctxt: Context, i: Intent) {
        val pm = ctxt.packageManager
        val matches = pm.queryBroadcastReceivers(i, 0)

        for (resolveInfo in matches) {
            val explicit = Intent(i)
            val cn = ComponentName(
                resolveInfo.activityInfo.applicationInfo.packageName,
                resolveInfo.activityInfo.name
            )

            explicit.component = cn
            ctxt.sendBroadcast(explicit)
        }
    }

}