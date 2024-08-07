/*
 * Copyright © 2024 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin

import com.sudoplatform.sudologging.AndroidUtilsLogDriver
import com.sudoplatform.sudologging.LogLevel
import com.sudoplatform.sudologging.Logger

/**
 * Default logger.
 */
class DefaultLogger {

    companion object {
        val instance = Logger("SudoEntitlementsAdmin", AndroidUtilsLogDriver(LogLevel.INFO))
    }
}
