package com.emulnk.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.emulnk.R
import com.emulnk.model.ScreenTarget
import com.emulnk.model.StoreWidget
import com.emulnk.ui.theme.*

@Composable
fun WidgetSelectionCard(
    widget: StoreWidget,
    previewBaseUrl: String?,
    assignment: ScreenTarget?,
    isDualScreen: Boolean,
    onAssignmentChanged: (ScreenTarget?) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(EmuLnkDimens.cornerSm)
    val isSelected = assignment != null

    Column(
        modifier = modifier
            .clip(shape)
            .background(if (isSelected) SurfaceElevated else SurfaceBase)
            .border(
                width = 1.dp,
                color = when (assignment) {
                    ScreenTarget.SECONDARY -> BrandCyan.copy(alpha = 0.5f)
                    ScreenTarget.PRIMARY -> BrandPurple.copy(alpha = 0.5f)
                    else -> DividerColor
                },
                shape = shape
            )
            .clickable {
                if (isDualScreen) {
                    // Cycle: null -> PRIMARY -> SECONDARY -> null
                    val next = when (assignment) {
                        null -> ScreenTarget.PRIMARY
                        ScreenTarget.PRIMARY -> ScreenTarget.SECONDARY
                        ScreenTarget.SECONDARY -> null
                    }
                    onAssignmentChanged(next)
                } else {
                    onAssignmentChanged(if (isSelected) null else ScreenTarget.PRIMARY)
                }
            }
            .padding(EmuLnkDimens.spacingSm)
    ) {
        // Preview image
        val previewUrl = if (widget.previewUrl != null && previewBaseUrl != null) {
            "$previewBaseUrl/${widget.previewUrl}"
        } else null

        if (previewUrl != null) {
            AsyncImage(
                model = previewUrl,
                contentDescription = widget.label,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(EmuLnkDimens.spacingXs)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(EmuLnkDimens.spacingXs))
        }

        Text(
            text = widget.label,
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (widget.description.isNotBlank()) {
            Text(
                text = widget.description,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(EmuLnkDimens.spacingXs))

        // Screen assignment indicator
        if (isDualScreen) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ScreenToggleChip(
                    label = stringResource(R.string.builder_assign_primary),
                    isActive = assignment == ScreenTarget.PRIMARY,
                    screenTarget = ScreenTarget.PRIMARY,
                    onClick = {
                        onAssignmentChanged(
                            if (assignment == ScreenTarget.PRIMARY) null else ScreenTarget.PRIMARY
                        )
                    }
                )
                ScreenToggleChip(
                    label = stringResource(R.string.builder_assign_secondary),
                    isActive = assignment == ScreenTarget.SECONDARY,
                    screenTarget = ScreenTarget.SECONDARY,
                    onClick = {
                        onAssignmentChanged(
                            if (assignment == ScreenTarget.SECONDARY) null else ScreenTarget.SECONDARY
                        )
                    }
                )
            }
        } else {
            val statusText = if (isSelected) "\u2713" else "\u2014"
            val statusColor = if (isSelected) BrandCyan else TextSecondary
            Text(
                text = statusText,
                color = statusColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ScreenToggleChip(
    label: String,
    isActive: Boolean,
    screenTarget: ScreenTarget = ScreenTarget.PRIMARY,
    onClick: () -> Unit
) {
    val bg = if (isActive) {
        if (screenTarget == ScreenTarget.PRIMARY) BrandPurple else BrandCyan
    } else SurfaceBase
    val textColor = if (isActive) SurfaceBase else TextSecondary
    val shape = RoundedCornerShape(6.dp)

    Text(
        text = label,
        color = textColor,
        fontSize = 11.sp,
        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}
