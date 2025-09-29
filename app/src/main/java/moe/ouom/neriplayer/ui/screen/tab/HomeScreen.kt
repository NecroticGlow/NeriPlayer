package moe.ouom.neriplayer.ui.screen.tab

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
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.
 * If not, see <https://www.gnu.org/licenses/>.
 *
 * File: moe.ouom.neriplayer.ui.screen.tab/HomeScreen
 * Created: 2025/8/8
 */

import android.annotation.SuppressLint
import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Radar
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil.compose.AsyncImage
import coil.request.ImageRequest
import moe.ouom.neriplayer.core.di.AppContainer
import moe.ouom.neriplayer.data.UsageEntry
import moe.ouom.neriplayer.ui.LocalMiniPlayerHeight
import moe.ouom.neriplayer.ui.viewmodel.playlist.SongItem
import moe.ouom.neriplayer.ui.viewmodel.tab.HomeViewModel
import moe.ouom.neriplayer.ui.viewmodel.tab.NeteasePlaylist
import moe.ouom.neriplayer.util.HapticIconButton
import moe.ouom.neriplayer.util.formatDuration
import moe.ouom.neriplayer.util.formatPlayCount
import kotlin.math.ceil
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onItemClick: (NeteasePlaylist) -> Unit = {},
    gridState: LazyGridState,
    onOpenRecent: (UsageEntry) -> Unit = {},
    onSongClick: (List<SongItem>, Int) -> Unit = { _, _ -> } // 推荐歌曲点击，默认 no-op
) {
    val context = LocalContext.current
    val vm: HomeViewModel = viewModel(
        factory = viewModelFactory {
            initializer {
                val app = context.applicationContext as Application
                HomeViewModel(app)
            }
        }
    )
    val ui by vm.uiState.collectAsState()
    val usage by AppContainer.playlistUsageRepo.frequentPlaylistsFlow.collectAsState(initial = emptyList())

    // 推荐歌曲（热歌 / 私人雷达）
    val hotSongs by vm.hotSongsFlow.collectAsState()
    val radarSongs by vm.radarSongsFlow.collectAsState()

    val titleOptions = listOf("音理音理音?", "音理音理!", "音理音理!!", "音理音理~", "喵~", "音理!", "NeriPlayer")
    val appBarTitle = rememberSaveable { titleOptions.random() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Column(
        Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LargeTopAppBar(
            title = { Text(appBarTitle) },
            actions = {
                HapticIconButton(onClick = { vm.refreshRecommend(); vm.loadHomeRecommendations() }) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = "刷新推荐")
                }
            },
            scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                scrolledContainerColor = Color.Transparent
            )
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.0f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxSize()
        ) {
            when {
                ui.loading -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(text = "  正在为你加载首页推荐...", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                ui.error != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "加载失败：${ui.error}",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(text = "点右上角刷新重试~", style = MaterialTheme.typography.bodySmall)
                    }
                }

                else -> {
                    val miniPlayerHeight = LocalMiniPlayerHeight.current
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Adaptive(120.dp),
                        contentPadding = PaddingValues(
                            start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp + miniPlayerHeight
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        // 继续播放 //
                        if (usage.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                SectionHeader(icon = Icons.Outlined.History, title = "继续播放")
                            }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ContinueSection(
                                    items = usage.take(12),
                                    onClick = { entry -> onOpenRecent(entry) }
                                )
                            }
                        }

                        // 热力飙升 //
                        if (hotSongs.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                SectionHeader(icon = Icons.Outlined.Bolt, title = "热力飙升")
                            }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ResponsiveSongPagerList(
                                    songs = hotSongs,
                                    onSongClick = onSongClick
                                )
                            }
                        }

                        // 私人雷达 //
                        if (radarSongs.isNotEmpty()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                SectionHeader(icon = Icons.Outlined.Radar, title = "私人雷达")
                            }
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                ResponsiveSongPagerList(
                                    songs = radarSongs,
                                    onSongClick = onSongClick
                                )
                            }
                        }

                        // 为你推荐//
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            SectionHeader(icon = Icons.Outlined.Star, title = "为你推荐")
                        }
                        items(items = ui.playlists, key = { it.id }) { item ->
                            PlaylistCard(item) { onItemClick(item) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

/** 紧凑型歌曲行（用于首页推荐区） */
@Composable
private fun SongRowMini(
    index: Int,
    song: SongItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 索引
        Text(
            text = index.toString(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(28.dp),
            maxLines = 1,
            overflow = TextOverflow.Clip
        )

        // 封面
        if (!song.coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current).data(song.coverUrl).build(),
                contentDescription = song.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(44.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.width(10.dp))
        } else {
            Spacer(Modifier.width(10.dp))
        }

        // 文本
        Column(Modifier.weight(1f)) {
            Text(
                text = song.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = listOfNotNull(
                    song.artist.takeIf { it.isNotBlank() },
                    song.album.takeIf { it.isNotBlank() }
                ).joinToString(" · "),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 时长
        Text(
            text = formatDuration(song.durationMs),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun PlaylistCard(playlist: NeteasePlaylist, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(playlist.picUrl).crossfade(true).build(),
            contentDescription = playlist.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
        )
        Column(modifier = Modifier.padding(top = 6.dp, start = 4.dp, end = 4.dp, bottom = 4.dp)) {
            Text(
                text = playlist.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "${formatPlayCount(playlist.playCount)} · ${playlist.trackCount}首",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Clip
            )
        }
    }
}

@Composable
private fun ContinueSection(items: List<UsageEntry>, onClick: (UsageEntry) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.source + ":" + it.id }) { entry ->
                ContinueCard(entry) { onClick(entry) }
            }
        }
    }
}

@Composable
private fun ContinueCard(entry: UsageEntry, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .width(150.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(entry.picUrl).build(),
            contentDescription = entry.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
        )
        Column(modifier = Modifier.padding(6.dp)) {
            Text(text = entry.name, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall)
            Text(text = "${entry.trackCount} 首", style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        }
    }
}


@SuppressLint("ConfigurationScreenWidthHeight")
@Composable
private fun ResponsiveSongPagerList(
    songs: List<SongItem>,
    onSongClick: (List<SongItem>, Int) -> Unit
) {
    val widthDp = LocalConfiguration.current.screenWidthDp
    val columns = when {
        widthDp >= 840 -> 3   // 大平板/桌面
        widthDp >= 600 -> 2   // 小平板
        else -> 1             // 手机
    }
    val rowsPerColumn = 3
    val perPage = (columns * rowsPerColumn).coerceAtLeast(1)

    val pageCount = ceil(songs.size / perPage.toFloat()).toInt().coerceAtLeast(1)
    val pagerState = rememberPagerState(pageCount = { pageCount })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) { page ->
        val start = page * perPage
        val end = min(start + perPage, songs.size)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            for (c in 0 until columns) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (r in 0 until rowsPerColumn) {
                        val absoluteIndex = start + (c * rowsPerColumn + r)
                        if (absoluteIndex < end) {
                            val song = songs[absoluteIndex]
                            SongRowMini(
                                index = absoluteIndex + 1,
                                song = song,
                                onClick = { onSongClick(songs, absoluteIndex) }
                            )
                        } else {
                            Spacer(Modifier.height(0.dp))
                        }
                    }
                }
            }
        }
    }
}
