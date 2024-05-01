package com.example.tallermovil_ii

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.tallermovil_ii.model.User
import com.squareup.picasso.Picasso


class UsersAdapter (private val context: Context?, private val users: List<User>): BaseAdapter() {
    override fun getCount(): Int {
        return users.size
    }

    override fun getItem(position: Int): User {
        return users[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong() // You can use a unique ID if available
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.user_view, parent, false)

        val user = getItem(position)
        val name = view.findViewById<TextView>(R.id.username)
        val photo = view.findViewById<ImageView>(R.id.photo)
        name.text = user.name

        if (context != null) {
            Picasso.get()
                .load(user.profilePhoto)
                .resize(500, 500) // Reemplaza esto con las dimensiones que desees
                .centerCrop() // O usa fitCenter() si prefieres
                .into(photo)
        }

        return view
    }
}