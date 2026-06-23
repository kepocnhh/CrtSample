package test.android.crt.provider

import kotlinx.coroutines.flow.StateFlow

interface Admins {
    val owners: StateFlow<Boolean>

    fun update(isDeviceOwner: Boolean)
}
