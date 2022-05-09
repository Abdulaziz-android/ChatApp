package com.example.chatapp

import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.adapters.MessageAdapter
import com.example.chatapp.databinding.ActivityMessageBinding
import com.example.chatapp.models.Group
import com.example.chatapp.models.Message
import com.example.chatapp.models.User
import com.example.chatapp.notifications.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class MessageActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var messageAdapter: MessageAdapter
    private var isUser = true
    private var user: User? = null
    private var group: Group? = null
    private var groupKey: String? = null

    //change
    private lateinit var apiService: ApiService
    private var notify = false
    private var userid: String? = null

    private lateinit var sPref: SharedPreferences

    private lateinit var binding: ActivityMessageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        apiService = ApiClient.getRetrofit("https://fcm.googleapis.com/")
            .create(ApiService::class.java)

        sPref = getSharedPreferences("shared", MODE_PRIVATE)

        val bundle = intent.extras!!
        if (bundle.getString("userid") != null) {
            userid = bundle.getString("userid")
            getUser()
        } else {
            groupKey = bundle.getString("group")
            getGroup()
            isUser = false
        }

        if (bundle.getBoolean("notification") != null) {
            checkNotification(bundle)
        }

        binding.backIv.setOnClickListener {
            onBackPressed()
        }

        binding.sendBtn.setOnClickListener {
            val text = binding.messageEt.text.toString()
            if (text.isNotEmpty()) {
                notify = true
                val simpleFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.ENGLISH)
                val date = simpleFormat.format(Date())
                val message = Message(text, date, auth.currentUser?.uid)
                val key = reference.push().key!!
                if (isUser) {
                    reference.child("${auth.currentUser?.uid}/messages/${user?.uid!!}/${key}")
                        .setValue(message)
                    reference.child("${user?.uid!!}/messages/${auth.currentUser?.uid!!}/${key}")
                        .setValue(message)
                } else {
                    reference = firebaseDatabase.getReference("groups")
                    reference.child("${groupKey}/messages/${key}")
                        .setValue(message)
                }


                //send notification
                reference = FirebaseDatabase.getInstance().getReference("users")
                reference.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (isUser) {
                            var userChild: User? = null
                            snapshot.children.forEach {
                                val value = it.getValue(User::class.java)
                                if (user?.uid == value?.uid) {
                                    userChild = value
                                }
                            }
                            if (notify) {
                                sendNotification(
                                    userChild?.uid,
                                    auth.currentUser?.displayName,
                                    text,
                                    ""
                                )
                                notify = false
                            }
                        } else {
                            if (notify) {
                                snapshot.children.forEach {
                                    val value = it.getValue(User::class.java)
                                    if (auth.currentUser?.uid != value?.uid)
                                        sendNotification(
                                            value?.uid,
                                            auth.currentUser?.displayName,
                                            text, groupKey!!
                                        )
                                }
                                notify = false
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })

            }
            binding.messageEt.text.clear()
        }

        getMessages()

        checkCurrentUser()
    }


    private fun getMessages() {
        if (isUser) {
            reference.child("${auth.currentUser!!.uid}/messages/${userid}")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list = arrayListOf<Message>()
                        val children = snapshot.children
                        children.forEach {
                            val value = it.getValue(Message::class.java)
                            if (value != null) {
                                list.add(value)
                            }
                        }

                        messageAdapter =
                            MessageAdapter(
                                list,
                                auth.currentUser!!.uid,
                                false,
                                binding.root.context
                            )
                        binding.recyclerView.adapter = messageAdapter
                        binding.recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val children = snapshot.children
                    children.forEach {
                        val value = it.getValue(User::class.java)
                        if (value != null) {
                            if (value.uid == user?.uid) {
                                if (value.status == "online") {
                                    binding.userStatusTv.setTextColor(Color.parseColor("#2675EC"))
                                } else {
                                    binding.userStatusTv.setTextColor(Color.parseColor("#131313"))
                                }
                                binding.userStatusTv.text = value.status
                            }
                        }
                    }

                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        } else {
            reference = firebaseDatabase.getReference("groups")
            reference.child("${groupKey}/messages")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val list = arrayListOf<Message>()
                        val children = snapshot.children
                        children.forEach {
                            val value = it.getValue(Message::class.java)
                            if (value != null) {
                                list.add(value)
                            }
                        }

                        messageAdapter =
                            MessageAdapter(list, auth.currentUser!!.uid, true, this@MessageActivity)
                        binding.recyclerView.adapter = messageAdapter
                        binding.recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })
        }
    }

    private fun getUser() {
        reference = firebaseDatabase.getReference("users")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val value = it.getValue(User::class.java)
                    if (userid == value!!.uid) {
                        user = value
                    }
                }
                if (user != null) {
                    Picasso.get().load(user?.photoUrl).into(binding.userIv)
                    binding.userNameTv.text = user?.displayName
                    binding.userStatusTv.text = user?.status
                    isUser = true
                } else {
                    groupKey = intent.extras?.getString("group")
                    //null ni kast qilayapti notification dan kirganda!!!
                    getGroup()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
        if (user != null)
            getMessages()

    }

    private fun getGroup() {
        isUser = false
        reference = firebaseDatabase.getReference("groups")
        reference.child("$groupKey")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    group = snapshot.getValue(Group::class.java)
                    val list = arrayListOf<Message>()
                    val children = snapshot.child("messages").children
                    children.forEach {
                        val value = it.getValue(Message::class.java)
                        if (value != null) {
                            list.add(value)
                        }
                    }
                    messageAdapter =
                        MessageAdapter(list, auth.currentUser!!.uid, true, this@MessageActivity)
                    binding.recyclerView.adapter = messageAdapter
                    binding.recyclerView.scrollToPosition(messageAdapter.itemCount - 1)

                    binding.userNameTv.text = group?.name
                    binding.cardView.visibility = View.GONE
                    binding.userStatusTv.visibility = View.GONE
                    isUser = false
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
        //  group = arguments?.getSerializable("group") as Group
        getMessages()
    }

    private fun checkNotification(bundle: Bundle) {
        val isNotification = bundle.getBoolean("notification")
        val notificationID = bundle.getInt("notiID")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val oreoNotification = OreoNotification(this)
            oreoNotification.getManager().cancel(notificationID)
        } else {
            val noti =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            noti.cancel(notificationID)
        }

    }

    private fun sendNotification(
        receiverUID: String?,
        currentUserName: String?,
        msg: String?,
        groupkey: String,
    ) {
        val tokens: DatabaseReference = FirebaseDatabase.getInstance().getReference("Tokens")
        val query = tokens.orderByKey().equalTo(receiverUID)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val mtitle = if (isUser) "New Message:" else "${group?.name!!}:"
                    val token = it.getValue(Token::class.java)
                    val data = Data(
                        auth.currentUser!!.uid,
                        R.mipmap.ic_launcher,
                        currentUserName.toString() + ": " + msg,
                        title = mtitle,
                        user?.uid.toString(), groupKey, System.currentTimeMillis().toInt()
                    )

                    val sender = Sender(data, token!!.token)
                    apiService.sendNotification(sender)
                        .enqueue(object : Callback<MyResponce> {
                            override fun onResponse(
                                call: Call<MyResponce>,
                                response: Response<MyResponce>,
                            ) {
                                if (response.code() == 200) {
                                    if (response.body()?.success != 1) {
                                        Toast.makeText(
                                            this@MessageActivity,
                                            "Failed!",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    }
                                }
                            }

                            override fun onFailure(call: Call<MyResponce>, t: Throwable) {

                                Toast.makeText(
                                    this@MessageActivity,
                                    "Failure!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                            }

                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    override fun onResume() {
        super.onResume()
        checkCurrentUser()
    }

    override fun onPause() {
        super.onPause()
        sPref.edit().putString("current", "null").apply()
    }

    private fun checkCurrentUser() {
        if (isUser)
            sPref.edit().putString("current", userid).apply()
        else {
            sPref.edit().putString("current", groupKey).apply()
        }
    }

}