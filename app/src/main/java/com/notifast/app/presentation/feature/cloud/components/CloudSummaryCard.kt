package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.cloud.BatchResult
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard

@Composable
fun CloudSummaryCard(
    summary: BatchResult,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = summary.outcomes.size
    val failed = summary.failed.size
    val succeeded = total - failed

    NotiFastCard(
        modifier = modifier.fillMaxWidth(),
        onClick = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.cloud_summary_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (succeeded > 0) {
                Text(
                    text = stringResource(R.string.cloud_summary_success_count, succeeded),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            if (failed > 0) {
                Text(
                    text = stringResource(R.string.cloud_summary_failure_count, failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.btn_close))
                }

                if (failed > 0) {
                    NotiFastButton(
                        text = stringResource(R.string.cloud_summary_btn_retry),
                        onClick = onRetry,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}
