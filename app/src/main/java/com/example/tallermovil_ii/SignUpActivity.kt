package com.example.tallermovil_ii

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.example.tallermovil_ii.databinding.ActivityLoginBinding
import com.example.tallermovil_ii.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //SET LISTENNERS
        setListenners()
    }

    //SET LISTENNERS
    private fun setListenners(){
        binding.btnSignUp.setOnClickListener {
            if(validate() && checkCredentials()){

            }else{
                Toast.makeText(this, "Invalid fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkCredentials(): Boolean{
        var valid = true

        if (TextUtils.isEmpty(binding.email.text.toString())) {
            binding.email.error = "Invalid format."
            valid = false
        } else {
            binding.email.error = null
        }

        if (TextUtils.isEmpty(binding.password.text.toString())) {
            binding.password.error = "Insecure password."
            valid = false
        } else {
            binding.password.error = null
        }

        if (TextUtils.isEmpty(binding.identification.text.toString())) {
            binding.identification.error = "Identification must be a number."
            valid = false
        } else {
            binding.identification.error = null
        }

        return valid
    }

    private fun validate(): Boolean {
        var valid = true
        if (TextUtils.isEmpty(binding.name.text.toString())) {
            binding.name.error = "Required."
            valid = false
        } else {
            binding.name.error = null
        }

        if (TextUtils.isEmpty(binding.lastName.text.toString())) {
            binding.lastName.error = "Required."
            valid = false
        } else {
            binding.lastName.error = null
        }

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

        if (TextUtils.isEmpty(binding.identification.text.toString())) {
            binding.identification.error = "Required."
            valid = false
        } else {
            binding.identification.error = null
        }
        return valid
    }

}