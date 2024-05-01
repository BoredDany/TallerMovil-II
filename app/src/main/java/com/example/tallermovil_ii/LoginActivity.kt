package com.example.tallermovil_ii

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.tallermovil_ii.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {
    //VIEW
    lateinit var binding: ActivityLoginBinding
    val TAG = "Login";

    //AUTH
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //INITIALIZE
        auth = Firebase.auth

        //LISTENNERS
        setListenners()
    }

    //LIFECYCLE
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }

    private fun updateUI(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val intent = Intent(this, MapActivity::class.java)
            intent.putExtra("user", currentUser.email)
            startActivity(intent)
            Toast.makeText(this, "ya inicio: " + currentUser.email, Toast.LENGTH_SHORT).show()
        } else {
            binding.email.setText("")
            binding.password.setText("")
            Toast.makeText(this, "NO EXISTE", Toast.LENGTH_SHORT).show()
        }
    }

    //LISTENNERS
    private fun setListenners(){
        binding.btnLogin.setOnClickListener {
            signInUser(binding.email.text.toString(), binding.password.text.toString())
        }

        binding.btnSignUp.setOnClickListener { startActivity(Intent(this, SignUpActivity::class.java)) }
    }

    private fun signInUser(email: String, password: String){
        if(validate() && isEmailValid(email)){
            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI
                        Log.d(TAG, "signInWithEmail:success:")
                        val user = auth.currentUser
                        updateUI(auth.currentUser)
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                        updateUI(null)
                    }
                }
        }else{
            Toast.makeText(this, "Invalid fields.", Toast.LENGTH_SHORT).show()
        }
    }
    private fun validate(): Boolean {
        var valid = true
        if (TextUtils.isEmpty(binding.email.text.toString())) {
            binding.email.error = "Required."
            valid = false
        } else {
            binding.email.error = null
        }
        if (TextUtils.isEmpty(binding.password.text.toString())) {
            binding.password.error = "Required."
            valid = false
        } else {
            binding.password.error = null
        }
        return valid
    }

    private fun isEmailValid(email: String): Boolean {
        if (!email.contains("@") ||
            !email.contains(".") ||
            email.length < 5)
            return false
        return true
    }
}