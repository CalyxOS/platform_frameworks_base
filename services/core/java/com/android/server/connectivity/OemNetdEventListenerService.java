/*
 * Copyright (C) 2022 The Calyx Institute
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

package com.android.server.connectivity;

import android.os.RemoteException;
import android.util.Log;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.net.IOemNetdEventCallback;
import com.android.internal.net.IOemNetdEventListener;

/**
 * Implementation of the INetdEventListener interface.
 */
public class OemNetdEventListenerService extends IOemNetdEventListener.Stub {

    public static final String SERVICE_NAME = "oem_netd_listener";

    private static final String TAG = OemNetdEventListenerService.class.getSimpleName();
    private static final boolean DBG = false;

    @GuardedBy("this")
    private static final int[] ALLOWED_CALLBACK_TYPES = {
        IOemNetdEventCallback.CALLBACK_CALLER_NETWORK_POLICY_MANAGER_SERVICE
    };

    @GuardedBy("this")
    private IOemNetdEventCallback[] mNetdEventCallbackList =
            new IOemNetdEventCallback[ALLOWED_CALLBACK_TYPES.length];

    public synchronized boolean addNetdEventCallback(int callerType, IOemNetdEventCallback callback) {
        if (!isValidCallerType(callerType)) {
            Log.e(TAG, "Invalid caller type: " + callerType);
            return false;
        }
        mNetdEventCallbackList[callerType] = callback;
        return true;
    }

    public synchronized boolean removeNetdEventCallback(int callerType) {
        if (!isValidCallerType(callerType)) {
            Log.e(TAG, "Invalid caller type: " + callerType);
            return false;
        }
        mNetdEventCallbackList[callerType] = null;
        return true;
    }

    private static boolean isValidCallerType(int callerType) {
        for (int i = 0; i < ALLOWED_CALLBACK_TYPES.length; i++) {
            if (callerType == ALLOWED_CALLBACK_TYPES[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    // This method must not block or perform long-running operations.
    public synchronized void onBindEvent(int netId, int uid) throws RemoteException {
        for (IOemNetdEventCallback callback : mNetdEventCallbackList) {
            if (callback != null) {
                callback.onBindEvent(netId, uid);
            }
        }
    }
}
