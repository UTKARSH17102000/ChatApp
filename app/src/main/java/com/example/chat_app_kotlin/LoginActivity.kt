package com.example.chat_app_kotlin

import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsApi
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.activity_login.*


class LoginActivity : AppCompatActivity() {
    private lateinit var phoneNumber:String
    private lateinit var countryCode :String
    private lateinit var HintNumber:String
    private val CREDENTIAL_PICKER_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

// this func brings the auto phone number picker in the mobile
        RequestHint()
           // this is the validation for the next button
            phoneNumberEt.addTextChangedListener{
               nextBtn.isEnabled =  !(it.isNullOrBlank() || it.length <10)
           }
        nextBtn.setOnClickListener {
            checkNumber()
        }
    }

    private fun RequestHint() {
        val hintRequest = HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build()

        val intent = Credentials.getClient(this).getHintPickerIntent(hintRequest)
        startIntentSenderForResult(intent.intentSender, CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0, Bundle())
    }


    private fun checkNumber() {
        // This code add the country code and phone numbers and then call the alert dialog which take it to the next otp screen
        // All thing happen after pressing next button
        countryCode = ccp.selectedCountryCodeWithPlus
        phoneNumber = countryCode + phoneNumberEt.text.toString()
        notifyUser()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == RESULT_OK){
             val credentials = data?.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
            if (credentials != null) {
                HintNumber = credentials.id.substring(3)
                phoneNumberEt.setText(HintNumber,TextView.BufferType.EDITABLE)
                Log.e("TAG","${credentials.id}")
            }
        }else if(requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE){
            Toast.makeText(this, "No phone numbers found", Toast.LENGTH_LONG).show()
        }
    }

    private fun notifyUser() {
        MaterialAlertDialogBuilder(this).apply {
            setMessage(
                    "We will be verifying the phone number:$phoneNumber\n" +
                            "Is this OK, or you would like to edit the number?"
            )

            setPositiveButton("OK"){ _, _ ->
                showOtpActivity()
            }

            setNegativeButton("Edit"){ dialog, which ->
                dialog.dismiss()
            }
            setCancelable(false)
            create()
            show()
        }
    }

    private fun showOtpActivity() {
        startActivity(Intent(this, OtpActivity::class.java).putExtra(PHONE_NUMBER, phoneNumber))
        finish()
    }
}