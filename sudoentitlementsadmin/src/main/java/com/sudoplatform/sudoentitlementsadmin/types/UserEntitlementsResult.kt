/*
 * Copyright © 2024 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin.types

import com.sudoplatform.sudoentitlementsadmin.SudoEntitlementsAdminException

sealed class UserEntitlementsResult {
    data class Success(
        val value: UserEntitlements,
    ) : UserEntitlementsResult()

    data class Failure(
        val error: SudoEntitlementsAdminException,
    ) : UserEntitlementsResult()
}
