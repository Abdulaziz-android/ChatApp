package com.example.chatapp.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.get
import com.example.chatapp.R
import com.example.chatapp.adapters.PagerAdapter
import com.example.chatapp.databinding.FragmentHomeBinding
import com.example.chatapp.databinding.ItemTabBinding
import com.example.chatapp.notifications.Token
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    lateinit var pagerAdapter: PagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        val list: List<String> = arrayOf("Chats", "Groups").toList()
        pagerAdapter = PagerAdapter(requireActivity(), list)
        binding.viewpager.adapter = pagerAdapter

        TabLayoutMediator(binding.tablayout, binding.viewpager) { tab, position ->
            tab.text = list[position]
            binding.viewpager.setCurrentItem(tab.position, true)
        }.attach()

        val count: Int = binding.tablayout.tabCount
        for (i in 0 until count) {
            val tabView = ItemTabBinding.inflate(layoutInflater)
            tabView.tabTitle.text = list[i]
            if (i == 0) {
                tabView.tabTitle.setTextColor(Color.parseColor("#ffffff"))
                tabView.layout.setBackgroundColor(Color.parseColor("#2675EC"))
            }
            binding.tablayout.getTabAt(i)?.customView = tabView.root
        }

        binding.tablayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val tabView = tab.customView!!
                val layout = tabView.findViewById<LinearLayout>(R.id.layout)
                val textView = tabView.findViewById<TextView>(R.id.tab_title)
                textView.setTextColor(Color.parseColor("#ffffff"))
                layout.setBackgroundColor(Color.parseColor("#2675EC"))
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val tabView = tab.customView!!
                val layout = tabView.findViewById<LinearLayout>(R.id.layout)
                val textView = tabView.findViewById<TextView>(R.id.tab_title)

                textView.setTextColor(Color.parseColor("#858585"))
                layout.setBackgroundColor(Color.parseColor("#E5E5E5"))
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })


        instanceFirebaseMessaging()
        return binding.root
    }


    @SuppressLint("StringFormatInvalid")
    private fun instanceFirebaseMessaging() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
              //  Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            if (token != null) {
                updateToken(token)
            }


            })
    }


    private fun updateToken(token: String) {
        val reference = FirebaseDatabase.getInstance().getReference("Tokens")
        val token1 = Token(token)
        reference.child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(token1)
    }

}