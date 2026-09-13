package com.alzimer.echobox.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.alzimer.echobox.domain.mediaservice.handler.ControlState
import com.alzimer.echobox.domain.mediaservice.handler.RepeatState
import com.alzimer.echobox.viewModel.UIEvent
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Smoke tests for the player transport controls: the layout must render from a [ControlState]
 * and route taps to the right [UIEvent]s. The icons carry empty contentDescriptions, so taps
 * are placed geometrically — shuffle sits in the left third, play/pause dead centre.
 */
@RunWith(AndroidJUnit4::class)
class PlayerControlLayoutTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun controlState(
        isPlaying: Boolean = false,
        isShuffle: Boolean = false,
    ) = ControlState(
        isPlaying = isPlaying,
        isShuffle = isShuffle,
        repeatState = RepeatState.None,
        isLiked = false,
        isNextAvailable = true,
        isPreviousAvailable = true,
        isCrossfading = false,
        volume = 1f,
    )

    @Test
    fun rendersInPausedState() {
        composeRule.setContent {
            MaterialTheme {
                PlayerControlLayout(
                    controllerState = controlState(isPlaying = false),
                    onUIEvent = {},
                )
            }
        }
        composeRule.onRoot().assertExists()
    }

    @Test
    fun rendersInPlayingState() {
        composeRule.setContent {
            MaterialTheme {
                PlayerControlLayout(
                    controllerState = controlState(isPlaying = true, isShuffle = true),
                    onUIEvent = {},
                )
            }
        }
        composeRule.onRoot().assertExists()
    }

    @Test
    fun centreTapEmitsPlayPause() {
        val events = mutableListOf<UIEvent>()
        composeRule.setContent {
            MaterialTheme {
                PlayerControlLayout(
                    controllerState = controlState(),
                    onUIEvent = { events.add(it) },
                )
            }
        }
        composeRule.onRoot().performTouchInput { click(center) }
        composeRule.waitForIdle()
        assertTrue(UIEvent.PlayPause in events)
    }

    @Test
    fun leftTapEmitsShuffle() {
        val events = mutableListOf<UIEvent>()
        composeRule.setContent {
            MaterialTheme {
                PlayerControlLayout(
                    controllerState = controlState(),
                    onUIEvent = { events.add(it) },
                )
            }
        }
        composeRule.onRoot().performTouchInput { click(Offset(width * 0.12f, center.y)) }
        composeRule.waitForIdle()
        assertTrue(UIEvent.Shuffle in events)
    }
}
