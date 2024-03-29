package com.example.chat_app_kotlin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import com.example.chat_app_kotlin.adapters.ScreenSliderAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    // this is the main activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)
        viewPager.adapter = ScreenSliderAdapter(this)

        TabLayoutMediator(tabs,viewPager,TabLayoutMediator.TabConfigurationStrategy{ tab: TabLayout.Tab, pos: Int ->
            when(pos){
                0->tab.text = "CHATS"
                1->tab.text = "PEOPLE"
            }
        }).attach()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}