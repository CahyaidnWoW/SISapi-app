package com.latihan.si_sapimobile.helpers

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.latihan.si_sapimobile.R
import com.latihan.si_sapimobile.ui.HistoryActivity
import com.latihan.si_sapimobile.ui.MainActivity
import com.latihan.si_sapimobile.ui.ProfileActivity

object BottomNav {
    const val TAB_HOME = "home"
    const val TAB_HISTORY = "history"
    const val TAB_PROFILE = "profile"

    fun setup(activity: Activity, activeTab: String) {
        val tabs = mapOf(
            TAB_HISTORY to Tab(
                activity.findViewById(R.id.navHistory),
                activity.findViewById(R.id.circleHistory),
                activity.findViewById(R.id.icNavHistory),
                activity.findViewById(R.id.tvNavHistory)
            ),
            TAB_HOME to Tab(
                activity.findViewById(R.id.navHome),
                activity.findViewById(R.id.circleHome),
                activity.findViewById(R.id.icNavHome),
                activity.findViewById(R.id.tvNavHome)
            ),
            TAB_PROFILE to Tab(
                activity.findViewById(R.id.navProfile),
                activity.findViewById(R.id.circleProfile),
                activity.findViewById(R.id.icNavProfile),
                activity.findViewById(R.id.tvNavProfile)
            )
        )

        val activeText = ContextCompat.getColor(activity, R.color.royal)
        val inactiveText = ContextCompat.getColor(activity, R.color.muted)
        val white = ContextCompat.getColor(activity, android.R.color.white)

        // reset semua ke inactive
        tabs.values.forEach { tab ->
            tab.circle.setBackgroundResource(0)               // transparan
            tab.icon.setColorFilter(inactiveText)
            tab.label.setTextColor(inactiveText)
            tab.label.setTypeface(null, Typeface.NORMAL)
        }

        // aktifkan tab
        tabs[activeTab]?.let { tab ->
            tab.circle.setBackgroundResource(R.drawable.bg_tab_active)
            tab.icon.setColorFilter(white)                    // ikon putih di atas biru
            tab.label.setTextColor(activeText)
            tab.label.setTypeface(null, Typeface.BOLD)
        }

        tabs[TAB_HISTORY]?.root?.setOnClickListener {
            if (activeTab != TAB_HISTORY) go(activity, HistoryActivity::class.java)
        }
        tabs[TAB_HOME]?.root?.setOnClickListener {
            if (activeTab != TAB_HOME) go(activity, MainActivity::class.java)
        }
        tabs[TAB_PROFILE]?.root?.setOnClickListener {
            if (activeTab != TAB_PROFILE) go(activity, ProfileActivity::class.java)
        }
    }

    private fun go(activity: Activity, target: Class<*>) {
        activity.startActivity(
            Intent(activity, target)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
    }

    private data class Tab(val root: View, val circle: View, val icon: ImageView, val label: TextView)
}