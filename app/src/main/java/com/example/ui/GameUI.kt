package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.LevelProgress
import kotlinx.coroutines.launch

// Beautiful Bento Palette Constants
val BentoBg = Color(0xFFFFFBEB)       // Sands/warm cream base
val BentoBrownMain = Color(0xFF5D4037)  // Chocolate brown
val BentoBrownSub = Color(0xFF795548)   // Warm greyish brown
val BentoBrownHint = Color(0xFF8D6E63)  // Secondary brown hint
val BentoBorderColor = Color(0xFFFFECE0) // Soft orange-peach card outline

val BentoBlueBg = Color(0xFFE3F2FD)   // Letter Soft Blue
val BentoBlueText = Color(0xFF1565C0)
val BentoBlueAccent = Color(0xFF1976D2)

val BentoGreenBg = Color(0xFFF1F8E9)  // Numbers Soft Green
val BentoGreenText = Color(0xFF33691E)
val BentoGreenAccent = Color(0xFF558B2F)

val BentoOrangeBg = Color(0xFFFFF3E0) // Colors Soft Orange/Yellow
val BentoOrangeText = Color(0xFFE65100)
val BentoOrangeAccent = Color(0xFFEF6C00)

val BentoPurpleBg = Color(0xFFF3E5F5) // Soft decoration purple
val BentoPurpleText = Color(0xFF7B1FA2)

val BentoCoralColor = Color(0xFFFF8A65) // Play button color

