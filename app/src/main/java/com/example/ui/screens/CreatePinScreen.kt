package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.AiFillState
import com.example.ui.PinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePinScreen(
    viewModel: PinViewModel,
    onPinCreated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boards by viewModel.allBoards.collectAsState()
    val aiFillState by viewModel.aiFillState.collectAsState()

    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Art") }
    var selectedBoardId by remember { mutableStateOf<Int?>(null) }
    var selectedImageName by remember { mutableStateOf("img_pin_art") }

    var aiConceptInput by remember { mutableStateOf("") }
    var showBoardDropdown by remember { mutableStateOf(false) }

    val categories = listOf("Architecture", "Culinary", "Fashion", "Art")

    // Image templates that match the generated drawables in the app
    val imageTemplates = listOf(
        Pair("Architecture", "img_pin_architecture"),
        Pair("Culinary", "img_pin_culinary"),
        Pair("Fashion", "img_pin_fashion"),
        Pair("Art", "img_pin_art")
    )

    // Respond to AI Fill successes
    LaunchedEffect(aiFillState) {
        if (aiFillState is AiFillState.Success) {
            val successState = aiFillState as AiFillState.Success
            title = successState.title
            description = successState.description
            category = successState.category
            // Map category to template image for convenience
            selectedImageName = when (successState.category) {
                "Architecture" -> "img_pin_architecture"
                "Culinary" -> "img_pin_culinary"
                "Fashion" -> "img_pin_fashion"
                else -> "img_pin_art"
            }
            viewModel.clearAiFillState()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Scrollable Form area
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Create",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Create Visual Pin",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // --- SECTION 1: AI ASSISTANT SPARKS ---
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✨", fontSize = 18.sp)
                        Text(
                            text = "AI Creator Magic Spark",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Enter a simple concept idea. Gemini will automatically fill out a title, description, and categorize it!",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = aiConceptInput,
                            onValueChange = { aiConceptInput = it },
                            placeholder = { Text("e.g. neon space coffee shop", fontSize = 13.sp) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("ai_concept_input")
                        )

                        Button(
                            onClick = { viewModel.autoFillPinDetails(aiConceptInput) },
                            enabled = aiConceptInput.isNotBlank() && aiFillState !is AiFillState.Loading,
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("ai_autofill_button")
                        ) {
                            if (aiFillState is AiFillState.Loading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Autofill",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Autofill", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (aiFillState is AiFillState.Error) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = (aiFillState as AiFillState.Error).message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Divider(color = Color.LightGray.copy(alpha = 0.3f), thickness = 1.dp)

            // --- SECTION 2: CHOOSE COVER IMAGE TEMPLATE ---
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Select Image Style",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(imageTemplates) { (name, drawableName) ->
                        val resId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
                        val isSelected = selectedImageName == drawableName

                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(100.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(
                                        alpha = 0.5f
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    selectedImageName = drawableName
                                    // Optionally map category if user selects an image style
                                    category = name
                                }
                        ) {
                            if (resId != 0) {
                                Image(
                                    painter = painterResource(id = resId),
                                    contentDescription = name,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // --- SECTION 3: STANDARD FORM ---
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Pin Title") },
                placeholder = { Text("What is this visual idea about?") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_title_input")
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Detail the aesthetic vibe, materials, recipes, steps, or styling elements...") },
                maxLines = 4,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_description_input")
            )

            // Category choice row chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Category",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    categories.forEach { cat ->
                        val isSelected = category == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Board Selection Dropdown
            Box(modifier = Modifier.fillMaxWidth()) {
                val currentBoardName = boards.firstOrNull { it.id == selectedBoardId }?.name ?: "Save to Board (None)"
                Button(
                    onClick = { showBoardDropdown = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Folder, contentDescription = "Folder")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = currentBoardName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
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
                        text = { Text("None") },
                        onClick = {
                            selectedBoardId = null
                            showBoardDropdown = false
                        }
                    )
                    Divider()
                    boards.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b.name) },
                            onClick = {
                                selectedBoardId = b.id
                                showBoardDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Action Buttons Bar
        Surface(
            tonalElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp)
            ) {
                OutlinedButton(
                    onClick = onPinCreated, // Cancel goes back/reset
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.createPin(
                                title = title,
                                description = description,
                                imageUrl = selectedImageName,
                                category = category,
                                boardId = selectedBoardId
                            )
                            onPinCreated() // Navigate back/success
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("publish_pin_button")
                ) {
                    Text("Publish", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
