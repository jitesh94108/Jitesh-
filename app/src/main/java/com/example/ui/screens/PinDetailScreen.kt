package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Pin
import com.example.ui.AiState
import com.example.ui.PinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinDetailScreen(
    viewModel: PinViewModel,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pin by viewModel.selectedPin.collectAsState()
    val boards by viewModel.allBoards.collectAsState()
    val comments by viewModel.currentPinComments.collectAsState()
    val aiNotesState by viewModel.aiNotesState.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val chatLoading by viewModel.chatLoading.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var commentText by remember { mutableStateOf("") }
    var chatMessageText by remember { mutableStateOf("") }
    var showBoardDropdown by remember { mutableStateOf(false) }

    // Track active sub-tab inside detail page: "Comments" or "Visual AI Assistant"
    var selectedDetailTab by remember { mutableStateOf(0) } // 0 = Assistant, 1 = Comments

    if (pin == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val activePin = pin!!
    val imageRes = remember(activePin.imageUrl) {
        if (activePin.isLocalResource) {
            context.resources.getIdentifier(activePin.imageUrl, "drawable", context.packageName)
        } else {
            0
        }
    }

    // Auto-fetch AI curator notes if idle
    LaunchedEffect(activePin.id) {
        if (viewModel.aiNotesState.value is AiState.Idle) {
            viewModel.getAICreatorNotes(activePin)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pin Details", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBackClicked, modifier = Modifier.testTag("detail_back")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Quick togglable bookmark button
                    IconButton(onClick = { viewModel.toggleSavePin(activePin.id) }) {
                        Icon(
                            imageVector = if (activePin.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            tint = if (activePin.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            contentDescription = "Save"
                        )
                    }

                    // Delete button if wanted (Admin Only)
                    if (currentUser?.isAdmin == true) {
                        IconButton(onClick = {
                            viewModel.deletePin(activePin)
                            onBackClicked()
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Pin")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main content area is scrollable except comment/chat inputs at bottom
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
            ) {
                // --- 1. Hero Image Card ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    if (activePin.isLocalResource && imageRes != 0) {
                        Image(
                            painter = painterResource(id = imageRes),
                            contentDescription = activePin.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp),
                            contentScale = ContentScale.FillWidth
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🎨 Visual Inspiration", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // --- 2. Title & Description ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = activePin.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 30.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = activePin.author.firstOrNull()?.uppercase() ?: "P",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Created by ${activePin.author}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (viewModel.isAdminUser(activePin.author)) {
                                Icon(
                                    imageVector = Icons.Filled.Verified,
                                    contentDescription = "Verified Administrator",
                                    tint = Color(0xFF1DA1F2),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = activePin.description,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                        lineHeight = 22.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // --- 3. Save to Board Dropdown Selector ---
                    Box(modifier = Modifier.fillMaxWidth()) {
                        val currentBoardName = boards.firstOrNull { it.id == activePin.boardId }?.name ?: "No Board"
                        Button(
                            onClick = { showBoardDropdown = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Folder, contentDescription = "Board")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Board: $currentBoardName",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }

                        DropdownMenu(
                            expanded = showBoardDropdown,
                            onDismissRequest = { showBoardDropdown = false },
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("None (Remove from Board)", color = Color.Red) },
                                onClick = {
                                    viewModel.savePinToBoard(activePin.id, null)
                                    showBoardDropdown = false
                                }
                            )
                            Divider()
                            boards.forEach { board ->
                                DropdownMenuItem(
                                    text = { Text(board.name) },
                                    onClick = {
                                        viewModel.savePinToBoard(activePin.id, board.id)
                                        showBoardDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                Divider(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // --- 4. Sub-Tabs Section: Assistant vs Comments ---
                TabRow(
                    selectedTabIndex = selectedDetailTab,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedDetailTab]),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                ) {
                    Tab(
                        selected = selectedDetailTab == 0,
                        onClick = { selectedDetailTab = 0 },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("✨ AI Co-pilot", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    )
                    Tab(
                        selected = selectedDetailTab == 1,
                        onClick = { selectedDetailTab = 1 },
                        text = {
                            Text("💬 Comments (${comments.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    )
                }

                // --- 5. Selected Tab Content ---
                if (selectedDetailTab == 0) {
                    // --- TAB: AI Assistant ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // AI Notes Box
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("✨", fontSize = 20.sp)
                                    Text(
                                        text = "AI Curator Insight Note",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                when (val state = aiNotesState) {
                                    is AiState.Loading -> {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            CircularProgressIndicator(
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                            Text(
                                                "Curating aesthetic analysis and tips...",
                                                fontSize = 13.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }

                                    is AiState.Success -> {
                                        Text(
                                            text = state.response,
                                            fontSize = 14.sp,
                                            lineHeight = 21.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    is AiState.Error -> {
                                        Text(
                                            text = "Failed to load notes. Check your API configuration.",
                                            color = MaterialTheme.colorScheme.error,
                                            fontSize = 13.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Button(
                                            onClick = { viewModel.getAICreatorNotes(activePin) },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = MaterialTheme.colorScheme.primary
                                            )
                                        ) {
                                            Text("Retry")
                                        }
                                    }

                                    else -> {
                                        // Idle state or ungenerated
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual Co-pilot Chat
                        Text(
                            text = "Ask Visual Co-pilot",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (chatHistory.isEmpty()) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                PaddingValues(12.dp)
                                Text(
                                    text = "Ask anything about this pin! E.g., \"What color palettes match this?\" or \"How do I style this outfit?\"",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                chatHistory.forEach { (sender, text) ->
                                    val isUser = sender == "user"
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
                                    ) {
                                        Card(
                                            shape = RoundedCornerShape(
                                                topStart = 12.dp,
                                                topEnd = 12.dp,
                                                bottomStart = if (isUser) 12.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 12.dp
                                            ),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                            ),
                                            modifier = Modifier.fillMaxWidth(0.85f)
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Text(
                                                    text = if (isUser) "You" else "✨ Co-pilot",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp,
                                                    color = if (isUser) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary
                                                )
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = text,
                                                    fontSize = 13.sp,
                                                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                                    lineHeight = 18.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (chatLoading) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth()
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Text("Co-pilot is writing...", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                } else {
                    // --- TAB: Comments ---
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (comments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Be the first to comment on this inspiration!",
                                    color = Color.Gray,
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            comments.forEach { comment ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    // Commenter Avatar initial
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.LightGray.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = comment.author.firstOrNull()?.uppercase() ?: "U",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = comment.author,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (viewModel.isAdminUser(comment.author)) {
                                                Icon(
                                                    imageVector = Icons.Filled.Verified,
                                                    contentDescription = "Verified Administrator",
                                                    tint = Color(0xFF1DA1F2),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = comment.text,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- 6. Input Area at bottom (Contextual based on active tab) ---
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding() // crucial notch/bar protection!
                        .imePadding()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    if (selectedDetailTab == 0) {
                        // AI Chat Input
                        OutlinedTextField(
                            value = chatMessageText,
                            onValueChange = { chatMessageText = it },
                            placeholder = { Text("Ask Co-pilot about this idea...", fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_input")
                        )

                        IconButton(
                            onClick = {
                                if (chatMessageText.isNotBlank()) {
                                    viewModel.chatWithAssistant(activePin, chatMessageText)
                                    chatMessageText = ""
                                }
                            },
                            enabled = !chatLoading && chatMessageText.isNotBlank(),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (chatMessageText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.LightGray.copy(
                                        alpha = 0.3f
                                    )
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (chatMessageText.isNotBlank()) Color.White else Color.Gray
                            )
                        }
                    } else {
                        // Standard Comments Input
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Add a comment...", fontSize = 14.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_input")
                        )

                        IconButton(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    val authorName = currentUser?.username ?: "Guest"
                                    viewModel.addComment(activePin.id, authorName, commentText)
                                    commentText = ""
                                }
                            },
                            enabled = commentText.isNotBlank(),
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary else Color.LightGray.copy(
                                        alpha = 0.3f
                                    )
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Post Comment",
                                tint = if (commentText.isNotBlank()) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
