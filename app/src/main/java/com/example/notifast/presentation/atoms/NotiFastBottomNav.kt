package com.example.notifast.presentation.atoms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notifast.presentation.navigation.Screen
import com.example.notifast.presentation.theme.color.Ruby
import kotlinx.coroutines.delay

@Composable
fun NotiFastBottomNav(
    items: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TooltipOverlay(items = items, currentRoute = currentRoute)
        Spacer(Modifier.height(8.dp))
        Surface(
            color = Ruby,
            shape = RoundedCornerShape(28.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { screen ->
                    val selected = currentRoute == screen.route ||
                        currentRoute?.startsWith(screen.route) == true
                    val animatedScale by animateFloatAsState(
                        targetValue = if (selected) 1.15f else 1f,
                        animationSpec = spring(dampingRatio = 0.5f, stiffness = 300f),
                        label = "scale"
                    )
                    IconButton(
                        onClick = { onItemClick(screen) }
                    ) {
                        screen.icon?.let {
                            Icon(
                                imageVector = it,
                                contentDescription = screen.title,
                                tint = if (selected) Color.White else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .size(22.dp)
                                    .graphicsLayer {
                                        scaleX = animatedScale
                                        scaleY = animatedScale
                                    }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TooltipOverlay(
    items: List<Screen>,
    currentRoute: String?
) {
    val density = LocalDensity.current
    val slideOffsetPx = with(density) { 60.dp.roundToPx() }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        items.forEach { screen ->
            val selected = currentRoute == screen.route ||
                currentRoute?.startsWith(screen.route) == true
            var showTooltip by remember(selected) { mutableStateOf(selected) }

            LaunchedEffect(selected) {
                if (selected) {
                    showTooltip = true
                    delay(3000)
                    showTooltip = false
                }
            }

            AnimatedVisibility(
                visible = showTooltip,
                enter = slideInVertically(
                    initialOffsetY = { slideOffsetPx },
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 280f)
                ) + fadeIn(animationSpec = spring(dampingRatio = 0.65f, stiffness = 280f)),
                exit = fadeOut(animationSpec = spring(dampingRatio = 0.65f, stiffness = 280f)) + slideOutVertically(
                    targetOffsetY = { slideOffsetPx },
                    animationSpec = spring(dampingRatio = 0.65f, stiffness = 280f)
                )
            ) {
                Surface(
                    color = Ruby,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 6.dp
                ) {
                    Text(
                        text = screen.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
