/*
 * Copyright © 2022 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin.types

import java.util.Date

enum class AccountState {
    /**
     * Account is active and can consume entitlements.
     */
    ACTIVE,

    /**
     * Account is locked and entitlement cannot be consumed.
     */
    LOCKED
}

/**
 * Entitlements of a user.
 */
data class UserEntitlements(
    /**
     * Time of initial creation of user entitlements mapping.
     */
    val createdAt: Date,

    /**
     * Time of last updates of user entitlements mapping.
     */
    val updatedAt: Date,

    /**
     * Version number of the user's entitlements. This is incremented every
     * time there is a change of entitlements set or explicit entitlements
     * for this user.
     *
     * For users entitled by entitlement set, the fractional part of this version
     * specifies the version of the entitlements set itself. Entitlements set version
     * is divided by 100000 then added to the user entitlements version
     *
     * This ensures that the version of user entitlements always increases monotonically.
     */
    val version: Double,

    /**
     * External IDP identifier identifying the user
     */
    val externalId: String,

    /**
     * Sudo Platform owner. This value matches the subject in identity
     * tokens used to authenticate to Sudo Platform services. Will not
     * be present if the user has not yet redeemed their identity token
     * with the entitlements service.
     */
    val owner: String?,

    /**
     * Name of the entitlements set specified for this user. Will be undefined
     * if entitlements have been specified explicitly rather than by an
     * entitlements set.
     */
    val entitlementsSetName: String?,

    /**
     * Name of the entitlements sequence specified for this user. Will be
     * undefined if entitlements have been specified explicitly or entitlements
     * set is set.
     */
    val entitlementsSequenceName: String?,

    /**
     * Effective entitlements for the user either obtained from the entitlements
     * set or as specified explicitly for this user.
     */
    val entitlements: List<Entitlement>,

    /**
     * Date from when user's transitions should be calculated. Defaults to current
     * time.
     */
    val transitionsRelativeTo: Date?,

    /**
     * If `locked` the user can no longer consume entitlements. It will be nil
     * if the user has not redeemed any entitlements.
     */
    val accountState: AccountState
)
