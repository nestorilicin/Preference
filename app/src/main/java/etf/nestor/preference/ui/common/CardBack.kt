package etf.nestor.preference.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import etf.nestor.preference.R

@Composable
fun CardBack(modifier: Modifier = Modifier) {
    val resId = remember {
        R.drawable.card_back_blue
    }
    if (resId != 0) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = "Lice dole",
            contentScale = ContentScale.Fit,
            modifier = modifier.clip(RoundedCornerShape(4.dp))
        )
    } else {
        Box(
            modifier = modifier
                .background(Color(0xFF1565C0), RoundedCornerShape(4.dp))
                .border(1.dp, Color.White, RoundedCornerShape(4.dp))
        )
    }
}
