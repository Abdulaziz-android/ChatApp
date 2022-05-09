package com.example.chatapp.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemUserBinding
import com.example.chatapp.models.Message
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class UserAdapter(val list: List<User>, val listener: OnItemClickListener) :
    RecyclerView.Adapter<UserAdapter.UserVH>() {

    inner class UserVH(val itemBinding: ItemUserBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        fun onBind(user: User) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            FirebaseDatabase.getInstance().getReference("users")
                .child("${currentUser!!.uid}/messages/${user.uid}")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    @SuppressLint("SimpleDateFormat")
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.children.firstOrNull() != null) {
                            val children = snapshot.children.last().getValue(Message::class.java)
                            itemBinding.lastMessageTv.text = children?.text

                            val lastMessageStr = children?.date
                            val dayFormat: DateFormat = SimpleDateFormat("dd.MM.yyyy")
                            val todayDate = dayFormat.parse(dayFormat.format(Date()))
                            val lastMessageDate: Date = dayFormat.parse(lastMessageStr)
                            
                            if (lastMessageDate < todayDate) {
                                itemBinding.timeMessageTv.text = children?.date?.take(10)
                            } else {
                                itemBinding.timeMessageTv.text = children?.date?.takeLast(5)
                            }

                        } else {
                            itemBinding.lastMessageTv.text = ""
                            itemBinding.timeMessageTv.text = ""
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }
                })

            Picasso.get().load(user.photoUrl).into(itemBinding.imageView)
            itemBinding.displayNameTv.text = user.displayName
            itemBinding.root.setOnClickListener {
                listener.OnItemClick(user)
            }
            if (user.status == "online") {
                itemBinding.indicatorIv.visibility = View.VISIBLE
            } else itemBinding.indicatorIv.visibility = View.GONE

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        return UserVH(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: UserVH, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

    interface OnItemClickListener {
        fun OnItemClick(user: User)
    }

}