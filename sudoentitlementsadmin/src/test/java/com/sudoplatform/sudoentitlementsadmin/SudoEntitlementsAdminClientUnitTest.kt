/**
 * Copyright © 2020 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient
import com.amazonaws.mobileconnectors.appsync.AppSyncMutationCall
import com.amazonaws.mobileconnectors.appsync.AppSyncQueryCall
import com.apollographql.apollo.GraphQLCall
import com.apollographql.apollo.api.Error
import com.apollographql.apollo.api.Operation
import com.sudoplatform.sudoentitlementsadmin.type.AccountStates
import com.sudoplatform.sudoentitlementsadmin.type.AddEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.type.AddEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.type.ApplyEntitlementsSequenceToUserInput
import com.sudoplatform.sudoentitlementsadmin.type.ApplyEntitlementsSetToUserInput
import com.sudoplatform.sudoentitlementsadmin.type.ApplyEntitlementsToUserInput
import com.sudoplatform.sudoentitlementsadmin.type.EntitlementInput
import com.sudoplatform.sudoentitlementsadmin.type.EntitlementsSequenceTransitionInput
import com.sudoplatform.sudoentitlementsadmin.type.GetEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.type.GetEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.type.RemoveEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.type.RemoveEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.type.SetEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.type.SetEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.types.AccountState
import com.sudoplatform.sudoentitlementsadmin.types.Entitlement
import com.sudoplatform.sudoentitlementsadmin.types.EntitlementsSequenceTransition
import com.sudoplatform.sudologging.Logger
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(DefaultSudoEntitlementsAdminClient::class, Log::class)
class SudoEntitlementsAdminClientUnitTest {

    private lateinit var client: SudoEntitlementsAdminClient
    private lateinit var graphQLClient: AWSAppSyncClient
    private lateinit var appContext: Context
    private lateinit var assets: AssetManager
    private val logger: Logger = mock()
    private val config = mapOf(
        "adminConsoleProjectService" to mapOf(
            "apiUrl" to "https://myfulnonlrb4lao7kj4f76zfpa.appsync-api.us-west-2.amazonaws.com/graphql",
            "region" to "us-west-2",
            "clientId" to "2f8kflcpsdibmoik2t8654dm3s",
        )
    )

    @Before
    fun setUp() {
        this.appContext = mock()
        this.assets = mock()

        whenever(this.appContext.applicationContext).thenReturn(this.appContext)
        whenever(this.appContext.assets).thenReturn(this.assets)
        whenever(this.assets.open("sudoplatformconfig.json")).thenReturn(JSONObject(this.config).toString().byteInputStream())
        this.graphQLClient = mock()

        this.client = spy(
            SudoEntitlementsAdminClient.builder(appContext, "dummy_api_key")
                .setGraphQLClient(this.graphQLClient)
                .setConfig(JSONObject(this.config))
                .setLogger(this.logger)
                .build()
        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testGetEntitlementsSet() = runBlocking {
        val call: AppSyncQueryCall<GetEntitlementsSetQuery> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.query<GetEntitlementsSetQuery.Data, GetEntitlementsSetQuery, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(call.responseFetcher(any())).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val query = GetEntitlementsSetQuery.builder()
                .input(GetEntitlementsSetInput.builder().name("dummy_name").build()).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<GetEntitlementsSetQuery.Data>(query)
            val response = builder.data(
                GetEntitlementsSetQuery.Data(
                    GetEntitlementsSetQuery.GetEntitlementsSet(
                        "GetEntitlementsSet",
                        1.0,
                        2.0,
                        1,
                        "dummy_name",
                        "dummy_description",
                        listOf(
                            GetEntitlementsSetQuery.Entitlement(
                                "Entitlement",
                                "dummy_entitlement",
                                "dummy_description",
                                1
                            )
                        ),
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<GetEntitlementsSetQuery.Data>).onResponse(
                response
            )
        }

        val entitlementsSet =
            this@SudoEntitlementsAdminClientUnitTest.client.getEntitlementsSet("dummy_name")

        verify(this@SudoEntitlementsAdminClientUnitTest.graphQLClient).query<GetEntitlementsSetQuery.Data, GetEntitlementsSetQuery, GetEntitlementsSetQuery.Variables>(
            check {
                assertEquals("dummy_name", it.variables().input().name())
            }
        )

        assertEquals("dummy_name", entitlementsSet?.name)
        assertEquals(1L, entitlementsSet?.createdAt?.time)
        assertEquals(2L, entitlementsSet?.updatedAt?.time)
        assertEquals(1, entitlementsSet?.version)
        assertEquals("dummy_description", entitlementsSet?.description)
        val entitlement = entitlementsSet?.entitlements?.first()
        assertNotNull(entitlement)
        assertEquals("dummy_entitlement", entitlement?.name)
        assertEquals("dummy_description", entitlement?.description)
        assertEquals(1, entitlement?.value)
    }

    @Test
    fun testAddEntitlementsSet() = runBlocking {
        val call: AppSyncMutationCall<AddEntitlementsSetMutation> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.mutate<AddEntitlementsSetMutation.Data, AddEntitlementsSetMutation, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = AddEntitlementsSetMutation.builder()
                .input(
                    AddEntitlementsSetInput.builder().name("dummy_name")
                        .description("dummy_description").entitlements(
                            listOf(
                                EntitlementInput.builder().name("dummy_entitlement")
                                    .description("dummy_description").value(1).build()
                            )
                        ).build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<AddEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.data(
                AddEntitlementsSetMutation.Data(
                    AddEntitlementsSetMutation.AddEntitlementsSet(
                        "AddEntitlementsSet",
                        1.0,
                        2.0,
                        1,
                        "dummy_name",
                        "dummy_description",
                        listOf(
                            AddEntitlementsSetMutation.Entitlement(
                                "Entitlement",
                                "dummy_entitlement",
                                "dummy_description",
                                1
                            )
                        ),
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<AddEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        val entitlementsSet =
            this@SudoEntitlementsAdminClientUnitTest.client.addEntitlementsSet(
                "dummy_name", "dummy_description",
                listOf(
                    Entitlement("dummy_entitlement", "dummy_description", 1)
                )
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.graphQLClient).mutate<AddEntitlementsSetMutation.Data, AddEntitlementsSetMutation, AddEntitlementsSetMutation.Variables>(
            check {
                assertEquals("dummy_name", it.variables().input().name())
                assertEquals("dummy_description", it.variables().input().description())
                val entitlement = it.variables().input().entitlements().first()
                assertEquals("dummy_entitlement", entitlement.name())
                assertEquals("dummy_description", entitlement.description())
                assertEquals(1, entitlement.value())
            }
        )

        assertEquals("dummy_name", entitlementsSet.name)
        assertEquals(1L, entitlementsSet.createdAt.time)
        assertEquals(2L, entitlementsSet.updatedAt.time)
        assertEquals(1, entitlementsSet.version)
        assertEquals("dummy_description", entitlementsSet.description)
        val entitlement = entitlementsSet.entitlements.first()
        assertNotNull(entitlement)
        assertEquals("dummy_entitlement", entitlement.name)
        assertEquals("dummy_description", entitlement.description)
        assertEquals(1, entitlement.value)
    }

    @Test
    fun testSetEntitlementsSet() = runBlocking {
        val call: AppSyncMutationCall<SetEntitlementsSetMutation> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.mutate<SetEntitlementsSetMutation.Data, SetEntitlementsSetMutation, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = SetEntitlementsSetMutation.builder()
                .input(
                    SetEntitlementsSetInput.builder().name("dummy_name")
                        .description("dummy_description").entitlements(
                            listOf(
                                EntitlementInput.builder().name("dummy_entitlement")
                                    .description("dummy_description").value(1).build()
                            )
                        ).build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<SetEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.data(
                SetEntitlementsSetMutation.Data(
                    SetEntitlementsSetMutation.SetEntitlementsSet(
                        "SetEntitlementsSet",
                        1.0,
                        2.0,
                        1,
                        "dummy_name",
                        "dummy_description",
                        listOf(
                            SetEntitlementsSetMutation.Entitlement(
                                "Entitlement",
                                "dummy_entitlement",
                                "dummy_description",
                                1
                            )
                        ),
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<SetEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        val entitlementsSet =
            this@SudoEntitlementsAdminClientUnitTest.client.setEntitlementsSet(
                "dummy_name", "dummy_description",
                listOf(
                    Entitlement("dummy_entitlement", "dummy_description", 1)
                )
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.graphQLClient).mutate<SetEntitlementsSetMutation.Data, SetEntitlementsSetMutation, SetEntitlementsSetMutation.Variables>(
            check {
                assertEquals("dummy_name", it.variables().input().name())
                assertEquals("dummy_description", it.variables().input().description())
                val entitlement = it.variables().input().entitlements().first()
                assertEquals("dummy_entitlement", entitlement.name())
                assertEquals("dummy_description", entitlement.description())
                assertEquals(1, entitlement.value())
            }
        )

        assertEquals("dummy_name", entitlementsSet.name)
        assertEquals(1L, entitlementsSet.createdAt.time)
        assertEquals(2L, entitlementsSet.updatedAt.time)
        assertEquals(1, entitlementsSet.version)
        assertEquals("dummy_description", entitlementsSet.description)
        val entitlement = entitlementsSet.entitlements.first()
        assertNotNull(entitlement)
        assertEquals("dummy_entitlement", entitlement.name)
        assertEquals("dummy_description", entitlement.description)
        assertEquals(1, entitlement.value)
    }

    @Test
    fun testRemoveEntitlementsSet() = runBlocking {
        val call: AppSyncMutationCall<RemoveEntitlementsSetMutation> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.mutate<RemoveEntitlementsSetMutation.Data, RemoveEntitlementsSetMutation, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = RemoveEntitlementsSetMutation.builder()
                .input(
                    RemoveEntitlementsSetInput.builder().name("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<RemoveEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.data(
                RemoveEntitlementsSetMutation.Data(
                    RemoveEntitlementsSetMutation.RemoveEntitlementsSet(
                        "RemoveEntitlementsSet",
                        1.0,
                        2.0,
                        1,
                        "dummy_name",
                        "dummy_description",
                        listOf(
                            RemoveEntitlementsSetMutation.Entitlement(
                                "Entitlement",
                                "dummy_entitlement",
                                "dummy_description",
                                1
                            )
                        ),
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<RemoveEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        val entitlementsSet =
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                "dummy_name"
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.graphQLClient).mutate<RemoveEntitlementsSetMutation.Data, RemoveEntitlementsSetMutation, RemoveEntitlementsSetMutation.Variables>(
            check {
                assertEquals("dummy_name", it.variables().input().name())
            }
        )

        assertEquals("dummy_name", entitlementsSet?.name)
        assertEquals(1L, entitlementsSet?.createdAt?.time)
        assertEquals(2L, entitlementsSet?.updatedAt?.time)
        assertEquals(1, entitlementsSet?.version)
        assertEquals("dummy_description", entitlementsSet?.description)
        val entitlement = entitlementsSet?.entitlements?.first()
        assertNotNull(entitlement)
        assertEquals("dummy_entitlement", entitlement?.name)
        assertEquals("dummy_description", entitlement?.description)
        assertEquals(1, entitlement?.value)
    }

    @Test
    fun testApplyEntitlementsSetToUser() = runBlocking {
        val call: AppSyncMutationCall<ApplyEntitlementsSetToUserMutation> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.mutate<ApplyEntitlementsSetToUserMutation.Data, ApplyEntitlementsSetToUserMutation, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = ApplyEntitlementsSetToUserMutation.builder()
                .input(
                    ApplyEntitlementsSetToUserInput.builder().externalId("dummy_external_id")
                        .entitlementsSetName("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<ApplyEntitlementsSetToUserMutation.Data>(
                    mutation
                )
            val response = builder.data(
                ApplyEntitlementsSetToUserMutation.Data(
                    ApplyEntitlementsSetToUserMutation.ApplyEntitlementsSetToUser(
                        "ApplyEntitlementsSetToUser",
                        1.0,
                        2.0,
                        1.0,
                        "dummy_external_id",
                        "dummy_owner",
                        AccountStates.ACTIVE,
                        "dummy_name",
                        null,
                        listOf(
                            ApplyEntitlementsSetToUserMutation.Entitlement(
                                "Entitlement",
                                "dummy_entitlement",
                                "dummy_description",
                                1
                            )
                        ),
                        null
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<ApplyEntitlementsSetToUserMutation.Data>).onResponse(
                response
            )
        }

        val userEntitlements =
            this@SudoEntitlementsAdminClientUnitTest.client.applyEntitlementsSetToUser(
                "dummy_external_id", "dummy_name"
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.graphQLClient).mutate<ApplyEntitlementsSetToUserMutation.Data, ApplyEntitlementsSetToUserMutation, ApplyEntitlementsSetToUserMutation.Variables>(
            check {
                assertEquals("dummy_external_id", it.variables().input().externalId())
                assertEquals("dummy_name", it.variables().input().entitlementsSetName())
            }
        )

        assertEquals(AccountState.ACTIVE, userEntitlements.accountState)
        assertEquals("dummy_name", userEntitlements.entitlementsSetName)
        assertEquals(1.0, userEntitlements.version, 0.0)
        assertEquals("dummy_owner", userEntitlements.owner)
        assertNull(userEntitlements.entitlementsSequenceName)
        assertNull(userEntitlements.transitionsRelativeTo)
        assertEquals(1L, userEntitlements.createdAt.time)
        assertEquals(2L, userEntitlements.updatedAt.time)
        val entitlement = userEntitlements.entitlements.first()
        assertEquals("dummy_entitlement", entitlement.name)
        assertEquals("dummy_description", entitlement.description)
        assertEquals(1, entitlement.value)
    }

    @Test
    fun testGetEntitlementsSequence() = runBlocking {
        val call: AppSyncQueryCall<GetEntitlementsSequenceQuery> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.query<GetEntitlementsSequenceQuery.Data, GetEntitlementsSequenceQuery, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(call.responseFetcher(any())).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val query = GetEntitlementsSequenceQuery.builder()
                .input(GetEntitlementsSequenceInput.builder().name("dummy_name").build()).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<GetEntitlementsSequenceQuery.Data>(
                    query
                )
            val response = builder.data(
                GetEntitlementsSequenceQuery.Data(
                    GetEntitlementsSequenceQuery.GetEntitlementsSequence(
                        "GetEntitlementsSequence",
                        "dummy_name",
                        "dummy_description",
                        1.0,
                        2.0,
                        1,
                        listOf(
                            GetEntitlementsSequenceQuery.Transition(
                                "Transition",
                                "dummy_entitlements_set",
                                "dummy_duration",
                            )
                        ),
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<GetEntitlementsSequenceQuery.Data>).onResponse(
                response
            )
        }

        val entitlementsSequence =
            this@SudoEntitlementsAdminClientUnitTest.client.getEntitlementsSequence("dummy_name")

        verify(this@SudoEntitlementsAdminClientUnitTest.graphQLClient).query<GetEntitlementsSequenceQuery.Data, GetEntitlementsSequenceQuery, GetEntitlementsSequenceQuery.Variables>(
            check {
                assertEquals("dummy_name", it.variables().input().name())
            }
        )

        assertEquals("dummy_name", entitlementsSequence?.name)
        assertEquals(1L, entitlementsSequence?.createdAt?.time)
        assertEquals(2L, entitlementsSequence?.updatedAt?.time)
        assertEquals(1, entitlementsSequence?.version)
        assertEquals("dummy_description", entitlementsSequence?.description)
        val transition = entitlementsSequence?.transitions?.first()
        assertNotNull(transition)
        assertEquals("dummy_entitlements_set", transition?.entitlementsSetName)
        assertEquals("dummy_duration", transition?.duration)
    }

    @Test
    fun testAddEntitlementsSequence() = runBlocking {
        val call: AppSyncMutationCall<AddEntitlementsSequenceMutation> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.mutate<AddEntitlementsSequenceMutation.Data, AddEntitlementsSequenceMutation, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = AddEntitlementsSequenceMutation.builder()
                .input(
                    AddEntitlementsSequenceInput.builder().name("dummy_name")
                        .description("dummy_description").transitions(
                            listOf(
                                EntitlementsSequenceTransitionInput.builder()
                                    .entitlementsSetName("dummy_entitlements_set")
                                    .duration("dummy_duration").build()
                            )
                        ).build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<AddEntitlementsSequenceMutation.Data>(
                    mutation
                )
            val response = builder.data(
                AddEntitlementsSequenceMutation.Data(
                    AddEntitlementsSequenceMutation.AddEntitlementsSequence(
                        "AddEntitlementsSequence",
                        "dummy_name",
                        "dummy_description",
                        1.0,
                        2.0,
                        1,
                        listOf(
                            AddEntitlementsSequenceMutation.Transition(
                                "Transition",
                                "dummy_entitlements_set",
                                "dummy_duration",
                            )
                        ),
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<AddEntitlementsSequenceMutation.Data>).onResponse(
                response
            )
        }

        val entitlementsSequence =
            this@SudoEntitlementsAdminClientUnitTest.client.addEntitlementsSequence(
                "dummy_name", "dummy_description",
                listOf(
                    EntitlementsSequenceTransition("dummy_entitlements_set", "dummy_duration")
                )
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.graphQLClient).mutate<AddEntitlementsSequenceMutation.Data, AddEntitlementsSequenceMutation, AddEntitlementsSequenceMutation.Variables>(
            check {
                assertEquals("dummy_name", it.variables().input().name())
                assertEquals("dummy_description", it.variables().input().description())
                val transition = it.variables().input().transitions().first()
                assertEquals("dummy_entitlements_set", transition.entitlementsSetName())
                assertEquals("dummy_duration", transition.duration())
            }
        )

        assertEquals("dummy_name", entitlementsSequence.name)
        assertEquals(1L, entitlementsSequence.createdAt.time)
        assertEquals(2L, entitlementsSequence.updatedAt.time)
        assertEquals(1, entitlementsSequence.version)
        assertEquals("dummy_description", entitlementsSequence.description)
        val transition = entitlementsSequence.transitions.first()
        assertNotNull(transition)
        assertEquals("dummy_entitlements_set", transition.entitlementsSetName)
        assertEquals("dummy_duration", transition.duration)
    }

    @Test
    fun testSetEntitlementsSequence() = runBlocking {
        val call: AppSyncMutationCall<SetEntitlementsSequenceMutation> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.mutate<SetEntitlementsSequenceMutation.Data, SetEntitlementsSequenceMutation, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = SetEntitlementsSequenceMutation.builder()
                .input(
                    SetEntitlementsSequenceInput.builder().name("dummy_name")
                        .description("dummy_description").transitions(
                            listOf(
                                EntitlementsSequenceTransitionInput.builder()
                                    .entitlementsSetName("dummy_entitlements_set")
                                    .duration("dummy_duration").build()
                            )
                        ).build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<SetEntitlementsSequenceMutation.Data>(
                    mutation
                )
            val response = builder.data(
                SetEntitlementsSequenceMutation.Data(
                    SetEntitlementsSequenceMutation.SetEntitlementsSequence(
                        "SetEntitlementsSequence",
                        "dummy_name",
                        "dummy_description",
                        1.0,
                        2.0,
                        1,
                        listOf(
                            SetEntitlementsSequenceMutation.Transition(
                                "Transition",
                                "dummy_entitlements_set",
                                "dummy_duration",
                            )
                        ),
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<SetEntitlementsSequenceMutation.Data>).onResponse(
                response
            )
        }

        val entitlementsSequence =
            this@SudoEntitlementsAdminClientUnitTest.client.setEntitlementsSequence(
                "dummy_name", "dummy_description",
                listOf(
                    EntitlementsSequenceTransition("dummy_entitlements_set", "dummy_duration")
                )
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.graphQLClient).mutate<SetEntitlementsSequenceMutation.Data, SetEntitlementsSequenceMutation, SetEntitlementsSequenceMutation.Variables>(
            check {
                assertEquals("dummy_name", it.variables().input().name())
                assertEquals("dummy_description", it.variables().input().description())
                val transition = it.variables().input().transitions().first()
                assertEquals("dummy_entitlements_set", transition.entitlementsSetName())
                assertEquals("dummy_duration", transition.duration())
            }
        )

        assertEquals("dummy_name", entitlementsSequence.name)
        assertEquals(1L, entitlementsSequence.createdAt.time)
        assertEquals(2L, entitlementsSequence.updatedAt.time)
        assertEquals(1, entitlementsSequence.version)
        assertEquals("dummy_description", entitlementsSequence.description)
        val transition = entitlementsSequence.transitions.first()
        assertNotNull(transition)
        assertEquals("dummy_entitlements_set", transition.entitlementsSetName)
        assertEquals("dummy_duration", transition.duration)
    }

    @Test
    fun testRemoveEntitlementsSequence() = runBlocking {
        val call: AppSyncMutationCall<RemoveEntitlementsSequenceMutation> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.mutate<RemoveEntitlementsSequenceMutation.Data, RemoveEntitlementsSequenceMutation, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = RemoveEntitlementsSequenceMutation.builder()
                .input(
                    RemoveEntitlementsSequenceInput.builder().name("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<RemoveEntitlementsSequenceMutation.Data>(
                    mutation
                )
            val response = builder.data(
                RemoveEntitlementsSequenceMutation.Data(
                    RemoveEntitlementsSequenceMutation.RemoveEntitlementsSequence(
                        "RemoveEntitlementsSequence",
                        "dummy_name",
                        "dummy_description",
                        1.0,
                        2.0,
                        1,
                        listOf(
                            RemoveEntitlementsSequenceMutation.Transition(
                                "Transition",
                                "dummy_entitlements_set",
                                "dummy_duration",
                            )
                        ),
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<RemoveEntitlementsSequenceMutation.Data>).onResponse(
                response
            )
        }

        val entitlementsSequence =
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSequence(
                "dummy_name"
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.graphQLClient).mutate<RemoveEntitlementsSequenceMutation.Data, RemoveEntitlementsSequenceMutation, RemoveEntitlementsSequenceMutation.Variables>(
            check {
                assertEquals("dummy_name", it.variables().input().name())
            }
        )

        assertEquals("dummy_name", entitlementsSequence?.name)
        assertEquals(1L, entitlementsSequence?.createdAt?.time)
        assertEquals(2L, entitlementsSequence?.updatedAt?.time)
        assertEquals(1, entitlementsSequence?.version)
        assertEquals("dummy_description", entitlementsSequence?.description)
        val transition = entitlementsSequence?.transitions?.first()
        assertNotNull(transition)
        assertEquals("dummy_entitlements_set", transition?.entitlementsSetName)
        assertEquals("dummy_duration", transition?.duration)
    }

    @Test
    fun testApplyEntitlementsSequenceToUser() = runBlocking {
        val call: AppSyncMutationCall<ApplyEntitlementsSequenceToUserMutation> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.mutate<ApplyEntitlementsSequenceToUserMutation.Data, ApplyEntitlementsSequenceToUserMutation, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = ApplyEntitlementsSequenceToUserMutation.builder()
                .input(
                    ApplyEntitlementsSequenceToUserInput.builder().externalId("dummy_external_id")
                        .entitlementsSequenceName("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<ApplyEntitlementsSequenceToUserMutation.Data>(
                    mutation
                )
            val response = builder.data(
                ApplyEntitlementsSequenceToUserMutation.Data(
                    ApplyEntitlementsSequenceToUserMutation.ApplyEntitlementsSequenceToUser(
                        "ApplyEntitlementsSequenceToUser",
                        1.0,
                        2.0,
                        1.0,
                        "dummy_external_id",
                        "dummy_owner",
                        AccountStates.ACTIVE,
                        null,
                        "dummy_name",
                        listOf(
                            ApplyEntitlementsSequenceToUserMutation.Entitlement(
                                "Entitlement",
                                "dummy_entitlement",
                                "dummy_description",
                                1
                            )
                        ),
                        1.0
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<ApplyEntitlementsSequenceToUserMutation.Data>).onResponse(
                response
            )
        }

        val userEntitlements =
            this@SudoEntitlementsAdminClientUnitTest.client.applyEntitlementsSequenceToUser(
                "dummy_external_id", "dummy_name"
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.graphQLClient).mutate<ApplyEntitlementsSequenceToUserMutation.Data, ApplyEntitlementsSequenceToUserMutation, ApplyEntitlementsSequenceToUserMutation.Variables>(
            check {
                assertEquals("dummy_external_id", it.variables().input().externalId())
                assertEquals("dummy_name", it.variables().input().entitlementsSequenceName())
            }
        )

        assertEquals(AccountState.ACTIVE, userEntitlements.accountState)
        assertEquals("dummy_name", userEntitlements.entitlementsSequenceName)
        assertEquals(1.0, userEntitlements.version, 0.0)
        assertEquals("dummy_owner", userEntitlements.owner)
        assertNull(userEntitlements.entitlementsSetName)
        assertEquals(1L, userEntitlements.transitionsRelativeTo?.time)
        assertEquals(1L, userEntitlements.createdAt.time)
        assertEquals(2L, userEntitlements.updatedAt.time)
        val entitlement = userEntitlements.entitlements.first()
        assertEquals("dummy_entitlement", entitlement.name)
        assertEquals("dummy_description", entitlement.description)
        assertEquals(1, entitlement.value)
    }

    @Test
    fun testApplyEntitlementsToUser() = runBlocking {
        val call: AppSyncMutationCall<ApplyEntitlementsToUserMutation> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.mutate<ApplyEntitlementsToUserMutation.Data, ApplyEntitlementsToUserMutation, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = ApplyEntitlementsToUserMutation.builder()
                .input(
                    ApplyEntitlementsToUserInput.builder().externalId("dummy_external_id")
                        .entitlements(
                            listOf(
                                EntitlementInput.builder().name("dummy_entitlement")
                                    .description("dummy_description").value(1).build()
                            )
                        ).build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<ApplyEntitlementsToUserMutation.Data>(
                    mutation
                )
            val response = builder.data(
                ApplyEntitlementsToUserMutation.Data(
                    ApplyEntitlementsToUserMutation.ApplyEntitlementsToUser(
                        "ApplyEntitlementsToUser",
                        1.0,
                        2.0,
                        1.0,
                        "dummy_external_id",
                        "dummy_owner",
                        AccountStates.LOCKED,
                        null,
                        null,
                        listOf(
                            ApplyEntitlementsToUserMutation.Entitlement(
                                "Entitlement",
                                "dummy_entitlement",
                                "dummy_description",
                                1
                            )
                        ),
                        null
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<ApplyEntitlementsToUserMutation.Data>).onResponse(
                response
            )
        }

        val userEntitlements =
            this@SudoEntitlementsAdminClientUnitTest.client.applyEntitlementsToUser(
                "dummy_external_id",
                listOf(Entitlement("dummy_entitlement", "dummy_description", 1))
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.graphQLClient).mutate<ApplyEntitlementsToUserMutation.Data, ApplyEntitlementsToUserMutation, ApplyEntitlementsToUserMutation.Variables>(
            check {
                assertEquals("dummy_external_id", it.variables().input().externalId())
                val entitlement = it.variables().input().entitlements().first()
                assertEquals("dummy_entitlement", entitlement.name())
                assertEquals("dummy_description", entitlement.description())
                assertEquals(1, entitlement.value())
            }
        )

        assertEquals(AccountState.LOCKED, userEntitlements.accountState)
        assertEquals(1.0, userEntitlements.version, 0.0)
        assertEquals("dummy_owner", userEntitlements.owner)
        assertNull(userEntitlements.entitlementsSequenceName)
        assertNull(userEntitlements.entitlementsSetName)
        assertNull(userEntitlements.transitionsRelativeTo)
        assertEquals(1L, userEntitlements.createdAt.time)
        assertEquals(2L, userEntitlements.updatedAt.time)
        val entitlement = userEntitlements.entitlements.first()
        assertEquals("dummy_entitlement", entitlement.name)
        assertEquals("dummy_description", entitlement.description)
        assertEquals(1, entitlement.value)
    }

    @Test
    fun testErrorHandling() = runBlocking {
        val call: AppSyncMutationCall<RemoveEntitlementsSetMutation> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.graphQLClient.mutate<RemoveEntitlementsSetMutation.Data, RemoveEntitlementsSetMutation, Operation.Variables>(
                any()
            )
        ).thenReturn(call)

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = RemoveEntitlementsSetMutation.builder()
                .input(
                    RemoveEntitlementsSetInput.builder().name("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<RemoveEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.errors(
                listOf(
                    Error(
                        null,
                        null,
                        mapOf("errorType" to "sudoplatform.DecodingError")
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<RemoveEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        try {
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                "dummy_name"
            )
        } catch (e: SudoEntitlementsAdminException.InvalidInputException) {
            // Expected exception thrown.
        } catch (e: Throwable) {
            fail("Expected exception not thrown.")
        }

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = RemoveEntitlementsSetMutation.builder()
                .input(
                    RemoveEntitlementsSetInput.builder().name("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<RemoveEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.errors(
                listOf(
                    Error(
                        null,
                        null,
                        mapOf("errorType" to "sudoplatform.ServiceError")
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<RemoveEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        try {
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                "dummy_name"
            )
        } catch (e: SudoEntitlementsAdminException.InternalServerException) {
            // Expected exception thrown.
        } catch (e: Throwable) {
            fail("Expected exception not thrown.")
        }

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = RemoveEntitlementsSetMutation.builder()
                .input(
                    RemoveEntitlementsSetInput.builder().name("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<RemoveEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.errors(
                listOf(
                    Error(
                        null,
                        null,
                        mapOf("errorType" to "sudoplatform.InvalidArgumentError")
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<RemoveEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        try {
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                "dummy_name"
            )
        } catch (e: SudoEntitlementsAdminException.InvalidInputException) {
            // Expected exception thrown.
        } catch (e: Throwable) {
            fail("Expected exception not thrown.")
        }

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = RemoveEntitlementsSetMutation.builder()
                .input(
                    RemoveEntitlementsSetInput.builder().name("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<RemoveEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.errors(
                listOf(
                    Error(
                        null,
                        null,
                        mapOf("errorType" to "sudoplatform.entitlements.EntitlementsSetInUse")
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<RemoveEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        try {
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                "dummy_name"
            )
        } catch (e: SudoEntitlementsAdminException.EntitlementsSetInUseException) {
            // Expected exception thrown.
        } catch (e: Throwable) {
            fail("Expected exception not thrown.")
        }

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = RemoveEntitlementsSetMutation.builder()
                .input(
                    RemoveEntitlementsSetInput.builder().name("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<RemoveEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.errors(
                listOf(
                    Error(
                        null,
                        null,
                        mapOf("errorType" to "sudoplatform.entitlements.EntitlementsSequenceNotFoundError")
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<RemoveEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        try {
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                "dummy_name"
            )
        } catch (e: SudoEntitlementsAdminException.EntitlementsSequenceNotFoundException) {
            // Expected exception thrown.
        } catch (e: Throwable) {
            fail("Expected exception not thrown.")
        }

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = RemoveEntitlementsSetMutation.builder()
                .input(
                    RemoveEntitlementsSetInput.builder().name("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<RemoveEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.errors(
                listOf(
                    Error(
                        null,
                        null,
                        mapOf("errorType" to "sudoplatform.entitlements.EntitlementsSetNotFoundError")
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<RemoveEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        try {
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                "dummy_name"
            )
        } catch (e: SudoEntitlementsAdminException.EntitlementsSetNotFoundException) {
            // Expected exception thrown.
        } catch (e: Throwable) {
            fail("Expected exception not thrown.")
        }

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = RemoveEntitlementsSetMutation.builder()
                .input(
                    RemoveEntitlementsSetInput.builder().name("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<RemoveEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.errors(
                listOf(
                    Error(
                        null,
                        null,
                        mapOf("errorType" to "sudoplatform.entitlements.EntitlementsSetAlreadyExistsError")
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<RemoveEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        try {
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                "dummy_name"
            )
        } catch (e: SudoEntitlementsAdminException.EntitlementsSetAlreadyExistsException) {
            // Expected exception thrown.
        } catch (e: Throwable) {
            fail("Expected exception not thrown.")
        }

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = RemoveEntitlementsSetMutation.builder()
                .input(
                    RemoveEntitlementsSetInput.builder().name("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<RemoveEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.errors(
                listOf(
                    Error(
                        null,
                        null,
                        mapOf("errorType" to "sudoplatform.entitlements.EntitlementsSequenceAlreadyExistsError")
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<RemoveEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        try {
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                "dummy_name"
            )
        } catch (e: SudoEntitlementsAdminException.EntitlementsSequenceAlreadyExistsException) {
            // Expected exception thrown.
        } catch (e: Throwable) {
            fail("Expected exception not thrown.")
        }

        whenever(
            call.enqueue(
                any()
            )
        ).thenAnswer {
            val mutation = RemoveEntitlementsSetMutation.builder()
                .input(
                    RemoveEntitlementsSetInput.builder().name("dummy_name").build()
                ).build()

            val builder =
                com.apollographql.apollo.api.Response.builder<RemoveEntitlementsSetMutation.Data>(
                    mutation
                )
            val response = builder.errors(
                listOf(
                    Error(
                        null,
                        null,
                        mapOf("errorType" to "sudoplatform.entitlements.EntitlementsSetImmutableError")
                    )
                )
            ).build()

            @Suppress("UNCHECKED_CAST")
            (it.arguments[0] as GraphQLCall.Callback<RemoveEntitlementsSetMutation.Data>).onResponse(
                response
            )
        }

        try {
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                "dummy_name"
            )
        } catch (e: SudoEntitlementsAdminException.EntitlementsSetImmutableException) {
            // Expected exception thrown.
        } catch (e: Throwable) {
            fail("Expected exception not thrown.")
        }

        return@runBlocking
    }
}
