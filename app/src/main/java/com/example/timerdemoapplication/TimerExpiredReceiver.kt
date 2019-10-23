package com.example.timerdemoapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timerdemoapplication.utils.NotificationsUtil
import com.example.timerdemoapplication.utils.PrefUtil

class TimerExpiredReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        //show notifications
        NotificationsUtil.showTimerExpired(context)

        /*
            When the timer expire the Timer will stopped.
         */
        PrefUtil.setTimerState(MainActivity.TimerState.Stopped,context)
        PrefUtil.setAlarmSetTime(0,context)
    }
}
