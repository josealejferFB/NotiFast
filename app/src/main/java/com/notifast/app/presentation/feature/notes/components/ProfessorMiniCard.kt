package com.notifast.app.presentation.feature.notes.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.Teacher
import com.notifast.app.presentation.atoms.NotiFastCard

/**
 * NotiFast 3.2 Reactive Professor Mini Card for carousels.
 * Adopts NotiFastCard atom with tactile physics, theme-reactive surface/container tokens,
 * and WCAG AAA compliant typography and icons.
 */
@Composable
fun ProfessorMiniCard(
    teacher: Teacher,
    onClick: () -> Unit,
    onWhatsAppClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    NotiFastCard(
        modifier = modifier
            .width(160.dp)
            .height(118.dp),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        contentPadding = 12.dp
    ) {
        Column(
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val firstChar = teacher.firstName.trim().take(1)
                        val lastChar = teacher.lastName.trim().take(1)
                        val initials = if (firstChar.isNotBlank() && lastChar.isNotBlank()) {
                            "$firstChar$lastChar"
                        } else {
                            teacher.firstName.trim().take(2).ifBlank { "P" }
                        }.uppercase()

                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                if (teacher.hasPhoneNumber && onWhatsAppClick != null) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = onWhatsAppClick,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = stringResource(R.string.teacher_action_whatsapp),
                                tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                } else if (teacher.email.isNotBlank()) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        onClick = onClick,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = teacher.fullName.ifBlank { stringResource(R.string.teachers_tab_title) },
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(2.dp))

            val subtitle = teacher.subjectNames.firstOrNull()
                ?: teacher.email.ifBlank { stringResource(R.string.teachers_subtab_professors) }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
