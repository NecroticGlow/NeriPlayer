package moe.ouom.neriplayer.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/*
 * NeriPlayer - A unified Android player for streaming music and videos from multiple online platforms.
 * Copyright (C) 2025-2025 NeriPlayer developers
 * https://github.com/cwuom/NeriPlayer
 *
 * This software is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.
 * If not, see <https://www.gnu.org/licenses/>.
 *
 * File: moe.ouom.neriplayer.navigation/Destinations
 * Created: 2025/8/8
 */

sealed class Destinations(val route: String, val label: String) {
    // TAB
    data object Home : Destinations("home", "首页")
    data object Explore : Destinations("explore", "探索")
    data object Library : Destinations("library", "媒体库")
    data object Settings : Destinations("settings", "设置")
    object Recent { const val route = "recent" }

    // DEBUG
    data object Debug : Destinations("debug", "调试")
    data object DebugBili : Destinations("debug/bili", "B 站调试")
    data object DebugNetease : Destinations("debug/netease", "网易云调试")
    data object DebugSearch : Destinations("debug/search", "搜索调试")
    data object DebugLogsList : Destinations("debug_logs_list", "日志列表")

    // 网易云歌单详情路由
    data object PlaylistDetail : Destinations("playlist_detail/{playlistJson}", "歌单详情") {
        fun createRoute(playlistJson: String) = "playlist_detail/$playlistJson"
    }

    // B 站收藏夹详情路由
    data object BiliPlaylistDetail : Destinations("bili_playlist_detail/{playlistJson}", "B站收藏夹详情") {
        fun createRoute(playlistJson: String) = "bili_playlist_detail/$playlistJson"
    }

    // 本地歌单详情路由
    data object LocalPlaylistDetail : Destinations("local_playlist_detail/{playlistId}", "本地歌单详情") {
        fun createRoute(playlistId: Long) = "local_playlist_detail/$playlistId"
    }

    // 下载管理器路由
    data object DownloadManager : Destinations("download_manager", "下载管理")

    data object DebugLogViewer : Destinations("debug_log_viewer/{filePath}", "日志查看") {
        fun createRoute(filePath: String): String {
            val encodedPath = URLEncoder.encode(filePath, StandardCharsets.UTF_8.name())
            return "debug_log_viewer/$encodedPath"
        }
    }
}