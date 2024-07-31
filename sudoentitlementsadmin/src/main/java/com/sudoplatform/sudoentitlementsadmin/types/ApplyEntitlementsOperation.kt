/*
 * Copyright © 2024 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin.types

/**
 * Description of an operation to be performed when calling
 * applyEntitlementsToUsers bulk entitlements application
 * method.
 */
data class ApplyEntitlementsOperation(
    /**
     * External ID of the user to apply entitlements to
     */
    val externalId: String,

    /**
     * List of entitlements to apply
     */
    val entitlements: List<Entitlement>,
)
