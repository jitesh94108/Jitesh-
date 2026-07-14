package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Board
import com.example.data.Pin
import com.example.ui.PinViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SavedScreen(
    viewModel: PinViewModel,
    onPinClicked: (Pin) -> Unit,
    modifier: Modifier = Modifier
) {
    val boards by viewModel.allBoards.collectAsState()
    val savedPins by viewModel.savedPins.collectAsState()
    val allPins by viewModel.allPins.collectAsState()
    val selectedBoard by viewModel.selectedBoard.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val userVal = currentUser

    val profileName = userVal?.username ?: "Guest"
    val profileInitial = profileName.firstOrNull()?.uppercase() ?: "G"
    val profileRole = if (userVal?.isAdmin == true) {
        "Administrator 👑"
    } else if (userVal?.isCreator == true) {
        "Verified Creator 👑 (${userVal.creatorCategory})"
    } else {
        "Regular Explorer 🎨"
    }

    var activeSavedTab by remember { mutableStateOf(0) } // 0 = Boards, 1 = Pins, 2 = Creator Studio
    var showCreateBoardDialog by remember { mutableStateOf(false) }

    if (selectedBoard != null) {
        // --- Board Detail View ---
        val boardPins = remember(allPins, selectedBoard) {
            allPins.filter { it.boardId == selectedBoard!!.id }
        }

        BoardDetailView(
            board = selectedBoard!!,
            pins = boardPins,
            onBackClicked = { viewModel.selectBoard(null) },
            onPinClicked = onPinClicked,
            onDeleteBoardClicked = {
                viewModel.deleteBoard(selectedBoard!!)
                viewModel.selectBoard(null)
            },
            isAdminUser = { viewModel.isAdminUser(it) }
        )
    } else {
        // --- Saved Profile View ---
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- Profile Header ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profileInitial,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = profileName,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (userVal?.isAdmin == true || viewModel.isAdminUser(profileName)) {
                        Icon(
                            imageVector = Icons.Filled.Verified,
                            contentDescription = "Verified Administrator",
                            tint = Color(0xFF1DA1F2),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Text(
                    text = profileRole,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (currentUser?.isAdmin == true) MaterialTheme.colorScheme.primary else Color.Gray
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { viewModel.logout() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Text("Logout / Switch User", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${boards.size}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Text(text = "Boards", fontSize = 12.sp, color = Color.Gray)
                    }
                    Divider(
                        modifier = Modifier
                            .height(24.dp)
                            .width(1.dp), color = Color.LightGray
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "${savedPins.size}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp
                        )
                        Text(text = "Saved Pins", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            // --- Toggle Tab Row ---
            TabRow(
                selectedTabIndex = activeSavedTab,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = activeSavedTab == 0,
                    onClick = { activeSavedTab = 0 },
                    text = { Text("Boards", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = activeSavedTab == 1,
                    onClick = { activeSavedTab = 1 },
                    text = { Text("All Saved Pins", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
                Tab(
                    selected = activeSavedTab == 2,
                    onClick = { activeSavedTab = 2 },
                    text = { Text("Creator Studio 👑", fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // --- Tab Contents ---
            if (activeSavedTab == 0) {
                // --- BOARDS GRID ---
                Box(modifier = Modifier.fillMaxSize()) {
                    if (boards.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = "No boards created yet.", color = Color.Gray)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(boards) { board ->
                                val boardPins = allPins.filter { it.boardId == board.id }
                                BoardGridCard(
                                    board = board,
                                    pinsCount = boardPins.size,
                                    onClick = { viewModel.selectBoard(board) }
                                )
                            }
                        }
                    }

                    // Floating Create Board FAB
                    ExtendedFloatingActionButton(
                        onClick = { showCreateBoardDialog = true },
                        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add") },
                        text = { Text("New Board", fontWeight = FontWeight.Bold) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = Color.White,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp)
                            .testTag("create_board_fab")
                    )
                }
            } else if (activeSavedTab == 1) {
                // --- SAVED PINS STAGGERED GRID ---
                if (savedPins.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📌", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "No saved pins yet.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                } else {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalItemSpacing = 10.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(savedPins) { pin ->
                            PinGridItem(
                                pin = pin,
                                onPinClicked = { onPinClicked(pin) },
                                onSaveClicked = { viewModel.toggleSavePin(pin.id) },
                                isAdminAuthor = viewModel.isAdminUser(pin.author)
                            )
                        }
                    }
                }
            } else {
                // --- CREATOR STUDIO CONTENT ---
                CreatorStudioContent(viewModel = viewModel, currentUser = currentUser)
            }
        }
    }

    // --- Create Board Dialog ---
    if (showCreateBoardDialog) {
        var boardName by remember { mutableStateOf("") }
        var boardDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreateBoardDialog = false },
            title = { Text("Create New Board", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = boardName,
                        onValueChange = { boardName = it },
                        label = { Text("Board Name") },
                        placeholder = { Text("e.g. Dream Living Room") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("board_name_input")
                    )

                    OutlinedTextField(
                        value = boardDesc,
                        onValueChange = { boardDesc = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Concepts, thoughts and inspiration notes...") },
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("board_desc_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (boardName.isNotBlank()) {
                            viewModel.createBoard(boardName, boardDesc)
                            showCreateBoardDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    enabled = boardName.isNotBlank(),
                    modifier = Modifier.testTag("board_confirm_button")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateBoardDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun BoardGridCard(
    board: Board,
    pinsCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageRes = remember(board.coverImageUrl) {
        if (board.coverImageUrl != null) {
            context.resources.getIdentifier(board.coverImageUrl, "drawable", context.packageName)
        } else {
            0
        }
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("board_card_${board.id}")
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (imageRes != 0) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = board.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Board folder",
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = board.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$pinsCount pins",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardDetailView(
    board: Board,
    pins: List<Pin>,
    onBackClicked: () -> Unit,
    onPinClicked: (Pin) -> Unit,
    onDeleteBoardClicked: () -> Unit,
    isAdminUser: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(board.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${pins.size} pins", fontSize = 11.sp, color = Color.Gray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onDeleteBoardClicked) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Board",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (board.description.isNotBlank()) {
                Text(
                    text = board.description,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)
            }

            if (pins.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "📂", fontSize = 40.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "This board is empty.", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(pins) { pin ->
                        PinGridItem(
                            pin = pin,
                            onPinClicked = { onPinClicked(pin) },
                            // Boards detail doesn't need to unsave right away, just standard toggle is fine
                            onSaveClicked = { },
                            isAdminAuthor = isAdminUser(pin.author)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorStudioContent(
    viewModel: PinViewModel,
    currentUser: com.example.ui.AppUser?,
    modifier: Modifier = Modifier
) {
    val isCreator = currentUser?.isCreator == true
    var bioText by remember(currentUser) { mutableStateOf(currentUser?.creatorBio ?: "") }
    var selectedCategory by remember(currentUser) { mutableStateOf(currentUser?.creatorCategory ?: "Art") }

    val categories = listOf("Architecture", "Culinary", "Fashion", "Art")

    val formatViewsHelper = { views: Int ->
        when {
            views >= 1_000_000 -> String.format("%.1fM", views / 1_000_000f)
            views >= 1_000 -> String.format("%.1fK", views / 1_000f)
            else -> views.toString()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(androidx.compose.foundation.rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!isCreator) {
            // Become Creator Promotion Card
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Join the Creator System! 👑",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Publish pins under your own unique artistic brand, explore trending niches, and let followers message you directly in your inbox!",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Text(
                text = "Set Up Your Creator Bio",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = bioText,
                onValueChange = { bioText = it },
                placeholder = { Text("What inspires you? Share your design/styling bio...") },
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Choose Your Specialty Category",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    val selected = selectedCategory == category
                    ElevatedFilterChip(
                        selected = selected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.registerAsCreator(bioText.ifBlank { "Inspirational visual explorer on Infinity" }, selectedCategory)
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Launch My Creator Studio 🚀", fontWeight = FontWeight.Bold)
            }
        } else {
            // Dashboard Panel
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Verified Creator Studio",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFD4AF37)) // Gold Accent
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "VERIFIED 👑", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = currentUser?.followersCount?.toString() ?: "148", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            Text(text = "Followers", fontSize = 11.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = formatViewsHelper(currentUser?.viewsCount ?: 12400), fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            Text(text = "Total Views", fontSize = 11.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text(text = "+18.4%", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color(0xFF4CAF50))
                            Text(text = "Monthly Reach", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }

            // --- MONETIZATION ELIGIBILITY CARD ---
            val followersGoal = 1000
            val viewsGoal = 1000000
            
            val currentFollowers = currentUser?.followersCount ?: 0
            val currentViews = currentUser?.viewsCount ?: 0
            
            val followersProgress = (currentFollowers.toFloat() / followersGoal).coerceIn(0f, 1f)
            val viewsProgress = (currentViews.toFloat() / viewsGoal).coerceIn(0f, 1f)
            
            val isEligible = currentFollowers >= followersGoal && currentViews >= viewsGoal
            val isAlreadyMonetized = currentUser?.isMonetized == true

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAlreadyMonetized) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    } else if (isEligible) {
                        Color(0xFFE8F5E9) // soft green for eligibility
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "💰 Monetization Eligibility",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (isAlreadyMonetized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            )
                        }

                        if (isAlreadyMonetized) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF4CAF50))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "MONETIZED 💎",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else if (isEligible) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFFB300))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "ELIGIBLE 🎉",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "In Progress",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "To enroll in the Partner Fund, you need at least 1,000 followers and 1,000,000 total clip views within a 5-month window.",
                        fontSize = 12.sp,
                        color = if (isAlreadyMonetized) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress 1: Followers
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Followers Goal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAlreadyMonetized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$currentFollowers / $followersGoal",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAlreadyMonetized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { followersProgress },
                            color = if (isAlreadyMonetized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    // Progress 2: Views
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Total Views (5 Months)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAlreadyMonetized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${formatViewsHelper(currentViews)} / 1.0M",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAlreadyMonetized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.primary
                            )
                        }
                        LinearProgressIndicator(
                            progress = { viewsProgress },
                            color = if (isAlreadyMonetized) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.secondary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Simulation Tools section
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "⚡ Creator Studio Simulator Controls",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = { viewModel.addFollowers(250) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+250 Followers", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.addViews(300000) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    modifier = Modifier.weight(1f).height(36.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("+300K Views", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Bottom Action Button
                    if (isAlreadyMonetized) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🎉 Verified partner program active! You are earning ad revenue Splits on visual Shorts views.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (isEligible) {
                        var showSuccessDialog by remember { mutableStateOf(false) }

                        Button(
                            onClick = {
                                viewModel.enrollInMonetization()
                                showSuccessDialog = true
                            },
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Activate Monetization Now! 🚀💰", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }

                        if (showSuccessDialog) {
                            AlertDialog(
                                onDismissRequest = { showSuccessDialog = false },
                                title = {
                                    Text(
                                        text = "Congratulations! 🎉💰",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp,
                                        color = Color(0xFF4CAF50)
                                    )
                                },
                                text = {
                                    Text(
                                        text = "You have met all monetization requirements of 1000+ followers and 1M+ views in under 5 months!\n\nYour account has been enrolled in the Creator Fund. Enjoy premium ad revenue splits on your Shorts! ✨",
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = { showSuccessDialog = false },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                                    ) {
                                        Text("Awesome!")
                                    }
                                }
                            )
                        }
                    } else {
                        Button(
                            onClick = { },
                            enabled = false,
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text("Locked (Requirements Not Met)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }

            Text(
                text = "Edit Your Creator Profile",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            OutlinedTextField(
                value = bioText,
                onValueChange = { bioText = it },
                label = { Text("Creator Biography") },
                maxLines = 3,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Specialty Niche",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { category ->
                    val selected = selectedCategory == category
                    ElevatedFilterChip(
                        selected = selected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) }
                    )
                }
            }

            Button(
                onClick = {
                    viewModel.updateCreatorProfile(bioText, selectedCategory)
                },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Save Profile Changes ✨", fontWeight = FontWeight.Bold)
            }
        }
    }
}
