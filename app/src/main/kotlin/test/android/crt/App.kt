package test.android.crt

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import test.android.crt.provider.Admins
import test.android.crt.provider.Contexts
import test.android.crt.provider.FinalAdmins
import test.android.crt.provider.FinalLoggers
import test.android.crt.provider.FinalSecrets
import test.android.crt.provider.Loggers
import test.android.crt.provider.Providers
import test.android.crt.provider.Secrets

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val context: Context = this
        val loggers: Loggers = FinalLoggers()
        val contexts = Contexts(
            main = Dispatchers.Main,
            default = Dispatchers.Default,
        )
        val job = SupervisorJob()
        val coroutineScope = CoroutineScope(contexts.main + job)
        val admins: Admins = FinalAdmins(
            context = context,
            loggers = loggers,
        )
        val secrets: Secrets = FinalSecrets(
            context = context,
            loggers = loggers,
        )
        _providers = Providers(
            loggers = loggers,
            contexts = contexts,
            secrets = secrets,
            admins = admins,
        )
    }

    companion object {
        private var _providers: Providers? = null
        val providers: Providers get() = checkNotNull(_providers) { "No providers!" }
    }
}
