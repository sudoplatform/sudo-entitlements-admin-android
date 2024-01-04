/*
 * Copyright © 2022 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin.types

/**
 * Entitlements consumption information.
 */
data class UserEntitlementsConsumption(
    /**
     * User's entitlements.
     */
    val entitlements: UserEntitlements,

    /**
     * Entitlement consumption information for each of a user's
     * entitlements. If there is no entry for an entitlement,
     * none of the entitlement has been consumed.
     */
    val consumption: List<EntitlementConsumption>,
)
