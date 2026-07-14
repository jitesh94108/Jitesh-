package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.PinViewModel

@Composable
fun MainAppScreen(
    viewModel: PinViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0 = Explore, 1 = Shorts, 2 = Create, 3 = Messages, 4 = Saved

    val selectedPin by viewModel.selectedPin.collectAsState()
    val selectedBoard by viewModel.selectedBoard.collectAsState()

    // Handle Android system back presses cleanly
    BackHandler(enabled = selectedPin != null || selectedBoard != null || activeTab != 0) {
        if (selectedPin != null) {
            viewModel.selectPin(-1) // Deselect/close
        } else if (selectedBoard != null) {
            viewModel.selectBoard(null) // Deselect/close board
        } else {
            activeTab = 0 // Return to explore feed
        }
    }

    Scaffold(
        bottomBar = {
            if (selectedPin == null) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    NavigationBarItem(
                        selected = activeTab == 0,
                        onClick = {
                            activeTab = 0
                            viewModel.selectBoard(null)
                        },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == 0) Icons.Default.Explore else Icons.Outlined.Explore,
                                contentDescription = "Explore"
                            )
                        },
                        label = { Text("Explore", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("nav_explore")
                    )

                    NavigationBarItem(
                        selected = activeTab == 1,
                        onClick = {
                            activeTab = 1
                            viewModel.selectBoard(null)
                        },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == 1) Icons.Default.PlayCircle else Icons.Outlined.PlayCircle,
                                contentDescription = "Shorts"
                            )
                        },
                        label = { Text("Shorts", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("nav_shorts")
                    )

                    NavigationBarItem(
                        selected = activeTab == 2,
                        onClick = {
                            activeTab = 2
                            viewModel.selectBoard(null)
                        },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == 2) Icons.Default.AddCircle else Icons.Outlined.AddCircleOutline,
                                contentDescription = "Create"
                            )
                        },
                        label = { Text("Create", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("nav_create")
                    )

                    NavigationBarItem(
                        selected = activeTab == 3,
                        onClick = {
                            activeTab = 3
                            viewModel.selectBoard(null)
                        },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == 3) Icons.Default.ChatBubble else Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Messages"
                            )
                        },
                        label = { Text("Messages", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("nav_messages")
                    )

                    NavigationBarItem(
                        selected = activeTab == 4,
                        onClick = {
                            activeTab = 4
                            viewModel.selectBoard(null)
                        },
                        icon = {
                            Icon(
                                imageVector = if (activeTab == 4) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Saved"
                            )
                        },
                        label = { Text("Saved", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("nav_saved")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Content Switcher
            when (activeTab) {
                0 -> ExploreScreen(
                    viewModel = viewModel,
                    onPinClicked = { pin -> viewModel.selectPin(pin.id) }
                )

                1 -> ShortsScreen(
                    viewModel = viewModel
                )

                2 -> CreatePinScreen(
                    viewModel = viewModel,
                    onPinCreated = {
                        activeTab = 0 // Go to explore feed upon publishing/cancelling
                    }
                )

                3 -> MessagesScreen(
                    viewModel = viewModel
                )

                4 -> SavedScreen(
                    viewModel = viewModel,
                    onPinClicked = { pin -> viewModel.selectPin(pin.id) }
                )
            }

            // --- Unified Pin Detail Overlay ---
            // Slides up smoothly from the bottom, exactly like standard mobile applications!
            AnimatedVisibility(
                visible = selectedPin != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                if (selectedPin != null) {
                    PinDetailScreen(
                        viewModel = viewModel,
                        onBackClicked = { viewModel.selectPin(-1) }
                    )
                }
            }
        }
    }
}
