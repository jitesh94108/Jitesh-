package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.AppDatabase
import com.example.data.Board
import com.example.data.Comment
import com.example.data.GeminiHelper
import com.example.data.Pin
import com.example.data.PinRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AiState {
    object Idle : AiState
    object Loading : AiState
    data class Success(val response: String) : AiState
    data class Error(val message: String) : AiState
}

sealed interface AiFillState {
    object Idle : AiFillState
    object Loading : AiFillState
    data class Success(val title: String, val description: String, val category: String) : AiFillState
    data class Error(val message: String) : AiFillState
}

data class AppUser(
    val username: String,
    val email: String? = null,
    val isAdmin: Boolean = false,
    val isCreator: Boolean = false,
    val creatorBio: String = "Inspiring visual curator on Infinity.",
    val creatorCategory: String = "Art",
    val creatorBannerUrl: String = "",
    val followersCount: Int = 148,
    val viewsCount: Int = 12400,
    val isMonetized: Boolean = false
)

data class CreatorProfile(
    val username: String,
    val bio: String,
    val specialtyCategory: String,
    val followersCount: Int,
    val initialMessage: String
)

data class ChatMessage(
    val id: Int,
    val sender: String,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ShortClip(
    val id: Int,
    val title: String,
    val description: String,
    val imageResName: String,
    val creator: String,
    val likesCount: Int,
    val isLiked: Boolean = false,
    val isSaved: Boolean = false,
    val comments: List<Comment> = emptyList()
)

class PinViewModel(application: Application) : AndroidViewModel(application) {

    // Logged-in User State
    private val _currentUser = MutableStateFlow<AppUser?>(null)
    val currentUser: StateFlow<AppUser?> = _currentUser.asStateFlow()

    // Followed Creators
    private val _followedCreators = MutableStateFlow<Set<String>>(setOf("CabinArchitect", "StyleInspo"))
    val followedCreators: StateFlow<Set<String>> = _followedCreators.asStateFlow()

    // Creator Profiles
    val creatorProfiles = mapOf(
        "CabinArchitect" to CreatorProfile(
            username = "CabinArchitect",
            bio = "Building cozy dream retreats in nature. Eco-conscious A-frame architecture.",
            specialtyCategory = "Architecture",
            followersCount = 12500,
            initialMessage = "Hey! Thanks for following. Cozy cabins are my passion—have you seen my misty forest design yet?"
        ),
        "PastryChef_" to CreatorProfile(
            username = "PastryChef_",
            bio = "Exquisite French pastries & sweet delights. Plating art with pure passion.",
            specialtyCategory = "Culinary",
            followersCount = 8400,
            initialMessage = "Bonjour! Welcome to my kitchen. What is your favorite dessert to bake?"
        ),
        "StyleInspo" to CreatorProfile(
            username = "StyleInspo",
            bio = "Curating daily minimalist wardrobe ideas & warm cozy autumn outfit palettes.",
            specialtyCategory = "Fashion",
            followersCount = 9300,
            initialMessage = "Hi there! Let's elevate your styling game. Ask me any cozy wardrobe question!"
        ),
        "DreamWeaverArt" to CreatorProfile(
            username = "DreamWeaverArt",
            bio = "Surreal digital artist creating cosmic neon dreamscapes and celestial spheres.",
            specialtyCategory = "Art",
            followersCount = 15100,
            initialMessage = "Greetings, space traveler. Let's create beautiful dreamscapes together!"
        )
    )

    // Chat History with Creators
    private val _chats = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val chats: StateFlow<Map<String, List<ChatMessage>>> = _chats.asStateFlow()

    private val _isChatGenerating = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val isChatGenerating: StateFlow<Map<String, Boolean>> = _isChatGenerating.asStateFlow()

    // Visual Shorts list state
    private val _shorts = MutableStateFlow<List<ShortClip>>(emptyList())
    val shorts: StateFlow<List<ShortClip>> = _shorts.asStateFlow()

    fun login(username: String, password: String): Boolean {
        if (username == "jitesh5416" && password == "jitesh@2008") {
            _currentUser.value = AppUser(
                username = "jitesh5416",
                email = "jitesh5416@gmail.com",
                isAdmin = true,
                isCreator = true,
                creatorBio = "Official Administrator & Creative Architect"
            )
            return true
        }
        return false
    }

    fun loginAsNormal(username: String) {
        val finalUsername = if (username.isBlank()) "Guest" else username
        _currentUser.value = AppUser(username = finalUsername, isAdmin = false)
    }

    fun loginWithGmail(email: String): Boolean {
        if (!email.contains("@") || !email.endsWith(".com")) return false
        val prefix = email.substringBefore("@")
        val isAdmin = email == "jitesh9410875804@gmail.com" || prefix == "jitesh5416"
        _currentUser.value = AppUser(
            username = prefix,
            email = email,
            isAdmin = isAdmin,
            isCreator = true,
            creatorBio = "Artistic visual explorer & content creator"
        )
        return true
    }

    fun logout() {
        _currentUser.value = null
    }

    fun toggleFollowCreator(creatorName: String) {
        val currentSet = _followedCreators.value.toMutableSet()
        if (currentSet.contains(creatorName)) {
            currentSet.remove(creatorName)
        } else {
            currentSet.add(creatorName)
            // Trigger welcome message from followed creator
            val welcomeText = creatorProfiles[creatorName]?.initialMessage ?: "Hi! Thanks for the follow!"
            addCreatorSystemMessage(creatorName, welcomeText)
        }
        _followedCreators.value = currentSet
    }

    fun registerAsCreator(bio: String, category: String) {
        val user = _currentUser.value ?: return
        _currentUser.value = user.copy(
            isCreator = true,
            creatorBio = bio,
            creatorCategory = category
        )
    }

    fun updateCreatorProfile(bio: String, category: String) {
        val user = _currentUser.value ?: return
        _currentUser.value = user.copy(
            creatorBio = bio,
            creatorCategory = category
        )
    }

    fun addFollowers(amount: Int) {
        val user = _currentUser.value ?: return
        _currentUser.value = user.copy(
            followersCount = user.followersCount + amount
        )
    }

    fun addViews(amount: Int) {
        val user = _currentUser.value ?: return
        _currentUser.value = user.copy(
            viewsCount = user.viewsCount + amount
        )
    }

    fun enrollInMonetization() {
        val user = _currentUser.value ?: return
        _currentUser.value = user.copy(
            isMonetized = true
        )
    }

    fun isAdminUser(username: String): Boolean {
        val nameLower = username.trim().lowercase()
        return nameLower == "jitesh5416" || nameLower == "jitesh9410875804" || nameLower == "admin" || nameLower == "administrator" || 
               (_currentUser.value?.username?.trim()?.lowercase() == nameLower && _currentUser.value?.isAdmin == true)
    }

    // Interactive messaging helper
    private fun addCreatorSystemMessage(creatorName: String, text: String) {
        val currentChats = _chats.value.toMutableMap()
        val history = currentChats[creatorName]?.toMutableList() ?: mutableListOf()
        if (history.none { it.text == text }) {
            history.add(ChatMessage(id = history.size + 1, sender = creatorName, text = text))
            currentChats[creatorName] = history
            _chats.value = currentChats
        }
    }

    fun sendChatMessageToCreator(creatorName: String, text: String) {
        if (text.isBlank()) return
        val currentChats = _chats.value.toMutableMap()
        val history = currentChats[creatorName]?.toMutableList() ?: mutableListOf()
        val userName = _currentUser.value?.username ?: "Guest"

        history.add(ChatMessage(id = history.size + 1, sender = userName, text = text))
        currentChats[creatorName] = history
        _chats.value = currentChats

        // Mark creator as generating response
        val currentGenerating = _isChatGenerating.value.toMutableMap()
        currentGenerating[creatorName] = true
        _isChatGenerating.value = currentGenerating

        // Call Gemini to simulate a highly contextual, customized reply in character
        viewModelScope.launch {
            val profile = creatorProfiles[creatorName]
            val specialty = profile?.specialtyCategory ?: "Art"
            val bio = profile?.bio ?: "Visual creator on Infinity."

            val chatPrompt = history.joinToString("\n") { msg ->
                "${if (msg.sender == userName) "User" else creatorName}: ${msg.text}"
            }

            val prompt = """
                You are $creatorName, a highly creative verified creator on Infinity.
                Your Specialty category is: $specialty.
                Your Creator Bio is: "$bio".
                
                The user ($userName) is messaging you about your creative design projects. 
                Here is the recent message logs:
                $chatPrompt
                
                Please reply back as $creatorName. Be incredibly inspiring, enthusiastic, friendly, and aligned with your specialty. Keep your reply concise (1 to 3 sentences maximum) and formatted clearly. Do not use complex markdown or cite that you are an AI model.
            """.trimIndent()

            val response = GeminiHelper.generateContent(
                prompt = prompt,
                systemInstruction = "You are $creatorName, a warm and inspiring Pinterest creator. Speak authentically as a stylist, architect, artist, or chef, and guide the user in your specialty."
            )

            val finalReply = if (response.startsWith("Error")) {
                "That's so inspiring! Thanks for sharing. Let's keep exploring amazing visual concept designs together!"
            } else {
                response
            }

            // Post reply
            val updatedChats = _chats.value.toMutableMap()
            val updatedHistory = updatedChats[creatorName]?.toMutableList() ?: mutableListOf()
            updatedHistory.add(ChatMessage(id = updatedHistory.size + 1, sender = creatorName, text = finalReply))
            updatedChats[creatorName] = updatedHistory
            _chats.value = updatedChats

            // Turn off generating
            val updatedGenerating = _isChatGenerating.value.toMutableMap()
            updatedGenerating[creatorName] = false
            _isChatGenerating.value = updatedGenerating
        }
    }

    // Shorts Features
    fun initializeShorts() {
        if (_shorts.value.isNotEmpty()) return
        _shorts.value = listOf(
            ShortClip(
                id = 1,
                title = "Rainy Forest Cabin Twilight Loop 🌲🌧️",
                description = "Immersive relaxing twilight view from our black A-frame forest cabin during a gentle thunderstorm. Sound of rain pouring down.",
                imageResName = "img_pin_architecture",
                creator = "CabinArchitect",
                likesCount = 2450,
                comments = listOf(
                    Comment(pinId = -1, author = "Wanderlust", text = "This is pure ASMR for my soul..."),
                    Comment(pinId = -1, author = "GeoM", text = "Where is this cabin located exactly?")
                )
            ),
            ShortClip(
                id = 2,
                title = "Gourmet Strawberry Plating Art 🍓🧁",
                description = "Watch the elegant process of piping creamy green tea matcha and dusting fine gold flakes on fresh French tarts.",
                imageResName = "img_pin_culinary",
                creator = "PastryChef_",
                likesCount = 1890,
                comments = listOf(
                    Comment(pinId = -1, author = "BakerPro", text = "The gold flake dusting is absolutely top-tier!"),
                    Comment(pinId = -1, author = "Emma3", text = "I am drooling over this matcha color.")
                )
            ),
            ShortClip(
                id = 3,
                title = "Cozy Trench Styling Lookbook 🧥🍁",
                description = "Transitioning from warm lazy summer attire to a chic, layered autumn trench coat look. Stylist ideas.",
                imageResName = "img_pin_fashion",
                creator = "StyleInspo",
                likesCount = 3120,
                comments = listOf(
                    Comment(pinId = -1, author = "FashionLover", text = "Perfect outfit coordinates. Added to my fall catalog!"),
                    Comment(pinId = -1, author = "Luna", text = "Which brand is that cream knit sweater?")
                )
            ),
            ShortClip(
                id = 4,
                title = "Vaporwave Neon Sphere Creation 🌌🎨",
                description = "Watch me blend glowing neon spheres with stellar dust clouds to draft our latest celestial dreamscape illustration.",
                imageResName = "img_pin_art",
                creator = "DreamWeaverArt",
                likesCount = 4920,
                comments = listOf(
                    Comment(pinId = -1, author = "PixelKnight", text = "What digital brush set is this? The neon bleed is unreal."),
                    Comment(pinId = -1, author = "Synth", text = "Truly futuristic. Absolutely mind blowing colors!")
                )
            )
        )
    }

    fun likeShort(clipId: Int) {
        val updated = _shorts.value.map { clip ->
            if (clip.id == clipId) {
                val isLikedNow = !clip.isLiked
                clip.copy(
                    isLiked = isLikedNow,
                    likesCount = if (isLikedNow) clip.likesCount + 1 else clip.likesCount - 1
                )
            } else clip
        }
        _shorts.value = updated
    }

    fun toggleSaveShort(clipId: Int) {
        val updated = _shorts.value.map { clip ->
            if (clip.id == clipId) {
                clip.copy(isSaved = !clip.isSaved)
            } else clip
        }
        _shorts.value = updated
    }

    fun addShortComment(clipId: Int, author: String, text: String) {
        if (text.isBlank()) return
        val updated = _shorts.value.map { clip ->
            if (clip.id == clipId) {
                val newComments = clip.comments.toMutableList()
                newComments.add(Comment(id = newComments.size + 1, pinId = -clipId, author = author, text = text))
                clip.copy(comments = newComments)
            } else clip
        }
        _shorts.value = updated
    }

    private val db = Room.databaseBuilder(
        application,
        AppDatabase::class.java,
        "prinrest_db_new"
    ).fallbackToDestructiveMigration().build()

    private val repository = PinRepository(
        db.pinDao(),
        db.boardDao(),
        db.commentDao()
    )

    // State Flows
    val allPins: StateFlow<List<Pin>> = repository.allPins.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allBoards: StateFlow<List<Board>> = repository.allBoards.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val savedPins: StateFlow<List<Pin>> = repository.savedPins.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Active/Selected items
    private val _selectedPinId = MutableStateFlow<Int?>(null)
    val selectedPinId: StateFlow<Int?> = _selectedPinId.asStateFlow()

    private val _selectedPin = MutableStateFlow<Pin?>(null)
    val selectedPin: StateFlow<Pin?> = _selectedPin.asStateFlow()

    private val _selectedBoard = MutableStateFlow<Board?>(null)
    val selectedBoard: StateFlow<Board?> = _selectedBoard.asStateFlow()

    // Comments flow for selected pin
    val currentPinComments: StateFlow<List<Comment>> = _selectedPinId.flatMapLatest { pinId ->
        if (pinId != null) {
            repository.getCommentsForPin(pinId)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // AI Assistant state
    private val _aiNotesState = MutableStateFlow<AiState>(AiState.Idle)
    val aiNotesState: StateFlow<AiState> = _aiNotesState.asStateFlow()

    // AI Fill State for Creation
    private val _aiFillState = MutableStateFlow<AiFillState>(AiFillState.Idle)
    val aiFillState: StateFlow<AiFillState> = _aiFillState.asStateFlow()

    // Chat History
    private val _chatHistory = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val chatHistory: StateFlow<List<Pair<String, String>>> = _chatHistory.asStateFlow()

    private val _chatLoading = MutableStateFlow(false)
    val chatLoading: StateFlow<Boolean> = _chatLoading.asStateFlow()

    init {
        // Run database prepopulation on startup
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
        }
    }

    fun selectPin(pinId: Int) {
        _selectedPinId.value = pinId
        _chatHistory.value = emptyList() // clear chat on pin change
        _aiNotesState.value = AiState.Idle
        viewModelScope.launch {
            _selectedPin.value = repository.getPinById(pinId)
        }
    }

    fun selectBoard(board: Board?) {
        _selectedBoard.value = board
    }

    fun toggleSavePin(pinId: Int) {
        viewModelScope.launch {
            val pin = repository.getPinById(pinId) ?: return@launch
            val updated = pin.copy(isSaved = !pin.isSaved)
            repository.updatePin(updated)
            // If the pin is currently active, update active selectedPin
            if (_selectedPin.value?.id == pinId) {
                _selectedPin.value = updated
            }
        }
    }

    fun savePinToBoard(pinId: Int, boardId: Int?) {
        viewModelScope.launch {
            val pin = repository.getPinById(pinId) ?: return@launch
            val updated = pin.copy(boardId = boardId, isSaved = boardId != null || pin.isSaved)
            repository.updatePin(updated)
            if (_selectedPin.value?.id == pinId) {
                _selectedPin.value = updated
            }
        }
    }

    fun addComment(pinId: Int, author: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            repository.insertComment(
                Comment(
                    pinId = pinId,
                    author = author.ifBlank { "Anonym" },
                    text = text
                )
            )
        }
    }

    fun createBoard(name: String, description: String, coverImageUrl: String? = null) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.insertBoard(
                Board(
                    name = name,
                    description = description,
                    coverImageUrl = coverImageUrl ?: "img_brand_logo_new_1784001293536"
                )
            )
        }
    }

    fun createPin(title: String, description: String, imageUrl: String, category: String, boardId: Int?) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.insertPin(
                Pin(
                    title = title,
                    description = description,
                    imageUrl = imageUrl,
                    isLocalResource = imageUrl.startsWith("img_"),
                    category = category,
                    boardId = boardId,
                    isSaved = true // Auto-save newly created pins!
                )
            )
        }
    }

    fun deletePin(pin: Pin) {
        viewModelScope.launch {
            repository.deletePin(pin)
        }
    }

    fun deleteBoard(board: Board) {
        viewModelScope.launch {
            repository.deleteBoard(board)
        }
    }

    // --- Gemini AI Integrations ---

    fun getAICreatorNotes(pin: Pin) {
        _aiNotesState.value = AiState.Loading
        viewModelScope.launch {
            val prompt = """
                Analyze this Pin from our Visual Discoveries app:
                Title: ${pin.title}
                Category: ${pin.category}
                Description: ${pin.description}
                
                Please provide:
                1. A brief "Aesthetic Breakdown" explaining why this visual is compelling.
                2. A "How-to/DIY Guide" or "Styling Tips" on how a user can replicate or incorporate this idea into their own life (e.g. materials, steps, recipes, or clothing combinations based on the category).
                3. A few suggested related tags (prefixed with #).
                
                Keep the tone warm, highly engaging, and inspirational. Use clean markdown styling.
            """.trimIndent()

            val response = GeminiHelper.generateContent(
                prompt = prompt,
                systemInstruction = "You are the head visual curator and stylist of Infinity, a premium visual-discovery app. You write insightful and inspirational DIY/Styling notes for visual inspiration items."
            )

            if (response.startsWith("Error")) {
                _aiNotesState.value = AiState.Error(response)
            } else {
                _aiNotesState.value = AiState.Success(response)
            }
        }
    }

    fun chatWithAssistant(pin: Pin, message: String) {
        if (message.isBlank()) return
        val currentHistory = _chatHistory.value.toMutableList()
        currentHistory.add(Pair("user", message))
        _chatHistory.value = currentHistory
        _chatLoading.value = true

        viewModelScope.launch {
            val historyPrompt = _chatHistory.value.joinToString("\n") { (sender, text) ->
                "${if (sender == "user") "User" else "Assistant"}: $text"
            }

            val prompt = """
                The user is viewing this Pin on Infinity:
                Title: ${pin.title}
                Description: ${pin.description}
                Category: ${pin.category}
                Creator: ${pin.author}
                
                Conversation history:
                $historyPrompt
                
                Please answer the user's latest query in detail, continuing the conversation. Be helpful, specific, and enthusiastic about the creative concepts in the visual. Keep the response formatted neatly.
            """.trimIndent()

            val response = GeminiHelper.generateContent(
                prompt = prompt,
                systemInstruction = "You are the Infinity Creative Assistant, a friendly and knowledgeable styling, DIY, and visual concept helper. You help users style rooms, cook dishes, create art, or choose outfits based on Pins."
            )

            currentHistory.add(Pair("ai", response))
            _chatHistory.value = currentHistory
            _chatLoading.value = false
        }
    }

    fun autoFillPinDetails(userConcept: String) {
        if (userConcept.isBlank()) return
        _aiFillState.value = AiFillState.Loading
        viewModelScope.launch {
            val prompt = """
                The user has an idea for an Infinity pin. Their concept is: "$userConcept".
                Please expand this concept into standard fields for our form.
                
                Provide a JSON-like block with exactly these keys:
                TITLE: <an engaging, clickable title>
                DESCRIPTION: <a descriptive, rich, and elegant description (2-3 sentences) detailing the aesthetic, style, or recipe>
                CATEGORY: <Choose the single most appropriate category from: "Architecture", "Culinary", "Fashion", "Art", "Other">
                
                Output ONLY these fields, in plain text. Format:
                [TITLE] <expanded title>
                [DESCRIPTION] <expanded description>
                [CATEGORY] <one of the category names>
            """.trimIndent()

            val response = GeminiHelper.generateContent(
                prompt = prompt,
                systemInstruction = "You are the Infinity Content Creator Assistant. You expand simple raw thoughts into beautiful, descriptive, search-optimized Pins for a visual discovery feed."
            )

            try {
                val titleRegex = "\\[TITLE\\]\\s*(.*)".toRegex(RegexOption.IGNORE_CASE)
                val descRegex = "\\[DESCRIPTION\\]\\s*(.*)".toRegex(RegexOption.IGNORE_CASE)
                val catRegex = "\\[CATEGORY\\]\\s*(.*)".toRegex(RegexOption.IGNORE_CASE)

                val title = titleRegex.find(response)?.groupValues?.get(1)?.trim() ?: "AI Inspired Idea"
                val description = descRegex.find(response)?.groupValues?.get(1)?.trim() ?: userConcept
                var category = catRegex.find(response)?.groupValues?.get(1)?.trim() ?: "Art"

                // Normalize category
                val validCats = listOf("Architecture", "Culinary", "Fashion", "Art")
                if (!validCats.any { it.equals(category, ignoreCase = true) }) {
                    category = "Art"
                } else {
                    category = validCats.first { it.equals(category, ignoreCase = true) }
                }

                _aiFillState.value = AiFillState.Success(title, description, category)
            } catch (e: Exception) {
                _aiFillState.value = AiFillState.Error("Failed to parse AI output: ${e.message}")
            }
        }
    }

    fun clearAiFillState() {
        _aiFillState.value = AiFillState.Idle
    }
}
