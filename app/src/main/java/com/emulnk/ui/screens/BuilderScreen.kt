package com.emulnk.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emulnk.R
import com.emulnk.model.CustomOverlayConfig
import com.emulnk.model.ScreenTarget
import com.emulnk.model.StoreWidget
import com.emulnk.ui.components.WidgetSelectionCard
import com.emulnk.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuilderScreen(
    profileId: String,
    console: String,
    installedWidgets: List<StoreWidget>,
    savedConfig: CustomOverlayConfig?,
    isDualScreen: Boolean,
    previewBaseUrl: String?,
    totalWidgetCount: Int = 0,
    onBack: () -> Unit,
    onBrowseGallery: () -> Unit = {},
    onLaunchPreview: (selectedIds: List<String>, screenAssignments: Map<String, ScreenTarget>) -> Unit
) {
    // Initialize assignments from saved config or widget defaults
    val assignments = remember(installedWidgets, savedConfig) {
        mutableStateMapOf<String, ScreenTarget?>().apply {
            for (widget in installedWidgets) {
                val savedAssignment = savedConfig?.screenAssignments?.get(widget.id)
                val isSelected = savedConfig == null || widget.id in (savedConfig.selectedWidgetIds)
                this[widget.id] = when {
                    !isSelected -> null
                    savedAssignment != null -> savedAssignment
                    else -> widget.screenTarget ?: ScreenTarget.PRIMARY
                }
            }
        }
    }

    // Filter tabs state (only shown for dual-screen)
    var filterTab by remember { mutableIntStateOf(0) }

    val selectedCount by remember { derivedStateOf { assignments.values.count { it != null } } }
    val filteredWidgets by remember(installedWidgets, isDualScreen) {
        derivedStateOf {
            if (!isDualScreen || filterTab == 0) installedWidgets
            else {
                val target = if (filterTab == 1) ScreenTarget.PRIMARY else ScreenTarget.SECONDARY
                installedWidgets.filter { (assignments[it.id] ?: it.screenTarget ?: ScreenTarget.PRIMARY) == target }
            }
        }
    }

    Scaffold(
        containerColor = SurfaceBase,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.builder_screen_title),
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceBase)
            )
        },
        bottomBar = {
            Surface(
                color = SurfaceElevated,
                tonalElevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(EmuLnkDimens.spacingLg)
                ) {
                    Button(
                        onClick = {
                            val selectedIds = assignments.filter { it.value != null }.keys.toList()
                            val screenMap = assignments.filterValues { it != null }
                                .mapValues { it.value!! }
                            onLaunchPreview(selectedIds, screenMap)
                        },
                        enabled = selectedCount > 0,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(EmuLnkDimens.cornerMd),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrandPurple,
                            disabledContainerColor = BrandPurple.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            stringResource(R.string.builder_launch_preview),
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (installedWidgets.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.builder_no_widgets),
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(EmuLnkDimens.spacingXl)
                    )
                }
            } else {
                // Filter tabs for dual-screen
                if (isDualScreen) {
                    val tabs = listOf(
                        stringResource(R.string.builder_tab_all),
                        stringResource(R.string.screen_primary),
                        stringResource(R.string.screen_secondary)
                    )
                    TabRow(
                        selectedTabIndex = filterTab,
                        containerColor = SurfaceBase,
                        contentColor = BrandPurple,
                        divider = {}
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = filterTab == index,
                                onClick = { filterTab = index },
                                text = {
                                    val tabColor = when {
                                        filterTab != index -> TextSecondary
                                        index == 2 -> BrandCyan
                                        else -> BrandPurple
                                    }
                                    Text(
                                        title,
                                        color = tabColor,
                                        fontSize = 13.sp
                                    )
                                }
                            )
                        }
                    }
                }

                // Widget grid (paired rows so cards in each row match height)
                val rows = filteredWidgets.chunked(2)
                val missingCount = totalWidgetCount - installedWidgets.size
                LazyColumn(
                    contentPadding = PaddingValues(EmuLnkDimens.spacingSm),
                    verticalArrangement = Arrangement.spacedBy(EmuLnkDimens.spacingSm),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(rows, key = { row -> row.map { it.id }.joinToString() }) { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(EmuLnkDimens.spacingSm)
                        ) {
                            for (widget in row) {
                                WidgetSelectionCard(
                                    widget = widget,
                                    previewBaseUrl = previewBaseUrl,
                                    assignment = assignments[widget.id],
                                    isDualScreen = isDualScreen,
                                    onAssignmentChanged = { newAssignment ->
                                        assignments[widget.id] = newAssignment
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }
                            if (row.size == 1 && missingCount > 0) {
                                MoreWidgetsCard(
                                    missingCount = missingCount,
                                    onClick = onBrowseGallery,
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                            } else if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                    if (missingCount > 0 && (rows.isEmpty() || rows.last().size == 2)) {
                        item(key = "more_widgets_hint") {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                                horizontalArrangement = Arrangement.spacedBy(EmuLnkDimens.spacingSm)
                            ) {
                                MoreWidgetsCard(
                                    missingCount = missingCount,
                                    onClick = onBrowseGallery,
                                    modifier = Modifier.weight(1f).fillMaxHeight()
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoreWidgetsCard(
    missingCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(EmuLnkDimens.cornerSm)
    Column(
        modifier = modifier
            .clip(shape)
            .background(SurfaceBase)
            .border(1.dp, BrandPurple.copy(alpha = 0.3f), shape)
            .clickable(onClick = onClick)
            .padding(EmuLnkDimens.spacingSm),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(BrandPurple.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_download),
                contentDescription = null,
                tint = BrandPurple,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(EmuLnkDimens.spacingSm))
        Text(
            text = stringResource(R.string.builder_more_widgets),
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            text = stringResource(R.string.builder_more_widgets_sub, missingCount),
            fontSize = 11.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}
