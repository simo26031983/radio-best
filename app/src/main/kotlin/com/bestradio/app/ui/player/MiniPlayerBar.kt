package com.bestradio.app.ui.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.bestradio.app.R

@Composable
fun MiniPlayerBar(
    stationName: String,
    stationFaviconUrl: String,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    modifier: Modifier = Modifier,
    nowPlayingText: String? = null,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        tonalElevation = 3.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 56.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = stationFaviconUrl.ifBlank { null },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_station_placeholder),
                    error = painterResource(R.drawable.ic_station_placeholder),
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(6.dp)),
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stationName,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // Live ICY "now playing" text; absent entirely (no reserved
                    // space) for streams that don't carry ICY metadata.
                    if (!nowPlayingText.isNullOrBlank()) {
                        Text(
                            text = nowPlayingText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
            IconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.align(Alignment.CenterEnd),
            ) {
                if (isPlaying) {
                    Icon(painter = painterResource(R.drawable.ic_pause), contentDescription = "Pause")
                } else {
                    Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Lecture")
                }
            }
        }
    }
}
