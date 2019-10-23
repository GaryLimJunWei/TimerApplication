package com.example.timerdemoapplication

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.timerdemoapplication.utils.NotificationsUtil
import com.example.timerdemoapplication.utils.PrefUtil

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    /*
        To make the timer run on the background, we are going to use Alarms.
        We are having 2 functions, one to Set the Alarm, one to Remove the Alarm.
        We do not need to create this functions directly inside the Activity because these
        function do not need any instances of the Activity.
        We are also calling them in other classes so is better to use a companion object.
     */
    companion object{
        // nowSeconds -> Current time in Seconds , secondsRemaining -> Remaining Time in Seconds
        fun setAlarm(context: Context, nowSeconds: Long,secondsRemaining: Long) : Long{


            val wakeupTime = (nowSeconds + secondsRemaining) * 1000
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            /*
                   This intent will direct to the BroadCast Receiver when the alarm goes off.
                   Broadcast Receiver are App Components just like Activities or Fragments.
                   They can subscribe to certain events and then an unreceived function inside
                   the broadcast receiver will be called. We want to call the unreceived function
                   inside the broadcast receiver whenever the alarm finishes.
             */
            val intent = Intent(context,TimerExpiredReceiver::class.java)

            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)

            // RTC_WAKEUP is to wake up the device if it is in sleep.
            alarmManager.setExact(AlarmManager.RTC_WAKEUP,wakeupTime,pendingIntent)

            // This is to remember the time where the alarm is SET
            PrefUtil.setAlarmSetTime(nowSeconds,context)
            return wakeupTime
        }

        fun removeAlarm(context: Context)
        {
            val intent = Intent(context,TimerExpiredReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(context,0,intent,0)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            PrefUtil.setAlarmSetTime(0,context)
        }

        val nowSeconds : Long
        get() = Calendar.getInstance().timeInMillis / 1000
    }

    // ENUM class TimerState Constant Variable
    enum class TimerState{
        Stopped,Paused,Running
    }
    private lateinit var timer : CountDownTimer

    // 0L is setting the value to 0 and the type to Long
    private var timerLengthSeconds = 0L
    private var secondsRemaining = 0L

    // Setting timerState to be Stopped by Default
    private var timerState = TimerState.Stopped


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // Setting the icon and the title on the Action Bar
        supportActionBar?.setIcon(R.drawable.ic_timer)
        supportActionBar?.title = "     Timer"

        /*
            updateButtons is to ensure that each button is enabled only each at a time.
            For Example if the stop button is click, it is not going to be enabled.
         */
        fab_start.setOnClickListener { v ->
            startTimer()
            timerState = TimerState.Running
            updateButtons()
        }

        fab_pause.setOnClickListener { v ->
            timer.cancel()
            timerState = TimerState.Paused
            updateButtons()
        }

       /*
           While the timer is stopped, the updateButtons will be called inside
           onTimerFinished method.
        */
        fab_stop.setOnClickListener { v ->
            timer.cancel()
            onTimerFinished()
        }
    }

    override fun onResume() {
        super.onResume()

        initTimer()

        //remove background timer, hide notification
        removeAlarm(this)
        NotificationsUtil.hideTimerNotification(this)
    }

    override fun onPause() {
        super.onPause()

        /*
            Double check again while entering this method that the timerState is not running,
            else pause it.
         */
        if(timerState == TimerState.Running)
        {
            timer.cancel()

            //start background timer and show notification
            val wakeUpTime = setAlarm(this, nowSeconds,secondsRemaining)
            NotificationsUtil.showTimerRunning(this,wakeUpTime)

        }
        else if(timerState == TimerState.Paused)
        {
            //show notification
            NotificationsUtil.showTimerPaused(this)
        }

        /*
            Preferences is like local storage cookies to store temporary data even when the app is killed.
            Created a new Kotlin Class, PrefUtil with a Companion Object ( It is like Static In Java )
            Created a new function, getTimerLength inside PrefUtil with Context as Parameter, as we need the context
            in order to store data inside Preferences.
         */


        /*
            While the timer is PAUSED.
            We needs to set the previousTimerLength and SecondsRemaining to update the timer.
            And we also update the TimerState to PAUSED.
         */
        PrefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds,this)
        PrefUtil.setSecondsRemaning(secondsRemaining,this)
        PrefUtil.setTimerState(timerState,this)


    }

    /*
          This is the function we are calling from onResume()
          Just after onCreate()
          OnResume() is called every single time you see the activity come up
          on the screen.
          OnCreate() is only called once when the activity started, and it's called
          again only when the app is destroyed and called again.

          Inside initTimer function :

          We are getting the current TimerState.
          If the TimerState is stopped, we are setting the new TimerLength.
          If not we are setting it's previous TimerLength.

          The secondsRemaining is equals to the seconds that are running/paused
          otherwise the current timerLengthSeconds.
     */
    private fun initTimer()
    {
        timerState = PrefUtil.getTimerState(this)

        if(timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        /*
            IF timerState is Running OR Paused it means that previously the Timer have started.
            So we wants to continue the timer from where it is LEFT OFF.
            Hence, we will get getSecondsRemaining that was saved when it was last used.
            ELSE, if it was not the case then we can set secondsRemaining back to timerLengthSeconds. (Default)

         */
        secondsRemaining = if(timerState == TimerState.Running || timerState == TimerState.Paused)
            PrefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

        /*
            Getting the alarmSetTime from SharedPreferences,
            Check if the alarmSetTime is more than 0.
            If the alarmSetTime is more than 0 it means that the alarm is SET
            Else the alarm is NOT SET.
         */
        val alarmSetTime = PrefUtil.getAlarmSetTime(this)
        if(alarmSetTime > 0)
            secondsRemaining -= nowSeconds - alarmSetTime

        //TODO : change secondsRemaining according to where the background timer stopped

        if(secondsRemaining <= 0)
            onTimerFinished()
        else if(timerState == TimerState.Running)
            startTimer()

        updateButtons()
        updateCountdownUI()


    }

    /*
        This function is called when the timer is finished.
        We will set the timerState to Stopped
        set the new timer length
        set the progressbar to 0
     */
    private fun onTimerFinished()
    {
        timerState = TimerState.Stopped

        setNewTimerLength()

        progress_countdown.progress = 0

        PrefUtil.setSecondsRemaning(timerLengthSeconds,this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }

    private fun startTimer() {
        timerState = TimerState.Running
        timer = object : CountDownTimer(secondsRemaining * 1000, 1000) {

            override fun onFinish() = onTimerFinished()
            override fun onTick(millisUntilFinished: Long) {

                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }

    private fun setNewTimerLength()
    {
        val lengthInMinutes = PrefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinutes * 60L)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    private fun setPreviousTimerLength()
    {
        timerLengthSeconds = PrefUtil.getPreviousTimerLengthSeconds(this)
        progress_countdown.max = timerLengthSeconds.toInt()
    }

    /*
        This function is responsible of the UI textView from moving timing.
     */
    private fun updateCountdownUI()
    {
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        textView_countdown.text = "$minutesUntilFinished:${
        if(secondsStr.length == 2 ) secondsStr
        else "0" + secondsStr }"
        progress_countdown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }

    /*
        This function is to enable or disable certain button when one or another function is on-going.
     */
    private fun updateButtons()
    {
        when(timerState)
        {
            TimerState.Running -> {
                fab_start.isEnabled = false
                fab_pause.isEnabled = true
                fab_stop.isEnabled = true
            }
            TimerState.Stopped -> {
                fab_start.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = false
            }
            TimerState.Paused -> {
                fab_start.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = true
            }
        }
    }




    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings ->
            {
                val intent = Intent(this,SettingActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
