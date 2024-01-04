/*
 * Copyright © 2022 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin.types

/**
 * Representation of an entitlement.
 */
data class Entitlement(
    /**
     * Name of the entitlement.
     */
    val name: String,

    /**
     * Description, if any, of the entitlement.
     */
    val description: String?,

    /**
     * Value of the entitlement.
     */
    val value: Int,
)
