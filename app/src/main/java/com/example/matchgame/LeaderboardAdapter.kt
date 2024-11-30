package com.example.matchgame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LeaderboardAdapter(
    private var leaderboardEntries: List<LeaderboardEntry>

) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmail: TextView = itemView.findViewById(R.id.tvEmail)
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        val tvDifficulty: TextView = itemView.findViewById(R.id.tvDifficulty)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = leaderboardEntries[position]
        holder.tvEmail.text = entry.email
        holder.tvScore.text = "${entry.score}"
        holder.tvDifficulty.text = entry.difficulty
    }

    override fun getItemCount(): Int {
        return leaderboardEntries.size
    }


    // Updates Leaderboard data with new list of scores
    fun updateLeaderboardList(newList: List<LeaderboardEntry>) {
        leaderboardEntries = newList
        notifyDataSetChanged()
    }
}