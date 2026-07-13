package com.bestradio.app.ui.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bestradio.app.R

@Composable
fun MiniPlayerBar(
    stationName: String,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), tonalElevation = 3.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = stationName,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(end = 56.dp),
            )
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
