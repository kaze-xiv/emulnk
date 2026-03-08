package com.emulnk.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emulnk.R
import com.emulnk.model.ScreenTarget
import com.emulnk.model.ThemeConfig
import com.emulnk.model.ThemeType
import com.emulnk.model.resolvedScreenTarget
import com.emulnk.model.resolvedType
import com.emulnk.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PairingBottomSheet(
    selectedItem: ThemeConfig,
    companions: List<ThemeConfig>,
    gameName: String,
    rootPath: String = "",
    isDualScreenBundle: Boolean = false,
    onDismiss: () -> Unit,
    isCurrentDefault: Boolean = false,
    defaultCompanionId: String? = null,
    onLaunch: (theme: ThemeConfig?, overlay: ThemeConfig?, setDefault: Boolean) -> Unit,
    onLaunchBundle: (primary: ThemeConfig?, secondary: ThemeConfig?, setDefault: Boolean) -> Unit = { _, _, _ -> }
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedCompanionIndex by remember {
        mutableIntStateOf(
            if (defaultCompanionId != null) companions.indexOfFirst { it.id == defaultCompanionId }
            else -1
        )
    }
    var setDefault by remember { mutableStateOf(isCurrentDefault) }
    var isSwapped by remember {
        mutableStateOf(
            selectedItem.resolvedScreenTarget == ScreenTarget.SECONDARY
        )
    }

    val isTheme = selectedItem.resolvedType == ThemeType.THEME
    val title = when {
        isDualScreenBundle -> stringResource(R.string.pair_overlay_bundle)
        isTheme -> stringResource(R.string.pair_with_overlay)
        else -> stringResource(R.string.pair_with_theme)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceRaised,
        shape = RoundedCornerShape(topStart = EmuLnkDimens.cornerLg, topEnd = EmuLnkDimens.cornerLg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = EmuLnkDimens.spacingXl)
                .padding(bottom = EmuLnkDimens.spacingXl)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            val selectedCompanion = if (selectedCompanionIndex >= 0) companions[selectedCompanionIndex] else null

            if (isDualScreenBundle) {
                Spacer(modifier = Modifier.height(EmuLnkDimens.spacingMd))

                val topConfig = if (isSwapped) selectedCompanion else selectedItem
                val bottomConfig = if (isSwapped) selectedItem else selectedCompanion

                ScreenCard(
                    config = topConfig,
                    rootPath = rootPath,
                    screenLabel = stringResource(R.string.screen_primary),
                    borderColor = BrandPurple
                )

                // Swap Screens button
                TextButton(
                    onClick = { isSwapped = !isSwapped },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_swap_vert),
                        contentDescription = null,
                        tint = BrandPurple,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(EmuLnkDimens.spacingSm))
                    Text(
                        text = stringResource(R.string.swap_screens),
                        color = BrandPurple,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                ScreenCard(
                    config = bottomConfig,
                    rootPath = rootPath,
                    screenLabel = stringResource(R.string.screen_secondary),
                    borderColor = BrandCyan
                )

                Spacer(modifier = Modifier.height(EmuLnkDimens.spacingLg))
                HorizontalDivider(color = DividerColor)
            }

            Spacer(modifier = Modifier.height(EmuLnkDimens.spacingLg))

            val companionSelectionColor = if (isDualScreenBundle && isSwapped) BrandPurple else BrandCyan

            Column(verticalArrangement = Arrangement.spacedBy(EmuLnkDimens.spacingSm)) {
                CompanionCard(
                    config = null,
                    rootPath = rootPath,
                    isSelected = selectedCompanionIndex == -1,
                    selectionColor = companionSelectionColor,
                    onClick = { selectedCompanionIndex = -1 }
                )

                companions.forEachIndexed { index, companion ->
                    CompanionCard(
                        config = companion,
                        rootPath = rootPath,
                        isSelected = selectedCompanionIndex == index,
                        selectionColor = companionSelectionColor,
                        onClick = { selectedCompanionIndex = index }
                    )
                }
            }

            Spacer(modifier = Modifier.height(EmuLnkDimens.spacingMd))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = setDefault,
                    onCheckedChange = { setDefault = it },
                    colors = CheckboxDefaults.colors(checkedColor = BrandPurple, uncheckedColor = TextTertiary)
                )
                Spacer(modifier = Modifier.width(EmuLnkDimens.spacingSm))
                Text(
                    text = stringResource(R.string.pair_set_default, gameName),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                FilledIconButton(
                    onClick = {
                        if (isDualScreenBundle) {
                            if (isSwapped) {
                                onLaunchBundle(selectedCompanion, selectedItem, setDefault)
                            } else {
                                onLaunchBundle(selectedItem, selectedCompanion, setDefault)
                            }
                        } else if (isTheme) {
                            onLaunch(selectedItem, selectedCompanion, setDefault)
                        } else {
                            onLaunch(selectedCompanion, selectedItem, setDefault)
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = BrandPurple)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play_arrow),
                        contentDescription = stringResource(R.string.pair_launch),
                        tint = TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CompanionCard(
    config: ThemeConfig?,
    rootPath: String,
    isSelected: Boolean,
    selectionColor: Color = BrandCyan,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(EmuLnkDimens.cornerMd),
        color = SurfaceElevated,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) selectionColor else DividerColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EmuLnkDimens.spacingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EmuLnkDimens.spacingMd)
        ) {
            ThemePreviewSquare(config = config, rootPath = rootPath, size = 28.dp, fontSize = 11.sp)

            // Name
            Text(
                text = config?.meta?.name ?: stringResource(R.string.pair_none),
                fontSize = 14.sp,
                color = if (config != null) TextPrimary else TextSecondary,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            if (config != null) {
                ThemeTypeBadge(config = config)
            }

            // Check icon
            if (isSelected) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_circle),
                    contentDescription = null,
                    tint = selectionColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ScreenCard(
    config: ThemeConfig?,
    rootPath: String,
    screenLabel: String,
    borderColor: Color
) {
    Surface(
        shape = RoundedCornerShape(EmuLnkDimens.cornerMd),
        color = SurfaceElevated,
        border = BorderStroke(1.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(EmuLnkDimens.spacingMd),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(EmuLnkDimens.spacingMd)
        ) {
            ThemePreviewSquare(config = config, rootPath = rootPath, size = 36.dp, fontSize = 14.sp)

            // Name
            Text(
                text = config?.meta?.name ?: stringResource(R.string.pair_none),
                fontSize = 14.sp,
                color = if (config != null) TextPrimary else TextSecondary,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            if (config != null) {
                ThemeTypeBadge(config = config)
            }

            // Dual-screen position icon
            val isPrimary = screenLabel == stringResource(R.string.screen_primary)
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                // Top screen rectangle (always purple)
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 10.dp)
                        .background(
                            color = if (isPrimary) BrandPurple else BrandPurple.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
                // Bottom screen rectangle (always cyan)
                Box(
                    modifier = Modifier
                        .size(width = 16.dp, height = 10.dp)
                        .background(
                            color = if (!isPrimary) BrandCyan else BrandCyan.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}
