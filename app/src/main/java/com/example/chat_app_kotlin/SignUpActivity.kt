package com.example.chat_app_kotlin

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.chat_app_kotlin.models.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sign_up.*


class SignUpActivity : AppCompatActivity() {

     val storage by lazy {
        FirebaseStorage.getInstance()
    }

    val  auth by lazy {
        FirebaseAuth.getInstance()
    }
    val database by lazy{
        FirebaseFirestore.getInstance()
    }

    private lateinit var downloadUrl: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        userImgView.setOnClickListener {
            checkPermissionForImage()
            //Firebase extensions - Image thumbnail

            // upload user info
            nextBtn.setOnClickListener {
                nextBtn.isEnabled = false
                val name:String = nameEt.text.toString()
                if (name.isEmpty()){
                    Toast.makeText(this,"Name cannot be Empty",Toast.LENGTH_LONG).show()
                }else if (downloadUrl.isEmpty()){
                    Toast.makeText(this,"Image cannot be Empty",Toast.LENGTH_LONG).show()
                }else{
                    val user = User(name,downloadUrl,downloadUrl,auth.uid!!)
                    database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {
                        startActivity(
                                Intent(this,MainActivity::class.java)
                        )
                        finish()
                    }.addOnFailureListener {
                        nextBtn.isEnabled = true
                    }
                }
            }
        }
    }

    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionWrite = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(
                    permission,
                    1001
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                requestPermissions(
                    permissionWrite,
                    1002
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            } else {
                pickImageFromGallery()
            }
        }
    }

    private fun pickImageFromGallery() {
       val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            1000
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == Activity.RESULT_OK && requestCode == 1000){

            data?.data?.let {
                userImgView.setImageURI(it)
                uplodeImage(it)
            }
        }
    }

    private fun uplodeImage(it: Uri) {
      nextBtn.isEnabled = false
        val ref = storage.reference.child("uploads/"+auth.uid.toString())

        val uploadTask = ref.putFile(it)
        uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{ task ->
            if(!task.isSuccessful){
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl

        }).addOnCompleteListener { task->
            if (task.isSuccessful){
                downloadUrl = task.result.toString()
                Log.i("URL","download: $downloadUrl")
                nextBtn.isEnabled = true
            }else{
                nextBtn.isEnabled = true
            }

        }.addOnFailureListener {
            Toast.makeText(this,"failed",Toast.LENGTH_LONG).show()
        }

    }
}