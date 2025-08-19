/*
 * Copyright © 2024 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin.types

import java.util.Date

/**
 * Definition of a single transition within an entitlements sequence
 */
data class EntitlementsSequenceTransition(
    /**
     * Name of entitlements set.
     */
    val entitlementsSetName: String,
    /**
     * ISO8601 period string - if not specified then this transition
     * is the final state for all users on the sequence.
     */
    val duration: String?,
)

/**
 * Definition of a sequence of entitlements sets through which a user will transition
 */
data class EntitlementsSequence(
    /**
     * Time at which the entitlements sequence was originally created
     */
    val createdAt: Date,
    /**
     * Time at which the entitlements sequence was most recently updated.
     */
    val updatedAt: Date,
    /**
     * Version number of the entitlements sequence. This is incremented every
     * time there is a change to this entitlements sequence.
     */
    val version: Int,
    /**
     * Name of this entitlements sequence.
     */
    val name: String,
    /**
     * Description, if any, of the entitlements sequence as specified by the entitlements
     * administrator.
     */
    val description: String?,
    /**
     * The sequence of transitions a user will go through in order.
     */
    val transitions: List<EntitlementsSequenceTransition>,
)
