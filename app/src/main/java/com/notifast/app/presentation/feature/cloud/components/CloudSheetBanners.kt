package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.cloud.BatchResult
import com.notifast.app.domain.model.cloud.UploadProgress
import com.notifast.app.presentation.atoms.NotiFastCard

/**
 * Renders status banners, scanning skeleton, progress bar, and summary card in CloudHubSheet.
 */
@Composable
fun CloudSheetBanners(
    isOffline: Boolean,
    cloudOnlyBanner: Int,
    isLoadingScanning: Boolean,
    progress: UploadProgress.Progress?,
    summary: BatchResult?,
    onSelectAllCopies: () -> Unit,
    onRetrySummary: () -> Unit,
    onDismissSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (isOffline) {
            Text(
                text = stringResource(R.string.cloud_offline_banner),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (cloudOnlyBanner > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            NotiFastCard(onClick = onSelectAllCopies) {
                Text(
                    text = stringResource(R.string.cloud_banner_found_copies, cloudOnlyBanner),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        if (isLoadingScanning) {
            Spacer(modifier = Modifier.height(6.dp))
            CloudScanningSkeletonCard()
        }

        if (progress != null) {
            Spacer(modifier = Modifier.height(6.dp))
            CloudProgressOverlay(progress = progress)
        }

        AnimatedVisibility(
            visible = summary != null,
            enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) +
                expandVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)),
            exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)) +
                shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow))
        ) {
            summary?.let {
                Column {
                    Spacer(modifier = Modifier.height(6.dp))
                    CloudSummaryCard(
                        summary = it,
                        onRetry = onRetrySummary,
                        onDismiss = onDismissSummary
                    )
                }
            }
        }
    }
}
