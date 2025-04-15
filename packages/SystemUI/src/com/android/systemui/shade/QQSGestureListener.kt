/*
 * Copyright (C) 2023 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.shade

import android.content.Context
import android.database.ContentObserver
import android.os.PowerManager
import android.view.GestureDetector
import android.view.MotionEvent
import com.android.systemui.dagger.SysUISingleton
import com.android.systemui.dagger.qualifiers.Background
import com.android.systemui.plugins.FalsingManager
import com.android.systemui.plugins.statusbar.StatusBarStateController
import com.android.systemui.user.domain.interactor.SelectedUserInteractor
import lineageos.providers.LineageSettings
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@SysUISingleton
class QQSGestureListener @Inject constructor(
        @Background private val backgroundScope: CoroutineScope,
        private val context: Context,
        private val falsingManager: FalsingManager,
        private val powerManager: PowerManager,
        private val statusBarStateController: StatusBarStateController,
        private val selectedUserInteractor: SelectedUserInteractor,
) : GestureDetector.SimpleOnGestureListener() {

    private var currentUserId: Int? = null
    // Let our handling of this setting be reused elsewhere via callbacks.
    private val doubleTapToSleepCallbacks = mutableListOf<(Boolean) -> Unit>()
    private var doubleTapToSleepEnabled = false
    private val quickQsOffsetHeight: Int

    init {
        initDoubleTapToSleepSettingObserver()

        quickQsOffsetHeight = context.resources.getDimensionPixelSize(
                com.android.internal.R.dimen.quick_qs_offset_height)
    }

    override fun onDoubleTapEvent(e: MotionEvent): Boolean {
        // Go to sleep on double tap the QQS status bar
        if (e.actionMasked == MotionEvent.ACTION_UP &&
                !statusBarStateController.isDozing &&
                doubleTapToSleepEnabled &&
                e.getY() < quickQsOffsetHeight &&
                !falsingManager.isFalseDoubleTap
        ) {
            powerManager.goToSleep(e.getEventTime())
            return true
        }
        return false
    }

    /**
     * Initialize all necessary handling of the double-tap-to-sleep setting and user switches.
     * We need to keep track of user switches to know which user's setting to read.
     */
    private fun initDoubleTapToSleepSettingObserver() {
        val isDoubleTapToSleepEnabledByDefault = context.resources.getBoolean(
            org.lineageos.platform.internal.R.bool.config_dt2sGestureEnabledByDefault)

        // Monitor setting changes.
        val doubleTapToSleepSettingObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean) {
                val value = LineageSettings.System.getIntForUser(
                    context.contentResolver, LineageSettings.System.DOUBLE_TAP_SLEEP_GESTURE,
                    if (isDoubleTapToSleepEnabledByDefault) 1 else 0,
                    currentUserId!!) != 0
                doubleTapToSleepEnabled = value
                doubleTapToSleepCallbacks.forEach { it.invoke(value) }
            }
        }

        // Monitor user switches.
        backgroundScope.launch {
            selectedUserInteractor.selectedUser.collect { userId ->
                if (currentUserId != null) {
                    context.contentResolver.unregisterContentObserver(
                        doubleTapToSleepSettingObserver)
                }
                currentUserId = userId
                context.contentResolver.registerContentObserver(
                    LineageSettings.System.getUriFor(
                        LineageSettings.System.DOUBLE_TAP_SLEEP_GESTURE),
                    false, doubleTapToSleepSettingObserver, userId)
                doubleTapToSleepSettingObserver.onChange(true)
            }
        }
    }

    fun addDoubleTapToSleepCallbackAndInvoke(callback: (Boolean) -> Unit) {
        doubleTapToSleepCallbacks.add(callback)
        callback.invoke(doubleTapToSleepEnabled)
    }

    fun removeDoubleTapToSleepCallback(callback: (Boolean) -> Unit) {
        doubleTapToSleepCallbacks.remove(callback)
    }
}
