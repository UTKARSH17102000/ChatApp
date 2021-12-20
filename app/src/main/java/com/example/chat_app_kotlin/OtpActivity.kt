package com.example.chat_app_kotlin

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Message
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.activity_otp.*
import java.util.concurrent.TimeUnit
import android.text.style.ClickableSpan as ClickableSpan1

const val PHONE_NUMBER = "phone number"
class OtpActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    var phoneNumber:String? = null
    var mVerificationId:String? = null
    var mResendToken:PhoneAuthProvider.ForceResendingToken? = null
    var  mCounterDown:CountDownTimer? = null
    private lateinit var progressDialog: ProgressDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp)
       initViews()
        startVerify()

    }

    private fun startVerify() {

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber!!,
                60,
                TimeUnit.SECONDS,
                this,
                callbacks
        )

        showTimer(6000)

         progressDialog = createProgressDialog("Sending a Verification Code",false)
        progressDialog.show()
    }

    private fun showTimer(milliSecInFuture: Long) {

        // This function shows the timer
        resendBtn.isEnabled = false
         mCounterDown = object : CountDownTimer(milliSecInFuture, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                counterTv.isVisible = true
                counterTv.text = getString(R.string.second_remaining, millisUntilFinished / 1000)
            }

            override fun onFinish() {
                resendBtn.isEnabled = true
                counterTv.isVisible = false
            }

        }.start()

    }
// We need to stop the timer otherwise app got crashed if we press the back button
    override fun onDestroy() {
        super.onDestroy()
        if(mCounterDown != null){
             mCounterDown!!.cancel()
        }
    }

    private fun initViews() {

        // this is OTP func from the firebase
        phoneNumber = intent.getStringExtra(PHONE_NUMBER)
        verifyTv.text = getString(R.string.verify_number,phoneNumber)

        verificationBtn.setOnClickListener(this)
        resendBtn.setOnClickListener(this)

        setSpannableString()
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                    if(::progressDialog.isInitialized){
                        progressDialog.dismiss()
                    }

                val smsCode:String? = credential.smsCode
                if(!smsCode.isNullOrBlank()){
                    sentcodeEt.setText(smsCode)
                }


                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                // :: is called a scope operator
                if(::progressDialog.isInitialized){
                    progressDialog.dismiss()
                }

                if (e is FirebaseAuthInvalidCredentialsException) {
                    Log.e("Exception:", "FirebaseAuthInvalidCredentialsException", e)
                    Log.e("=========:", "FirebaseAuthInvalidCredentialsException " + e.message)
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // ...
                    Log.e("Exception:", "FirebaseTooManyRequestsException", e)
                }

                Log.e("ERROR_FIREBASE",e.localizedMessage)

                // Show a message and update the UI
                // ...
                notifyUserAndRetry("Your Phone Number might be wrong or connection error.Retry again!")
            }

            override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                progressDialog.dismiss()
                counterTv.isVisible = false
                Log.e("TAG", "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                mVerificationId = verificationId
                mResendToken = token

                // ...
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
// This func is use for sign in
        val m_Auth = FirebaseAuth.getInstance()
        m_Auth.signInWithCredential(credential)
                .addOnCompleteListener {
                    if(it.isSuccessful){

                        startActivity(
                                Intent(this,SignUpActivity::class.java)
                        )
                        finish()
                        if (::progressDialog.isInitialized) {
                            progressDialog.dismiss()
                        }
                    }else{
                        if (::progressDialog.isInitialized) {
                            progressDialog.dismiss()
                        }
                        notifyUserAndRetry("Your Phone Number Verification failed .Retry again!")
                    }
                }
    }

    private fun setSpannableString() {
        // this func is used to set the spannableString
        val span  = SpannableString(getString(R.string.waiting_text,phoneNumber))

        val clickableSpan = object : ClickableSpan1(){
            override fun onClick(widget: View) {
             showLoginActivity()
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = ds.linkColor
            }
        }
          span.setSpan(clickableSpan,span.length - 13,span.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        waitingTv.movementMethod = LinkMovementMethod.getInstance()
        waitingTv.text = span
    }

    override fun onBackPressed() {
    }

    private fun notifyUserAndRetry(message: String) {
        // this is the Alert dialog

        MaterialAlertDialogBuilder(this).apply {
            setMessage(message)
            setPositiveButton("Ok") { _, _ ->
                showLoginActivity()
            }

            setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

            setCancelable(false)
            create()
            show()
        }
    }
    private fun showLoginActivity() {
        startActivity(
                Intent(this, LoginActivity::class.java)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
    }

    override fun onClick(v: View?) {
        when(v){
            verificationBtn ->{
                val code : String = sentcodeEt.text.toString()
                if(code.isNotEmpty() && !mVerificationId.isNullOrBlank()){
                    progressDialog = createProgressDialog("Please wait......",true)
                    progressDialog.show()

                    val credential= PhoneAuthProvider.getCredential(mVerificationId!!,code)
                    signInWithPhoneAuthCredential(credential)
                }
            }

            resendBtn ->{
                val code : String = sentcodeEt.text.toString()
                if(mResendToken != null){
                    showTimer(60000)
                    progressDialog = createProgressDialog("Sending a verification Code......",true)
                    progressDialog.show()
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber!!,
                            60,
                            TimeUnit.SECONDS,
                            this,
                            callbacks,
                            mResendToken)
                }
            }
        }
    }
}


  fun Context.createProgressDialog(message: String, isCancelable:Boolean):ProgressDialog{

      return ProgressDialog(this).apply {
          setCancelable(true)
          setMessage(message)
          setCanceledOnTouchOutside(false)
      }
  }