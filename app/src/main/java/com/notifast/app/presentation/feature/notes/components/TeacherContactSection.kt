package com.notifast.app.presentation.feature.notes.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.data.model.Teacher
import com.notifast.app.domain.util.TeacherFormatters
import com.notifast.app.presentation.theme.color.ForestSage

@Composable
fun TeacherContactSection(
    teacher: Teacher,
    context: Context,
    modifier: Modifier = Modifier
) {
    if (!teacher.hasPhoneNumber) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)),
            modifier = modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.teacher_no_phone_warning),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TeacherActionButton(
                icon = Icons.AutoMirrored.Filled.Chat,
                label = stringResource(R.string.teacher_action_whatsapp),
                tint = ForestSage,
                onClick = {
                    val url = TeacherFormatters.buildWhatsAppUrl(
                        phoneNumber = teacher.phoneNumber,
                        teacherName = teacher.fullName,
                        subjectName = teacher.subjectNames.firstOrNull().orEmpty()
                    )
                    runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                        .onFailure { Toast.makeText(context, context.getString(R.string.teacher_contact_whatsapp_unavailable), Toast.LENGTH_SHORT).show() }
                }
            )
            TeacherActionButton(
                icon = Icons.Default.Call,
                label = stringResource(R.string.teacher_action_call),
                tint = MaterialTheme.colorScheme.primary,
                onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${teacher.formattedPhoneNumber}"))) }
                        .onFailure { Toast.makeText(context, context.getString(R.string.teacher_contact_call_unavailable), Toast.LENGTH_SHORT).show() }
                }
            )
            TeacherActionButton(
                icon = Icons.Default.Sms,
                label = stringResource(R.string.teacher_action_sms),
                tint = MaterialTheme.colorScheme.tertiary,
                onClick = {
                    runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${teacher.formattedPhoneNumber}"))) }
                        .onFailure { Toast.makeText(context, context.getString(R.string.teacher_contact_sms_unavailable), Toast.LENGTH_SHORT).show() }
                }
            )
            if (teacher.email.isNotBlank()) {
                TeacherActionButton(
                    icon = Icons.Default.Email,
                    label = stringResource(R.string.teacher_action_email),
                    tint = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        runCatching { context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${teacher.email}"))) }
                            .onFailure { Toast.makeText(context, context.getString(R.string.teacher_contact_email_unavailable), Toast.LENGTH_SHORT).show() }
                    }
                )
            }
        }
    }
}

@Composable
fun TeacherActionButton(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(horizontal = 6.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = tint.copy(alpha = 0.14f),
            onClick = onClick,
            modifier = Modifier.size(50.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
