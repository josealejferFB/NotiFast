package com.notifast.app.presentation.feature.share.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.package_data.SubjectPackage
import com.notifast.app.presentation.feature.notes.components.SubjectIconMapper
import com.notifast.app.presentation.theme.ThemeUtils

/**
 * Preview card displaying subject metadata and content counts of an imported package.
 */
@Composable
fun ImportPayloadPreview(
    pkg: SubjectPackage?,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    AnimatedVisibility(
        visible = pkg != null,
        modifier = modifier
    ) {
        if (pkg != null) {
            val subjectColor = ThemeUtils.getAccessibleColor(pkg.subject.colorHex, isDark)
            Column {
                Spacer(Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(subjectColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = SubjectIconMapper.getIcon(pkg.subject.icon),
                                    contentDescription = pkg.subject.name,
                                    tint = subjectColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = pkg.subject.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = stringResource(
                                        R.string.import_preview_summary,
                                        pkg.schedules.size,
                                        pkg.evaluations.size,
                                        pkg.notes.size
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
