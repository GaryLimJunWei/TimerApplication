package com.example.timerdemoapplication

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.timerdemoapplication.utils.AppConstants
import com.example.timerdemoapplication.utils.NotificationsUtil
import com.example.timerdemoapplication.utils.PrefUtil

class TimerNotificationActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        when(intent.action)
        {
            AppConstants.ACTION_STOP ->
            {
                MainActivity.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity.TimerState.Stopped,context)
                NotificationsUtil.hideTimerNotification(context)
            }
            AppConstants.ACTION_PAUSE ->
            {
                var secondsRemaining = PrefUtil.getSecondsRemaining(context)
                var alarmSetTime = PrefUtil.getAlarmSetTime(context)
                val nowSeconds = MainActivity.nowSeconds

                secondsRemaining -= nowSeconds - alarmSetTime
                PrefUtil.setSecondsRemaning(secondsRemaining,context)

                MainActivity.removeAlarm(context)
                PrefUtil.setTimerState(MainActivity.TimerState.Paused,context)
                NotificationsUtil.showTimerPaused(context)
            }

            AppConstants.ACTION_RESUME ->
            {
                val secondsRemaning = PrefUtil.getSecondsRemaining(context)
                val wakeUpTime = MainActivity.setAlarm(context,MainActivity.nowSeconds,secondsRemaning)
                PrefUtil.setTimerState(MainActivity.TimerState.Running,context)
                NotificationsUtil.showTimerRunning(context,wakeUpTime)
            }

            AppConstants.ACTION_START ->
            {
                val minutesRemaing = PrefUtil.getTimerLength(context)
                val secondsRemaining = minutesRemaing * 60L
                val wakeUpTime = MainActivity.setAlarm(context,MainActivity.nowSeconds,secondsRemaining)
                PrefUtil.setTimerState(MainActivity.TimerState.Running,context)
                PrefUtil.setSecondsRemaning(secondsRemaining,context)
                NotificationsUtil.showTimerRunning(context,wakeUpTime)
            }
        }
    }
}
