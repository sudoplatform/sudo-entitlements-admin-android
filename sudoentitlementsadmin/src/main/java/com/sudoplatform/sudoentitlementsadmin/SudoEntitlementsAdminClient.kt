/*
 * Copyright © 2024 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin

import android.content.Context
import com.amazonaws.regions.Regions
import com.amplifyframework.api.ApiCategory
import com.amplifyframework.api.ApiCategoryConfiguration
import com.amplifyframework.api.aws.AWSApiPlugin
import com.apollographql.apollo3.api.Optional
import com.sudoplatform.sudoconfigmanager.DefaultSudoConfigManager
import com.sudoplatform.sudoentitlementsadmin.SudoEntitlementsAdminException.Companion.toSudoEntitlementsAdminException
import com.sudoplatform.sudoentitlementsadmin.graphql.AddEntitlementsSequenceMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.AddEntitlementsSetMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsSequenceToUserMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsSequenceToUsersMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsSetToUserMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsSetToUsersMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsToUserMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsToUsersMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyExpendableEntitlementsToUserMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.GetEntitlementsForUserQuery
import com.sudoplatform.sudoentitlementsadmin.graphql.GetEntitlementsSequenceQuery
import com.sudoplatform.sudoentitlementsadmin.graphql.GetEntitlementsSetQuery
import com.sudoplatform.sudoentitlementsadmin.graphql.ListEntitlementsSequencesQuery
import com.sudoplatform.sudoentitlementsadmin.graphql.ListEntitlementsSetsQuery
import com.sudoplatform.sudoentitlementsadmin.graphql.RemoveEntitledUserMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.RemoveEntitlementsSequenceMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.RemoveEntitlementsSetMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.SetEntitlementsSequenceMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.SetEntitlementsSetMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.type.AccountStates
import com.sudoplatform.sudoentitlementsadmin.graphql.type.AddEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.AddEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsSequenceToUserInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsSequenceToUsersInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsSetToUserInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsSetToUsersInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsToUserInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsToUsersInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyExpendableEntitlementsToUserInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.EntitlementInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.EntitlementsSequenceTransitionInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.GetEntitlementsForUserInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.GetEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.GetEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.RemoveEntitledUserInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.RemoveEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.RemoveEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.SetEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.SetEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.types.AccountState
import com.sudoplatform.sudoentitlementsadmin.types.ApplyEntitlementsOperation
import com.sudoplatform.sudoentitlementsadmin.types.ApplyEntitlementsSequenceOperation
import com.sudoplatform.sudoentitlementsadmin.types.ApplyEntitlementsSetOperation
import com.sudoplatform.sudoentitlementsadmin.types.EntitledUser
import com.sudoplatform.sudoentitlementsadmin.types.Entitlement
import com.sudoplatform.sudoentitlementsadmin.types.EntitlementConsumption
import com.sudoplatform.sudoentitlementsadmin.types.EntitlementsSequence
import com.sudoplatform.sudoentitlementsadmin.types.EntitlementsSequenceTransition
import com.sudoplatform.sudoentitlementsadmin.types.EntitlementsSet
import com.sudoplatform.sudoentitlementsadmin.types.ListOutput
import com.sudoplatform.sudoentitlementsadmin.types.UserEntitlements
import com.sudoplatform.sudoentitlementsadmin.types.UserEntitlementsConsumption
import com.sudoplatform.sudoentitlementsadmin.types.UserEntitlementsResult
import com.sudoplatform.sudologging.Logger
import com.sudoplatform.sudouser.amplify.GraphQLClient
import com.sudoplatform.sudouser.http.ConvertClientErrorsInterceptor
import org.json.JSONObject
import java.util.Date

/**
 * Interface encapsulating a library of functions for Entitlements Admin API.
 */
interface SudoEntitlementsAdminClient {

    companion object {
        /**
         * Creates a [Builder] for [SudoEntitlementsAdminClient].
         * @param context Android app context.
         * @param apiKey API key to use for authentication.
         */
        fun builder(context: Context, apiKey: String) =
            Builder(context, apiKey)
    }

    /**
     * Builder used to construct [SudoEntitlementsAdminClient].
     * @param context Android app context.
     * @param apiKey API key to use for authentication.
     */
    class Builder(private val context: Context, private val apiKey: String) {
        private var graphQLClient: GraphQLClient? = null
        private var config: JSONObject? = null
        private var logger: Logger = DefaultLogger.instance

        /**
         * Provide a [GraphQLClient] for the [SudoEntitlementsAdminClient]. This is mainly
         * used for unit testing.
         */
        fun setGraphQLClient(graphQLClient: GraphQLClient?) = also {
            this.graphQLClient = graphQLClient
        }

        /**
         * Provide a config object used mainly for unit testing.
         */
        fun setConfig(config: JSONObject) = also {
            this.config = config
        }

        /**
         * Provide the implementation of the [Logger] used for logging. If a value is not supplied
         * a default implementation will be used.
         */
        fun setLogger(logger: Logger) = also {
            this.logger = logger
        }

        /**
         * Constructs and returns an [SudoEntitlementsAdminClient].
         */
        fun build(): SudoEntitlementsAdminClient {
            return DefaultSudoEntitlementsAdminClient(
                this.context,
                this.apiKey,
                this.config,
                this.logger,
                this.graphQLClient,
            )
        }
    }

