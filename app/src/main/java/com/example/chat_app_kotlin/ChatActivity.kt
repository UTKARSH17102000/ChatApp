package com.example.chat_app_kotlin

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chat_app_kotlin.adapters.ChatAdapter
import com.example.chat_app_kotlin.models.User
import com.example.chat_app_kotlin.models.Inbox
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.EmojiPopup
import com.vanniktech.emoji.google.GoogleEmojiProvider
import kotlinx.android.synthetic.main.activity_chat.*

const val UID = "uid"
const val NAME = "name"
const val IMAGE = "photo"
class ChatActivity : AppCompatActivity() {

    private val friendid by lazy {
        intent.getStringExtra(UID)
    }

    private val name by lazy {
        intent.getStringExtra(NAME)
    }

    private val image by lazy {
        intent.getStringExtra(IMAGE)
    }

    private val mCurrentUid by lazy {
        FirebaseAuth.getInstance().uid!!
    }

    private val db by lazy {
        FirebaseDatabase.getInstance()
    }

    lateinit var currentUser: User
    private val messages = mutableListOf<ChatEvent>()
 lateinit var chatAdapter: ChatAdapter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(GoogleEmojiProvider())
        setContentView(R.layout.activity_chat)


        FirebaseFirestore.getInstance().collection("users").document(mCurrentUid).get()
                .addOnSuccessListener {
                    currentUser = it.toObject(User::class.java)!!
                }
        chatAdapter = ChatAdapter(messages,mCurrentUid)
        msgRv.apply {
        layoutManager = LinearLayoutManager(this@ChatActivity)
            adapter = chatAdapter
        }
        nameTv.text = name
        Picasso.get().load(image).into(userImgView)

        val emojiPopup = EmojiPopup.Builder.fromRootView(rootView).build(msgEdtv)
        smileBtn.setOnClickListener {
            emojiPopup.toggle()
        }
        listenToMessages()

        sendBtn.setOnClickListener {
            msgEdtv.text?.let {
                if(it.isNotEmpty()){
                    sendMessage(it.toString())
                    it.clear()
                }
            }
        }
        updateReadCount()



    }

    private fun updateReadCount() {
        getInbox(mCurrentUid,friendid.toString()).child("count").setValue(0)
    }

    private fun listenToMessages(){
        getMessages(friendid.toString())
                .orderByKey()
                .addChildEventListener( object: ChildEventListener{
                    override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                        val msg = snapshot.getValue(Message::class.java)!!
                        addMessage(msg)
                    }

                    override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildRemoved(snapshot: DataSnapshot) {
                        TODO("Not yet implemented")
                    }

                    override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                        TODO("Not yet implemented")
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
    }

    private fun addMessage(msg: Message) {
            val eventBefore = messages.lastOrNull()
        if((eventBefore!= null && !eventBefore.sentAt.isSameDayAs(msg.sentAt)) || eventBefore == null){
            messages.add(
                    DateHeader(
                            msg.sentAt,context = this
                    )
            )
        }
            messages.add(msg)
        chatAdapter.notifyItemInserted(messages.size)
       msgRv.scrollToPosition(messages.size+1)
    }

    private fun sendMessage(msg: String) {
         val id = getMessages(friendid.toString()).push().key //- uniqueKey
       checkNotNull(id){"cannot be null"}
        val msgMap = Message(msg,mCurrentUid,id)
        getMessages(friendid.toString()).child(id).setValue(msgMap).addOnSuccessListener {
            Log.i("CHATS","completed")
        }.addOnFailureListener {
            Log.i("CHATS",it.localizedMessage)
        }

        updateLastMessage(msgMap)
    }

    private fun updateLastMessage(message: Message) {
       val InboxMap = Inbox(
               message.msg,
               friendid.toString(),
               name.toString(),
               image.toString(),
               count =0
       )
        getInbox(mCurrentUid,friendid.toString()).setValue(InboxMap).addOnSuccessListener {
            getInbox(friendid.toString(),mCurrentUid).addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val value = snapshot.getValue(Inbox::class.java)

                    InboxMap.apply {
                        from = message.senderId
                        name = currentUser.name
                        image = currentUser.thumbImage
                        count =1
                    }
                    value?.let{
                        if(it.from == message.senderId){
                            InboxMap.count = value.count+1
                        }
                    }

                    getInbox(mCurrentUid,friendid.toString()).setValue(InboxMap)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        }
    }

    private fun markAsRead(){
        getInbox(friendid.toString(),mCurrentUid).child("count").setValue(0)
    }

    private fun getMessages(friendId: String) =
            db.reference.child("messages/${getId(friendId)}")
    private fun getInbox(toUser:String,fromUser:String) =
            db.reference.child("chats/${toUser}/$fromUser")



    private fun getId(friendId:String):String{// Id for the messages
        return if(friendId>mCurrentUid){
            mCurrentUid+friendId
        }else{
            friendId+mCurrentUid
        }
    }



    companion object {

        fun createChatActivity(context: Context, id: String, name: String, image: String): Intent {
            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra(UID, id)
            intent.putExtra(NAME, name)
            intent.putExtra(IMAGE, image)
            return intent
        }
    }
}


