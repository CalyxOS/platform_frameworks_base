// HACK: Just a quick hack - Don't use this!

/*
 * Copyright (C) 2021 Zhaofeng Li
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

package com.android.systemui.biometrics;

import android.annotation.Nullable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.Surface;

import com.android.systemui.biometrics.UdfpsHbmTypes.HbmType;

import com.google.hardware.pixel.display.IDisplay;

public class RavioleUdfpsHbmProvider implements UdfpsHbmProvider {
	private static final String TAG = "RavioleUdfpsHbmProvider";

	private final IDisplay mDisplayHal;

	public RavioleUdfpsHbmProvider() {
		mDisplayHal = IDisplay.Stub.asInterface(ServiceManager.waitForDeclaredService("com.google.hardware.pixel.display.IDisplay/default"));

		if (mDisplayHal == null) {
			Log.d(TAG, "failed to get display HAL - this will be noop");
		}
		
		Log.d(TAG, "initialized HBM provider");
	}

	@Override
    public void enableHbm(@HbmType int hbmType, @Nullable Surface surface,
            @Nullable Runnable onHbmEnabled) {
		if (mDisplayHal == null) {
			Log.d(TAG, "enableHbm called without a valid display HAL (noop)");
			return;
		}

		// FIXME: Handle different HBM types
		try {
			mDisplayHal.setLhbmState(true);

			Log.d(TAG, "enabled HBM");

			if (onHbmEnabled != null) {
				onHbmEnabled.run();
			}
		} catch (RemoteException e) {
			Log.e(TAG, "enableHbm failed", e);
		}
	}

	@Override
    public void disableHbm(@Nullable Runnable onHbmDisabled) {
		if (mDisplayHal == null) {
			Log.d(TAG, "disableHbm called without a valid display HAL (noop)");
			return;
		}

		// FIXME: Handle different HBM types
		try {
			mDisplayHal.setLhbmState(false);

			Log.d(TAG, "disabled HBM");

			if (onHbmDisabled != null) {
				onHbmDisabled.run();
			}
		} catch (RemoteException e) {
			Log.e(TAG, "disableHbm failed", e);
		}
	}
}
