package ai.nuwa.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import ai.nuwa.app.data.repository.PreferenceManager

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val backgroundColor: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    
    val pages = listOf(
        OnboardingPage(
            title = "你好，我是女娲",
            description = "您的智能伙伴，帮助您完成手机上的各种任务",
            icon = Icons.Default.Favorite,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        ),
        OnboardingPage(
            title = "语音控制",
            description = "只需说出你想做什么，我会帮你完成",
            icon = Icons.Default.Mic,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        OnboardingPage(
            title = "自动执行",
            description = "打开微信发消息、导航到目的地、打开文档，都能自动完成",
            icon = Icons.Default.AutoAwesome,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        OnboardingPage(
            title = "学习成长",
            description = "我会随着使用越来越懂你，技能越来越强",
            icon = Icons.Default.TrendingUp,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        ),
        OnboardingPage(
            title = "隐私安全",
            description = "所有数据都在本地处理，你的隐私只有你知道",
            icon = Icons.Default.Security,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (pagerState.currentPage < pages.size - 1) {
                TextButton(
                    onClick = {
                        preferenceManager.hasCompletedOnboarding = true
                        onComplete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text("跳过")
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            OnboardingPageContent(page = pages[page])
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(pages.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(if (pagerState.currentPage == index) 24.dp else 8.dp, 8.dp)
                            .clip(CircleShape)
                            .background(
                                if (pagerState.currentPage == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                    )
                }
            }

            if (pagerState.currentPage == pages.size - 1) {
                Button(
                    onClick = {
                        preferenceManager.hasCompletedOnboarding = true
                        onComplete()
                    },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("开始使用")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ArrowForward, null)
                }
            } else {
                FilledIconButton(
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "下一步")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            page.backgroundColor,
                            page.backgroundColor.copy(alpha = 0.6f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                page.icon,
                null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Composable
fun SplashScreen(
    onNavigateToOnboarding: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val preferenceManager = remember { PreferenceManager(context) }
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        animationPlayed = true
    }

    LaunchedEffect(animationPlayed) {
        if (animationPlayed) {
            kotlinx.coroutines.delay(500)
            if (preferenceManager.hasCompletedOnboarding) {
                onNavigateToHome()
            } else {
                onNavigateToOnboarding()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "娲",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "女娲",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "您的智能伙伴",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
