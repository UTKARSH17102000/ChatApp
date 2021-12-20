package com.example.chat_app_kotlin.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.chat_app_kotlin.*
import com.example.chat_app_kotlin.models.User
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_chats.*

private const val DELETE_VIEW_TYPE = 1
private const val NORMAL_VIEW_TYPE = 2

class PeopleFragment : Fragment() {

    lateinit var mAdapter:FirestorePagingAdapter<User,RecyclerView.ViewHolder>

    val auth by lazy{
        FirebaseAuth.getInstance()
    }

    val database by lazy {
        FirebaseFirestore.getInstance().collection("users")
            .orderBy("name", Query.Direction.ASCENDING)
    }

    override fun onCreateView(
        inflater: LayoutInflater,container:ViewGroup?,
        savedInstanceState: Bundle?): View? {
        setupAdapter()
        return  inflater.inflate(R.layout.fragment_chats,container,false)
    }

    private fun setupAdapter() {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setPageSize(10)
            .setPrefetchDistance(2)
            .build()

        val options = FirestorePagingOptions.Builder<User>()
            .setLifecycleOwner(this)
            .setQuery(database,config, User::class.java)
            .build()

        mAdapter = object : FirestorePagingAdapter<User,RecyclerView.ViewHolder>(options){
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val inflater = layoutInflater
                return when(viewType){
                    NORMAL_VIEW_TYPE-> UserViewHolder(layoutInflater.inflate(R.layout.list_item,parent,false))
                  else -> EmptyViewHolder(layoutInflater.inflate(R.layout.empty_view,parent,false))
                }
            }

            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: User) {
                   if(holder is UserViewHolder){
                       holder.bind(user = model){ name:String,photo:String,id:String ->
                           startActivity(
                                   ChatActivity.createChatActivity(
                                           requireContext(),
                                           id,
                                           name,
                                           photo
                                   )
                           )

                       }

                   }else{
                      //TODO - Something
                   }
                   }

            override fun onLoadingStateChanged(state: LoadingState) {
                super.onLoadingStateChanged(state)
                when(state){
                    LoadingState.ERROR ->{
                        Toast.makeText(
                                requireContext(),
                                "Error Occurred!",
                                Toast.LENGTH_SHORT
                        ).show()
                    }
                    LoadingState.LOADING_INITIAL ->{ }
                    LoadingState.LOADING_MORE ->{ }
                    LoadingState.LOADED ->{ }
                    LoadingState.FINISHED ->{ }

                }
            }

            override fun getItemViewType(position: Int): Int {
                val item = getItem(position)?.toObject(User::class.java)
                return if(auth.uid == item!!.uid){
                    DELETE_VIEW_TYPE
                }else{
                    NORMAL_VIEW_TYPE
                }
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }
    }
}


/**
 * You have 1000 users
 *1 user -10kb
 *10*1000 = 10000kb - You may get a timeout
 * Pagination - getting data in pages
 */