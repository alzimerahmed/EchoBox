package com.alzimer.echobox.ui.navigation.graph

import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.alzimer.echobox.ui.navigation.destination.home.CreditDestination
import com.alzimer.echobox.ui.navigation.destination.home.ListenTogetherDestination
import com.alzimer.echobox.ui.navigation.destination.home.ListenTogetherSettingsDestination
import com.alzimer.echobox.ui.navigation.destination.home.MoodDestination
import com.alzimer.echobox.ui.navigation.destination.home.NotificationDestination
import com.alzimer.echobox.ui.navigation.destination.home.RecentlySongsDestination
import com.alzimer.echobox.ui.navigation.destination.home.SettingsDestination
import com.alzimer.echobox.ui.screen.home.ListenTogetherScreen
import com.alzimer.echobox.ui.screen.home.ListenTogetherSettingsScreen
import com.alzimer.echobox.ui.screen.home.MoodScreen
import com.alzimer.echobox.ui.screen.home.NotificationScreen
import com.alzimer.echobox.ui.screen.home.RecentlySongsScreen
import com.alzimer.echobox.ui.screen.home.SettingScreen
import com.alzimer.echobox.ui.screen.other.CreditScreen

fun NavGraphBuilder.homeScreenGraph(
    innerPadding: PaddingValues,
    navController: NavController,
) {
    composable<CreditDestination> {
        CreditScreen(
            paddingValues = innerPadding,
            navController = navController,
        )
    }
    composable<MoodDestination> { entry ->
        val params = entry.toRoute<MoodDestination>().params
        MoodScreen(
            navController = navController,
            params = params,
        )
    }
    composable<ListenTogetherDestination> {
        ListenTogetherScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<ListenTogetherSettingsDestination> {
        ListenTogetherSettingsScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<NotificationDestination> {
        NotificationScreen(
            navController = navController,
        )
    }
    composable<RecentlySongsDestination> {
        RecentlySongsScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
    composable<SettingsDestination> {
        SettingScreen(
            navController = navController,
            innerPadding = innerPadding,
        )
    }
}