package com.example.senpaichallenge.adapters

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.models.ChatUser
import de.hdodenhof.circleimageview.CircleImageView

class ChatListAdapter(
    private val users: List<ChatUser>,
    private val onClick: (ChatUser) -> Unit
) : RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgAvatar: CircleImageView = itemView.findViewById(R.id.imgAvatar)
        val tvUsername: TextView = itemView.findViewById(R.id.tvUsername)
        val tvLastMessage: TextView = itemView.findViewById(R.id.tvLastMessage)
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val dotUnread: ImageView = itemView.findViewById(R.id.dotUnread)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_user, parent, false)
        return ChatViewHolder(view)
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val user = users[position]

        // 🔹 Username bold
        holder.tvUsername.text = user.username

        // 🔹 Last message gray
        holder.tvLastMessage.text = user.lastMessage

        // 🔹 Show last message time
        if (user.lastSeen > 0) {
            holder.tvTime.text = DateUtils.getRelativeTimeSpanString(user.lastSeen)
        } else {
            holder.tvTime.text = ""
        }

        // 🔹 Unread dot
        holder.dotUnread.visibility = if (user.unreadCount > 0) View.VISIBLE else View.GONE

        // 🔹 Avatar load
        val resId = holder.itemView.context.resources.getIdentifier(
            user.avatar, "drawable", holder.itemView.context.packageName
        )
        if (resId != 0) {
            holder.imgAvatar.setImageResource(resId)
        } else {
            holder.imgAvatar.setImageResource(R.drawable.avatar1)
        }

        // 🔹 OnClick → open chat
        holder.itemView.setOnClickListener {
            onClick(user)
        }
    }
}
