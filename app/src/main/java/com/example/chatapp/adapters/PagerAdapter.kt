package com.example.chatapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chatapp.fragments.PagerFragment

class PagerAdapter(fa:FragmentActivity, val list:List<String>) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = list.size

    override fun createFragment(position: Int): Fragment {
        return PagerFragment.newInstance(list[position])
    }
}