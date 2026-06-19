package com.example.ui

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameDatabase
import com.example.data.GameRepository
import com.example.data.LevelProgress
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Screen {
    Splash,
    LevelMap,
    PlayLevel
}

enum class GameCategory {
    COLORS, NUMBERS, LETTERS
}

data class GameOption(
    val value: String,
    val displayLabel: String,
    val colorHex: String = "#FF3B30",
    val iconName: String? = null,
    val isMatched: Boolean = false
)

data class LevelConfig(
    val id: Int,
    val title: String,
    val description: String,
    val category: GameCategory,
    val instruction: String,
    val target: String,
    val options: List<GameOption>
)

object SoundHelper {
    private val toneGenerator = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 85)
    } catch (e: Exception) {
        null
    }

    fun playSuccess() {
        toneGenerator?.let { tg ->
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
            Thread {
                try {
                    Thread.sleep(150)
                    tg.startTone(ToneGenerator.TONE_CDMA_PIP, 180)
                } catch (e: Exception) {
                    // Ignore
                }
            }.start()
        }
    }

    fun playError() {
        toneGenerator?.let { tg ->
            tg.startTone(ToneGenerator.TONE_SUP_CONGESTION, 220)
        }
    }

    fun playClick() {
        toneGenerator?.let { tg ->
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
        }
    }
}

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: GameRepository
    
    private val _dbProgress = MutableStateFlow<List<LevelProgress>>(emptyList())
    
    private val _currentScreen = MutableStateFlow(Screen.Splash)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()
    
    private val _activeLevelId = MutableStateFlow(1)
    val activeLevelId: StateFlow<Int> = _activeLevelId.asStateFlow()
    
    private val _mistakeCount = MutableStateFlow(0)
    val mistakeCount: StateFlow<Int> = _mistakeCount.asStateFlow()
    
    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()
    
    private val _feedback = MutableStateFlow("")
    val feedback: StateFlow<String> = _feedback.asStateFlow()
    
    private val _currentLevelProgressMap = MutableStateFlow<Map<Int, LevelProgress>>(emptyMap())
    val currentLevelProgressMap: StateFlow<Map<Int, LevelProgress>> = _currentLevelProgressMap.asStateFlow()

    // Interactive custom state for sequence levels / drag drop
    private val _userSequence = MutableStateFlow<List<String>>(emptyList())
    val userSequence: StateFlow<List<String>> = _userSequence.asStateFlow()

    init {
        val database = GameDatabase.getDatabase(application)
        repository = GameRepository(database.dao)
        
        viewModelScope.launch {
            // Check & seed 20 levels in the Room local db
            repository.checkAndInitializeLevels()
            
            // Collect the progress updates reactively
            repository.allProgressFlow.collect { progressList ->
                _dbProgress.value = progressList
                _currentLevelProgressMap.value = progressList.associateBy { it.id }
            }
        }
    }

    // List of 20 Graded levels with Arabic names and educational activities
    val levels = listOf(
        LevelConfig(
            id = 1,
            title = "الألوان الأولى",
            description = "تعلم اللون الأحمر الأساسي مع البالونات الطائرة",
            category = GameCategory.COLORS,
            instruction = "يا بطل! اضغط على البالون الأحمر الطائر!",
            target = "أحمر",
            options = listOf(
                GameOption("أحمر", "أحمر", "#FF3B30"),
                GameOption("أزرق", "أزرق", "#007AFF"),
                GameOption("أصفر", "أصفر", "#FFCC00")
            )
        ),
        LevelConfig(
            id = 2,
            title = "اللون المختلف",
            description = "فرّق بذكاء بين اللون الأصفر والبرتقالي",
            category = GameCategory.COLORS,
            instruction = "اضغط على البالون المختلف ذو اللون البرتقالي!",
            target = "برتقالي",
            options = listOf(
                GameOption("أصفر", "أصفر", "#FFCC00"),
                GameOption("برتقالي", "برتقالي", "#FF9500"),
                GameOption("أصفر٢", "أصفر", "#FFCC00"),
                GameOption("أصفر٣", "أصفر", "#FFCC00")
            )
        ),
        LevelConfig(
            id = 3,
            title = "الأزرق والجمال",
            description = "تعلم اختيار اللون الأزرق الصافي كالسماء",
            category = GameCategory.COLORS,
            instruction = "اضغط على الدائرة ذات اللون الأزرق!",
            target = "أزرق",
            options = listOf(
                GameOption("أحمر", "أحمر", "#FF3B30"),
                GameOption("أخضر", "أخضر", "#34C759"),
                GameOption("أزرق", "أزرق", "#007AFF"),
                GameOption("وردي", "وردي", "#FF2D55")
            )
        ),
        LevelConfig(
            id = 4,
            title = "الألوان الثانوية",
            description = "تعلم الألوان المركبة الجميلة كاللون الأخضر",
            category = GameCategory.COLORS,
            instruction = "أين هي عُشبة الضفدع؟ اضغط على اللون الأخضر!",
            target = "أخضر",
            options = listOf(
                GameOption("بنفسجي", "بنفسجي", "#AF52DE"),
                GameOption("أخضر", "أخضر", "#34C759"),
                GameOption("بني", "بني", "#A2845E"),
                GameOption("أحمر", "أحمر", "#FF3B30")
            )
        ),
        LevelConfig(
            id = 5,
            title = "الورد الذكي",
            description = "تعرف على اللون الوردي اللطيف",
            category = GameCategory.COLORS,
            instruction = "ساعد الفراشة بالوقوف على الزهرة الوردية!",
            target = "وردي",
            options = listOf(
                GameOption("أزرق", "أزرق", "#007AFF"),
                GameOption("وردي", "وردي", "#FF2D55"),
                GameOption("برتقالي", "برتقالي", "#FF9500"),
                GameOption("أصفر", "أصفر", "#FFCC00")
            )
        ),
        LevelConfig(
            id = 6,
            title = "ألوان الليل والنهار",
            description = "تعلّم التمييز بين الأسود الحالك والأبيض الساطع",
            category = GameCategory.COLORS,
            instruction = "أين هي النجمة البيضاء المضيئة بالليل؟ اضغط على اللون الأبيض!",
            target = "أبيض",
            options = listOf(
                GameOption("أسود", "أسود", "#1C1C1E"),
                GameOption("بني", "بني", "#A2845E"),
                GameOption("أبيض", "أبيض", "#F2F2F7"),
                GameOption("رمادي", "رمادي", "#8E8E93")
            )
        ),
        LevelConfig(
            id = 7,
            title = "فرز الألوان",
            description = "المستوى المتقدم للألوان: اضغط البنفسجي الساحر",
            category = GameCategory.COLORS,
            instruction = "أين هي علبة الهدايا ذات اللون البنفسجي؟",
            target = "بنفسجي",
            options = listOf(
                GameOption("أحمر", "أحمر", "#FF3B30"),
                GameOption("أخضر", "أخضر", "#34C759"),
                GameOption("بنفسجي", "بنفسجي", "#AF52DE"),
                GameOption("رمادي", "رمادي", "#8E8E93")
            )
        ),
        LevelConfig(
            id = 8,
            title = "عدّ التفاحات",
            description = "عدّ التفاحات المغذية اللذيذة على الشجرة",
            category = GameCategory.NUMBERS,
            instruction = "كم تفاحة حمراء تشاهد على الشجرة؟ عدّهم ثم اضغط الرقم المناسب!",
            target = "٣",
            options = listOf(
                GameOption("١", "١", "#FF9500"),
                GameOption("٣", "٣", "#34C759"),
                GameOption("٥", "٥", "#AF52DE")
            )
        ),
        LevelConfig(
            id = 9,
            title = "ترتيب الأرقام الصغير",
            description = "رتب الأرقام الأبجدية من الصغير إلى الكبير",
            category = GameCategory.NUMBERS,
            instruction = "اضغط على الرقم الصغير جداً (الرقم ١) للبدء!",
            target = "١",
            options = listOf(
                GameOption("٣", "٣", "#FFCC00"),
                GameOption("١", "١", "#007AFF"),
                GameOption("٢", "٢", "#FF3B30")
            )
        ),
        LevelConfig(
            id = 10,
            title = "جمع الحيوانات الصديقة",
            description = "عملية جمع بسيطة لحيوان الأرانب اللطيف",
            category = GameCategory.NUMBERS,
            instruction = "أرنب أول (١) مضافاً إليه أرنب ثانٍ (١)، كم المجموع الكلي للأرانب؟",
            target = "٢",
            options = listOf(
                GameOption("٣", "٣", "#AF52DE"),
                GameOption("٤", "٤", "#FF9500"),
                GameOption("٢", "٢", "#34C759")
            )
        ),
        LevelConfig(
            id = 11,
            title = "الكمية الأكبر",
            description = "اكتشف المجموعة التي تحتوي على نجوم أكثر",
            category = GameCategory.NUMBERS,
            instruction = "اضغط على الصندوق الذي يمتلك نجوماً أكثر بالداخل!",
            target = "٥ نجوم",
            options = listOf(
                GameOption("نجمتان", "٢ نجمة", "#8E8E93"),
                GameOption("٥ نجوم", "٥ نجوم", "#FFCC00"),
                GameOption("نجمة واحدة", "١ نجمة", "#A2845E")
            )
        ),
        LevelConfig(
            id = 12,
            title = "العد التنازلي",
            description = "تعرف على الأرقام العكسية التنازلية",
            category = GameCategory.NUMBERS,
            instruction = "أي رقم يسبق الرقم ٤ مباشرة في العد التنازلي؟",
            target = "٣",
            options = listOf(
                GameOption("٥", "٥", "#FF2D55"),
                GameOption("٣", "٣", "#007AFF"),
                GameOption("١", "١", "#FF9500")
            )
        ),
        LevelConfig(
            id = 13,
            title = "الرقم المفقود",
            description = "اعثر على الرقم المفقود في السلسلة الحسابية",
            category = GameCategory.NUMBERS,
            instruction = "أكمل السلسلة: ١ ، ٢ ، [؟] ، ٤. ما هو الرقم الضائع؟",
            target = "٣",
            options = listOf(
                GameOption("٥", "٥", "#8E8E93"),
                GameOption("٣", "٣", "#34C759"),
                GameOption("٦", "٦", "#AF52DE")
            )
        ),
        LevelConfig(
            id = 14,
            title = "الكلمة والعدد",
            description = "تطابق الكلمات المكتوبة مع الرموز العددية العربية",
            category = GameCategory.NUMBERS,
            instruction = "اضغط على الرمز الرقمي للكلمة المكتوبة: 'أربعة'!",
            target = "٤",
            options = listOf(
                GameOption("٢", "٢", "#FF3B30"),
                GameOption("٤", "٤", "#FFCC00"),
                GameOption("٧", "٧", "#007AFF")
            )
        ),
        LevelConfig(
            id = 15,
            title = "حرف الألف الحيوي",
            description = "الألف رمز البداية، ابحث عن الألف اللطيفة للأرنب",
            category = GameCategory.LETTERS,
            instruction = "اضغط على حرف الألف (أ) لتبدأ مغامرة لغتنا العربية الصديقة!",
            target = "أ",
            options = listOf(
                GameOption("ب", "ب", "#34C759"),
                GameOption("ت", "ت", "#AF52DE"),
                GameOption("أ", "أ", "#FF3B30")
            )
        ),
        LevelConfig(
            id = 16,
            title = "بطة تبدأ بالباء",
            description = "التعرف على حرف الباء الرائع مع صورة البطة اللطيفة",
            category = GameCategory.LETTERS,
            instruction = "كلمة 'بـَطـَّة' تبدأ بأي حرف هجائي؟ اضغط عليه الآن يا بطل!",
            target = "ب",
            options = listOf(
                GameOption("ج", "ج", "#FF9500"),
                GameOption("ب", "ب", "#007AFF"),
                GameOption("أ", "أ", "#FF3B30")
            )
        ),
        LevelConfig(
            id = 17,
            title = "إطعام كتكوت سمسم",
            description = "فرّق بدقة بين تيار الحروف وتيار الأرقام",
            category = GameCategory.LETTERS,
            instruction = "أطعم سمسم! اضغط على حرف التاء (ت) اللذيذ وتجنب الأرقام!",
            target = "ت",
            options = listOf(
                GameOption("٥", "٥ (رقم)", "#8E8E93"),
                GameOption("ت", "ت (حرف)", "#FF2D55"),
                GameOption("٩", "٩ (رقم)", "#A2845E")
            )
        ),
        LevelConfig(
            id = 18,
            title = "الحروف المتشابهة في الرسم",
            description = "تعلّم الفرق البصري بين حرفي ج ومحاكيه ح",
            category = GameCategory.LETTERS,
            instruction = "ابحث عن حرف الخاء (خ) الذي يحمل النقطة على رأسه كالقبعة!",
            target = "خ",
            options = listOf(
                GameOption("ج", "ج", "#34C759"),
                GameOption("ح", "ح", "#FFCC00"),
                GameOption("خ", "خ", "#AF52DE")
            )
        ),
        LevelConfig(
            id = 19,
            title = "صديق الأبجدية",
            description = "ترتيب كلاسيكي للحروف الأولى أ، ب، ت، ث",
            category = GameCategory.LETTERS,
            instruction = "ما هو الحرف الذي يأتي مباشرة بعد حرف التاء (ت)؟",
            target = "ث",
            options = listOf(
                GameOption("ث", "ث", "#FF9500"),
                GameOption("أ", "أ", "#FF3B30"),
                GameOption("ج", "ج", "#007AFF")
            )
        ),
        LevelConfig(
            id = 20,
            title = "مستوى التخرج السعيد",
            description = "التحدي الكبير الشامل للألوان والحروف والأرقام معاً",
            category = GameCategory.LETTERS,
            instruction = "يا نابغة العصور! اضغط على البالون الأزرق الذي يحمل أول حرف في الأبجدية (أ)!",
            target = "أ (بالون أزرق)",
            options = listOf(
                GameOption("١ (بالون أحمر)", "١ (أحمر)", "#FF3B30"),
                GameOption("ب (بالون أخضر)", "ب (أخضر)", "#34C759"),
                GameOption("أ (بالون أزرق)", "أ (أزرق)", "#007AFF")
            )
        )
    )

    fun navigateTo(screen: Screen) {
        SoundHelper.playClick()
        _currentScreen.value = screen
    }

    fun startLevel(levelId: Int) {
        SoundHelper.playClick()
        _activeLevelId.value = levelId
        _mistakeCount.value = 0
        _isCompleted.value = false
        _feedback.value = ""
        _currentScreen.value = Screen.PlayLevel
    }

    fun submitAnswer(option: GameOption) {
        val currentLevel = levels.firstOrNull { it.id == _activeLevelId.value } ?: return
        
        // Let's check if the answer is target!
        if (option.value == currentLevel.target) {
            // Success!
            SoundHelper.playSuccess()
            _isCompleted.value = true
            _feedback.value = "رائع يا بطل! إجابة صحيحة ممتازة! 🎉✨"
            
            // Calculate stars
            val starsEarned = when (_mistakeCount.value) {
                0 -> 3
                1 -> 2
                else -> 1
            }
            
            // Save in database
            viewModelScope.launch {
                repository.saveProgress(
                    levelId = currentLevel.id,
                    stars = starsEarned,
                    score = starsEarned * 100
                )
            }
        } else {
            // Oops, incorrect
            SoundHelper.playError()
            _mistakeCount.update { it + 1 }
            _feedback.value = "حاول مجدداً يا بطل، أنت تستطيع فعلها! 💪🌟"
        }
    }

    fun resetProgress() {
        SoundHelper.playClick()
        viewModelScope.launch {
            repository.resetAllProgress()
            _activeLevelId.value = 1
            _currentScreen.value = Screen.LevelMap
        }
    }
}
