package com.sudoplatform.sudoentitlementsadmin.types

import com.sudoplatform.sudoentitlementsadmin.SudoEntitlementsAdminException

sealed class UserEntitlementsResult {
    data class Success(val value: UserEntitlements) : UserEntitlementsResult()
    data class Failure(val error: SudoEntitlementsAdminException) : UserEntitlementsResult()
}
