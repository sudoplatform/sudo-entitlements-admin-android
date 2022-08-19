/*
 * Copyright © 2022 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin.types

/**
 * Description of an operation to be performed when calling
 * applyEntitlementsSequenceToUsers bulk entitlements sequence application
 * method
 */
data class ApplyEntitlementsSequenceOperation(
    /**
     * External ID of the user to apply the entitlements sequence to
     */
    val externalId: String,

    /**
     * Name of the entitlements sequence to apply
     */
    val entitlementsSequenceName: String
)
