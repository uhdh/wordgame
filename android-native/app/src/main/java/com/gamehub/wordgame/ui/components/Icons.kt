package com.gamehub.wordgame.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/** Maps the design's Material-Symbols icon-name strings (e.g. "rotate_right") to Compose icons. */
fun iconFor(name: String): ImageVector = when (name) {
    "rotate_right" -> Icons.Filled.RotateRight
    "shuffle" -> Icons.Filled.Shuffle
    "restart_alt" -> Icons.Filled.RestartAlt
    "check" -> Icons.Filled.Check
    "chevron_right" -> Icons.Filled.ChevronRight
    "dark_mode" -> Icons.Filled.DarkMode
    "settings" -> Icons.Filled.Settings
    "share" -> Icons.Filled.Share
    "help" -> Icons.Filled.HelpOutline
    "grid_view" -> Icons.Filled.GridView
    "volume_up" -> Icons.Filled.VolumeUp
    "expand_less" -> Icons.Filled.ExpandLess
    "calendar_today" -> Icons.Filled.CalendarToday
    "calendar_month" -> Icons.Filled.CalendarMonth
    "local_fire_department" -> Icons.Filled.LocalFireDepartment
    "workspace_premium" -> Icons.Filled.WorkspacePremium
    "emoji_events" -> Icons.Filled.EmojiEvents
    "badge" -> Icons.Filled.Badge
    "text_fields" -> Icons.Filled.TextFields
    "delete" -> Icons.Filled.Delete
    "policy" -> Icons.Filled.Policy
    "extension" -> Icons.Filled.Extension
    "directions_subway" -> Icons.Filled.DirectionsSubway
    "storefront" -> Icons.Filled.Storefront
    "restaurant" -> Icons.Filled.Restaurant
    "whatshot" -> Icons.Filled.Whatshot
    "auto_fix_high" -> Icons.Filled.AutoFixHigh
    "chat_bubble" -> Icons.Filled.ChatBubble
    "dashboard" -> Icons.Filled.Dashboard
    "hive" -> Icons.Filled.Hive
    "apps" -> Icons.Filled.Apps
    "timeline" -> Icons.Filled.Timeline
    "style" -> Icons.Filled.Style
    "history_edu" -> Icons.Filled.HistoryEdu
    "insights" -> Icons.Filled.Insights
    "swap_horiz" -> Icons.Filled.SwapHoriz
    "lightbulb" -> Icons.Filled.Lightbulb
    "vibration" -> Icons.Filled.Vibration
    "arrow_right_alt" -> Icons.Filled.ArrowRightAlt
    "verified" -> Icons.Filled.Verified
    "today" -> Icons.Filled.Today
    "back" -> Icons.AutoMirrored.Filled.ArrowBack
    else -> Icons.Filled.Circle
}
