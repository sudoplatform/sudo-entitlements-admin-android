/*
 * Copyright © 2022 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin.types

/**
 * Description of an operation to be performed when calling
 * applyEntitlementsSetToUsers bulk entitlements set application
 * method
 */
data class ApplyEntitlementsSetOperation(
    /**
     * External ID of the user to apply the entitlements set to
     */
    val externalId: String,

    /**
     * Name of the entitlements set to apply
     */
    val entitlementsSetName: String
)
