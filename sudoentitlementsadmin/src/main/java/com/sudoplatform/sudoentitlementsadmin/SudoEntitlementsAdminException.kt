/*
 * Copyright © 2022 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin

import com.apollographql.apollo.api.Error

open class SudoEntitlementsAdminException(message: String? = null, cause: Throwable? = null) :
    RuntimeException(message, cause) {

    companion object {
        private const val GRAPHQL_ERROR_TYPE = "errorType"
        private const val GRAPHQL_ERROR_ALREADY_UPDATED_ERROR =
            "sudoplatform.entitlements.AlreadyUpdatedError"
        private const val GRAPHQL_ERROR_BULK_OPERATION_DUPLICATE_USERS_ERROR =
            "sudoplatform.entitlements.BulkOperationDuplicateUsersError"
        private const val GRAPHQL_ERROR_DECODING_ERROR = "sudoplatform.DecodingError"
        private const val GRAPHQL_ERROR_DUPLICATE_ENTITLEMENT_ERROR =
            "sudoplatform.entitlements.DuplicateEntitlementError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SEQUENCE_ALREADY_EXISTS_ERROR =
            "sudoplatform.entitlements.EntitlementsSequenceAlreadyExistsError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SEQUENCE_NOT_FOUND_ERROR =
            "sudoplatform.entitlements.EntitlementsSequenceNotFoundError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SEQUENCE_UPDATE_IN_PROGRESS_ERROR =
            "sudoplatform.entitlements.EntitlementsSequenceUpdateInProgressError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SET_ALREADY_EXISTS_ERROR =
            "sudoplatform.entitlements.EntitlementsSetAlreadyExistsError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SET_IMMUTABLE_ERROR =
            "sudoplatform.entitlements.EntitlementsSetImmutableError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SET_IN_USE_ERROR =
            "sudoplatform.entitlements.EntitlementsSetInUseError"
        private const val GRAPHQL_ERROR_ENTITLEMENTS_SET_NOT_FOUND_ERROR =
            "sudoplatform.entitlements.EntitlementsSetNotFoundError"
        private const val GRAPHQL_ERROR_INVALID_ARGUMENT_ERROR = "sudoplatform.InvalidArgumentError"
        private const val GRAPHQL_ERROR_INVALID_ENTITLEMENTS_ERROR =
            "sudoplatform.entitlements.InvalidEntitlementsError"
        private const val GRAPHQL_ERROR_LIMIT_EXCEEDED_ERROR = "sudoplatform.LimitExceededError"
        private const val GRAPHQL_ERROR_NEGATIVE_ENTITLEMENT_ERROR =
            "sudoplatform.entitlements.NegativeEntitlementError"
        private const val GRAPHQL_ERROR_NO_ENTITLEMENTS_ERROR =
            "sudoplatform.NoEntitlementsError"
        private const val GRAPHQL_ERROR_SERVICE_ERROR = "sudoplatform.ServiceError"

        /**
         * Convert from an error string into a custom exception of type [SudoEntitlementsAdminException]
         */
        fun sudoEntitlementsAdminException(errorString: String, message: String? = errorString): SudoEntitlementsAdminException {
            return when (errorString) {
                GRAPHQL_ERROR_ALREADY_UPDATED_ERROR -> AlreadyUpdatedException(message)
                GRAPHQL_ERROR_BULK_OPERATION_DUPLICATE_USERS_ERROR -> BulkOperationDuplicateUsersException(message)
                GRAPHQL_ERROR_DUPLICATE_ENTITLEMENT_ERROR -> DuplicateEntitlementException(message)
                GRAPHQL_ERROR_SERVICE_ERROR -> InternalServerException(message)
                GRAPHQL_ERROR_DECODING_ERROR, GRAPHQL_ERROR_INVALID_ARGUMENT_ERROR -> InvalidInputException(
                    message,
                )
                GRAPHQL_ERROR_INVALID_ENTITLEMENTS_ERROR -> InvalidEntitlementsException(message)
                GRAPHQL_ERROR_ENTITLEMENTS_SET_IN_USE_ERROR -> EntitlementsSetInUseException(message)
                GRAPHQL_ERROR_ENTITLEMENTS_SET_NOT_FOUND_ERROR -> EntitlementsSetNotFoundException(
                    message,
                )
                GRAPHQL_ERROR_ENTITLEMENTS_SET_ALREADY_EXISTS_ERROR -> EntitlementsSetAlreadyExistsException(
                    message,
                )
                GRAPHQL_ERROR_ENTITLEMENTS_SEQUENCE_ALREADY_EXISTS_ERROR -> EntitlementsSequenceAlreadyExistsException(
                    message,
                )
                GRAPHQL_ERROR_ENTITLEMENTS_SEQUENCE_NOT_FOUND_ERROR -> EntitlementsSequenceNotFoundException(
                    message,
                )
                GRAPHQL_ERROR_ENTITLEMENTS_SEQUENCE_UPDATE_IN_PROGRESS_ERROR -> EntitlementsSequenceUpdateInProgressException(
                    message,
                )
                GRAPHQL_ERROR_ENTITLEMENTS_SET_IMMUTABLE_ERROR -> EntitlementsSetImmutableException(
                    message,
                )
                GRAPHQL_ERROR_LIMIT_EXCEEDED_ERROR -> LimitExceededException(message)
                GRAPHQL_ERROR_NEGATIVE_ENTITLEMENT_ERROR -> NegativeEntitlementException(message)
                GRAPHQL_ERROR_NO_ENTITLEMENTS_ERROR -> NoEntitlementsException(message)
                else -> GraphQLException(message)
            }
        }

        /**
         * Convert from a GraphQL [Error] into a custom exception of type [SudoEntitlementsAdminException]
         */
        fun Error.toSudoEntitlementsAdminException(): SudoEntitlementsAdminException {
            return sudoEntitlementsAdminException(errorString = this.customAttributes()[GRAPHQL_ERROR_TYPE] as String)
        }
    }

    /**
     * Indicates that an attempt to update a user's entitlements is made after the
     * user's entitlements have already been updated to a later version
     */
    class AlreadyUpdatedException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that a bulk operation has specified multiple operations for the same user
     */
    class BulkOperationDuplicateUsersException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that an operation has invalidly specified the same entitlement multiple times
     */
    class DuplicateEntitlementException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that the attempt to add a new entitlement sequence failed because an entitlements
     * sequence with the same name already exists.
     */
    class EntitlementsSequenceAlreadyExistsException(
        message: String? = null,
        cause: Throwable? = null,
    ) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that the input entitlements sequence name does not exists when applying an
     * entitlements sequence to a user.
     */
    class EntitlementsSequenceNotFoundException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that an entitlements sequence update is already in progress
     * when setEntitlementsSequence or removeEntitlementsSequence is attempted.
     */
    class EntitlementsSequenceUpdateInProgressException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that the attempt to add a new entitlement set failed because an entitlements set
     * with the same name already exists.
     */
    class EntitlementsSetAlreadyExistsException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that an attempt was made to modify or delete an immutable entitlements set was made
     * (e.g. _unentitled_).
     */
    class EntitlementsSetImmutableException(message: String? = null, cause: Throwable? = null) :
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
     * Indicates that an internal server error caused the operation to fail. The error is
     * possibly transient and retrying at a later time may cause the operation to complete
     * successfully.
     */
    class InternalServerException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that the input entitlements name was not recognized.
     */
    class InvalidEntitlementsException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that invalid input was provided to the API call.
     */
    class InvalidInputException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Inidicates that parameters of an API call would exceed a limit
     */
    class LimitExceededException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that an operation would result in negative entitlements for user which is not permittd
     */
    class NegativeEntitlementException(message: String? = null, cause: Throwable? = null) :
        SudoEntitlementsAdminException(message = message, cause = cause)

    /**
     * Indicates that an operation that a user does not already have entitlements defined
     * for an operation that requires this.
     */
    class NoEntitlementsException(message: String? = null, cause: Throwable? = null) :
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
