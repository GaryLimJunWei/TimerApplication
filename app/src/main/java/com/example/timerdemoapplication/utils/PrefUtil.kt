package com.example.timerdemoapplication.utils

import android.content.Context
import android.preference.PreferenceManager
import com.example.timerdemoapplication.MainActivity

class PrefUtil
{
    companion object
    {
        private const val TIMER_LENGTH_ID = "com.timerdemoapplication.timer.timer_length"
        fun getTimerLength(context: Context) : Int
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            //placeholder
            return preferences.getInt(TIMER_LENGTH_ID,10)
        }

        /*
            This is the ID we are using in the preferences to identify the values.
            Preferences are basically key value pairs like HashMap.
            ID are Strings that are unique to your app.
            It is always a good practise to use your package name.
         */
        private const val PREVIOUS_TIMER_LENGTH_SECONDS_ID = "com.timerdemoapplication.timer.previous_timer_length"


        /*
             If the timer is running and user change the timing limit, we should not change the running time and only
             change the one in the future after the currently running timer. Therefore, we need to have 2 separate functions
             one to Get the previous timing, one to Set the previous timing.
         */
        fun getPreviousTimerLengthSeconds(context: Context) : Long
        {
            /*
                In getLong method parenthesis, 1st is the id and 2nd is the default value
                IF the ID in the preferences is not equal to the constant value.
             */
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID,0)
        }

        fun setPreviousTimerLengthSeconds(seconds:Long,context: Context)
        {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(PREVIOUS_TIMER_LENGTH_SECONDS_ID,seconds)
            editor.apply()
        }


        private const val TIMER_STATE_ID = "com.timerdemoapplication.timer.timer_state"

        /*
            This method is expecting a return type of TimerState which is the Enum class
            we declare at the start of this program.
         */
        fun getTimerState(context: Context) : MainActivity.TimerState
        {
            /*
                Storing it into preferences.
                Enum is basically integers with name, so we can store them as integers
                inside the preferences.
                So we are getting the TimerState at the end, and the default value is 0.
                And by default we have set that 0 is equal to Stopped.
             */
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val ordinal = preferences.getInt(TIMER_STATE_ID,0)
            return MainActivity.TimerState.values()[ordinal]
        }

        /*
            This method is to set the TimerState therefore it is not returning anything.
         */
        fun setTimerState(state:MainActivity.TimerState,context: Context)
        {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            val ordinal = state.ordinal
            editor.putInt(TIMER_STATE_ID,ordinal)
            editor.apply()
        }

        private const val SECONDS_REMAINING_ID = "com.timerdemoapplication.timer.previous_timer_length"

        fun getSecondsRemaining(context: Context) : Long
        {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(SECONDS_REMAINING_ID,0)

        }

        fun setSecondsRemaning(seconds:Long,context: Context)
        {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(SECONDS_REMAINING_ID,seconds)
            editor.apply()
        }

        // ID for AlarmSetTime
        private const val ALARM_SET_TIME_ID = "com.timerdemoapplication.timer.backgrounded_time"

        // This function will get the Alarm Set Time, if the ID is not there it will return 0
        fun getAlarmSetTime(context:Context) : Long{
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            return preferences.getLong(ALARM_SET_TIME_ID,0)

        }

        // It will accept the time and set the AlarmTime.
        fun setAlarmSetTime(time:Long,context: Context)
        {
            val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
            editor.putLong(ALARM_SET_TIME_ID,time)
            editor.apply()
        }


    }
}