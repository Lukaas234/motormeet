package com.motormeet.uruguay.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.motormeet.uruguay.ui.theme.MotorBlue
import com.motormeet.uruguay.ui.theme.MotorLine
import com.motormeet.uruguay.ui.theme.MotorMuted
import com.motormeet.uruguay.ui.theme.MotorRed
import com.motormeet.uruguay.ui.theme.MotorSurface
import com.motormeet.uruguay.ui.theme.MotorSurface2
import com.motormeet.uruguay.ui.theme.MotorText

@Composable
fun VerifiedBadge(modifier: Modifier = Modifier, compact: Boolean = false) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        if (compact) {
            Icon(Icons.Default.Verified, null, tint = MotorBlue, modifier = Modifier.size(16.dp))
        } else {
            androidx.compose.foundation.Image(
                painter = androidx.compose.ui.res.painterResource(com.motormeet.uruguay.R.drawable.verified_badge),
                contentDescription = "Verificado",
                modifier = Modifier.size(25.dp),
                contentScale = ContentScale.Fit
            )
            Text("Verificado", color = MotorBlue, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun SectionHeader(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(Modifier.fillMaxWidth().padding(top = 20.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null && onAction != null) TextButton(onClick = onAction) { Text(action, color = MotorRed, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(onClick = onClick, modifier = modifier.height(46.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = MotorRed, contentColor = Color.White)) { Text(text, fontWeight = FontWeight.ExtraBold) }
}

@Composable
fun MotorMeetLogo(modifier: Modifier = Modifier) {
    // Uses the bundled generated branding asset when available.
    val res = androidx.compose.ui.res.painterResource(com.motormeet.uruguay.R.drawable.motormeet_logo)
    androidx.compose.foundation.Image(painter = res, contentDescription = "MotorMeet Uruguay", modifier = modifier, contentScale = ContentScale.Fit)
}

@Composable
fun Avatar(photoUrl: String?, initials: String, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 42.dp) {
    if (!photoUrl.isNullOrBlank()) {
        AsyncImage(model = photoUrl, contentDescription = null, modifier = modifier.size(size).clip(CircleShape), contentScale = ContentScale.Crop)
    } else {
        Box(modifier.size(size).clip(CircleShape).background(Brush.linearGradient(listOf(MotorSurface2, MotorSurface))), contentAlignment = Alignment.Center) {
            Text(initials.take(1).uppercase(), color = MotorText, fontWeight = FontWeight.ExtraBold)
        }
    }
}
