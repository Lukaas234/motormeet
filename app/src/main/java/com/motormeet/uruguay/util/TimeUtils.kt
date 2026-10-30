package com.motormeet.uruguay.util

import android.text.format.DateUtils

object TimeUtils {
    fun formatElapsedTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        return when {
            diff < 60_000 -> "Hace un momento"
            diff < 3600_000 -> "Hace ${diff / 60_000} min"
            diff < 86400_000 -> "Hace ${diff / 3600_000} h"
            else -> DateUtils.getRelativeTimeSpanString(timestamp, now, DateUtils.DAY_IN_MILLIS).toString()
        }
    }
}
