package test.android.crt.provider

import java.io.InputStream
import java.security.PrivateKey
import java.security.cert.Certificate

internal interface Secrets {
    fun toCertificate(src: InputStream): Certificate
    fun toPrivateKey(src: InputStream): PrivateKey
    //
    fun getUserCrt(alias: String): Certificate?
    fun setUserKey(alias: String, key: PrivateKey, crt: Certificate)
    fun deleteUserKey(alias: String)
    //
    fun getCaCrt(serialNumber: ByteArray): Certificate?
    fun setCaCrt(crt: Certificate)
}
