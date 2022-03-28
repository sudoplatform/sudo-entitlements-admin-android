/**
 * Copyright © 2020 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin

import com.apollographql.apollo.api.Error

open class SudoEntitlementsAdminException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {

    companion object {
        private const val GRAPHQL_ERROR_TYPE = "errorType"
        private const val GRAPHQL_ERROR_SERVICE_ERROR = "sudoplatform.ServiceError"
        private const val GRAPHQL_ERROR_DECODING_ERROR = "sudoplatform.DecodingError"
        private const val GRAPHQL_ERROR_INVALID_ARGUMENT_ERROR = "sudoplatform.InvalidArgumentError"
        private const val GRAPHQL_ERROR_INVALID_ENTITLEMENTS_ERROR =
            "sudoplatform.entitlements.InvalidEntitlementsError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SET_IN_USE_ERROR =
            "sudoplatform.entitlements.EntitlementsSetInUse"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SET_NOT_FOUND_ERROR =
            "sudoplatform.entitlements.EntitlementsSetNotFoundError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SET_ALREADY_EXISTS_ERROR =
            "sudoplatform.entitlements.EntitlementsSetAlreadyExistsError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SEQUENCE_ALREADY_EXISTS_ERROR =
            "sudoplatform.entitlements.EntitlementsSequenceAlreadyExistsError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SEQUENCE_NOT_FOUND_ERROR =
            "sudoplatform.entitlements.EntitlementsSequenceNotFoundError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SET_IMMUTABLE_ERROR =
            "sudoplatform.entitlements.EntitlementsSetImmutableError"

        /**
         * Convert from a GraphQL [Error] into a custom exception of type [SudoEntitlementsAdminException]
         */
        fun Error.toSudoEntitlementsAdminException(): SudoEntitlementsAdminException {
            return when (this.customAttributes()[GRAPHQL_ERROR_TYPE]) {
                GRAPHQL_ERROR_SERVICE_ERROR -> InternalServerException(this.message())
                GRAPHQL_ERROR_DECODING_ERROR, GRAPHQL_ERROR_INVALID_ARGUMENT_ERROR -> InvalidInputException(
                    this.message()
                )
                GRAPHQL_ERROR_INVALID_ENTITLEMENTS_ERROR -> InvalidEntitlementsException(this.message())
                GRAPHQL_ERROR_ENTITLEMENTS_SET_IN_USE_ERROR -> EntitlementsSetInUseException(this.message())
                GRAPHQL_ERROR_ENTITLEMENTS_SET_NOT_FOUND_ERROR -> EntitlementsSetNotFoundException(
                    this.message()
                )
                GRAPHQL_ERROR_ENTITLEMENTS_SET_ALREADY_EXISTS_ERROR -> EntitlementsSetAlreadyExistsException(
                    this.message()
                )
                GRAPHQL_ERROR_ENTITLEMENTS_SEQUENCE_ALREADY_EXISTS_ERROR -> EntitlementsSequenceAlreadyExistsException(
                    this.message()
                )
                GRAPHQL_ERROR_ENTITLEMENTS_SEQUENCE_NOT_FOUND_ERROR -> EntitlementsSequenceNotFoundException(
                    this.message()
                )
                GRAPHQL_ERROR_ENTITLEMENTS_SET_IMMUTABLE_ERROR -> EntitlementsSetImmutableException(
                    this.message()
                )
                else -> GraphQLException(this.message())
            }
        }
    }

    /**
     * Indicates that invalid input was provided to the API call.
     */
    class InvalidInputException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that the input entitlements name was not recognized.
     */
    class InvalidEntitlementsException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that an attempt has been made to delete an entitlements set that is currently in
     * use by one or more entitlements sequences.
     */
    class EntitlementsSetInUseException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that the input entitlements set name does not exists when applying an entitlements
     * set to a user.
     */
    class EntitlementsSetNotFoundException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that the attempt to add a new entitlement set failed because an entitlements set
     * with the same name already exists.
     */
    class EntitlementsSetAlreadyExistsException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that the attempt to add a new entitlement sequence failed because an entitlements
     * sequence with the same name already exists.
     */
    class EntitlementsSequenceAlreadyExistsException(
        message: String? = null,
        cause: Throwable? = null
    ) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that the input entitlements sequence name does not exists when applying an
     * entitlements sequence to a user.
     */
    class EntitlementsSequenceNotFoundException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that an attempt was made to modify or delete an immutable entitlements set was made
     * (e.g. _unentitled_).
     */
    class EntitlementsSetImmutableException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that an internal server error caused the operation to fail. The error is
     * possibly transient and retrying at a later time may cause the operation to complete
     * successfully.
     */
    class InternalServerException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that GraphQL API returned an error that is not recognized by the client.
     */
    class GraphQLException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that an unexpected error occurred. This could be due to coding error, out-of-
     * memory conditions or other conditions that is beyond control of the client.
     *
     */
    class FailedException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)
}