/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.android.internal.net;

/** {@hide} */
oneway interface IOemNetdEventCallback {

    // Possible addNetdEventCallback callers.
    const int CALLBACK_CALLER_NETWORK_POLICY_MANAGER_SERVICE = 0;

    /**
     * Reports a single bind function call.
     * This method must not block or perform long-running operations.
     *
     * @param netId the ID of the network the bind was performed on.
     * @param uid the UID of the application that performed the bind.
     */
    void onBindEvent(int netId, int uid);
}
