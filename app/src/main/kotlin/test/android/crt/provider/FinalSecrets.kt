package test.android.crt.provider

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.security.KeyChain
import test.android.crt.MainDeviceAdminReceiver
import java.io.InputStream
import java.security.KeyFactory
import java.security.PrivateKey
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.spec.PKCS8EncodedKeySpec

internal class FinalSecrets(
    private val context: Context,
    loggers: Loggers,
) : Secrets {
    private val logger = loggers.create("[Secrets]")

    override fun getUserCrt(alias: String): Certificate? {
        val dm = context.getSystemService(DevicePolicyManager::class.java)
        if (!dm.isDeviceOwnerApp(context.packageName)) error("Not device owner!")
        if (!dm.hasKeyPair(alias)) {
            logger.warning("No keys($alias)!")
            return null
        }
        if (KeyChain.getPrivateKey(context, alias) == null) {
            dm.grantKeyPairToApp(ComponentName(context, MainDeviceAdminReceiver::class.java), alias, context.packageName)
        }
        return KeyChain.getCertificateChain(context, alias)?.firstOrNull()
    }

    override fun setUserKey(alias: String, key: PrivateKey, crt: Certificate) {
        val dm = context.getSystemService(DevicePolicyManager::class.java)
        if (!dm.isDeviceOwnerApp(context.packageName)) error("Not device owner!")
        val isInstalled = dm.installKeyPair(ComponentName(context, MainDeviceAdminReceiver::class.java), key, crt, alias)
        if (!isInstalled) error("Keys($alias) were not installed!")
    }

    override fun deleteUserKey(alias: String) {
        val dm = context.getSystemService(DevicePolicyManager::class.java)
        if (!dm.isDeviceOwnerApp(context.packageName)) error("Not device owner!")
        val isRemoved = dm.removeKeyPair(ComponentName(context, MainDeviceAdminReceiver::class.java), alias)
        if (!isRemoved) error("Keys($alias) were not removed!")
    }

    override fun toCertificate(src: InputStream): Certificate {
        val cf = CertificateFactory.getInstance("X.509")
        return cf.generateCertificate(src)
    }

    override fun toPrivateKey(src: InputStream): PrivateKey {
        val kf = KeyFactory.getInstance("rsa")
        return kf.generatePrivate(PKCS8EncodedKeySpec(src.readBytes()))
    }
}
