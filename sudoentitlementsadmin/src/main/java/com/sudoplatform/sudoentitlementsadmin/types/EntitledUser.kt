/**
 * Copyright © 2020 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin.types

/**
 * Entitled user.
 */
data class EntitledUser(
    /**
     * External IDP identifier identifying the user.
     */
    val externalId: String
)
