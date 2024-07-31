/*
 * Copyright © 2024 Anonyome Labs, Inc. All rights reserved.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.sudoplatform.sudoentitlementsadmin

import android.content.Context
import android.content.res.AssetManager
import com.amplifyframework.api.ApiCategory
import com.amplifyframework.api.graphql.GraphQLOperation
import com.amplifyframework.api.graphql.GraphQLResponse
import com.amplifyframework.core.Consumer
import com.apollographql.apollo3.api.Optional
import com.sudoplatform.sudoentitlementsadmin.graphql.AddEntitlementsSequenceMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.AddEntitlementsSetMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsSequenceToUserMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsSequenceToUsersMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsSetToUserMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsSetToUsersMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsToUserMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyEntitlementsToUsersMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.ApplyExpendableEntitlementsToUserMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.GetEntitlementsSequenceQuery
import com.sudoplatform.sudoentitlementsadmin.graphql.GetEntitlementsSetQuery
import com.sudoplatform.sudoentitlementsadmin.graphql.RemoveEntitlementsSequenceMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.RemoveEntitlementsSetMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.SetEntitlementsSequenceMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.SetEntitlementsSetMutation
import com.sudoplatform.sudoentitlementsadmin.graphql.type.AddEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.AddEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsSequenceToUserInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsSequenceToUsersInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsSetToUserInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsSetToUsersInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsToUserInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyEntitlementsToUsersInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.ApplyExpendableEntitlementsToUserInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.GetEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.RemoveEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.RemoveEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.SetEntitlementsSequenceInput
import com.sudoplatform.sudoentitlementsadmin.graphql.type.SetEntitlementsSetInput
import com.sudoplatform.sudoentitlementsadmin.types.AccountState
import com.sudoplatform.sudoentitlementsadmin.types.ApplyEntitlementsOperation
import com.sudoplatform.sudoentitlementsadmin.types.ApplyEntitlementsSequenceOperation
import com.sudoplatform.sudoentitlementsadmin.types.ApplyEntitlementsSetOperation
import com.sudoplatform.sudoentitlementsadmin.types.Entitlement
import com.sudoplatform.sudoentitlementsadmin.types.EntitlementsSequenceTransition
import com.sudoplatform.sudoentitlementsadmin.types.UserEntitlementsResult
import com.sudoplatform.sudologging.Logger
import com.sudoplatform.sudouser.amplify.GraphQLClient
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.check
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SudoEntitlementsAdminClientUnitTest {

    private lateinit var client: SudoEntitlementsAdminClient
    private lateinit var apiCategory: ApiCategory
    private lateinit var graphQLClient: GraphQLClient
    private lateinit var appContext: Context
    private lateinit var assets: AssetManager
    private val logger: Logger = mock()
    private val config = mapOf(
        "adminConsoleProjectService" to mapOf(
            "apiUrl" to "https://myfulnonlrb4lao7kj4f76zfpa.appsync-api.us-west-2.amazonaws.com/graphql",
            "region" to "us-west-2",
            "clientId" to "2f8kflcpsdibmoik2t8654dm3s",
        ),
    )

    @Before
    fun setUp() {
        this.appContext = mock()
        this.assets = mock()
        this.apiCategory = mock()
        this.graphQLClient = GraphQLClient(this.apiCategory)

        whenever(this.appContext.applicationContext).thenReturn(this.appContext)
        whenever(this.appContext.assets).thenReturn(this.assets)
        whenever(this.assets.open("sudoplatformconfig.json")).thenReturn(JSONObject(this.config).toString().byteInputStream())

        this.client = spy(
            SudoEntitlementsAdminClient.builder(appContext, "dummy_api_key")
                .setGraphQLClient(this.graphQLClient)
                .setConfig(JSONObject(this.config))
                .setLogger(this.logger)
                .build(),
        )
    }

    @After
    fun tearDown() {
    }

    @Test
    fun testGetEntitlementsSet() = runBlocking {
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.query<String>(
                argThat { this.query.equals(GetEntitlementsSetQuery.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'getEntitlementsSet': {
                        '__typename': 'EntitlementsSet',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1,
                        'name': 'dummy_name',
                        'description': 'dummy_description',
                        'entitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'dummy_entitlement',
                                'description': 'dummy_description',
                                'value': 1.0
                            }
                        ]
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val entitlementsSet =
            this@SudoEntitlementsAdminClientUnitTest.client.getEntitlementsSet("dummy_name")

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).query<String>(
            check {
                assertEquals(GetEntitlementsSetQuery.OPERATION_DOCUMENT, it.query)
            },
            any(),
            any(),

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
        assertEquals(1L, entitlement?.value)
    }

    @Test
    fun testAddEntitlementsSet() = runBlocking {
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(AddEntitlementsSetMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'addEntitlementsSet': {
                        '__typename': 'EntitlementsSet',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1,
                        'name': 'dummy_name',
                        'description': 'dummy_description',
                        'entitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'dummy_entitlement',
                                'description': 'dummy_description',
                                'value': 1.0
                            }
                        ]
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val entitlementsSet =
            this@SudoEntitlementsAdminClientUnitTest.client.addEntitlementsSet(
                "dummy_name",
                "dummy_description",
                listOf(
                    Entitlement("dummy_entitlement", "dummy_description", 1),
                ),
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as AddEntitlementsSetInput?
                assertEquals("dummy_name", input?.name)
                assertEquals(Optional.Present("dummy_description"), input?.description)
                val entitlement = input?.entitlements?.first()
                assertEquals("dummy_entitlement", entitlement?.name)
                assertEquals(Optional.Present("dummy_description"), entitlement?.description)
                assertEquals(1L, entitlement?.value?.toLong())
            },
            any(),
            any(),
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
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(SetEntitlementsSetMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'setEntitlementsSet': {
                        '__typename': 'EntitlementsSet',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1,
                        'name': 'dummy_name',
                        'description': 'dummy_description',
                        'entitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'dummy_entitlement',
                                'description': 'dummy_description',
                                'value': 1.0
                            }
                        ]
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val entitlementsSet =
            this@SudoEntitlementsAdminClientUnitTest.client.setEntitlementsSet(
                "dummy_name",
                "dummy_description",
                listOf(
                    Entitlement("dummy_entitlement", "dummy_description", 1),
                ),
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as SetEntitlementsSetInput?
                assertEquals("dummy_name", input?.name)
                assertEquals(Optional.Present("dummy_description"), input?.description)
                val entitlement = input?.entitlements?.first()
                assertEquals("dummy_entitlement", entitlement?.name)
                assertEquals(Optional.Present("dummy_description"), entitlement?.description)
                assertEquals(1L, entitlement?.value?.toLong())
            },
            any(),
            any(),
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
        assertEquals(1L, entitlement.value)
    }

    @Test
    fun testRemoveEntitlementsSet() = runBlocking {
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(RemoveEntitlementsSetMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'removeEntitlementsSet': {
                        '__typename': 'EntitlementsSet',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1,
                        'name': 'dummy_name',
                        'description': 'dummy_description',
                        'entitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'dummy_entitlement',
                                'description': 'dummy_description',
                                'value': 1.0
                            }
                        ]
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val entitlementsSet =
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                "dummy_name",
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as RemoveEntitlementsSetInput?
                assertEquals("dummy_name", input?.name)
            },
            any(),
            any(),
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
        assertEquals(1L, entitlement?.value)
    }

    @Test
    fun testApplyEntitlementsSetToUser() = runBlocking {
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(ApplyEntitlementsSetToUserMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'applyEntitlementsSetToUser': {
                        '__typename': 'ExternalUserEntitlements',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1.0,
                        'externalId': 'dummy_external_id',
                        'owner': 'dummy_owner',
                        'accountState': 'ACTIVE',
                        'entitlementsSetName': 'dummy_name',
                        'entitlementsSequenceName': null,
                        'entitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'dummy_entitlement',
                                'description': 'dummy_description',
                                'value': 1.0
                            }
                        ],
                        'expendableEntitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'expendable_entitlement',
                                'description': 'expendable_description',
                                'value': 2.0
                            }
                        ],
                        transitionsRelativeToEpochMs: null
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val userEntitlements =
            this@SudoEntitlementsAdminClientUnitTest.client.applyEntitlementsSetToUser(
                "dummy_external_id",
                "dummy_name",
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as ApplyEntitlementsSetToUserInput?
                assertEquals("dummy_external_id", input?.externalId)
                assertEquals("dummy_name", input?.entitlementsSetName)
            },
            any(),
            any(),
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
    fun testApplyEntitlementsSetToUsers() = runBlocking {
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(ApplyEntitlementsSetToUsersMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'applyEntitlementsSetToUsers': [{
                        '__typename': 'ExternalUserEntitlements',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1.0,
                        'externalId': 'dummy_external_id',
                        'owner': 'dummy_owner',
                        'accountState': 'ACTIVE',
                        'entitlementsSetName': 'dummy_name',
                        'entitlementsSequenceName': null,
                        'entitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'dummy_entitlement',
                                'description': 'dummy_description',
                                'value': 1.0
                            }
                        ],
                        'expendableEntitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'expendable_entitlement',
                                'description': 'expendable_description',
                                'value': 2.0
                            }
                        ],
                        transitionsSinceEpoch: null
                    },
                    {
                        '__typename': 'ExternalUserEntitlementsError',
                        'error': 'sudoplatform.entitlements.EntitlementsSetNotFoundError'
                    }]
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val results =
            this@SudoEntitlementsAdminClientUnitTest.client.applyEntitlementsSetToUsers(
                listOf(
                    ApplyEntitlementsSetOperation("dummy_external_id", "dummy_name"),
                    ApplyEntitlementsSetOperation("error_external_id", "error_name"),
                ),
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as ApplyEntitlementsSetToUsersInput?
                assertNotNull(input)
                assertEquals("dummy_external_id", input!!.operations[0].externalId)
                assertEquals("dummy_name", input.operations[0].entitlementsSetName)
                assertEquals("error_external_id", input.operations[1].externalId)
                assertEquals("error_name", input.operations[1].entitlementsSetName)
            },
            any(),
            any(),
        )

        assertEquals(2, results.size)
        val results0 = results[0]
        val results1 = results[1]

        when (results0) {
            is UserEntitlementsResult.Success -> {
                val userEntitlements = results0.value
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
            else -> {
                fail("results[0] is not of expected type but ${results[0].javaClass}")
            }
        }

        when (results1) {
            is UserEntitlementsResult.Failure -> {
                when (results1.error) {
                    is SudoEntitlementsAdminException.EntitlementsSetNotFoundException -> {}
                    else -> {
                        fail("Error not mapped correctly")
                    }
                }
            }
            else -> {
                fail("results[1] is not of expected type but ${results[1].javaClass}")
            }
        }
    }

    @Test
    fun testGetEntitlementsSequence() = runBlocking {
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.query<String>(
                argThat { this.query.equals(GetEntitlementsSequenceQuery.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'getEntitlementsSequence': {
                        '__typename': 'EntitlementsSequence',
                        'name': 'dummy_name',
                        'description': 'dummy_description',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1,
                        'transitions': [
                            {
                                '__typename': 'Transition',
                                'entitlementsSetName': 'dummy_entitlements_set',
                                'duration': 'dummy_duration'
                            }
                        ]
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val entitlementsSequence =
            this@SudoEntitlementsAdminClientUnitTest.client.getEntitlementsSequence("dummy_name")

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).query<String>(
            check {
                val input = it.variables["input"] as GetEntitlementsSequenceInput?
                assertEquals("dummy_name", input?.name)
            },
            any(),
            any(),
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
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(AddEntitlementsSequenceMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'addEntitlementsSequence': {
                        '__typename': 'EntitlementsSequence',
                        'name': 'dummy_name',
                        'description': 'dummy_description',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1,
                        'transitions': [
                            {
                                '__typename': 'Transition',
                                'entitlementsSetName': 'dummy_entitlements_set',
                                'duration': 'dummy_duration'
                            }
                        ]
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val entitlementsSequence =
            this@SudoEntitlementsAdminClientUnitTest.client.addEntitlementsSequence(
                "dummy_name",
                "dummy_description",
                listOf(
                    EntitlementsSequenceTransition("dummy_entitlements_set", "dummy_duration"),
                ),
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as AddEntitlementsSequenceInput?
                assertEquals("dummy_name", input?.name)
                assertEquals(Optional.Present("dummy_description"), input?.description)
                val transition = input?.transitions?.first()
                assertEquals("dummy_entitlements_set", transition?.entitlementsSetName)
                assertEquals(Optional.Present("dummy_duration"), transition?.duration)
            },
            any(),
            any(),
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
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(SetEntitlementsSequenceMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'setEntitlementsSequence': {
                        '__typename': 'EntitlementsSequence',
                        'name': 'dummy_name',
                        'description': 'dummy_description',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1,
                        'transitions': [
                            {
                                '__typename': 'Transition',
                                'entitlementsSetName': 'dummy_entitlements_set',
                                'duration': 'dummy_duration'
                            }
                        ]
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val entitlementsSequence =
            this@SudoEntitlementsAdminClientUnitTest.client.setEntitlementsSequence(
                "dummy_name",
                "dummy_description",
                listOf(
                    EntitlementsSequenceTransition("dummy_entitlements_set", "dummy_duration"),
                ),
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as SetEntitlementsSequenceInput?
                assertEquals("dummy_name", input?.name)
                assertEquals(Optional.Present("dummy_description"), input?.description)
                val transition = input?.transitions?.first()
                assertEquals("dummy_entitlements_set", transition?.entitlementsSetName)
                assertEquals(Optional.Present("dummy_duration"), transition?.duration)
            },
            any(),
            any(),
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
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(RemoveEntitlementsSequenceMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'removeEntitlementsSequence': {
                        '__typename': 'EntitlementsSequence',
                        'name': 'dummy_name',
                        'description': 'dummy_description',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1,
                        'transitions': [
                            {
                                 '__typename': 'Transition',
                                'entitlementsSetName': 'dummy_entitlements_set',
                                'duration': 'dummy_duration'
                            }
                        ]
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val entitlementsSequence =
            this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSequence(
                "dummy_name",
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as RemoveEntitlementsSequenceInput?
                assertEquals("dummy_name", input?.name)
            },
            any(),
            any(),
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
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(ApplyEntitlementsSequenceToUserMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'applyEntitlementsSequenceToUser': {
                        '__typename': 'ExternalUserEntitlements',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1.0,
                        'externalId': 'dummy_external_id',
                        'owner': 'dummy_owner',
                        'accountState': 'ACTIVE',
                        'entitlementsSetName': null,
                        'entitlementsSequenceName': 'dummy_name',
                        'entitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'dummy_entitlement',
                                'description': 'dummy_description',
                                'value': 1.0
                            }
                        ],
                        'expendableEntitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'expendable_entitlement',
                                'description': 'expendable_description',
                                'value': 2.0
                            }
                        ],
                        'transitionsRelativeToEpochMs': 1.0
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val userEntitlements =
            this@SudoEntitlementsAdminClientUnitTest.client.applyEntitlementsSequenceToUser(
                "dummy_external_id",
                "dummy_name",
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as ApplyEntitlementsSequenceToUserInput?
                assertEquals("dummy_external_id", input?.externalId)
                assertEquals("dummy_name", input?.entitlementsSequenceName)
            },
            any(),
            any(),
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
    fun testApplyEntitlementsSequenceToUsers() = runBlocking {
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(ApplyEntitlementsSequenceToUsersMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'applyEntitlementsSequenceToUsers': [{
                        '__typename': 'ExternalUserEntitlements',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1.0,
                        'externalId': 'dummy_external_id',
                        'owner': 'dummy_owner',
                        'accountState': 'ACTIVE',
                        'entitlementsSetName': 'dummy_name',
                        'entitlementsSequenceName': 'dummy_name',
                        'entitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'dummy_entitlement',
                                'description': 'dummy_description',
                                'value': 1.0
                            }
                        ],
                        'expendableEntitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'expendable_entitlement',
                                'description': 'expendable_description',
                                'value': 2.0
                            }
                        ],
                        'transitionsRelativeToEpochMs': null
                    },
                    {
                        '__typename': 'ExternalUserEntitlementsError',
                        'error': 'sudoplatform.entitlements.EntitlementsSequenceNotFoundError'
                    
                    }]
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val results =
            this@SudoEntitlementsAdminClientUnitTest.client.applyEntitlementsSequenceToUsers(
                listOf(
                    ApplyEntitlementsSequenceOperation("dummy_external_id", "dummy_name"),
                    ApplyEntitlementsSequenceOperation("error_external_id", "error_name"),
                ),
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as ApplyEntitlementsSequenceToUsersInput?
                assertNotNull(input)
                assertEquals("dummy_external_id", input!!.operations[0].externalId)
                assertEquals("dummy_name", input.operations[0].entitlementsSequenceName)
                assertEquals("error_external_id", input.operations[1].externalId)
                assertEquals("error_name", input.operations[1].entitlementsSequenceName)
            },
            any(),
            any(),
        )

        assertEquals(2, results.size)
        val results0 = results[0]
        val results1 = results[1]

        when (results0) {
            is UserEntitlementsResult.Success -> {
                val userEntitlements = results0.value
                assertEquals(AccountState.ACTIVE, userEntitlements.accountState)
                assertEquals("dummy_name", userEntitlements.entitlementsSetName)
                assertEquals(1.0, userEntitlements.version, 0.0)
                assertEquals("dummy_owner", userEntitlements.owner)
                assertEquals("dummy_name", userEntitlements.entitlementsSequenceName)
                assertNull(userEntitlements.transitionsRelativeTo)
                assertEquals(1L, userEntitlements.createdAt.time)
                assertEquals(2L, userEntitlements.updatedAt.time)
                val entitlement = userEntitlements.entitlements.first()
                assertEquals("dummy_entitlement", entitlement.name)
                assertEquals("dummy_description", entitlement.description)
                assertEquals(1, entitlement.value)
            }
            else -> {
                fail("results[0] is not of expected type but ${results[0].javaClass}")
            }
        }

        when (results1) {
            is UserEntitlementsResult.Failure -> {
                val error = results1.error
                when (error) {
                    is SudoEntitlementsAdminException.EntitlementsSequenceNotFoundException -> {}
                    else -> {
                        fail("Error not mapped correctly")
                    }
                }
            }
            else -> {
                fail("results[1] is not of expected type but ${results[1].javaClass}")
            }
        }
    }

    @Test
    fun testApplyEntitlementsToUser() = runBlocking {
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(ApplyEntitlementsToUserMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'applyEntitlementsToUser': {
                        '__typename': 'ExternalUserEntitlements',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1.0,
                        'externalId': 'dummy_external_id',
                        'owner': 'dummy_owner',
                        'accountState': 'LOCKED',
                        'entitlementsSetName': null,
                        'entitlementsSequenceName': null,
                        'entitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'dummy_entitlement',
                                'description': 'dummy_description',
                                'value': 1.0
                            }
                        ],
                        'expendableEntitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'expendable_entitlement',
                                'description': 'expendable_description',
                                'value': 2.0
                            }
                        ],
                        'transitionsRelativeToEpochMs': null
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }
        val userEntitlements =
            this@SudoEntitlementsAdminClientUnitTest.client.applyEntitlementsToUser(
                "dummy_external_id",
                listOf(Entitlement("dummy_entitlement", "dummy_description", 1)),
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as ApplyEntitlementsToUserInput?
                assertEquals("dummy_external_id", input?.externalId)
                val entitlement = input?.entitlements?.first()
                assertEquals("dummy_entitlement", entitlement?.name)
                assertEquals(Optional.Present("dummy_description"), entitlement?.description)
                assertEquals(1L, entitlement?.value?.toLong())
            },
            any(),
            any(),
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
    fun testApplyEntitlementsToUsers() = runBlocking {
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(ApplyEntitlementsToUsersMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'applyEntitlementsToUsers': [{
                        '__typename': 'ExternalUserEntitlements',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1.0,
                        'externalId': 'dummy_external_id',
                        'owner': 'dummy_owner',
                        'accountState': 'ACTIVE',
                        'entitlementsSetName': "dummy_owner",
                        'entitlementsSequenceName': null,
                        'entitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'dummy_entitlement',
                                'description': 'dummy_description',
                                'value': 1.0
                            }
                        ],
                        'expendableEntitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'expendable_entitlement',
                                'description': 'expendable_description',
                                'value': 2.0
                            }
                        ],
                        'transitionsRelativeToEpochMs': null
                    }, 
                    {
                        '__typename': 'ExternalUserEntitlementsError',
                        error: 'sudoplatform.entitlements.InvalidEntitlementsError'
                    }]
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val results =
            this@SudoEntitlementsAdminClientUnitTest.client.applyEntitlementsToUsers(
                listOf(
                    ApplyEntitlementsOperation("dummy_external_id", listOf(Entitlement("dummy_name", "dummy_description", 1))),
                    ApplyEntitlementsOperation("error_external_id", listOf(Entitlement("error_name", "error_description", 1))),
                ),
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as ApplyEntitlementsToUsersInput?
                assertNotNull(input)
                assertEquals("dummy_external_id", input!!.operations[0].externalId)
                assertEquals("dummy_name", input.operations[0].entitlements[0].name)
                assertEquals("error_external_id", input.operations[1].externalId)
                assertEquals("error_name", input.operations[1].entitlements[0].name)
            },
            any(),
            any(),
        )

        assertEquals(2, results.size)
        val results0 = results[0]
        val results1 = results[1]

        when (results0) {
            is UserEntitlementsResult.Success -> {
                val userEntitlements = results0.value
                assertEquals(AccountState.ACTIVE, userEntitlements.accountState)
                assertEquals("dummy_owner", userEntitlements.entitlementsSetName)
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
            else -> {
                fail("results[0] is not of expected type but ${results[0].javaClass}")
            }
        }

        when (results1) {
            is UserEntitlementsResult.Failure -> {
                val error = results1.error
                when (error) {
                    is SudoEntitlementsAdminException.InvalidEntitlementsException -> {}
                    else -> {
                        fail("Error not mapped correctly")
                    }
                }
            }
            else -> {
                fail("results[1] is not of expected type but ${results[1].javaClass}")
            }
        }
    }

    @Test
    fun testApplyExpendableEntitlementsToUser() = runBlocking {
        val mockOperation: GraphQLOperation<String> = mock()
        whenever(
            this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                argThat { this.query.equals(ApplyExpendableEntitlementsToUserMutation.OPERATION_DOCUMENT) },
                any(),
                any(),
            ),
        ).thenAnswer {
            val graphqlResponse = JSONObject(
                """
                {
                    'applyExpendableEntitlementsToUser': {
                        '__typename': 'ExternalUserEntitlements',
                        'createdAtEpochMs': 1.0,
                        'updatedAtEpochMs': 2.0,
                        'version': 1.0,
                        'externalId': 'dummy_external_id',
                        'owner': 'dummy_owner',
                        'accountState': 'LOCKED',
                        'entitlementsSetName': null,
                        'entitlementsSequenceName': null,
                        'entitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'dummy_entitlement',
                                'description': 'dummy_description',
                                'value': 1.0
                            }
                        ],
                        'expendableEntitlements': [
                            {
                                '__typename': 'Entitlement',
                                'name': 'expendable_entitlement',
                                'description': 'expendable_description',
                                'value': 2.0
                            }
                        ],
                        'transitionsRelativeToEpochMs': null
                    }
                } 
                """.trimIndent(),
            )
            @Suppress("UNCHECKED_CAST")
            (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                GraphQLResponse(graphqlResponse.toString(), null),
            )
            mockOperation
        }

        val userEntitlements =
            this@SudoEntitlementsAdminClientUnitTest.client.applyExpendableEntitlementsToUser(
                "dummy_external_id",
                listOf(Entitlement("expendable_entitlement", "expendable_description", 2)),
                "request_id",
            )

        verify(this@SudoEntitlementsAdminClientUnitTest.apiCategory).mutate<String>(
            check {
                val input = it.variables["input"] as ApplyExpendableEntitlementsToUserInput?
                assertEquals("dummy_external_id", input?.externalId)
                val entitlement = input?.expendableEntitlements?.first()
                assertEquals("expendable_entitlement", entitlement?.name)
                assertEquals(Optional.Present("expendable_description"), entitlement?.description)
                assertEquals(2L, entitlement?.value?.toLong())
                assertEquals("request_id", input?.requestId)
            },
            any(),
            any(),
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
        val expendableEntitlement = userEntitlements.expendableEntitlements.first()
        assertEquals("expendable_entitlement", expendableEntitlement.name)
        assertEquals("expendable_description", expendableEntitlement.description)
        assertEquals(2, expendableEntitlement.value)
    }

    @Test
    fun testErrorHandling() = runBlocking {
        val mockOperation: GraphQLOperation<String> = mock()

        val expectedErrors = mapOf(
            "sudoplatform.DecodingError" to SudoEntitlementsAdminException.InvalidInputException().javaClass,
            "sudoplatform.InvalidArgumentError" to SudoEntitlementsAdminException.InvalidInputException().javaClass,
            "sudoplatform.LimitExceededError" to SudoEntitlementsAdminException.LimitExceededException().javaClass,
            "sudoplatform.NoEntitlementsError" to SudoEntitlementsAdminException.NoEntitlementsException().javaClass,
            "sudoplatform.ServiceError" to SudoEntitlementsAdminException.InternalServerException().javaClass,
            "sudoplatform.entitlements.AlreadyUpdatedError" to SudoEntitlementsAdminException.AlreadyUpdatedException().javaClass,
            "sudoplatform.entitlements.BulkOperationDuplicateUsersError" to SudoEntitlementsAdminException.BulkOperationDuplicateUsersException().javaClass,
            "sudoplatform.entitlements.DuplicateEntitlementError" to SudoEntitlementsAdminException.DuplicateEntitlementException().javaClass,
            "sudoplatform.entitlements.EntitlementsSetImmutableError" to SudoEntitlementsAdminException.EntitlementsSetImmutableException().javaClass,
            "sudoplatform.entitlements.EntitlementsSetInUseError" to SudoEntitlementsAdminException.EntitlementsSetInUseException().javaClass,
            "sudoplatform.entitlements.EntitlementsSetNotFoundError" to SudoEntitlementsAdminException.EntitlementsSetNotFoundException().javaClass,
            "sudoplatform.entitlements.EntitlementsSequenceAlreadyExistsError" to SudoEntitlementsAdminException.EntitlementsSequenceAlreadyExistsException().javaClass,
            "sudoplatform.entitlements.EntitlementsSequenceNotFoundError" to SudoEntitlementsAdminException.EntitlementsSequenceNotFoundException().javaClass,
            "sudoplatform.entitlements.EntitlementsSequenceUpdateInProgressError" to SudoEntitlementsAdminException.EntitlementsSequenceUpdateInProgressException().javaClass,
            "sudoplatform.entitlements.InvalidEntitlementsError" to SudoEntitlementsAdminException.InvalidEntitlementsException().javaClass,
            "sudoplatform.entitlements.NegativeEntitlementError" to SudoEntitlementsAdminException.NegativeEntitlementException().javaClass,
        )

        for (entry in expectedErrors) {
            whenever(
                this@SudoEntitlementsAdminClientUnitTest.apiCategory.mutate<String>(
                    argThat { this.query.equals(RemoveEntitlementsSetMutation.OPERATION_DOCUMENT) },
                    any(),
                    any(),
                ),
            ).thenAnswer {
                val error = GraphQLResponse.Error(entry.key, null, null, mapOf("errorType" to entry.key))

                @Suppress("UNCHECKED_CAST")
                (it.arguments[1] as Consumer<GraphQLResponse<String>>).accept(
                    GraphQLResponse(null, listOf(error)),
                )
                mockOperation
            }

            try {
                this@SudoEntitlementsAdminClientUnitTest.client.removeEntitlementsSet(
                    "dummy_name",
                )
            } catch (e: Throwable) {
                assertEquals(entry.key, e.javaClass, entry.value)
            }
        }

        return@runBlocking
    }
}
