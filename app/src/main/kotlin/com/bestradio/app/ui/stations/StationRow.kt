package com.bestradio.app.ui.stations

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.bestradio.app.R
import com.bestradio.app.data.model.Station

@Composable
fun StationRow(
    station: Station,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        leadingContent = {
            AsyncImage(
                model = station.faviconUrl.ifBlank { null },
                contentDescription = null,
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.ic_station_placeholder),
                error = painterResource(R.drawable.ic_station_placeholder),
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp)),
            )
        },
        headlineContent = { Text(station.name) },
        supportingContent = {
            if (station.genre.isNotBlank()) Text(station.genre, maxLines = 1)
        },
        trailingContent = {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.semantics {
                    contentDescription = if (isFavorite) {
                        "Retirer ${station.name} des favoris"
                    } else {
                        "Ajouter ${station.name} aux favoris"
                    }
                },
            ) {
                Text(if (isFavorite) "★" else "☆")
            }
        },
        modifier = modifier.clickable(
            onClickLabel = "Écouter ${station.name}",
            onClick = onClick,
        ),
    )
}
