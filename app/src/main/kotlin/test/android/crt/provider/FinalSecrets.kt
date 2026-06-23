package test.android.crt.provider

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.security.KeyChain
import test.android.crt.MainDeviceAdminReceiver
import java.security.cert.Certificate

internal class FinalSecrets(
    private val context: Context,
    loggers: Loggers,
) : Secrets {
    private val logger = loggers.create("[Secrets]")

    override fun getCertificate(alias: String): Certificate? {
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
}
