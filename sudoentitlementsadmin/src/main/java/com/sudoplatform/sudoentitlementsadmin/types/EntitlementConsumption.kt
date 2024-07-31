/*
 * Copyright © 2024 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin.types

import java.util.Date

/**
 * Entitlement consumption information.
 */
data class EntitlementConsumption(
    /**
     * Name of the entitlement.
     */
    val name: String,

    /**
     * Value of the entitlement.
     */
    val value: Long,
    /**
     * Remaining amount of entitlement.
     */
    val available: Long,

    /**
     * Consumed amount of entitlement.
     */
    val consumed: Long,

    /**
     * The time at which this entitlement was first consumed.
     */
    val firstConsumedAt: Date?,

    /**
     * The most recent time at which this entitlement was consumed.
     */
    val lastConsumedAt: Date?,
)
