package com.example.tallermovil_ii

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
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
            if(validate()){

            }else{

            }
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

}