    /**
     * Checksum's for each file are generated and are used to create a checksum that is used when publishing to maven central.
     * In order to retry a failed publish without needing to change any functionality, we need a way to generate a different checksum
     * for the source code.  We can change the value of this property which will generate a different checksum for publishing
     * and allow us to retry.  The value of `version` doesn't need to be kept up-to-date with the version of the code.
     */
    val version: String

    /**
     * Get an entitlements set.
     *
     * @param name Name of the entitlements set to return.
     *
     * @returns Named entitlements set or null if no entitlements set
     *          of the specified name has been defined.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun getEntitlementsSet(name: String): EntitlementsSet?

    /**
     * Add a new entitlements set.
     *
     * @param name Name of the new entitlements set.
     * @param description Description of the new entitlements set.
     * @param entitlements List of entitlements associated with the new entitlements set.
     *
     * @returns The created entitlements set.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun addEntitlementsSet(
        name: String,
        description: String?,
        entitlements: List<Entitlement>,
    ): EntitlementsSet

    /**
     * List all entitlements sets.
     *
     * Call again with a token parameter to continue paginated listing.
     *
     * @param nextToken Optional token from which to continue listing.
     *
     * @returns Paginated list of entitlements sets.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun listEntitlementsSets(nextToken: String?): ListOutput<EntitlementsSet>

    /**
     * Get entitlements for a user.
     *
     * @param externalId External IDP user ID of user to retrieve entitlements for.
     *
     * @returns Entitlements consumption for the user.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun getEntitlementsForUser(
        externalId: String,
    ): UserEntitlementsConsumption?

    /**
     * Update an entitlements set.
     *
     * @param name Name of the entitlements set to update.
     * @param description Description of the new entitlements set to update.
     * @param entitlements List of entitlements to update.
     *
     * @returns The updated entitlements set.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun setEntitlementsSet(
        name: String,
        description: String?,
        entitlements: List<Entitlement>,
    ): EntitlementsSet

    /**
     * Remove entitlements set.
     *
     * @param name Name of entitlements set to remove.
     *
     * @returns The entitlements set removed or undefined if entitlements set was not present.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun removeEntitlementsSet(name: String): EntitlementsSet?

    /**
     * Apply an entitlements set to a user.
     *
     * If a record for that user's entitlements does not yet exist it will be created.
     *
     * @param externalId External IDP user ID of user to retrieve entitlements for.
     * @param entitlements The entitlements to apply to the user.
     * @param version If specified, version of any current entitlements that must be matched
     *
     * @returns The effective entitlements for the user
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun applyEntitlementsToUser(
        externalId: String,
        entitlements: List<Entitlement>,
        version: Double? = null,
    ): UserEntitlements

    /**
     * Apply entitlements to users.
     *
     * If a record for that user's entitlements does not yet exist it will be created.
     *
     * Equivalent to calling applyEntitlementsToUser for each operation
     *
     * @param operations Array of ApplyEntitlemetnsOperations to perform
     *
     * @returns Array of results
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun applyEntitlementsToUsers(
        operations: List<ApplyEntitlementsOperation>,
    ): List<UserEntitlementsResult>

    /**
     * Apply entitlements set to a user.
     *
     * If a record for that user's entitlements does not yet exist it will be created.
     *
     * @param externalId External IDP user ID of user to retrieve entitlements for.
     * @param entitlementSetName Name of the entitlements set to apply to the user.
     * @param version If specified, version of any current entitlements that must be matched
     *
     * @returns The effective entitlements for the user.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun applyEntitlementsSetToUser(
        externalId: String,
        entitlementSetName: String,
        version: Double? = null,
    ): UserEntitlements

    /**
     * Apply entitlements sets to users.
     *
     * If a record for that user's entitlements does not yet exist it will be created.
     *
     * Equivalent to calling applyEntitlementsSetToUser for each operation
     *
     * @param operations Array of ApplyEntitlementsSetOperations to perform
     *
     * @returns Array of results
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun applyEntitlementsSetToUsers(
        operations: List<ApplyEntitlementsSetOperation>,
    ): List<UserEntitlementsResult>

    /**
     * Apply an expendable entitlements delta to a user. If a record for the user's
     * entitlements does not yet exist a NoEntitlementsForUserError is thrown. Call
     * an applyEntitlements method to assign entitlements before calling this method.
     *
     * @param externalId external IDP user ID of user to apply entitlements to.
     * @param expendableEntitlements the expendable entitlements delta to apply to the user
     * @param requestId
     *     Request of this delta. Repetition of requests for the same external
     *     ID with the same requestId are idempotent
     *
     * @returns The resulting user entitlements
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun applyExpendableEntitlementsToUser(
        externalId: String,
        expendableEntitlements: List<Entitlement>,
        requestId: String,
    ): UserEntitlements

    /**
     * Get an entitlements sequence
     *
     * @param name Name of the entitlements sequence to return
     *
     * @returns Named entitlements sequence or undefined if no entitlements sequence
     *          of the specified name has been defined.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun getEntitlementsSequence(
        name: String,
    ): EntitlementsSequence?

    /**
     * List all entitlements sequences.
     *
     * Call again with a token parameter to continue paginated listing.
     *
     * @param nextToken Optional token from which to continue listing.
     *
     * @returns Paginated list of entitlements sequences.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun listEntitlementsSequences(
        nextToken: String?,
    ): ListOutput<EntitlementsSequence>

    /**
     * Add a new entitlements sequence.
     *
     * @param name Name of the new entitlements sequence.
     * @param description Description of the new entitlements sequence.
     * @param transitions List of entitlements sequence transitions associated with the new entitlements sequence.
     *
     * @returns The created entitlements sequence
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun addEntitlementsSequence(
        name: String,
        description: String,
        transitions: List<EntitlementsSequenceTransition>,
    ): EntitlementsSequence

    /**
     * Update an entitlements sequence.
     *
     * @param name Name of the entitlements sequence to update.
     * @param description Description of the entitlements sequence to update.
     * @param transitions List of entitlements sequence transitions to update.
     *
     * @returns The updated entitlements sequence.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun setEntitlementsSequence(
        name: String,
        description: String,
        transitions: List<EntitlementsSequenceTransition>,
    ): EntitlementsSequence

    /**
     * Remove entitlements sequence
     *
     * @param name Name of entitlements sequence to remove
     *
     * @returns The entitlements sequence removed or undefined if entitlements sequence was not present
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun removeEntitlementsSequence(
        name: String,
    ): EntitlementsSequence?

    /**
     * Apply entitlements sequence directly to a user.
     *
     * If a record for that user's entitlements sequence does not yet exist it will be created.
     *
     * @param externalId External IDP user ID of user to apply entitlements sequence to.
     * @param entitlementSequenceName Name of the entitlements sequence to apply to the user.
     * @param version If specified, version of any current entitlements that must be matched
     *
     * @returns The effective entitlements for the user.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun applyEntitlementsSequenceToUser(
        externalId: String,
        entitlementSequenceName: String,
        version: Double? = null,
    ): UserEntitlements

    /**
     * Apply entitlements sequences to users.
     *
     * If a record for that user's entitlements does not yet exist it will be created.
     *
     * Equivalent to calling applyEntitlementsSequenceToUser for each operation
     *
     * @param operations Array of ApplyEntitlementsSetOperations to perform
     *
     * @returns Array of results
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun applyEntitlementsSequenceToUsers(
        operations: List<ApplyEntitlementsSequenceOperation>,
    ): List<UserEntitlementsResult>

    /**
     * Remove an entitled user. Entitlements and consumption records related
     * to the specified user will be removed.
     *
     * @param externalId External IDP user ID of user to remove.
     *
     * @returns Information regarding the removed user or null if the user
     *  does not exist.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun removeEntitledUser(externalId: String): EntitledUser?
}

/**
 * Default implementation of [SudoEntitlementsAdminClient] interface.
 *
 * @param context Android app context.
 * @param apiKey API key to use for authentication.
 * @param config Configuration parameters.
 * @param logger logger used for logging messages.
 * @param graphQLClient optional GraphQLClient to use. Mainly used for unit testing.
 */
class DefaultSudoEntitlementsAdminClient(
    private val context: Context,
    apiKey: String,
    config: JSONObject? = null,
    private val logger: Logger = DefaultLogger.instance,
    graphQLClient: GraphQLClient? = null,
) : SudoEntitlementsAdminClient {

    companion object {
        private const val CONFIG_NAMESPACE_ADMIN_CONSOLE_PROJECT_SERVICE =
            "adminConsoleProjectService"

        private const val CONFIG_REGION = "region"
        private const val CONFIG_API_URL = "apiUrl"
    }

    override val version: String = "10.0.0"

    /**
     * Wrapped GraphQLClient used for calling service API.
     */
    private val graphQlClient: GraphQLClient

    init {
        val configManager = DefaultSudoConfigManager(context)

        val adminServiceConfig =
            config?.opt(CONFIG_NAMESPACE_ADMIN_CONSOLE_PROJECT_SERVICE) as JSONObject?
                ?: configManager.getConfigSet(CONFIG_NAMESPACE_ADMIN_CONSOLE_PROJECT_SERVICE)

        require(adminServiceConfig != null) { "Client configuration not found." }

        this.logger.info("Initializing the client using config: $adminServiceConfig")

        val apiUrl = adminServiceConfig[CONFIG_API_URL] as String?
        val region = adminServiceConfig[CONFIG_REGION] as String?

        require(region != null) { "region missing from config." }
        require(apiUrl != null) { "apiUrl missing from config." }

        if (graphQLClient !== null) {
            this.graphQlClient = graphQLClient
        } else {
            val graphqlConfig = JSONObject(
                """
                {
                    'plugins': {
                        'awsAPIPlugin': {
                            '$CONFIG_NAMESPACE_ADMIN_CONSOLE_PROJECT_SERVICE': {
                                'endpointType': 'GraphQL',
                                'endpoint': '$apiUrl',
                                'region': '${Regions.fromName(region)}',
                                'authorizationType': 'API_KEY',
                                'apiKey': "$apiKey"
                            }
                        }
                    }
                } 
                """.trimIndent(),
            )

            val apiCategoryConfiguration = ApiCategoryConfiguration()
            apiCategoryConfiguration.populateFromJSON(graphqlConfig)
            val apiCategory = ApiCategory()
            val awsApiPlugin = AWSApiPlugin
                .builder()
                .configureClient(
                    CONFIG_NAMESPACE_ADMIN_CONSOLE_PROJECT_SERVICE,
                ) { builder -> builder.apply { addInterceptor(ConvertClientErrorsInterceptor()) } }
                .build()

            apiCategory.addPlugin(awsApiPlugin)
            apiCategory.configure(apiCategoryConfiguration, context)

            apiCategory.initialize(context)

            this.graphQlClient = GraphQLClient(apiCategory)
        }
    }

    override suspend fun getEntitlementsSet(name: String): EntitlementsSet? {
        this.logger.info("Retrieving an entitlements set.")

        val input = GetEntitlementsSetInput(name)

        val response = this.graphQlClient.query<GetEntitlementsSetQuery, GetEntitlementsSetQuery.Data>(
            GetEntitlementsSetQuery.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.getEntitlementsSet?.entitlementsSet
        return if (output != null) {
            EntitlementsSet(
                Date(output.createdAtEpochMs.toLong()),
                Date(
                    output
                        .updatedAtEpochMs.toLong(),
                ),
                output.version,
                output.name,
                output.description,
                output.entitlements.map {
                    val entitlement = it.entitlement
                    Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
                },
            )
        } else {
            null
        }
    }

    override suspend fun addEntitlementsSet(
        name: String,
        description: String?,
        entitlements: List<Entitlement>,
    ): EntitlementsSet {
        this.logger.info("Adding an entitlements set.")

        val input =
            AddEntitlementsSetInput(
                name = name,
                description = Optional.presentIfNotNull(description),
                entitlements = entitlements.map {
                    EntitlementInput(name = it.name, description = Optional.presentIfNotNull(it.description), value = it.value.toDouble())
                },
            )

        val response = this.graphQlClient.mutate<AddEntitlementsSetMutation, AddEntitlementsSetMutation.Data>(
            AddEntitlementsSetMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.addEntitlementsSet?.entitlementsSet
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return EntitlementsSet(
            Date(output.createdAtEpochMs.toLong()),
            Date(
                output
                    .updatedAtEpochMs.toLong(),
            ),
            output.version,
            output.name,
            output.description,
            output.entitlements.map {
                val entitlement = it.entitlement
                Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
            },
        )
    }

    override suspend fun listEntitlementsSets(nextToken: String?): ListOutput<EntitlementsSet> {
        this.logger.info("Listing entitlements sets.")

        val response = this.graphQlClient.query<ListEntitlementsSetsQuery, ListEntitlementsSetsQuery.Data>(
            ListEntitlementsSetsQuery.OPERATION_DOCUMENT,
            mapOf("nextToken" to nextToken),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.listEntitlementsSets
            ?: throw SudoEntitlementsAdminException.FailedException("Query completed successfully but result was missing.")

        val items = output.items.map { it ->
            val entitlementsSet = it.entitlementsSet
            EntitlementsSet(
                Date(entitlementsSet.createdAtEpochMs.toLong()),
                Date(
                    entitlementsSet
                        .updatedAtEpochMs.toLong(),
                ),
                entitlementsSet.version,
                entitlementsSet.name,
                entitlementsSet.description,
                entitlementsSet.entitlements.map {
                    val entitlement = it.entitlement
                    Entitlement(
                        entitlement.name,
                        entitlement.description,
                        entitlement.value.toLong(),
                    )
                },
            )
        }

        return ListOutput(items, output.nextToken)
    }

    override suspend fun getEntitlementsForUser(externalId: String): UserEntitlementsConsumption? {
        this.logger.info("Retrieving user entitlements.")

        val input = GetEntitlementsForUserInput(externalId)
        val response = this.graphQlClient.query<GetEntitlementsForUserQuery, GetEntitlementsForUserQuery.Data>(
            GetEntitlementsForUserQuery.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.getEntitlementsForUser?.externalEntitlementsConsumption
            ?: return null
        val entitlements = output.entitlements.externalUserEntitlements
        return UserEntitlementsConsumption(
            UserEntitlements(
                Date(entitlements.createdAtEpochMs.toLong()),
                Date(
                    entitlements
                        .updatedAtEpochMs.toLong(),
                ),
                entitlements.version,
                entitlements.externalId,
                entitlements.owner,
                entitlements.entitlementsSetName,
                entitlements.entitlementsSequenceName,
                entitlements.entitlements.map {
                    val entitlement = it.entitlement
                    Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
                },
                entitlements.expendableEntitlements.map {
                    val entitlement = it.entitlement
                    Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
                },
                entitlements
                    .transitionsRelativeToEpochMs?.let {
                        Date(
                            it.toLong(),
                        )
                    },
                if (entitlements
                        .accountState == AccountStates.ACTIVE
                ) {
                    AccountState.ACTIVE
                } else {
                    AccountState.LOCKED
                },
            ),
            output.consumption.map {
                val consumption = it.entitlementConsumption
                EntitlementConsumption(
                    consumption.name,
                    consumption.value.toLong(),
                    consumption.available.toLong(),
                    consumption.consumed.toLong(),
                    consumption.firstConsumedAtEpochMs?.let { at ->
                        Date(
                            at.toLong(),
                        )
                    },
                    consumption.lastConsumedAtEpochMs?.let { at ->
                        Date(
                            at.toLong(),
                        )
                    },
                )
            },
        )
    }

    override suspend fun setEntitlementsSet(
        name: String,
        description: String?,
        entitlements: List<Entitlement>,
    ): EntitlementsSet {
        this.logger.info("Updating an entitlements set.")

        val input = SetEntitlementsSetInput(
            name = name,
            description = Optional.presentIfNotNull(description),
            entitlements =
            entitlements.map {
                EntitlementInput(name = it.name, description = Optional.presentIfNotNull(it.description), value = it.value.toDouble())
            },
        )
        val response = this.graphQlClient.mutate<SetEntitlementsSetMutation, SetEntitlementsSetMutation.Data>(
            SetEntitlementsSetMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.setEntitlementsSet?.entitlementsSet
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return EntitlementsSet(
            Date(output.createdAtEpochMs.toLong()),
            Date(
                output
                    .updatedAtEpochMs.toLong(),
            ),
            output.version,
            output.name,
            output.description,
            output.entitlements.map {
                val entitlement = it.entitlement
                Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
            },
        )
    }

    override suspend fun removeEntitlementsSet(name: String): EntitlementsSet? {
        this.logger.info("Removing an entitlements set.")

        val input = RemoveEntitlementsSetInput(name)
        val response = this.graphQlClient.mutate<RemoveEntitlementsSetMutation, RemoveEntitlementsSetMutation.Data>(
            RemoveEntitlementsSetMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.removeEntitlementsSet?.entitlementsSet
        return if (output != null) {
            EntitlementsSet(
                Date(output.createdAtEpochMs.toLong()),
                Date(
                    output
                        .updatedAtEpochMs.toLong(),
                ),
                output.version,
                output.name,
                output.description,
                output.entitlements.map {
                    val entitlement = it.entitlement
                    Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
                },
            )
        } else {
            null
        }
    }

    override suspend fun applyEntitlementsToUser(
        externalId: String,
        entitlements: List<Entitlement>,
        version: Double?,
    ): UserEntitlements {
        this.logger.info("Applying entitlements to a user.")

        val input =
            ApplyEntitlementsToUserInput(
                externalId = externalId,
                entitlements =
                entitlements.map {
                    EntitlementInput(name = it.name, description = Optional.presentIfNotNull(it.description), value = it.value.toDouble())
                },
                version = Optional.presentIfNotNull(version),
            )
        val response = this.graphQlClient.mutate<ApplyEntitlementsToUserMutation, ApplyEntitlementsToUserMutation.Data>(
            ApplyEntitlementsToUserMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.applyEntitlementsToUser?.externalUserEntitlements
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return UserEntitlements(
            Date(output.createdAtEpochMs.toLong()),
            Date(
                output
                    .updatedAtEpochMs.toLong(),
            ),
            output.version,
            output.externalId,
            output.owner,
            output.entitlementsSetName,
            output.entitlementsSequenceName,
            output.entitlements.map {
                val entitlement = it.entitlement
                Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
            },
            output.expendableEntitlements.map {
                val entitlement = it.entitlement
                Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
            },
            output
                .transitionsRelativeToEpochMs?.let {
                    Date(
                        it.toLong(),
                    )
                },
            if (output.accountState == AccountStates.ACTIVE) AccountState.ACTIVE else AccountState.LOCKED,
        )
    }

    override suspend fun applyEntitlementsToUsers(operations: List<ApplyEntitlementsOperation>): List<UserEntitlementsResult> {
        this.logger.info("Applying entitlements to users.")

        val input =
            ApplyEntitlementsToUsersInput(
                operations.map { it ->
                    ApplyEntitlementsToUserInput(
                        externalId = it.externalId,
                        entitlements =
                        it.entitlements.map {
                            EntitlementInput(name = it.name, description = Optional.presentIfNotNull(it.description), value = it.value.toDouble())
                        },
                        version = Optional.presentIfNotNull(it.version),
                    )
                },
            )

        val response = this.graphQlClient.mutate<
            ApplyEntitlementsToUsersMutation,
            ApplyEntitlementsToUsersMutation.Data,
            >(
            ApplyEntitlementsToUsersMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.applyEntitlementsToUsers
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return output.map { it ->
            val userEntitlements = it.onExternalUserEntitlements?.externalUserEntitlements
            val error = it.onExternalUserEntitlementsError?.externalUserEntitlementsError
            if (userEntitlements != null) {
                UserEntitlementsResult.Success(
                    value = UserEntitlements(
                        createdAt = Date(userEntitlements.createdAtEpochMs.toLong()),
                        updatedAt = Date(userEntitlements.updatedAtEpochMs.toLong()),
                        version = userEntitlements.version,
                        externalId = userEntitlements.externalId,
                        owner = userEntitlements.owner,
                        entitlementsSequenceName = userEntitlements.entitlementsSequenceName,
                        entitlementsSetName = userEntitlements.entitlementsSetName,
                        entitlements = userEntitlements.entitlements.map {
                            val entitlement = it.entitlement
                            Entitlement(name = entitlement.name, description = entitlement.description, value = entitlement.value.toLong())
                        },
                        expendableEntitlements = userEntitlements.expendableEntitlements.map {
                            val entitlement = it.entitlement
                            Entitlement(name = entitlement.name, description = entitlement.description, value = entitlement.value.toLong())
                        },
                        transitionsRelativeTo = userEntitlements.transitionsRelativeToEpochMs?.let {
                            Date(it.toLong())
                        },
                        accountState = if (userEntitlements.accountState == AccountStates.ACTIVE) AccountState.ACTIVE else AccountState.LOCKED,
                    ),
                )
            } else if (error != null) {
                UserEntitlementsResult.Failure(
                    error = SudoEntitlementsAdminException.sudoEntitlementsAdminException(
                        error.error,
                    ),
                )
            } else {
                UserEntitlementsResult.Failure(SudoEntitlementsAdminException.FailedException("Unknown result type ${it.__typename}"))
            }
        }
    }

    override suspend fun applyEntitlementsSetToUser(
        externalId: String,
        entitlementSetName: String,
        version: Double?,
    ): UserEntitlements {
        this.logger.info("Applying an entitlements set to a user.")

        val input =
            ApplyEntitlementsSetToUserInput(
                externalId = externalId,
                entitlementsSetName = entitlementSetName,
                version = Optional.presentIfNotNull(version),
            )

        val response = this.graphQlClient.mutate<ApplyEntitlementsSetToUserMutation, ApplyEntitlementsSetToUserMutation.Data>(
            ApplyEntitlementsSetToUserMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.applyEntitlementsSetToUser?.externalUserEntitlements
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return UserEntitlements(
            Date(output.createdAtEpochMs.toLong()),
            Date(
                output
                    .updatedAtEpochMs.toLong(),
            ),
            output.version,
            output.externalId,
            output.owner,
            output.entitlementsSetName,
            output.entitlementsSequenceName,
            output.entitlements.map {
                val entitlement = it.entitlement
                Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
            },
            output.expendableEntitlements.map {
                val entitlement = it.entitlement
                Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
            },
            output
                .transitionsRelativeToEpochMs?.let {
                    Date(
                        it.toLong(),
                    )
                },
            if (output.accountState == AccountStates.ACTIVE) AccountState.ACTIVE else AccountState.LOCKED,
        )
    }

    override suspend fun applyEntitlementsSetToUsers(operations: List<ApplyEntitlementsSetOperation>): List<UserEntitlementsResult> {
        this.logger.info("Applying entitlements sets to users.")

        val input =
            ApplyEntitlementsSetToUsersInput(
                operations.map {
                    ApplyEntitlementsSetToUserInput(
                        externalId = it.externalId,
                        entitlementsSetName = it.entitlementsSetName,
                        version = Optional.presentIfNotNull(it.version),
                    )
                },
            )

        val response = this.graphQlClient.mutate<ApplyEntitlementsSetToUsersMutation, ApplyEntitlementsSetToUsersMutation.Data>(
            ApplyEntitlementsSetToUsersMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.applyEntitlementsSetToUsers
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return output.map { it ->
            val userEntitlements = it.onExternalUserEntitlements?.externalUserEntitlements
            val error = it.onExternalUserEntitlementsError?.externalUserEntitlementsError
            if (userEntitlements != null) {
                UserEntitlementsResult.Success(
                    value = UserEntitlements(
                        createdAt = Date(userEntitlements.createdAtEpochMs.toLong()),
                        updatedAt = Date(userEntitlements.updatedAtEpochMs.toLong()),
                        version = userEntitlements.version,
                        externalId = userEntitlements.externalId,
                        owner = userEntitlements.owner,
                        entitlementsSequenceName = userEntitlements.entitlementsSequenceName,
                        entitlementsSetName = userEntitlements.entitlementsSetName,
                        entitlements = userEntitlements.entitlements.map {
                            val entitlement = it.entitlement
                            Entitlement(name = entitlement.name, description = entitlement.description, value = entitlement.value.toLong())
                        },
                        expendableEntitlements = userEntitlements.expendableEntitlements.map {
                            val entitlement = it.entitlement
                            Entitlement(name = entitlement.name, description = entitlement.description, value = entitlement.value.toLong())
                        },
                        transitionsRelativeTo = userEntitlements.transitionsRelativeToEpochMs?.let {
                            Date(it.toLong())
                        },
                        accountState = if (userEntitlements.accountState == AccountStates.ACTIVE) AccountState.ACTIVE else AccountState.LOCKED,
                    ),
                )
            } else if (error != null) {
                UserEntitlementsResult.Failure(
                    error = SudoEntitlementsAdminException.sudoEntitlementsAdminException(
                        error.error,
                    ),
                )
            } else {
                UserEntitlementsResult.Failure(SudoEntitlementsAdminException.FailedException("Unknown result type ${it.__typename}"))
            }
        }
    }

    override suspend fun applyExpendableEntitlementsToUser(
        externalId: String,
        expendableEntitlements: List<Entitlement>,
        requestId: String,
    ): UserEntitlements {
        this.logger.info("Applying expendable entitlements to a user.")

        val input =
            ApplyExpendableEntitlementsToUserInput(
                externalId = externalId,
                expendableEntitlements =
                expendableEntitlements.map {
                    EntitlementInput(name = it.name, description = Optional.presentIfNotNull(it.description), value = it.value.toDouble())
                },
                requestId = requestId,
            )
        val response = this.graphQlClient.mutate<ApplyExpendableEntitlementsToUserMutation, ApplyExpendableEntitlementsToUserMutation.Data>(
            ApplyExpendableEntitlementsToUserMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.applyExpendableEntitlementsToUser?.externalUserEntitlements
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return UserEntitlements(
            Date(output.createdAtEpochMs.toLong()),
            Date(
                output
                    .updatedAtEpochMs.toLong(),
            ),
            output.version,
            output.externalId,
            output.owner,
            output.entitlementsSetName,
            output.entitlementsSequenceName,
            output.entitlements.map {
                val entitlement = it.entitlement
                Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
            },
            output.expendableEntitlements.map {
                val entitlement = it.entitlement
                Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
            },
            output
                .transitionsRelativeToEpochMs?.let {
                    Date(
                        it.toLong(),
                    )
                },
            if (output.accountState == AccountStates.ACTIVE) AccountState.ACTIVE else AccountState.LOCKED,
        )
    }

    override suspend fun getEntitlementsSequence(name: String): EntitlementsSequence? {
        this.logger.info("Retrieving an entitlements sequence.")

        val input = GetEntitlementsSequenceInput(name)
        val response = this.graphQlClient.query<GetEntitlementsSequenceQuery, GetEntitlementsSequenceQuery.Data>(
            GetEntitlementsSequenceQuery.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.getEntitlementsSequence?.entitlementsSequence
        return if (output != null) {
            EntitlementsSequence(
                Date(output.createdAtEpochMs.toLong()),
                Date(
                    output
                        .updatedAtEpochMs.toLong(),
                ),
                output.version,
                output.name,
                output.description,
                output.transitions.map {
                    val transition = it.entitlementsSequenceTransition
                    EntitlementsSequenceTransition(transition.entitlementsSetName, transition.duration)
                },
            )
        } else {
            null
        }
    }

    override suspend fun listEntitlementsSequences(nextToken: String?): ListOutput<EntitlementsSequence> {
        this.logger.info("Listing entitlements sequences.")

        val response = this.graphQlClient.query<ListEntitlementsSequencesQuery, ListEntitlementsSequencesQuery.Data>(
            ListEntitlementsSequencesQuery.OPERATION_DOCUMENT,
            mapOf("nextToken" to nextToken),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.listEntitlementsSequences?.entitlementsSequencesConnection
            ?: throw SudoEntitlementsAdminException.FailedException("Query completed successfully but result was missing.")

        val items = output.items.map { it ->
            val sequence = it.entitlementsSequence
            EntitlementsSequence(
                Date(sequence.createdAtEpochMs.toLong()),
                Date(
                    sequence
                        .updatedAtEpochMs.toLong(),
                ),
                sequence.version,
                sequence.name,
                sequence.description,
                sequence.transitions.map {
                    val transition = it.entitlementsSequenceTransition
                    EntitlementsSequenceTransition(transition.entitlementsSetName, transition.duration)
                },
            )
        }

        return ListOutput(items, output.nextToken)
    }

    override suspend fun addEntitlementsSequence(
        name: String,
        description: String,
        transitions: List<EntitlementsSequenceTransition>,
    ): EntitlementsSequence {
        this.logger.info("Adding an entitlements sequence.")

        val input =
            AddEntitlementsSequenceInput(
                name = name,
                description = Optional.presentIfNotNull(description),
                transitions =
                transitions.map {
                    EntitlementsSequenceTransitionInput(entitlementsSetName = it.entitlementsSetName, duration = Optional.presentIfNotNull(it.duration))
                },
            )

        val response = this.graphQlClient.mutate<AddEntitlementsSequenceMutation, AddEntitlementsSequenceMutation.Data>(
            AddEntitlementsSequenceMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.addEntitlementsSequence?.entitlementsSequence
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return EntitlementsSequence(
            Date(output.createdAtEpochMs.toLong()),
            Date(
                output
                    .updatedAtEpochMs.toLong(),
            ),
            output.version,
            output.name,
            output.description,
            output.transitions.map {
                val transition = it.entitlementsSequenceTransition
                EntitlementsSequenceTransition(transition.entitlementsSetName, transition.duration)
            },
        )
    }

    override suspend fun setEntitlementsSequence(
        name: String,
        description: String,
        transitions: List<EntitlementsSequenceTransition>,
    ): EntitlementsSequence {
        this.logger.info("Updating an entitlements sequence.")

        val input =
            SetEntitlementsSequenceInput(
                name = name,
                description = Optional.presentIfNotNull(description),
                transitions =
                transitions.map {
                    EntitlementsSequenceTransitionInput(
                        entitlementsSetName = it.entitlementsSetName,
                        duration = Optional.presentIfNotNull(it.duration),
                    )
                },
            )

        val response = this.graphQlClient.mutate<SetEntitlementsSequenceMutation, SetEntitlementsSequenceMutation.Data>(
            SetEntitlementsSequenceMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.setEntitlementsSequence?.entitlementsSequence
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return EntitlementsSequence(
            Date(output.createdAtEpochMs.toLong()),
            Date(
                output
                    .updatedAtEpochMs.toLong(),
            ),
            output.version,
            output.name,
            output.description,
            output.transitions.map {
                val transition = it.entitlementsSequenceTransition
                EntitlementsSequenceTransition(transition.entitlementsSetName, transition.duration)
            },
        )
    }

    override suspend fun removeEntitlementsSequence(name: String): EntitlementsSequence? {
        this.logger.info("Removing an entitlements sequence.")

        val input = RemoveEntitlementsSequenceInput(name)
        val response = this.graphQlClient.mutate<RemoveEntitlementsSequenceMutation, RemoveEntitlementsSequenceMutation.Data>(
            RemoveEntitlementsSequenceMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.removeEntitlementsSequence?.entitlementsSequence
        return if (output != null) {
            EntitlementsSequence(
                Date(output.createdAtEpochMs.toLong()),
                Date(
                    output
                        .updatedAtEpochMs.toLong(),
                ),
                output.version,
                output.name,
                output.description,
                output.transitions.map {
                    val transition = it.entitlementsSequenceTransition
                    EntitlementsSequenceTransition(transition.entitlementsSetName, transition.duration)
                },
            )
        } else {
            null
        }
    }

    override suspend fun applyEntitlementsSequenceToUser(
        externalId: String,
        entitlementSequenceName: String,
        version: Double?,
    ): UserEntitlements {
        this.logger.info("Applying an entitlements sequence.")

        val input =
            ApplyEntitlementsSequenceToUserInput(
                externalId = externalId,
                entitlementsSequenceName = entitlementSequenceName,
                version = Optional.presentIfNotNull(version),
            )
        val response = this.graphQlClient.mutate<ApplyEntitlementsSequenceToUserMutation, ApplyEntitlementsSequenceToUserMutation.Data>(
            ApplyEntitlementsSequenceToUserMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.applyEntitlementsSequenceToUser?.externalUserEntitlements
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return UserEntitlements(
            Date(output.createdAtEpochMs.toLong()),
            Date(
                output
                    .updatedAtEpochMs.toLong(),
            ),
            output.version,
            output.externalId,
            output.owner,
            output.entitlementsSetName,
            output.entitlementsSequenceName,
            output.entitlements.map {
                val entitlement = it.entitlement
                Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
            },
            output.expendableEntitlements.map {
                val entitlement = it.entitlement
                Entitlement(entitlement.name, entitlement.description, entitlement.value.toLong())
            },
            output
                .transitionsRelativeToEpochMs?.let {
                    Date(
                        it.toLong(),
                    )
                },
            if (output.accountState == AccountStates.ACTIVE) AccountState.ACTIVE else AccountState.LOCKED,
        )
    }

    override suspend fun applyEntitlementsSequenceToUsers(operations: List<ApplyEntitlementsSequenceOperation>): List<UserEntitlementsResult> {
        this.logger.info("Applying entitlements sequences to users.")

        val input =
            ApplyEntitlementsSequenceToUsersInput(
                operations.map {
                    ApplyEntitlementsSequenceToUserInput(
                        externalId = it.externalId,
                        entitlementsSequenceName = it.entitlementsSequenceName,
                        version = Optional.presentIfNotNull(it.version),
                    )
                },
            )

        val response = this.graphQlClient.mutate<ApplyEntitlementsSequenceToUsersMutation, ApplyEntitlementsSequenceToUsersMutation.Data>(
            ApplyEntitlementsSequenceToUsersMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.applyEntitlementsSequenceToUsers
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return output.map { it ->
            val userEntitlements = it.onExternalUserEntitlements?.externalUserEntitlements
            val error = it.onExternalUserEntitlementsError?.externalUserEntitlementsError
            if (userEntitlements != null) {
                UserEntitlementsResult.Success(
                    value = UserEntitlements(
                        createdAt = Date(userEntitlements.createdAtEpochMs.toLong()),
                        updatedAt = Date(userEntitlements.updatedAtEpochMs.toLong()),
                        version = userEntitlements.version,
                        externalId = userEntitlements.externalId,
                        owner = userEntitlements.owner,
                        entitlementsSequenceName = userEntitlements.entitlementsSequenceName,
                        entitlementsSetName = userEntitlements.entitlementsSetName,
                        entitlements = userEntitlements.entitlements.map {
                            val entitlement = it.entitlement
                            Entitlement(name = entitlement.name, description = entitlement.description, value = entitlement.value.toLong())
                        },
                        expendableEntitlements = userEntitlements.expendableEntitlements.map {
                            val entitlement = it.entitlement
                            Entitlement(name = entitlement.name, description = entitlement.description, value = entitlement.value.toLong())
                        },
                        transitionsRelativeTo = userEntitlements.transitionsRelativeToEpochMs?.let {
                            Date(it.toLong())
                        },
                        accountState = if (userEntitlements.accountState == AccountStates.ACTIVE) AccountState.ACTIVE else AccountState.LOCKED,
                    ),
                )
            } else if (error != null) {
                UserEntitlementsResult.Failure(
                    error = SudoEntitlementsAdminException.sudoEntitlementsAdminException(
                        error.error,
                    ),
                )
            } else {
                UserEntitlementsResult.Failure(SudoEntitlementsAdminException.FailedException("Unknown result type ${it.__typename}"))
            }
        }
    }

    override suspend fun removeEntitledUser(externalId: String): EntitledUser? {
        this.logger.info("Removing an entitled user.")

        val input =
            RemoveEntitledUserInput(externalId)
        val response = this.graphQlClient.mutate<RemoveEntitledUserMutation, RemoveEntitledUserMutation.Data>(
            RemoveEntitledUserMutation.OPERATION_DOCUMENT,
            mapOf("input" to input),
        )

        if (response.hasErrors()) {
            throw response.errors.first().toSudoEntitlementsAdminException()
        }

        val output = response.data?.removeEntitledUser
        return output?.let { EntitledUser(it.externalId) }
    }
}
