package com.example.chatapp.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.ItemMessageFromBinding
import com.example.chatapp.databinding.ItemMessageToBinding
import com.example.chatapp.models.Message
import com.example.chatapp.models.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.LoadedFrom


class MessageAdapter(
    var list: List<Message>,
    val uid: String,
    val isGroup: Boolean,
    val context: Context
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class MessageFromVH(val itemFromBinding: ItemMessageFromBinding) :
        RecyclerView.ViewHolder(itemFromBinding.root) {
        fun onBind(message: Message) {
            itemFromBinding.textTv.text = message.text
            itemFromBinding.dateTv.text = message.date

        }

    }

    inner class MessageToVH(val itemToBinding: ItemMessageToBinding) :
        RecyclerView.ViewHolder(itemToBinding.root) {
        fun onBind(message: Message) {
            if (isGroup) {
                val fromUserId = message.fromUserId
                FirebaseDatabase.getInstance().getReference("users")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.children.firstOrNull() != null) {
                                snapshot.children.forEach {
                                    val value = it?.getValue(User::class.java)
                                    if (value?.uid == fromUserId) {
                                        Picasso.get()
                                            .load(value?.photoUrl)
                                            .into(object : Target {
                                                override fun onBitmapLoaded(
                                                    bitmap: Bitmap?,
                                                    from: LoadedFrom?
                                                ) {
                                                    val pixel: Int = bitmap!!.getPixel(10, 10)
                                                    val d: Drawable =
                                                        context.resources.getDrawable(R.drawable.message_back_to)
                                                    d.setColorFilter(pixel, PorterDuff.Mode.SRC_ATOP)
                                                    itemToBinding.layout.setBackground(d)

                                                    itemToBinding.textTv.text = message.text
                                                    itemToBinding.dateTv.text = message.date

                                                }

                                                override fun onBitmapFailed(
                                                    e: Exception?,
                                                    errorDrawable: Drawable?
                                                ) {

                                                }

                                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {

                                                }
                                            })
                                    }
                                }


                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }
                    })
            }
            else{
                itemToBinding.textTv.text = message.text
                itemToBinding.dateTv.text = message.date
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1) {
            return MessageFromVH(
                ItemMessageFromBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        } else return MessageToVH(
            ItemMessageToBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (getItemViewType(position) == 1) {
            val fromVh = holder as MessageFromVH
            fromVh.onBind(list[position])
        } else {
            val toVh = holder as MessageToVH
            toVh.onBind(list[position])
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (list[position].fromUserId == uid) {
            return 1
        } else return 2
    }

    override fun getItemCount(): Int = list.size


}