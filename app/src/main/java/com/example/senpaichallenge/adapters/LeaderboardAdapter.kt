package com.example.senpaichallenge.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.senpaichallenge.R
import com.example.senpaichallenge.models.UserModel
import com.example.senpaichallenge.ui.profile.UserProfileActivity
import de.hdodenhof.circleimageview.CircleImageView

class LeaderboardAdapter(private val users: List<UserModel>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_TOP3 = 0
    private val VIEW_TYPE_NORMAL = 1

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_TOP3 else VIEW_TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_TOP3) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_leaderboard_top3, parent, false)
            Top3ViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_leaderboard_normal, parent, false)
            NormalViewHolder(view)
        }
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is Top3ViewHolder) {
            val top3 = users.take(3) // Top 3 users
            holder.bind(top3)
        } else if (holder is NormalViewHolder) {
            val user = users[position]
            holder.bind(user, position + 3) // Other Ranker
        }
    }

    // ---------------- ViewHolders ----------------

    class Top3ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgFirst: CircleImageView = itemView.findViewById(R.id.imgFirst)
        private val imgSecond: CircleImageView = itemView.findViewById(R.id.imgSecond)
        private val imgThird: CircleImageView = itemView.findViewById(R.id.imgThird)

        private val tvFirstName: TextView = itemView.findViewById(R.id.tvFirstName)
        private val tvSecondName: TextView = itemView.findViewById(R.id.tvSecondName)
        private val tvThirdName: TextView = itemView.findViewById(R.id.tvThirdName)

        private val tvFirstPoints: TextView = itemView.findViewById(R.id.tvFirstPoints)
        private val tvSecondPoints: TextView = itemView.findViewById(R.id.tvSecondPoints)
        private val tvThirdPoints: TextView = itemView.findViewById(R.id.tvThirdPoints)

        fun bind(top3: List<UserModel>) {
            if (top3.size > 0) {
                val u = top3[0]
                tvFirstName.text = u.animeId
                tvFirstPoints.text = "${u.points} pts"
                val resId = itemView.context.resources.getIdentifier(u.avatar, "drawable", itemView.context.packageName)
                imgFirst.setImageResource(resId)

                itemView.findViewById<View>(R.id.cardFirst)?.setOnClickListener {
                    openProfile(itemView, u, 1)
                }
            }
            if (top3.size > 1) {
                val u = top3[1]
                tvSecondName.text = u.animeId
                tvSecondPoints.text = "${u.points} pts"
                val resId = itemView.context.resources.getIdentifier(u.avatar, "drawable", itemView.context.packageName)
                imgSecond.setImageResource(resId)

                itemView.findViewById<View>(R.id.cardSecond)?.setOnClickListener {
                    openProfile(itemView, u, 2)
                }
            }
            if (top3.size > 2) {
                val u = top3[2]
                tvThirdName.text = u.animeId
                tvThirdPoints.text = "${u.points} pts"
                val resId = itemView.context.resources.getIdentifier(u.avatar, "drawable", itemView.context.packageName)
                imgThird.setImageResource(resId)

                itemView.findViewById<View>(R.id.cardThird)?.setOnClickListener {
                    openProfile(itemView, u, 3)
                }
            }
        }

        private fun openProfile(view: View, user: UserModel, rank: Int) {
            val context = view.context
            val intent = Intent(context, UserProfileActivity::class.java)
            intent.putExtra("uid", user.uid) // ✅ now passing uid
            intent.putExtra("username", user.username)
            intent.putExtra("animeId", user.animeId)
            intent.putExtra("avatar", user.avatar)
            intent.putExtra("bio", user.bio)
            intent.putExtra("rank", rank)
            intent.putExtra("points", user.points)
            context.startActivity(intent)
        }
    }

    class NormalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgAvatar: CircleImageView = itemView.findViewById(R.id.imgAvatar)
        private val tvRank: TextView = itemView.findViewById(R.id.tvRank)
        private val tvName: TextView = itemView.findViewById(R.id.tvName)
        private val tvPoints: TextView = itemView.findViewById(R.id.tvPoints)

        fun bind(user: UserModel, rank: Int) {
            tvRank.text = rank.toString()
            tvName.text = user.animeId
            tvPoints.text = "${user.points} pts"

            val resId = itemView.context.resources.getIdentifier(user.avatar, "drawable", itemView.context.packageName)
            imgAvatar.setImageResource(resId)

            // 🔹 OnClick → open UserProfileActivity
            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, UserProfileActivity::class.java)
                intent.putExtra("uid", user.uid) // ✅ now passing uid
                intent.putExtra("username", user.username)
                intent.putExtra("animeId", user.animeId)
                intent.putExtra("avatar", user.avatar)
                intent.putExtra("bio", user.bio)
                intent.putExtra("rank", rank)
                intent.putExtra("points", user.points)
                context.startActivity(intent)
            }
        }
    }
}
