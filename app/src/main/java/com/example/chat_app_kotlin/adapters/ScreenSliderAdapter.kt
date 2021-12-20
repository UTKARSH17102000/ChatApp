package com.example.chat_app_kotlin.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.chat_app_kotlin.MainActivity
import com.example.chat_app_kotlin.fragments.ChatsFragment
import com.example.chat_app_kotlin.fragments.PeopleFragment

// adapter for the slider
class ScreenSliderAdapter(Fa: MainActivity) : FragmentStateAdapter(Fa) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
       return when(position){
            0 ->
                ChatsFragment()else -> PeopleFragment()
            }
        }
    }


