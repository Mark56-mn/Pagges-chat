package com.example.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderColor
import com.example.ui.theme.OnPrimaryContainer
import com.example.ui.theme.PrimaryContainer

enum class MainTab {
    WALLET, CHATS, PROFILE
}

@Composable
fun MainScreen(onNavigateToChat: (String) -> Unit, onLogout: () -> Unit, onNavigateToSettings: () -> Unit = {}) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.CHATS) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, BorderColor)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CustomBottomNavItem(
                        selected = selectedTab == MainTab.WALLET,
                        onClick = { selectedTab = MainTab.WALLET },
                        iconFilled = Icons.Filled.Wallet,
                        iconOutlined = Icons.Outlined.Wallet,
                        label = "Wallet"
                    )
                    CustomBottomNavItem(
                        selected = selectedTab == MainTab.CHATS,
                        onClick = { selectedTab = MainTab.CHATS },
                        iconFilled = Icons.Filled.ChatBubble,
                        iconOutlined = Icons.Outlined.ChatBubbleOutline,
                        label = "Chats"
                    )
                    CustomBottomNavItem(
                        selected = selectedTab == MainTab.PROFILE,
                        onClick = { selectedTab = MainTab.PROFILE },
                        iconFilled = Icons.Filled.Person,
                        iconOutlined = Icons.Outlined.PersonOutline,
                        label = "Profile"
                    )
                }
            }
        }
    ) { innerPadding ->
        Crossfade(
            targetState = selectedTab,
            label = "Main Tab Crossfade",
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) { tab ->
            when (tab) {
                MainTab.WALLET -> WalletScreen()
                MainTab.CHATS -> ChatsScreen(onNavigateToChat = onNavigateToChat)
                MainTab.PROFILE -> ProfileScreen(onLogout = onLogout, onNavigateToSettings = onNavigateToSettings)
            }
        }
    }
}

@Composable
fun CustomBottomNavItem(
    selected: Boolean,
    onClick: () -> Unit,
    iconFilled: ImageVector,
    iconOutlined: ImageVector,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = false, radius = 32.dp),
                onClick = onClick
            )
            .width(64.dp)
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val bgColor = if (selected) PrimaryContainer else Color.Transparent
        val iconColor = if (selected) OnPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
        val fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        val textColor = if (selected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant

        Box(
            modifier = Modifier
                .height(32.dp)
                .width(56.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) iconFilled else iconOutlined,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}
