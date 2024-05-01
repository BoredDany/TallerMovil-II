package com.example.tallermovil_ii

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Toast
import com.example.tallermovil_ii.databinding.ActivityLoginBinding
import com.example.tallermovil_ii.databinding.ActivityUsersListBinding
import com.example.tallermovil_ii.model.DataBase
import com.example.tallermovil_ii.model.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UsersListActivity : AppCompatActivity() {
    lateinit var binding: ActivityUsersListBinding

    val TAG = "UsersDB";

    private val database = FirebaseDatabase.getInstance()
    private lateinit var myRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersListBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    private fun updateListView(userList:List<User>){
        val adapter = UsersAdapter(this, userList)

        binding.users.adapter = adapter
    }

    /*fun loadUsers() {
        myRef = database.getReference(DataBase.PATH_USER)
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (singleSnapshot in dataSnapshot.children) {
                    val myUser = singleSnapshot.getValue(User::class.java)
                    Log.i(TAG, "Encontró usuario: " + myUser?.name)
                    val name = myUser?.name
                    Toast.makeText(baseContext, "name: $name", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.w(TAG, "error en la consulta", databaseError.toException())
            }
        })
    }*/
}