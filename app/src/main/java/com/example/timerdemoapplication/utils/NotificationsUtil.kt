package com.example.timerdemoapplication.utils

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color.BLUE
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.timerdemoapplication.MainActivity
import com.example.timerdemoapplication.R
import com.example.timerdemoapplication.TimerNotificationActionReceiver
import java.text.SimpleDateFormat
import java.util.*

class NotificationsUtil
{
    companion object{
        /*
            This 2 notification channel HAVE TO  be use from the API 26 (Oreo)
         */
        private const val CHANNEL_ID_TIMER = "menu_timer"
        private const val CHANNEL_NAME_TIMER = "Timer App Timer"
        private const val  TIMER_ID = 0

        /*
            This is when the timer expire which means it ends.
         */
        fun showTimerExpired(context:Context)
        {
            /*
                To control the timer from the notification we need an APP component,
                in this case the best component will be BroadCast Receiver.
                startIntent.action is to start the intent
             */
            val startIntent = Intent(context,TimerNotificationActionReceiver::class.java)
            startIntent.action = AppConstants.ACTION_START
            val startPendingIntent = PendingIntent.getBroadcast(context,
                0,startIntent,PendingIntent.FLAG_UPDATE_CURRENT)

            /*
                This is to set the title,text and what it does on the notification panel.
             */
            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER,true)
            nBuilder.setContentTitle("Timer Expired!")
                .setContentText("Start again?")
                .setContentIntent(getPendingIntentWithStack(context,MainActivity::class.java))
                .addAction(R.drawable.ic_play,"Start",startPendingIntent)

            /*
                Because we want to allow this notifications to work on Android Oreo and above
                we have to create a notification channel.
                We have to create 3 notifications in the same notification channel.
                It will be a waste of code to code it 3 times.
                So we are using an extension function.
             */
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)

            nManager.notify(TIMER_ID,nBuilder.build())

        }

        fun showTimerRunning(context:Context,wakeUpTime:Long)
        {
            val stopIntent = Intent(context,TimerNotificationActionReceiver::class.java)
            stopIntent.action = AppConstants.ACTION_STOP
            val stopPendingIntent = PendingIntent.getBroadcast(context,
                0,stopIntent,PendingIntent.FLAG_UPDATE_CURRENT)

            val pauseIntent = Intent(context,TimerNotificationActionReceiver::class.java)
            pauseIntent.action = AppConstants.ACTION_PAUSE
            val pausePendingIntent = PendingIntent.getBroadcast(context,
                0,pauseIntent,PendingIntent.FLAG_UPDATE_CURRENT)

            val df = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)

            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER,true)
            nBuilder.setContentTitle("Timer is Running")
                .setContentText("End : ${df.format(Date(wakeUpTime))}")
                .setContentIntent(getPendingIntentWithStack(context,MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_stop,"Stop",stopPendingIntent)
                .addAction(R.drawable.ic_pause,"Pause",pausePendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)

            nManager.notify(TIMER_ID,nBuilder.build())

        }

        fun showTimerPaused(context:Context)
        {
            val resumeIntent = Intent(context,TimerNotificationActionReceiver::class.java)
            resumeIntent.action = AppConstants.ACTION_RESUME
            val resumePendingIntent = PendingIntent.getBroadcast(context,
                0,resumeIntent,PendingIntent.FLAG_UPDATE_CURRENT)

            val nBuilder = getBasicNotificationBuilder(context, CHANNEL_ID_TIMER,true)
            nBuilder.setContentTitle("Timer is paused")
                .setContentText("Resume?")
                .setContentIntent(getPendingIntentWithStack(context,MainActivity::class.java))
                .setOngoing(true)
                .addAction(R.drawable.ic_play,"Resume",resumePendingIntent)

            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            nManager.createNotificationChannel(CHANNEL_ID_TIMER, CHANNEL_NAME_TIMER,true)

            nManager.notify(TIMER_ID,nBuilder.build())

        }

        fun hideTimerNotification(context:Context)
        {
            val nManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nManager.cancel(TIMER_ID)
        }

        /*
            This function is to build a basic notification interface with the ChannelID and the PlaySound of the notifications.
            The notificationSound is set by default using the RingtoneManager.
            The notification Builder will build a small icon.
            SetAutoCancel ensures that when the user click on the notifications is going to be automatically be dismissed.
         */
        private fun getBasicNotificationBuilder(context: Context,channelId:String,playSound:Boolean)
        : NotificationCompat.Builder
        {
            val notificationSound : Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val nBuilder = NotificationCompat.Builder(context,channelId)
                .setSmallIcon(R.drawable.ic_timer)
                .setAutoCancel(true)
                .setDefaults(0)

            if(playSound) nBuilder.setSound(notificationSound)
            return nBuilder
        }

        /*
            This is to get Pending Intent with Stack. This means that when you have an activity which
            is let's say two layers deep. This means there is another activity below it at the stack.
            When the user click on the notifications, it is gonna to take him to that activity.
            Also when later he presses on the back button, it is going to bring him to the activity which
            is one layer lower. Just like if he had opened that activity before but he hadn't.

            It is a Generic Function
         */
        private fun <T> getPendingIntentWithStack(context: Context,javaClass:Class<T>) : PendingIntent
        {
            val resultIntent = Intent(context,javaClass)
            /*
                This ensures that if the activity we are clicking into is already opened
                It will not be created again.
             */
            resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP

            val stackBuilder = TaskStackBuilder.create(context)
            stackBuilder.addParentStack(javaClass)

            // This is the activity which we want to open
            stackBuilder.addNextIntent(resultIntent)

            return stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT)

        }

        /*
            This is an EXTENSION FUNCTION.
            An Extension Function means we are going to extend one class by one function.
            In other words we are editing the code of the class.

         */
        @TargetApi(26)
        private fun NotificationManager.createNotificationChannel(channelID: String,
                                                                  channelName : String,
                                                                  playSound: Boolean){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                val channelImportance = if (playSound) NotificationManager.IMPORTANCE_DEFAULT
                else NotificationManager.IMPORTANCE_LOW
                val nChannel = NotificationChannel(channelID, channelName, channelImportance)
                nChannel.enableLights(true)
                nChannel.lightColor = BLUE
                this.createNotificationChannel(nChannel)
            }
        }
    }
}