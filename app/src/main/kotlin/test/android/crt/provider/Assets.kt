package test.android.crt.provider

import java.io.InputStream

internal interface Assets {
    fun open(name: String): InputStream
}
