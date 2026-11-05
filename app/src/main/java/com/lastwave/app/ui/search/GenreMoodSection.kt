package com.lastwave.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class GenreItem(
    val name: String,
    val emoji: String,
    val gradientColors: List<Color>
)

val GENRE_ITEMS = listOf(
    GenreItem("Pop", "🎤", listOf(Color(0xFFFF007F), Color(0xFFFF758C))),
    GenreItem("Rock", "🎸", listOf(Color(0xFFE52E71), Color(0xFFFF8A00))),
    GenreItem("Hip-Hop", "🎧", listOf(Color(0xFF8E2DE2), Color(0xFF4A00E0))),
    GenreItem("Lo-Fi", "☕", listOf(Color(0xFF2B5876), Color(0xFF4E4376))),
    GenreItem("Electronic", "⚡", listOf(Color(0xFF00F0FF), Color(0xFF0072FF))),
    GenreItem("Indie", "🌿", listOf(Color(0xFF11998E), Color(0xFF38EF7D))),
    GenreItem("Bollywood", "🪘", listOf(Color(0xFFFF512F), Color(0xFFDD2476))),
    GenreItem("Jazz", "🎷", listOf(Color(0xFFF2994A), Color(0xFFF2C94C))),
    GenreItem("Metal", "🤘", listOf(Color(0xFF232526), Color(0xFF414345))),
    GenreItem("Classical", "🎻", listOf(Color(0xFF654EA3), Color(0xFFEAAFC8))),
    GenreItem("Synthwave", "🌆", listOf(Color(0xFFF12711), Color(0xFFF5AF19)))
)

@Composable
fun GenreMoodSection(
    onGenreSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Explore Genres & Moods",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // 2-Column Grid
        val chunked = GENRE_ITEMS.chunked(2)
        chunked.forEach { pair ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (item in pair) {
                    GenreCard(
                        item = item,
                        onClick = { onGenreSelect(item.name) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GenreCard(
    item: GenreItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(item.gradientColors))
                .padding(horizontal = 14.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.emoji,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
