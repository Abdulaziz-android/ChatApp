package com.example.chatapp.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.chatapp.MessageActivity
import com.example.chatapp.R
import com.example.chatapp.adapters.GroupAdapter
import com.example.chatapp.adapters.UserAdapter
import com.example.chatapp.databinding.FragmentPagerBinding
import com.example.chatapp.databinding.ItemDialogBinding
import com.example.chatapp.models.Group
import com.example.chatapp.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val ARG_PARAM1 = "param1"

class PagerFragment : Fragment() {

    private var param1: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }
    }

    private var _binding: FragmentPagerBinding? = null
    private val binding get() = _binding!!
    lateinit var auth: FirebaseAuth
    lateinit var firebaseDatabase: FirebaseDatabase
    lateinit var reference: DatabaseReference
    lateinit var userAdapter: UserAdapter
    lateinit var groupAdapter: GroupAdapter
    val list = ArrayList<User>()
    val listGr = ArrayList<Group>()
    private var status = "online"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPagerBinding.inflate(layoutInflater, container, false)

        auth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()

        if (param1 == "Chats") {
            getChats()
        } else getGroups()


        return binding.root
    }


    private fun getChats() {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        val displayName = currentUser?.displayName
        val photoUrl = currentUser?.photoUrl.toString()
        val phoneNumber = currentUser?.phoneNumber
        val email = currentUser?.email
        val user = User(uid, displayName, photoUrl, phoneNumber, email, status)

        reference = firebaseDatabase.getReference("users")

        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                list.clear()
                val filterUser = arrayListOf<User>()
                snapshot.children.forEach {
                    val value = it.getValue(User::class.java)

                    if (value != null && user.uid != value.uid) {
                        list.add(value)
                    }
                    if (value != null && value.uid == user.uid) {
                        filterUser.add(value)
                    }
                }
                if (filterUser.isEmpty()) {
                    reference.child(currentUser?.uid!!).setValue(user)
                }

                userAdapter = UserAdapter(list, object : UserAdapter.OnItemClickListener {
                    override fun OnItemClick(user: User) {
                        val intent = Intent(binding.root.context, MessageActivity::class.java)
                        val bundle = Bundle()
                        bundle.putSerializable("userid", user.uid)
                        intent.putExtras(bundle)
                        startActivity(intent)
                    }
                })
                binding.recyclerView.adapter = userAdapter
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

    }


    private fun getGroups() {
        binding.fab.visibility = View.VISIBLE
        binding.fab.setOnClickListener {
            val alertDialog = AlertDialog.Builder(requireContext()).create()
            val itemDialog = ItemDialogBinding.inflate(layoutInflater)
            itemDialog.addBtn.setOnClickListener {
                if (itemDialog.groupNameEt.text.toString().isNotEmpty()){
                    val name = itemDialog.groupNameEt.text.toString()
                    val key = reference.push().key!!
                    val group = Group(name, key)
                    firebaseDatabase.getReference("groups").child(key).setValue(group)
                    alertDialog.dismiss()
                }
            }
            alertDialog.setView(itemDialog.root)
            alertDialog.show()
        }

        reference = firebaseDatabase.getReference("groups")
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.children.firstOrNull() != null) {
                    listGr.clear()
                    snapshot.children.forEach {
                        val value = it.getValue(Group::class.java)
                        if (value != null) {
                            listGr.add(value)
                        }

                    }

                    groupAdapter = GroupAdapter(listGr, object : GroupAdapter.OnClickListener {
                        override fun OnClick(group: Group) {
                            val intent = Intent(binding.root.context, MessageActivity::class.java)
                            val bundle = Bundle()
                            bundle.putString("group", group.key)
                            intent.putExtras(bundle)
                            startActivity(intent)
                            //findNavController().navigate(R.id.action_homeFragment_to_messageFragment,bundle)

                        }
                    })
                    binding.recyclerView.adapter = groupAdapter
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    companion object {

        @JvmStatic
        fun newInstance(param1: String) =
            PagerFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                }
            }
    }

    fun setStatus(sts: String) {
        if (param1 == "Chats") {
            val hashMap: HashMap<String, Any> = HashMap()
            hashMap.put("status", sts)
            reference.child(auth.currentUser?.uid!!).updateChildren(hashMap)
        }
    }

    override fun onStart() {
        super.onStart()
        setStatus("online")
    }

    override fun onDestroy() {
        super.onDestroy()
        setStatus("offline")
    }

}