package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.ai.FaceShapeAnalyzer
import com.example.ai.GeminiHairService
import com.example.ai.HairImageSynthesizer
import com.example.data.HairstyleRepository
import com.example.data.local.GenerationHistoryEntity
import com.example.data.local.HairStudioDatabase
import com.example.data.local.SavedStyleEntity
import com.example.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppDestination {
    HOME,
    GALLERY,
    STUDIO,
    RESULTS,
    RECOMMENDATIONS,
    DASHBOARD,
    PRIVACY
}

class HairStudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = HairStudioDatabase.getDatabase(application)
    private val dao = db.hairStudioDao()

    // Navigation state
    private val _currentScreen = MutableStateFlow(AppDestination.HOME)
    val currentScreen: StateFlow<AppDestination> = _currentScreen.asStateFlow()

    // Uploaded / Captured photo state
    private val _userBitmap = MutableStateFlow<Bitmap?>(null)
    val userBitmap: StateFlow<Bitmap?> = _userBitmap.asStateFlow()

    private val _photoUri = MutableStateFlow<Uri?>(null)
    val photoUri: StateFlow<Uri?> = _photoUri.asStateFlow()

    // Face analysis state
    private val _faceAnalysis = MutableStateFlow<FaceAnalysisResult?>(null)
    val faceAnalysis: StateFlow<FaceAnalysisResult?> = _faceAnalysis.asStateFlow()

    private val _aiStylistNotes = MutableStateFlow<String?>(null)
    val aiStylistNotes: StateFlow<String?> = _aiStylistNotes.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Try-on selections
    private val _selectedHairstyle = MutableStateFlow<Hairstyle>(HairstyleRepository.allHairstyles.first())
    val selectedHairstyle: StateFlow<Hairstyle> = _selectedHairstyle.asStateFlow()

    private val _customHairstylePrompt = MutableStateFlow("")
    val customHairstylePrompt: StateFlow<String> = _customHairstylePrompt.asStateFlow()

    private val _selectedHairColor = MutableStateFlow<HairColor>(HairColor.NATURAL_BLACK)
    val selectedHairColor: StateFlow<HairColor> = _selectedHairColor.asStateFlow()

    private val _selectedHairTexture = MutableStateFlow<HairTexture>(HairTexture.STRAIGHT)
    val selectedHairTexture: StateFlow<HairTexture> = _selectedHairTexture.asStateFlow()

    private val _maintenancePreference = MutableStateFlow("Medium")
    val maintenancePreference: StateFlow<String> = _maintenancePreference.asStateFlow()

    // Generation output state
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _generationStatus = MutableStateFlow("Preparing studio...")
    val generationStatus: StateFlow<String> = _generationStatus.asStateFlow()

    private val _generatedLooks = MutableStateFlow<List<GeneratedLook>>(emptyList())
    val generatedLooks: StateFlow<List<GeneratedLook>> = _generatedLooks.asStateFlow()

    private val _bestRecommendedLook = MutableStateFlow<GeneratedLook?>(null)
    val bestRecommendedLook: StateFlow<GeneratedLook?> = _bestRecommendedLook.asStateFlow()

    private val _selectedLookIndex = MutableStateFlow(0)
    val selectedLookIndex: StateFlow<Int> = _selectedLookIndex.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError: StateFlow<String?> = _generationError.asStateFlow()

    // Comparison slider state
    private val _comparisonSliderPos = MutableStateFlow(0.5f)
    val comparisonSliderPos: StateFlow<Float> = _comparisonSliderPos.asStateFlow()

    private val _isSideBySide = MutableStateFlow(false)
    val isSideBySide: StateFlow<Boolean> = _isSideBySide.asStateFlow()

    // Gallery Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _genderFilter = MutableStateFlow<HairGender?>(null)
    val genderFilter: StateFlow<HairGender?> = _genderFilter.asStateFlow()

    private val _lengthFilter = MutableStateFlow<HairLength?>(null)
    val lengthFilter: StateFlow<HairLength?> = _lengthFilter.asStateFlow()

    private val _textureFilter = MutableStateFlow<HairTexture?>(null)
    val textureFilter: StateFlow<HairTexture?> = _textureFilter.asStateFlow()

    // Database Flows
    val savedStyles: StateFlow<List<SavedStyleEntity>> = dao.getAllSavedStyles()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val generationHistory: StateFlow<List<GenerationHistoryEntity>> = dao.getAllHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Tracks if current photo is user's own upload/capture or a preset sample
    private val _isUserCustomPhoto = MutableStateFlow(false)
    val isUserCustomPhoto: StateFlow<Boolean> = _isUserCustomPhoto.asStateFlow()

    init {
        // Pre-load default sample model portrait for instant try-on delight
        loadDefaultSamplePhoto()
    }

    private fun loadDefaultSamplePhoto() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val opts = BitmapFactory.Options().apply { inPreferredConfig = Bitmap.Config.ARGB_8888 }
                val bmp = BitmapFactory.decodeResource(getApplication<Application>().resources, R.drawable.img_hair_men, opts)
                if (bmp != null) {
                    _userBitmap.value = bmp
                    _isUserCustomPhoto.value = false
                    val analysis = FaceShapeAnalyzer.analyzeFace(bmp)
                    _faceAnalysis.value = analysis
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun navigateTo(destination: AppDestination) {
        _currentScreen.value = destination
    }

    fun setUserBitmap(bitmap: Bitmap, uri: Uri? = null) {
        _userBitmap.value = bitmap
        _photoUri.value = uri
        _isUserCustomPhoto.value = true
        _generatedLooks.value = emptyList()
        _bestRecommendedLook.value = null
        analyzeUploadedFace(bitmap)
    }

    fun setSamplePhoto(drawableResId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bmp = BitmapFactory.decodeResource(getApplication<Application>().resources, drawableResId)
                if (bmp != null) {
                    _userBitmap.value = bmp
                    _photoUri.value = null
                    _isUserCustomPhoto.value = false
                    _generatedLooks.value = emptyList()
                    _bestRecommendedLook.value = null
                    analyzeUploadedFace(bmp)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearUserPhoto() {
        _userBitmap.value = null
        _photoUri.value = null
        _isUserCustomPhoto.value = false
        _faceAnalysis.value = null
        _generatedLooks.value = emptyList()
        _bestRecommendedLook.value = null
    }

    private fun analyzeUploadedFace(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.Default) {
            _isAnalyzing.value = true
            val localAnalysis = FaceShapeAnalyzer.analyzeFace(bitmap)
            _faceAnalysis.value = localAnalysis

            // If Gemini API is available, enhance with AI stylist diagnostic
            if (GeminiHairService.isApiKeyConfigured()) {
                val aiNotes = GeminiHairService.analyzeFaceMultimodal(bitmap)
                if (!aiNotes.isNullOrBlank()) {
                    _aiStylistNotes.value = aiNotes
                }
            }
            _isAnalyzing.value = false
        }
    }

    fun selectHairstyle(hairstyle: Hairstyle) {
        _selectedHairstyle.value = hairstyle
    }

    fun setCustomHairstylePrompt(prompt: String) {
        _customHairstylePrompt.value = prompt
    }

    fun selectHairColor(color: HairColor) {
        _selectedHairColor.value = color
    }

    fun recolorGeneratedLooks(color: HairColor) {
        val baseBmp = _userBitmap.value ?: return
        _selectedHairColor.value = color
        val detectedShape = _faceAnalysis.value?.detectedShape ?: FaceShape.OVAL
        val currentList = _generatedLooks.value
        if (currentList.isEmpty()) return

        val updatedList = currentList.mapIndexed { idx, look ->
            val newBmp = HairImageSynthesizer.synthesizeHairstyle(
                originalBitmap = baseBmp,
                styleName = look.hairstyleName,
                hairColor = color,
                faceShape = detectedShape,
                variationIndex = idx
            )
            look.copy(
                hairColor = color,
                generatedBitmap = newBmp
            )
        }
        _generatedLooks.value = updatedList
        val bestIdx = _selectedLookIndex.value
        _bestRecommendedLook.value = updatedList.getOrNull(bestIdx) ?: updatedList.firstOrNull()
    }

    fun setHairTexture(texture: HairTexture) {
        _selectedHairTexture.value = texture
    }

    fun setMaintenancePreference(level: String) {
        _maintenancePreference.value = level
    }

    fun setComparisonSliderPos(pos: Float) {
        _comparisonSliderPos.value = pos.coerceIn(0f, 1f)
    }

    fun toggleSideBySide() {
        _isSideBySide.value = !_isSideBySide.value
    }

    fun setSelectedLookIndex(index: Int) {
        if (index in 0 until _generatedLooks.value.size) {
            _selectedLookIndex.value = index
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGenderFilter(gender: HairGender?) {
        _genderFilter.value = gender
    }

    fun setLengthFilter(length: HairLength?) {
        _lengthFilter.value = length
    }

    fun setTextureFilter(texture: HairTexture?) {
        _textureFilter.value = texture
    }

    fun generateHairstylePreviews() {
        val baseBmp = _userBitmap.value
        if (baseBmp == null) {
            _generationError.value = "Please upload or capture a photo first."
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            _isGenerating.value = true
            _generationError.value = null
            _generationStatus.value = "Analyzing facial symmetry & hairline..."

            val detectedShape = _faceAnalysis.value?.detectedShape ?: FaceShape.OVAL
            val chosenStyle = _selectedHairstyle.value
            val chosenColor = _selectedHairColor.value

            val looksList = mutableListOf<GeneratedLook>()

            // 6 Distinct Visible Variations as specified:
            // 1. Modern Fade
            // 2. Textured Crop
            // 3. Korean Curtain Hair
            // 4. Curly Hairstyle
            // 5. Slick Back
            // 6. Long Layered Hair
            val lookConfigs = listOf(
                LookConfig(
                    title = "Look 1: Modern Fade",
                    styleName = "Modern Fade",
                    shortDesc = "Clean tapered fade on sides with textured top volume and sharp contours.",
                    hairLength = "Short",
                    maintenance = "Medium",
                    badge = "Sharp & Clean",
                    matchPercent = 98,
                    analysis = "Your recommended Modern Fade provides a crisp, balanced silhouette that elongates the face and defines the jawline.",
                    variationIndex = 0
                ),
                LookConfig(
                    title = "Look 2: Textured Crop",
                    styleName = "Textured Crop",
                    shortDesc = "Blunt micro-fringe with layered matte crown texture.",
                    hairLength = "Short",
                    maintenance = "Low",
                    badge = "Low Maintenance",
                    matchPercent = 95,
                    analysis = "Textured layers soften upper facial angles while the blunt fringe brings focal balance to the eyes.",
                    variationIndex = 1
                ),
                LookConfig(
                    title = "Look 3: Korean Curtain Hair",
                    styleName = "Korean Curtain Hair",
                    shortDesc = "Aesthetic center-part with flowing C-curls draping naturally along the cheekbones.",
                    hairLength = "Medium",
                    maintenance = "Low",
                    badge = "K-Wave Trend",
                    matchPercent = 97,
                    analysis = "Soft middle-part drape frames the temples gracefully and narrows broad cheekbones.",
                    variationIndex = 2
                ),
                LookConfig(
                    title = "Look 4: Curly Hairstyle",
                    styleName = "Curly Hairstyle",
                    shortDesc = "High-definition bouncy spiral coils with natural volume.",
                    hairLength = "Medium",
                    maintenance = "Medium",
                    badge = "Volume & Texture",
                    matchPercent = 94,
                    analysis = "Volumetric curl definition adds softness and roundness, highly complementary for sharp facial lines.",
                    variationIndex = 3
                ),
                LookConfig(
                    title = "Look 5: Slick Back",
                    styleName = "Slick Back",
                    shortDesc = "Classic backward sweep with glossy high-taper poise and clean hairline.",
                    hairLength = "Medium",
                    maintenance = "Medium",
                    badge = "Executive Poise",
                    matchPercent = 93,
                    analysis = "Polished backward sweep highlights full facial symmetry and high cheekbone apexes.",
                    variationIndex = 4
                ),
                LookConfig(
                    title = "Look 6: Long Layered Hair",
                    styleName = "Long Layered Hair",
                    shortDesc = "Cascading flowing locks cascading smoothly past the shoulders with stepped layers.",
                    hairLength = "Long",
                    maintenance = "High",
                    badge = "Dramatic Flow",
                    matchPercent = 96,
                    analysis = "Stepped cascading layers frame the neck and collarbones, introducing graceful fluid movement.",
                    variationIndex = 5
                )
            )

            // Try Gemini generation if configured, otherwise synthesize with realistic styling
            val useGeminiApi = GeminiHairService.isApiKeyConfigured()

            for ((idx, cfg) in lookConfigs.withIndex()) {
                withContext(Dispatchers.Main) {
                    _generationStatus.value = "AI is creating your personalized hairstyles (${idx + 1}/6: ${cfg.styleName})..."
                }

                var generatedBmp: Bitmap? = null
                if (useGeminiApi && idx == 0) {
                    // Try real Gemini transformation for the primary look
                    generatedBmp = GeminiHairService.transformHairstyleImage(
                        originalBitmap = baseBmp,
                        styleName = cfg.styleName,
                        colorName = chosenColor.displayName
                    )
                }

                // If Gemini didn't return or not configured, synthesize photorealistic transformation
                if (generatedBmp == null) {
                    generatedBmp = HairImageSynthesizer.synthesizeHairstyle(
                        originalBitmap = baseBmp,
                        styleName = cfg.styleName,
                        hairColor = chosenColor,
                        faceShape = detectedShape,
                        variationIndex = cfg.variationIndex
                    )
                }

                looksList.add(
                    GeneratedLook(
                        id = "look_${System.currentTimeMillis()}_$idx",
                        title = cfg.title,
                        hairstyleName = cfg.styleName,
                        hairColor = chosenColor,
                        faceShape = detectedShape,
                        badge = cfg.badge,
                        matchPercentage = cfg.matchPercent,
                        stylistAnalysis = cfg.analysis,
                        shortDescription = cfg.shortDesc,
                        hairLength = cfg.hairLength,
                        maintenanceLevel = cfg.maintenance,
                        generatedBitmap = generatedBmp
                    )
                )
            }

            // Determine best match based on detected face shape
            val bestIndex = when (detectedShape) {
                FaceShape.ROUND -> 0 // Modern Fade adds height
                FaceShape.SQUARE -> 1 // Textured Crop softens jaw
                FaceShape.OVAL -> 0 // Modern Fade golden ratio
                FaceShape.HEART -> 2 // Korean Curtain balances chin
                FaceShape.DIAMOND -> 3 // Curly Hairstyle softens cheekbones
                FaceShape.OBLONG -> 2 // Korean Curtain balances elongation
            }
            val bestMatch = looksList.getOrNull(bestIndex) ?: looksList.first()

            withContext(Dispatchers.Main) {
                _generatedLooks.value = looksList
                _bestRecommendedLook.value = bestMatch
                _selectedLookIndex.value = bestIndex
                _isGenerating.value = false
                _currentScreen.value = AppDestination.RESULTS

                // Automatically save best look to history
                saveLookToHistory(bestMatch)
            }
        }
    }

    fun saveLookToHistory(look: GeneratedLook) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertHistory(
                GenerationHistoryEntity(
                    title = look.title,
                    hairstyleName = look.hairstyleName,
                    hairColor = look.hairColor.displayName,
                    faceShape = look.faceShape.displayName,
                    matchPercentage = look.matchPercentage,
                    stylistNotes = look.stylistAnalysis
                )
            )
        }
    }

    fun toggleSaveHairstyle(hairstyle: Hairstyle) {
        viewModelScope.launch(Dispatchers.IO) {
            val isSaved = dao.isStyleSaved(hairstyle.id)
            if (isSaved) {
                dao.deleteSavedStyleById(hairstyle.id)
            } else {
                dao.insertSavedStyle(
                    SavedStyleEntity(
                        hairstyleId = hairstyle.id,
                        name = hairstyle.name,
                        gender = hairstyle.gender.name,
                        length = hairstyle.length.displayName,
                        description = hairstyle.description
                    )
                )
            }
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteHistoryById(id)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            dao.clearAllHistory()
        }
    }

    private data class LookConfig(
        val title: String,
        val styleName: String,
        val shortDesc: String,
        val hairLength: String,
        val maintenance: String,
        val badge: String,
        val matchPercent: Int,
        val analysis: String,
        val variationIndex: Int
    )
}
