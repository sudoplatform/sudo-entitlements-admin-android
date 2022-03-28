/**
 * Copyright © 2020 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin

import android.content.Context
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers
import com.amazonaws.regions.Regions
import com.sudoplatform.sudoconfigmanager.DefaultSudoConfigManager
import com.sudoplatform.sudoentitlementsadmin.SudoEntitlementsAdminException.Companion.toSudoEntitlementsAdminException
import com.sudoplatform.sudoentitlementsadmin.appsync.enqueue
import com.sudoplatform.sudoentitlementsadmin.type.AccountStates
import com.sudoplatform.sudoentitlementsadmin.type.AddEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.type.AddEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.type.ApplyEntitlementsSequenceToUserInput
import com.sudoplatform.sudoentitlementsadmin.type.ApplyEntitlementsSetToUserInput
import com.sudoplatform.sudoentitlementsadmin.type.ApplyEntitlementsToUserInput
import com.sudoplatform.sudoentitlementsadmin.type.EntitlementInput
import com.sudoplatform.sudoentitlementsadmin.type.EntitlementsSequenceTransitionInput
import com.sudoplatform.sudoentitlementsadmin.type.GetEntitlementsForUserInput
import com.sudoplatform.sudoentitlementsadmin.type.GetEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.type.GetEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.type.RemoveEntitledUserInput
import com.sudoplatform.sudoentitlementsadmin.type.RemoveEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.type.RemoveEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.type.SetEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.type.SetEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.types.AccountState
import com.sudoplatform.sudoentitlementsadmin.types.EntitledUser
import com.sudoplatform.sudoentitlementsadmin.types.Entitlement
import com.sudoplatform.sudoentitlementsadmin.types.EntitlementConsumption
import com.sudoplatform.sudoentitlementsadmin.types.EntitlementsSequence
import com.sudoplatform.sudoentitlementsadmin.types.EntitlementsSequenceTransition
import com.sudoplatform.sudoentitlementsadmin.types.EntitlementsSet
import com.sudoplatform.sudoentitlementsadmin.types.ListOutput
import com.sudoplatform.sudoentitlementsadmin.types.UserEntitlements
import com.sudoplatform.sudoentitlementsadmin.types.UserEntitlementsConsumption
import com.sudoplatform.sudologging.Logger
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
        private var graphQLClient: AWSAppSyncClient? = null
        private var config: JSONObject? = null
        private var logger: Logger = DefaultLogger.instance

        /**
         * Provide an [AWSAppSyncClient] for the [SudoEntitlementsAdminClient]. This is mainly
         * used for unit testing.
         */
        fun setGraphQLClient(graphQLClient: AWSAppSyncClient) = also {
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
                this.graphQLClient
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
        entitlements: List<Entitlement>
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
        entitlements: List<Entitlement>
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
     *
     * @returns The effective entitlements for the user
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun applyEntitlementsToUser(
        externalId: String,
        entitlements: List<Entitlement>,
    ): UserEntitlements

    /**
     * Apply entitlements directly to a user.
     *
     * If a record for that user's entitlements does not yet exist it will be created.
     *
     * @param externalId External IDP user ID of user to retrieve entitlements for.
     * @param entitlementSetName Name of the entitlements set to apply to the user.
     *
     * @returns The effective entitlements for the user.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun applyEntitlementsSetToUser(
        externalId: String,
        entitlementSetName: String
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
     *
     * @returns The effective entitlements for the user.
     */
    @Throws(SudoEntitlementsAdminException::class)
    suspend fun applyEntitlementsSequenceToUser(
        externalId: String,
        entitlementSequenceName: String,
    ): UserEntitlements

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
 * @param graphQLClient optional GraphQL client to use. Mainly used for unit testing.
 */
class DefaultSudoEntitlementsAdminClient(
    private val context: Context,
    apiKey: String,
    config: JSONObject? = null,
    private val logger: Logger = DefaultLogger.instance,
    graphQLClient: AWSAppSyncClient? = null
) : SudoEntitlementsAdminClient {

    companion object {
        private const val CONFIG_NAMESPACE_ADMIN_CONSOLE_PROJECT_SERVICE =
            "adminConsoleProjectService"

        private const val CONFIG_REGION = "region"
        private const val CONFIG_API_URL = "apiUrl"
    }

    override val version: String = "2.0.1"

    /**
     * GraphQL client used for calling Sudo service API.
     */
    private val graphQLClient: AWSAppSyncClient

    init {
        val configManager = DefaultSudoConfigManager(context)

        @Suppress("UNCHECKED_CAST")
        val adminServiceConfig =
            config?.opt(CONFIG_NAMESPACE_ADMIN_CONSOLE_PROJECT_SERVICE) as JSONObject?
                ?: configManager.getConfigSet(CONFIG_NAMESPACE_ADMIN_CONSOLE_PROJECT_SERVICE)

        require(adminServiceConfig != null) { "Client configuration not found." }

        this.logger.info("Initializing the client using config: $adminServiceConfig")

        val apiUrl = adminServiceConfig[CONFIG_API_URL] as String?
        val region = adminServiceConfig[CONFIG_REGION] as String?

        require(region != null) { "region missing from config." }
        require(apiUrl != null) { "apiUrl missing from config." }

        this.graphQLClient = graphQLClient ?: AWSAppSyncClient.builder()
            .serverUrl(apiUrl)
            .region(Regions.fromName(region))
            .apiKey {
                apiKey
            }
            .context(this.context)
            .build()
    }

    override suspend fun getEntitlementsSet(name: String): EntitlementsSet? {
        this.logger.info("Retrieving an entitlements set.")

        val input = GetEntitlementsSetInput.builder().name(name).build()
        val response =
            this.graphQLClient.query(GetEntitlementsSetQuery.builder().input(input).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.getEntitlementsSet
        return if (output != null) EntitlementsSet(
            Date(output.createdAtEpochMs().toLong()),
            Date(
                output
                    .updatedAtEpochMs().toLong()
            ),
            output.version(),
            output.name(),
            output.description(),
            output.entitlements.map {
                Entitlement(it.name(), it.description(), it.value())
            }
        ) else null
    }

    override suspend fun addEntitlementsSet(
        name: String,
        description: String?,
        entitlements: List<Entitlement>
    ): EntitlementsSet {
        this.logger.info("Adding an entitlements set.")

        val input =
            AddEntitlementsSetInput.builder().name(name).description(description).entitlements(
                entitlements.map {
                    EntitlementInput.builder().name(it.name).description(it.description)
                        .value(it.value).build()
                }
            ).build()

        val response =
            this.graphQLClient.mutate(AddEntitlementsSetMutation.builder().input(input).build())
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.addEntitlementsSet
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return EntitlementsSet(
            Date(output.createdAtEpochMs().toLong()),
            Date(
                output
                    .updatedAtEpochMs().toLong()
            ),
            output.version(),
            output.name(),
            output.description(),
            output.entitlements.map {
                Entitlement(it.name(), it.description(), it.value())
            }
        )
    }

    override suspend fun listEntitlementsSets(nextToken: String?): ListOutput<EntitlementsSet> {
        this.logger.info("Listing entitlements sets.")

        val response =
            this.graphQLClient.query(
                ListEntitlementsSetsQuery.builder().nextToken(nextToken).build()
            )
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.listEntitlementsSets
            ?: throw SudoEntitlementsAdminException.FailedException("Query completed successfully but result was missing.")

        val items = output.items.map { item ->
            EntitlementsSet(
                Date(item.createdAtEpochMs().toLong()),
                Date(
                    item
                        .updatedAtEpochMs().toLong()
                ),
                item.version(),
                item.name(),
                item.description(),
                item.entitlements.map { entitlement ->
                    Entitlement(
                        entitlement.name(),
                        entitlement.description(),
                        entitlement.value()
                    )
                }
            )
        }

        return ListOutput(items, output.nextToken())
    }

    override suspend fun getEntitlementsForUser(externalId: String): UserEntitlementsConsumption? {
        this.logger.info("Retrieving user entitlements.")

        val input = GetEntitlementsForUserInput.builder().externalId(externalId).build()
        val response =
            this.graphQLClient.query(GetEntitlementsForUserQuery.builder().input(input).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.getEntitlementsForUser
        return if (output != null) UserEntitlementsConsumption(
            UserEntitlements(
                Date(output.entitlements().createdAtEpochMs().toLong()),
                Date(
                    output.entitlements()
                        .updatedAtEpochMs().toLong()
                ),
                output.entitlements().version(),
                output.entitlements().externalId(),
                output.entitlements().owner(),
                output.entitlements().entitlementsSetName(),
                output.entitlements().entitlementsSequenceName(),
                output.entitlements().entitlements.map {
                    Entitlement(it.name(), it.description(), it.value())
                },
                output.entitlements()
                    .transitionsRelativeToEpochMs()?.let {
                        Date(
                            it.toLong()
                        )
                    },
                if (output.entitlements()
                    .accountState() == AccountStates.ACTIVE
                ) AccountState.ACTIVE else AccountState.LOCKED
            ),
            output.consumption.map {
                EntitlementConsumption(
                    it.name(), it.value(), it.available(), it.consumed(),
                    it.firstConsumedAtEpochMs()?.let { at ->
                        Date(
                            at.toLong()
                        )
                    },
                    it.lastConsumedAtEpochMs()?.let { at ->
                        Date(
                            at.toLong()
                        )
                    }
                )
            }
        ) else null
    }

    override suspend fun setEntitlementsSet(
        name: String,
        description: String?,
        entitlements: List<Entitlement>
    ): EntitlementsSet {
        this.logger.info("Updating an entitlements set.")

        val input =
            SetEntitlementsSetInput.builder().name(name).description(description).entitlements(
                entitlements.map {
                    EntitlementInput.builder().name(it.name).description(it.description)
                        .value(it.value).build()
                }
            ).build()

        val response =
            this.graphQLClient.mutate(SetEntitlementsSetMutation.builder().input(input).build())
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.setEntitlementsSet
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return EntitlementsSet(
            Date(output.createdAtEpochMs().toLong()),
            Date(
                output
                    .updatedAtEpochMs().toLong()
            ),
            output.version(),
            output.name(),
            output.description(),
            output.entitlements.map {
                Entitlement(it.name(), it.description(), it.value())
            }
        )
    }

    override suspend fun removeEntitlementsSet(name: String): EntitlementsSet? {
        this.logger.info("Removing an entitlements set.")

        val input = RemoveEntitlementsSetInput.builder().name(name).build()
        val response =
            this.graphQLClient.mutate(RemoveEntitlementsSetMutation.builder().input(input).build())
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.removeEntitlementsSet
        return if (output != null) EntitlementsSet(
            Date(output.createdAtEpochMs().toLong()),
            Date(
                output
                    .updatedAtEpochMs().toLong()
            ),
            output.version(),
            output.name(),
            output.description(),
            output.entitlements.map {
                Entitlement(it.name(), it.description(), it.value())
            }
        ) else null
    }

    override suspend fun applyEntitlementsToUser(
        externalId: String,
        entitlements: List<Entitlement>
    ): UserEntitlements {
        this.logger.info("Applying entitlements to a user.")

        val input =
            ApplyEntitlementsToUserInput.builder().externalId(externalId).entitlements(
                entitlements.map {
                    EntitlementInput.builder().name(it.name).description(it.description)
                        .value(it.value).build()
                }
            ).build()

        val response =
            this.graphQLClient.mutate(
                ApplyEntitlementsToUserMutation.builder().input(input).build()
            )
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.applyEntitlementsToUser
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return UserEntitlements(
            Date(output.createdAtEpochMs().toLong()),
            Date(
                output
                    .updatedAtEpochMs().toLong()
            ),
            output.version(),
            output.externalId(),
            output.owner(),
            output.entitlementsSetName(),
            output.entitlementsSequenceName(),
            output.entitlements.map {
                Entitlement(it.name(), it.description(), it.value())
            },
            output
                .transitionsRelativeToEpochMs()?.let {
                    Date(
                        it.toLong()
                    )
                },
            if (output.accountState() == AccountStates.ACTIVE) AccountState.ACTIVE else AccountState.LOCKED
        )
    }

    override suspend fun applyEntitlementsSetToUser(
        externalId: String,
        entitlementSetName: String
    ): UserEntitlements {
        this.logger.info("Applying an entitlements set to a user.")

        val input =
            ApplyEntitlementsSetToUserInput.builder().externalId(externalId)
                .entitlementsSetName(entitlementSetName).build()

        val response =
            this.graphQLClient.mutate(
                ApplyEntitlementsSetToUserMutation.builder().input(input).build()
            )
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.applyEntitlementsSetToUser
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return UserEntitlements(
            Date(output.createdAtEpochMs().toLong()),
            Date(
                output
                    .updatedAtEpochMs().toLong()
            ),
            output.version(),
            output.externalId(),
            output.owner(),
            output.entitlementsSetName(),
            output.entitlementsSequenceName(),
            output.entitlements.map {
                Entitlement(it.name(), it.description(), it.value())
            },
            output
                .transitionsRelativeToEpochMs()?.let {
                    Date(
                        it.toLong()
                    )
                },
            if (output.accountState() == AccountStates.ACTIVE) AccountState.ACTIVE else AccountState.LOCKED
        )
    }

    override suspend fun getEntitlementsSequence(name: String): EntitlementsSequence? {
        this.logger.info("Retrieving an entitlements sequence.")

        val input = GetEntitlementsSequenceInput.builder().name(name).build()
        val response =
            this.graphQLClient.query(GetEntitlementsSequenceQuery.builder().input(input).build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.getEntitlementsSequence
        return if (output != null) EntitlementsSequence(
            Date(output.createdAtEpochMs().toLong()),
            Date(
                output
                    .updatedAtEpochMs().toLong()
            ),
            output.version(),
            output.name(),
            output.description(),
            output.transitions.map {
                EntitlementsSequenceTransition(it.entitlementsSetName(), it.duration())
            }
        ) else null
    }

    override suspend fun listEntitlementsSequences(nextToken: String?): ListOutput<EntitlementsSequence> {
        this.logger.info("Listing entitlements sequences.")

        val response =
            this.graphQLClient.query(
                ListEntitlementsSequencesQuery.builder().nextToken(nextToken).build()
            )
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.listEntitlementsSequences
            ?: throw SudoEntitlementsAdminException.FailedException("Query completed successfully but result was missing.")

        val items = output.items.map { item ->
            EntitlementsSequence(
                Date(item.createdAtEpochMs().toLong()),
                Date(
                    item
                        .updatedAtEpochMs().toLong()
                ),
                item.version(),
                item.name(),
                item.description(),
                item.transitions.map {
                    EntitlementsSequenceTransition(it.entitlementsSetName(), it.duration())
                }
            )
        }

        return ListOutput(items, output.nextToken())
    }

    override suspend fun addEntitlementsSequence(
        name: String,
        description: String,
        transitions: List<EntitlementsSequenceTransition>
    ): EntitlementsSequence {
        this.logger.info("Adding an entitlements sequence.")

        val input =
            AddEntitlementsSequenceInput.builder().name(name).description(description).transitions(
                transitions.map {
                    EntitlementsSequenceTransitionInput.builder()
                        .entitlementsSetName(it.entitlementsSetName).duration(it.duration).build()
                }
            ).build()

        val response =
            this.graphQLClient.mutate(
                AddEntitlementsSequenceMutation.builder().input(input).build()
            )
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.addEntitlementsSequence
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return EntitlementsSequence(
            Date(output.createdAtEpochMs().toLong()),
            Date(
                output
                    .updatedAtEpochMs().toLong()
            ),
            output.version(),
            output.name(),
            output.description(),
            output.transitions.map {
                EntitlementsSequenceTransition(it.entitlementsSetName(), it.duration())
            }
        )
    }

    override suspend fun setEntitlementsSequence(
        name: String,
        description: String,
        transitions: List<EntitlementsSequenceTransition>
    ): EntitlementsSequence {
        this.logger.info("Updating an entitlements sequence.")

        val input =
            SetEntitlementsSequenceInput.builder().name(name).description(description).transitions(
                transitions.map {
                    EntitlementsSequenceTransitionInput.builder()
                        .entitlementsSetName(it.entitlementsSetName).duration(it.duration).build()
                }
            ).build()

        val response =
            this.graphQLClient.mutate(
                SetEntitlementsSequenceMutation.builder().input(input).build()
            )
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.setEntitlementsSequence
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return EntitlementsSequence(
            Date(output.createdAtEpochMs().toLong()),
            Date(
                output
                    .updatedAtEpochMs().toLong()
            ),
            output.version(),
            output.name(),
            output.description(),
            output.transitions.map {
                EntitlementsSequenceTransition(it.entitlementsSetName(), it.duration())
            }
        )
    }

    override suspend fun removeEntitlementsSequence(name: String): EntitlementsSequence? {
        this.logger.info("Removing an entitlements sequence.")

        val input = RemoveEntitlementsSequenceInput.builder().name(name).build()
        val response =
            this.graphQLClient.mutate(
                RemoveEntitlementsSequenceMutation.builder().input(input).build()
            )
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.removeEntitlementsSequence
        return if (output != null) EntitlementsSequence(
            Date(output.createdAtEpochMs().toLong()),
            Date(
                output
                    .updatedAtEpochMs().toLong()
            ),
            output.version(),
            output.name(),
            output.description(),
            output.transitions.map {
                EntitlementsSequenceTransition(it.entitlementsSetName(), it.duration())
            }
        ) else null
    }

    override suspend fun applyEntitlementsSequenceToUser(
        externalId: String,
        entitlementSequenceName: String
    ): UserEntitlements {
        this.logger.info("Applying an entitlements sequence.")

        val input =
            ApplyEntitlementsSequenceToUserInput.builder().externalId(externalId)
                .entitlementsSequenceName(entitlementSequenceName)
                .build()

        val response =
            this.graphQLClient.mutate(
                ApplyEntitlementsSequenceToUserMutation.builder().input(input).build()
            )
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.applyEntitlementsSequenceToUser
            ?: throw SudoEntitlementsAdminException.FailedException("Mutation completed successfully but result was missing.")

        return UserEntitlements(
            Date(output.createdAtEpochMs().toLong()),
            Date(
                output
                    .updatedAtEpochMs().toLong()
            ),
            output.version(),
            output.externalId(),
            output.owner(),
            output.entitlementsSetName(),
            output.entitlementsSequenceName(),
            output.entitlements.map {
                Entitlement(it.name(), it.description(), it.value())
            },
            output
                .transitionsRelativeToEpochMs()?.let {
                    Date(
                        it.toLong()
                    )
                },
            if (output.accountState() == AccountStates.ACTIVE) AccountState.ACTIVE else AccountState.LOCKED
        )
    }

    override suspend fun removeEntitledUser(externalId: String): EntitledUser? {
        this.logger.info("Removing an entitled user.")

        val input =
            RemoveEntitledUserInput.builder().externalId(externalId).build()

        val response =
            this.graphQLClient.mutate(
                RemoveEntitledUserMutation.builder().input(input).build()
            )
                .enqueue()

        if (response.hasErrors()) {
            throw response.errors().first().toSudoEntitlementsAdminException()
        }

        val output = response.data()?.removeEntitledUser
        return output?.let { EntitledUser(it.externalId()) }
    }
}
