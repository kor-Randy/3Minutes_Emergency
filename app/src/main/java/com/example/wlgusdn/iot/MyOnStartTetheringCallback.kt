package com.example.wlgusdn.iot

abstract class MyOnStartTetheringCallback
{
    abstract fun onTetheringStarted()

    /**
     * Called when starting tethering failed.
     */
    abstract fun onTetheringFailed()
}