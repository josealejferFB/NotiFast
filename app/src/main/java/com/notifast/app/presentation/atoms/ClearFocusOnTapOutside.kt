package com.notifast.app.presentation.atoms

import android.content.Context
import android.view.inputmethod.InputMethodManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView

/**
 * Reusable Compose modifier that clears input focus and smoothly hides
 * the software keyboard (IME) when tapping outside of inputs or on empty space.
 *
 * It coordinates [LocalFocusManager], [LocalSoftwareKeyboardController], and
 * Android's [InputMethodManager] via [LocalView] to ensure consistent dismissal
 * across all Android OEM keyboards without interfering with vertical scroll.
 */
fun Modifier.clearFocusOnTapOutside(
    onDismissExtra: (() -> Unit)? = null
): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    val onClear: () -> Unit = remember(focusManager, keyboardController, view, onDismissExtra) {
        {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
            onDismissExtra?.invoke()
            Unit
        }
    }

    this.clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
    ) {
        onClear()
    }
}

/**
 * Returns a remembered lambda that clears focus, hides the virtual keyboard,
 * and notifies [InputMethodManager].
 */
@Composable
fun rememberClearFocusAction(onExtra: (() -> Unit)? = null): () -> Unit {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val view = LocalView.current

    return remember(focusManager, keyboardController, view, onExtra) {
        {
            focusManager.clearFocus(force = true)
            keyboardController?.hide()
            val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
            onExtra?.invoke()
            Unit
        }
    }
}
