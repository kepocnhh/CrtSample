package test.android.crt.provider

import java.security.cert.Certificate

internal interface Secrets {
    fun getCertificate(alias: String): Certificate?
}
