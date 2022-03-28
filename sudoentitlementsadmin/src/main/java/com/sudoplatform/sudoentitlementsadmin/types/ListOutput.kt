/**
 * Copyright © 2020 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin.types

/**
 * Representation of a generic type to wrap around a GraphQL list type. This is useful for
 * exposing a list of [items] and [nextToken] to allow for pagination by calling for the next
 * set of paginated results.
 */
data class ListOutput<T> (
    /**
     * Items returned from a list query output.
     */
    val items: List<T>,
    /**
     * Generated next token to call for the next page of paginated results.
     */
    val nextToken: String?
)
