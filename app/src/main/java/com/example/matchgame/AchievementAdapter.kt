package com.example.matchgame

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.matchgame.models.Achievement

class AchievementAdapter(private val achievements: List<Achievement>,
                         private val context: Context) :
    RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    // ViewHolder for holding references to views for each achievement item
    class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val achievementName: TextView = itemView.findViewById(R.id.achievement_name)
        val achievementStatus: TextView = itemView.findViewById(R.id.achievement_status)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.achievement_card, parent, false)
        return AchievementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]

        // Set the achievement name
        holder.achievementName.text = achievement.name

        // Set the achievement status (Unlocked/Locked)
        if (achievement.isUnlocked) {
            holder.achievementStatus.text = context.getString(R.string.achievement_status_unlocked)
            holder.achievementStatus.setTextColor(Color.GREEN)
        } else {
            holder.achievementStatus.text = context.getString(R.string.achievement_status_locked)
            holder.achievementStatus.setTextColor(Color.RED)
        }
    }

    override fun getItemCount(): Int {
        return achievements.size
    }
}
