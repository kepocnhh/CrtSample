package test.android.crt.provider

import java.io.InputStream
import java.security.PrivateKey
import java.security.cert.Certificate

internal interface Secrets {
    fun toCertificate(src: InputStream): Certificate
    fun toPrivateKey(src: InputStream): PrivateKey
    fun getCertificate(alias: String): Certificate?
    fun setCertificate(alias: String, key: PrivateKey, crt: Certificate)
    fun deleteKeys(alias: String)
}
