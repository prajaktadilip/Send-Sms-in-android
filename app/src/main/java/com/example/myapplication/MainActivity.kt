package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.widget.Toast//import android.support.v4.app.ActivityCompat;

//import android.support.v4.content.ContextCompat;
//import android.support.v7.app.AppCompatActivity;

class MainActivity : AppCompatActivity() {
    //variables
    internal lateinit var btnsend: Button
    internal lateinit var txtphone: EditText
    internal lateinit var txtmsg: EditText
    private val MY_PERMISSIONS_REQUEST_SEND_SMS = 1

    private val SENT = "SMS_SENT"
    private val DELIVERED = "SMS_DELIVERED"
    internal lateinit var sentPI: PendingIntent
    internal lateinit var deliveredPI: PendingIntent
    internal lateinit var smsSentReceiver: BroadcastReceiver
    internal lateinit var smsDeliveredReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btnsend = findViewById<View>(R.id.send) as Button
        txtphone = findViewById<View>(R.id.txtphone) as EditText
        txtmsg = findViewById<View>(R.id.txtmsg) as EditText
        sentPI = PendingIntent.getBroadcast(this@MainActivity, 0, Intent(SENT), 0)
        deliveredPI = PendingIntent.getBroadcast(this@MainActivity, 0, Intent(DELIVERED), 0)




        btnsend.setOnClickListener {
            val message = txtmsg.text.toString()
            val telNr = txtphone.text.toString()

            if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.SEND_SMS),
                        MY_PERMISSIONS_REQUEST_SEND_SMS)
            } else {
                val sms = SmsManager.getDefault()

                //phone - Recipient's phone number
                //address - Service Center Address (null for default)
                //message - SMS message to be sent
                //piSent - Pending intent to be invoked when the message is sent
                //piDelivered - Pending intent to be invoked when the message is delivered to the recipient
                sms.sendTextMessage(telNr, null, message, sentPI, deliveredPI)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        val smsSentReceiver: BroadcastReceiver? = null
        unregisterReceiver(smsSentReceiver)
        unregisterReceiver(smsDeliveredReceiver)
    }

    override fun onResume() {
        super.onResume()

        //The deliveredPI PendingIntent does not fire in the Android emulator.
        //You have to test the application on a real device to view it.
        //However, the sentPI PendingIntent works on both, the emulator as well as on a real device.

        smsSentReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                when (resultCode) {
                    Activity.RESULT_OK -> Toast.makeText(context, "SMS sent successfully!", Toast.LENGTH_SHORT).show()

                    //Something went wrong and there's no way to tell what, why or how.
                    SmsManager.RESULT_ERROR_GENERIC_FAILURE -> Toast.makeText(context, "Generic failure!", Toast.LENGTH_SHORT).show()

                    //Your device simply has no cell reception. You're probably in the middle of
                    //nowhere, somewhere inside, underground, or up in space.
                    //Certainly away from any cell phone tower.
                    SmsManager.RESULT_ERROR_NO_SERVICE -> Toast.makeText(context, "No service!", Toast.LENGTH_SHORT).show()

                    //Something went wrong in the SMS stack, while doing something with a protocol
                    //description unit (PDU) (most likely putting it together for transmission).
                    SmsManager.RESULT_ERROR_NULL_PDU -> Toast.makeText(context, "Null PDU!", Toast.LENGTH_SHORT).show()

                    //You switched your device into airplane mode, which tells your device exactly
                    //"turn all radios off" (cell, wifi, Bluetooth, NFC, ...).
                    SmsManager.RESULT_ERROR_RADIO_OFF -> Toast.makeText(context, "Radio off!", Toast.LENGTH_SHORT).show()
                }

            }
        }

        smsDeliveredReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                when (resultCode) {
                    Activity.RESULT_OK -> Toast.makeText(context, "SMS delivered!", Toast.LENGTH_SHORT).show()

                    Activity.RESULT_CANCELED -> Toast.makeText(context, "SMS not delivered!", Toast.LENGTH_SHORT).show()
                }

            }
        }

        //register the BroadCastReceivers to listen for a specific broadcast
        //if they "hear" that broadcast, it will activate their onReceive() method
        registerReceiver(smsSentReceiver, IntentFilter(SENT))
        registerReceiver(smsDeliveredReceiver, IntentFilter(DELIVERED))
    }
}