@Composable
fun GameAppContent(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val activeLevelId by viewModel.activeLevelId.collectAsState()
    val progressMap by viewModel.currentLevelProgressMap.collectAsState()
    val mistakes by viewModel.mistakeCount.collectAsState()
    val isCompleted by viewModel.isCompleted.collectAsState()
    val feedbackMsg by viewModel.feedback.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BentoBg)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            },
            label = "ScreenTransition"
        ) { targetScreen ->
            when (targetScreen) {
                Screen.Splash -> SplashScreen(
                    onStartAdventure = { viewModel.navigateTo(Screen.LevelMap) }
                )
                Screen.LevelMap -> LevelMapScreen(
                    levels = viewModel.levels,
                    progressMap = progressMap,
                    activeLevelId = activeLevelId,
                    onSelectLevel = { levelId -> viewModel.startLevel(levelId) },
                    onResetProgress = { viewModel.resetProgress() }
                )
                Screen.PlayLevel -> {
                    val activeLevel = viewModel.levels.firstOrNull { it.id == activeLevelId }
                    if (activeLevel != null) {
                        PlayLevelScreen(
                            level = activeLevel,
                            mistakes = mistakes,
                            isCompleted = isCompleted,
                            feedback = feedbackMsg,
                            onSubmit = { option -> viewModel.submitAnswer(option) },
                            onBackToMap = { viewModel.navigateTo(Screen.LevelMap) },
                            onNextLevel = {
                                if (activeLevelId < 20) {
                                    viewModel.startLevel(activeLevelId + 1)
                                } else {
                                    viewModel.navigateTo(Screen.LevelMap)
                                }
                            },
                            onReplay = { viewModel.startLevel(activeLevelId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SplashScreen(
    onStartAdventure: () -> Unit
) {
    var animateStart by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        animateStart = true
    }

    val scaleAnims by animateFloatAsState(
        targetValue = if (animateStart) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "SplashScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App title in playful typography matching primary Bento color
        Text(
            text = "مغامرات سمسم",
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BentoBrownMain,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "العالم ثنائي الأبعاد للتعلم والاكتشاف والمرح! ✨",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = BentoBrownSub,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Hero illustration holder transformed into lovely Bento card
        Card(
            modifier = Modifier
                .size(230.dp)
                .scale(scaleAnims)
                .border(2.dp, BentoBorderColor, RoundedCornerShape(36.dp))
                .graphicsLayer {
                    shadowElevation = 8f
                    shape = RoundedCornerShape(36.dp)
                    clip = true
                },
            shape = RoundedCornerShape(36.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_simsim_logo),
                contentDescription = "Simsim Logo Cartoon Hero",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(44.dp))

        // Large bouncy coral-orange Bento button matching HTML action buttons
        Button(
            onClick = onStartAdventure,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .height(64.dp)
                .graphicsLayer {
                    shadowElevation = 6f
                    shape = RoundedCornerShape(28.dp)
                }
                .testTag("start_adventure_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = BentoCoralColor
            ),
            shape = RoundedCornerShape(28.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = "ابدأ المعامرة الآن!",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ابدأ المغامرة الآن!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = "للأطفال من عمر 4 إلى 8 سنوات 🎖️",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = BentoBrownHint
        )
    }
}

@Composable
fun LevelMapScreen(
    levels: List<LevelConfig>,
    progressMap: Map<Int, LevelProgress>,
    activeLevelId: Int,
    onSelectLevel: (Int) -> Unit,
    onResetProgress: () -> Unit
) {
    val scrollState = rememberScrollState()
    val totalStars = progressMap.values.sumOf { it.stars }
    
    // Find active or next playable level info
    val nextLevelId = progressMap.entries
        .filter { it.value.stars == 0 }
        .minByOrNull { it.key }?.key ?: activeLevelId
    
    val currentLevelConfig = levels.firstOrNull { it.id == nextLevelId } ?: levels[0]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // 1. Bento Grid Header (Profile + Star stats + Hearts count)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Card spanning most width
            Card(
                modifier = Modifier
                    .weight(1.3f)
                    .height(84.dp)
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Avatar with custom pink-orange gradient
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFFF8B80), Color(0xFFFFAB40))
                                ),
                                CircleShape
                            )
                            .border(2.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "🦁", fontSize = 24.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(10.dp))
                    
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "مرحباً يا بطل!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoBrownMain
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Stars Total",
                                tint = Color(0xFFFBC02D),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$totalStars/60 نجمة  |  المستوى $nextLevelId",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoBrownSub
                            )
                        }
                    }
                }
            }

            // Hearts Card mimicking top right of Bento grid
            Card(
                modifier = Modifier
                    .weight(0.7f)
                    .height(84.dp)
                    .border(1.5.dp, BentoBorderColor, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "5/5",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoBrownMain
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "❤️", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "طاقة كاملة",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoBrownSub
                        )
                    }
                }
            }
        }

        // Map scrollable content reflecting actual bento cells
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 2. Bento Next Task Indicator Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .border(1.5.dp, BentoBorderColor, RoundedCornerShape(28.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "المهمة التالية دائمًا 🎯",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoBrownHint
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = currentLevelConfig.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoBrownMain
                            )
                        }
                        
                        // Action "Play Now" Coral button
                        Button(
                            onClick = { onSelectLevel(currentLevelConfig.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoCoralColor),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("play_next_level_btn")
                        ) {
                            Text(
                                text = "العب الآن ◀",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        }
                    }
                }

                // 3. Decorative / Progression Category Bento Grid Block
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Left Column (Letters Module Card)
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .border(1.5.dp, Color(0xFFBBDEFB), RoundedCornerShape(32.dp)),
                        colors = CardDefaults.cardColors(containerColor = BentoBlueBg),
                        shape = RoundedCornerShape(32.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "أ ب",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoBlueAccent.copy(alpha = 0.15f),
                                modifier = Modifier.align(Alignment.TopStart)
                            )
                            Column(
                                modifier = Modifier.align(Alignment.BottomStart)
                            ) {
                                Text(
                                    text = "🦁",
                                    fontSize = 32.sp,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                                Text(
                                    text = "عالم الحروف",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = BentoBlueText
                                )
                                Text(
                                    text = "مغامرات بطة وألف",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoBlueAccent
                                )
                            }
                        }
                    }

                    // Right Column (Numbers & Colors Cells stacked)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Numbers Cell
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(0.9f)
                                .border(1.5.dp, Color(0xFFDCEDC8), RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = BentoGreenBg),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFF8BC34A), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "12",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                                Column {
                                    Text(
                                        text = "الأرقام",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BentoGreenText
                                    )
                                    Text(
                                        text = "تعلّم العد والجمع",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoGreenAccent
                                    )
                                }
                            }
                        }

                        // Colors Cell
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.1f)
                                .border(1.5.dp, Color(0xFFFFE0B2), RoundedCornerShape(24.dp)),
                            colors = CardDefaults.cardColors(containerColor = BentoOrangeBg),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(modifier = Modifier.size(16.dp).background(Color.Red, CircleShape))
                                    Box(modifier = Modifier.size(16.dp).background(Color.Blue, CircleShape))
                                    Box(modifier = Modifier.size(16.dp).background(Color.Yellow, CircleShape))
                                    Box(modifier = Modifier.size(16.dp).background(Color.Green, CircleShape))
                                }
                                Column {
                                    Text(
                                        text = "عالم الألوان",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = BentoOrangeText
                                    )
                                    Text(
                                        text = "اكتشف الألوان السحرية",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoOrangeAccent
                                    )
                                }
                            }
                        }
                    }
                }

                // Daily Reward and Badge Banner mimicking Bento footers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(0.9f)
                            .height(68.dp)
                            .border(1.5.dp, Color(0xFFE1BEE7), RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = BentoPurpleBg),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🎁", fontSize = 24.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "هدية يومية",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoPurpleText
                            )
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1.1f)
                            .height(68.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB300))
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "🏆", fontSize = 16.sp)
                                    Text(text = "🏅", fontSize = 16.sp)
                                    Text(text = "👑", fontSize = 16.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "مجموعة الأوسمة",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF6D4C41)
                                    )
                                    Text(
                                        text = "تم جمع 3 من 12",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF795548)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Magnificent Road Section of 20 levels
                Text(
                    text = "خريطة المغامرات السحرية 🗺️",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoBrownMain,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                levels.forEachIndexed { index, level ->
                    val isUnlocked = progressMap[level.id]?.isUnlocked ?: (index == 0)
                    val starsEarned = progressMap[level.id]?.stars ?: 0
                    
                    LevelBentoItemNode(
                        level = level,
                        isUnlocked = isUnlocked,
                        stars = starsEarned,
                        index = index + 1,
                        onClick = { if (isUnlocked) onSelectLevel(level.id) }
                    )
                    
                    // Draw a fun Bento visual connector dot sequence
                    if (index < levels.size - 1) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .height(26.dp)
                                .width(6.dp)
                                .background(BentoBrownHint.copy(alpha = 0.25f), shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(34.dp))

                // Reset progress option beautifully stylized near bottom
                OutlinedButton(
                    onClick = onResetProgress,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                    border = BorderStroke(1.5.dp, Color(0xFFEF5350)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.padding(bottom = 24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Reset Progress Button",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إعادة تصفير وحذف كل التقدم",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun LevelBentoItemNode(
    level: LevelConfig,
    isUnlocked: Boolean,
    stars: Int,
    index: Int,
    onClick: () -> Unit
) {
    // Pick soft Bento card design according to category status
    val cardBg = if (isUnlocked) {
        when (level.category) {
            GameCategory.COLORS -> BentoOrangeBg
            GameCategory.NUMBERS -> BentoGreenBg
            GameCategory.LETTERS -> BentoBlueBg
        }
    } else {
        Color(0xFFF5F5F5)
    }

    val outlineColor = if (isUnlocked) {
        when (level.category) {
            GameCategory.COLORS -> Color(0xFFFFFFE0B2)
            GameCategory.NUMBERS -> Color(0xFFDCEDC8)
            GameCategory.LETTERS -> Color(0xFFBBDEFB)
        }
    } else {
        Color(0xFFE0E0E0)
    }

    val indexToneColor = if (isUnlocked) {
        when (level.category) {
            GameCategory.COLORS -> BentoOrangeText
            GameCategory.NUMBERS -> BentoGreenText
            GameCategory.LETTERS -> BentoBlueText
        }
    } else {
        Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, outlineColor, RoundedCornerShape(28.dp))
            .clickable(enabled = isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Level Bubble ID
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(indexToneColor, CircleShape)
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(
                        text = index.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Lock,
                        contentDescription = "Locked level",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = level.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isUnlocked) BentoBrownMain else Color(0xFF757575)
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = if (isUnlocked) level.description else "المستوى مغلق! أنجز السابق لفتحه",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) BentoBrownSub else Color(0xFF9E9E9E),
                    maxLines = 1,
                    textAlign = TextAlign.Start
                )

                if (isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        repeat(3) { starOffset ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = "Star Progress Indicator",
                                tint = if (starOffset < stars) Color(0xFFFFB300) else Color(0x22795548),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PlayLevelScreen(
    level: LevelConfig,
    mistakes: Int,
    isCompleted: Boolean,
    feedback: String,
    onSubmit: (GameOption) -> Unit,
    onBackToMap: () -> Unit,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit
) {
    val categoryLabel = when (level.category) {
        GameCategory.COLORS -> "الألوان 🎨"
        GameCategory.NUMBERS -> "الأرقام 🔢"
        GameCategory.LETTERS -> "الحروف 🔠"
    }

    val categoryColor = when (level.category) {
        GameCategory.COLORS -> BentoOrangeText
        GameCategory.NUMBERS -> BentoGreenText
        GameCategory.LETTERS -> BentoBlueText
    }

    val categoryBg = when (level.category) {
        GameCategory.COLORS -> BentoOrangeBg
        GameCategory.NUMBERS -> BentoGreenBg
        GameCategory.LETTERS -> BentoBlueBg
    }

    val outlineColor = when (level.category) {
        GameCategory.COLORS -> Color(0xFFFFE0B2)
        GameCategory.NUMBERS -> Color(0xFFDCEDC8)
        GameCategory.LETTERS -> Color(0xFFBBDEFB)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
    ) {
        // Level header control bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onBackToMap,
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .border(2.dp, BentoBorderColor, CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back to map screen",
                    tint = BentoBrownMain
                )
            }

            // Category tag and Level Info Card scaled inside gorgeous Bento Tag
            Card(
                colors = CardDefaults.cardColors(containerColor = categoryBg),
                modifier = Modifier.border(1.5.dp, outlineColor, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "$categoryLabel - المستوى ${level.id}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = categoryColor,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            IconButton(
                onClick = { SoundHelper.playClick() },
                modifier = Modifier
                    .background(Color.White, CircleShape)
                    .border(2.dp, BentoBorderColor, CircleShape)
                    .size(44.dp)
            ) {
                Text(
                    text = "🔊",
                    fontSize = 20.sp
                )
            }
        }

        // Simsim Guidance speech bubble (Bento styled bubble)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .border(2.dp, BentoBorderColor, RoundedCornerShape(26.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(26.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simsim Cartoon Avatar
                Image(
                    painter = painterResource(id = R.drawable.img_simsim_logo),
                    contentDescription = "Simsim Avatar Guide",
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color(0xFFFFAB40), CircleShape)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "سمسم يقول لك:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF8A65)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = level.instruction,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = BentoBrownMain,
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Start
                    )
                }
            }
        }

        // Interactive Gaming Ground Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (level.id == 8) {
                AppleCountingMiniGame(
                    options = level.options,
                    onSubmit = onSubmit,
                    isCompleted = isCompleted
                )
            } else if (level.id == 10) {
                AnimalAdditionMiniGame(
                    options = level.options,
                    onSubmit = onSubmit,
                    isCompleted = isCompleted
                )
            } else {
                BubblyBalloonsLayout(
                    options = level.options,
                    onSubmit = onSubmit,
                    isCompleted = isCompleted
                )
            }
        }

        // Live Encouragement Feedback Banner
        if (feedback.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCompleted) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .border(
                            1.5.dp, 
                            if (isCompleted) Color(0xFFC8E6C9) else Color(0xFFFFE0B2), 
                            RoundedCornerShape(18.dp)
                        ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = feedback,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isCompleted) Color(0xFF2E7D32) else Color(0xFFE65100),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Success Completion Dialog Overlay with confetti rain
    if (isCompleted) {
        val stars = when (mistakes) {
            0 -> 3
            1 -> 2
            else -> 1
        }
        
        SuccessCelebrationDialog(
            stars = stars,
            mistakes = mistakes,
            onNext = onNextLevel,
            onReplay = onReplay
        )
    }
}

@Composable
fun BubblyBalloonsLayout(
    options: List<GameOption>,
    onSubmit: (GameOption) -> Unit,
    isCompleted: Boolean
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(options) { option ->
            var scaleState by remember { mutableStateOf(1f) }
            val animatedScale by animateFloatAsState(
                targetValue = scaleState,
                animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy, stiffness = Spring.StiffnessMedium),
                label = "BalloonScale"
            )

            Card(
                modifier = Modifier
                    .height(130.dp)
                    .scale(animatedScale)
                    .border(2.dp, Color(0xFFEFEBE9), RoundedCornerShape(28.dp))
                    .clickable(enabled = !isCompleted) {
                        scaleState = 1.15f
                        onSubmit(option)
                        scaleState = 1f
                    }
                    .testTag("balloon_option_${option.value}"),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Box(bottomHighlightBrush(option.colorHex)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Drawing a physical colored balloon inside the option card to feel 100% like a game
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(android.graphics.Color.parseColor(option.colorHex)), CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = option.displayLabel,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoBrownMain
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AppleCountingMiniGame(
    options: List<GameOption>,
    onSubmit: (GameOption) -> Unit,
    isCompleted: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        // Tree Visual Model formatted to mimic a clean Bento design
        Box(
            modifier = Modifier
                .size(230.dp)
                .background(Color.Transparent),
            contentAlignment = Alignment.BottomCenter
        ) {
            // Elegant tree trunk
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(110.dp)
                    .background(Color(0xFF8D6E63), RoundedCornerShape(8.dp))
            )
            // Tree leaves crown
            Box(
                modifier = Modifier
                    .size(170.dp)
                    .offset(y = (-45).dp)
                    .background(Color(0xFF81C784), CircleShape)
            )
            // Place 3 interactive Red Apples
            Box(modifier = Modifier.fillMaxSize()) {
                AppleNode(modifier = Modifier.align(Alignment.Center).offset(y = (-40).dp))
                AppleNode(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-30).dp, x = (-30).dp))
                AppleNode(modifier = Modifier.align(Alignment.TopCenter).offset(y = (-20).dp, x = 30.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Large count cards options row styled like Bento items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            options.forEach { option ->
                Button(
                    onClick = { onSubmit(option) },
                    enabled = !isCompleted,
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("apple_num_${option.value}"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = option.displayLabel,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AppleNode(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(34.dp)
            .background(Color(0xFFEF5350), CircleShape)
            .border(2.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Small leaf
        Box(
            modifier = Modifier
                .size(8.dp)
                .offset(y = (-15).dp)
                .background(Color(0xFF2E7D32), RoundedCornerShape(4.dp))
        )
    }
}

@Composable
fun AnimalAdditionMiniGame(
    options: List<GameOption>,
    onSubmit: (GameOption) -> Unit,
    isCompleted: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .border(2.dp, BentoBorderColor, RoundedCornerShape(24.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Bunny 1 group
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFFE1F5FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐰", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("١ أرنب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoBrownSub)
            }

            Text(
                text = "＋",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF9800),
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            // Bunny 2 group
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(Color(0xFFE1F5FE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐰", fontSize = 28.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text("١ أرنب", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoBrownSub)
            }

            Text(
                text = "＝",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFFF9800),
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            // Secret target block
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFFFECB3), RoundedCornerShape(16.dp))
                    .border(2.5.dp, Color(0xFFFFB300), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "؟",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF8F00)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Options Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            options.forEach { option ->
                Button(
                    onClick = { onSubmit(option) },
                    enabled = !isCompleted,
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("bunny_add_${option.value}"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        text = option.displayLabel,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SuccessCelebrationDialog(
    stars: Int,
    mistakes: Int,
    onNext: () -> Unit,
    onReplay: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        ConfettiRain()

        // Dialog Content body themed like magnificent Bento tile
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(24.dp)
                .clickable(enabled = false) {}
                .border(2.5.dp, BentoBorderColor, RoundedCornerShape(32.dp)),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆",
                    fontSize = 64.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Text(
                    text = "مذهل ورائع! 🎉",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = BentoBrownMain,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { starIndex ->
                        val isLit = starIndex < stars
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Star Reward",
                            tint = if (isLit) Color(0xFFFFC107) else Color(0x22795548),
                            modifier = Modifier
                                .size(if (starIndex == 1) 48.dp else 40.dp)
                                .padding(horizontal = 4.dp)
                        )
                    }
                }

                Text(
                    text = when (stars) {
                        3 -> "أنت عبقري بحق! درجة كاملة بدون أخطاء ⭐⭐⭐"
                        2 -> "مستوى ممتاز جداً! دقة رائعة ⭐⭐"
                        else -> "أحسنت العمل يا بطل! استمر بالتعلم ⭐"
                    },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoBrownSub,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("dialog_next_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "المستوى التالي ➔",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onReplay,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, BentoCoralColor)
                    ) {
                        Text(
                            text = "أعد المحاولة لنجوم أكثر",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = BentoCoralColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConfettiRain() {
    val infiniteTransition = rememberInfiniteTransition(label = "ConfettiTransition")
    val progress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ConfettiAnimation"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val randoms = listOf(
            0.1f to 0.15f, 0.25f to 0.7f, 0.42f to 0.35f, 0.55f to 0.85f, 0.68f to 0.25f,
            0.78f to 0.65f, 0.88f to 0.4f, 0.96f to 0.9f, 0.18f to 0.55f, 0.33f to 0.8f
        )
        
        randoms.forEachIndexed { i, (xRatio, speed) ->
            val py = (progress * size.height * speed * 1.6f) % size.height
            val px = xRatio * size.width
            val colors = listOf(
                Color(0xFFFF1744), Color(0xFF00E676), Color(0xFF29B6F6), 
                Color(0xFFFFEA00), Color(0xFFD500F9), Color(0xFFFF9100)
            )
            
            drawCircle(
                color = colors[i % colors.size],
                radius = 11f,
                center = Offset(px, py)
            )
        }
    }
}

@Composable
fun bottomHighlightBrush(colorHex: String): Modifier {
    val color = Color(android.graphics.Color.parseColor(colorHex))
    return Modifier.drawBehind {
        drawRect(
            color = color.copy(alpha = 0.12f),
            topLeft = Offset(0f, 0f),
            size = size
        )
        // Draw bottom thick bar
        drawRect(
            color = color,
            topLeft = Offset(0f, size.height - 12.dp.toPx()),
            size = size.copy(height = 12.dp.toPx())
        )
    }
}
