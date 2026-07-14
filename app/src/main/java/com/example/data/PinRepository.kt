package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class PinRepository(
    private val pinDao: PinDao,
    private val boardDao: BoardDao,
    private val commentDao: CommentDao
) {
    val allPins: Flow<List<Pin>> = pinDao.getAllPins()
    val allBoards: Flow<List<Board>> = boardDao.getAllBoards()
    val savedPins: Flow<List<Pin>> = pinDao.getSavedPins()

    fun getPinsByCategory(category: String): Flow<List<Pin>> = pinDao.getPinsByCategory(category)
    fun getPinsByBoard(boardId: Int): Flow<List<Pin>> = pinDao.getPinsByBoard(boardId)
    fun getCommentsForPin(pinId: Int): Flow<List<Comment>> = commentDao.getCommentsForPin(pinId)

    suspend fun getPinById(id: Int): Pin? = pinDao.getPinById(id)
    suspend fun getBoardById(id: Int): Board? = boardDao.getBoardById(id)

    suspend fun insertPin(pin: Pin): Long = pinDao.insertPin(pin)
    suspend fun updatePin(pin: Pin) = pinDao.updatePin(pin)
    suspend fun deletePin(pin: Pin) = pinDao.deletePin(pin)

    suspend fun insertBoard(board: Board): Long = boardDao.insertBoard(board)
    suspend fun deleteBoard(board: Board) = boardDao.deleteBoard(board)

    suspend fun insertComment(comment: Comment): Long = commentDao.insertComment(comment)

    // Prepopulate database if it's empty
    suspend fun prepopulateIfEmpty() = withContext(Dispatchers.IO) {
        val existingPins = pinDao.getAllPins().first()
        if (existingPins.isEmpty()) {
            // Create default boards
            val board1Id = boardDao.insertBoard(
                Board(
                    name = "Cozy Escapes",
                    description = "Inspirational architectural concepts and cozy retreats.",
                    coverImageUrl = "img_pin_architecture"
                )
            ).toInt()

            val board2Id = boardDao.insertBoard(
                Board(
                    name = "Artistic Inspiration",
                    description = "Creative digital artwork, color palettes, and vibrant visuals.",
                    coverImageUrl = "img_pin_art"
                )
            ).toInt()

            val board3Id = boardDao.insertBoard(
                Board(
                    name = "Culinary Wonders",
                    description = "Delectable pastries, unique food plating, and recipes.",
                    coverImageUrl = "img_pin_culinary"
                )
            ).toInt()

            val board4Id = boardDao.insertBoard(
                Board(
                    name = "Minimalist Style",
                    description = "Neutral wardrobes, trench coats, and chic fashion ideas.",
                    coverImageUrl = "img_pin_fashion"
                )
            ).toInt()

            // Create initial pins
            val pin1 = Pin(
                title = "A-Frame Forest Cabin",
                description = "A gorgeous modern black A-frame cabin nestled deep in a misty pine forest. Cozy twilight glows from the massive floor-to-ceiling glass windows.",
                imageUrl = "img_pin_architecture",
                isLocalResource = true,
                category = "Architecture",
                boardId = board1Id,
                isSaved = true,
                author = "CabinArchitect",
                likesCount = 342
            )

            val pin2 = Pin(
                title = "Strawberry Matcha Tart",
                description = "An exquisite French gourmet dessert. Creamy green tea matcha custard filled with fresh strawberries, gold leaf accents, and a rich butter shortbread crust.",
                imageUrl = "img_pin_culinary",
                isLocalResource = true,
                category = "Culinary",
                boardId = board3Id,
                isSaved = true,
                author = "PastryChef_",
                likesCount = 521
            )

            val pin3 = Pin(
                title = "Minimalist Autumn Trench Coat",
                description = "The ultimate cozy chic look. Styling a classic double-breasted beige trench coat over a cream knit sweater with neutral tones for cool crisp weather.",
                imageUrl = "img_pin_fashion",
                isLocalResource = true,
                category = "Fashion",
                boardId = board4Id,
                isSaved = true,
                author = "StyleInspo",
                likesCount = 215
            )

            val pin4 = Pin(
                title = "Neon Cosmic Dream",
                description = "Vibrant surreal digital art illustrating floating neon celestial spheres and starry dust clouds over a fantasy pastel landscape. Full of magical details.",
                imageUrl = "img_pin_art",
                isLocalResource = true,
                category = "Art",
                boardId = board2Id,
                isSaved = true,
                author = "DreamWeaverArt",
                likesCount = 895
            )

            val p1Id = pinDao.insertPin(pin1).toInt()
            val p2Id = pinDao.insertPin(pin2).toInt()
            val p3Id = pinDao.insertPin(pin3).toInt()
            val p4Id = pinDao.insertPin(pin4).toInt()

            // Add default comments to initial pins
            commentDao.insertComment(Comment(pinId = p1Id, author = "Wanderlust_101", text = "This cabin is literally my absolute dream retreat! Added to my travel goals."))
            commentDao.insertComment(Comment(pinId = p1Id, author = "DesignMaven", text = "The black cladding contrasts so beautifully with the emerald forest."))
            commentDao.insertComment(Comment(pinId = p2Id, author = "BakerGirl", text = "Matcha and strawberry is such an elite flavor combination! Beautiful work."))
            commentDao.insertComment(Comment(pinId = p3Id, author = "OutfitDiary", text = "Clean, classy, and timeless. I need this whole wardrobe."))
            commentDao.insertComment(Comment(pinId = p4Id, author = "CryptoAesthetic", text = "The color palette in this illustration is so incredibly satisfying."))
        }
    }
}
