package test.android.crt

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.cert.X509Certificate

internal class MainActivity : ComponentActivity() {
    private val providers = App.providers
    private val logger = providers.loggers.create("[Main]")
    private val userKeyAlias = "foo"

    private var _textOwner: TextView? = null
    private var _switchOwner: TextView? = null
    private var _textCrt: TextView? = null
    private var _addCrt: TextView? = null
    private var _deleteCrt: TextView? = null

    private suspend fun onRender() = withContext(providers.contexts.main) {
        val isDeviceOwner = providers.admins.owners.value
        val textOwner = _textOwner ?: TODO()
        val switchOwner = _switchOwner ?: TODO()
        val textCrt = _textCrt ?: TODO()
        val addCrt = _addCrt ?: TODO()
        val deleteCrt = _deleteCrt ?: TODO()
        textOwner.text = "owner: $isDeviceOwner"
        if (isDeviceOwner) {
            switchOwner.text = "remove admin"
            switchOwner.setOnClickListener { _ ->
                providers.admins.update(isDeviceOwner = false)
            }
            switchOwner.visibility = View.VISIBLE
            val crt = withContext(providers.contexts.default) {
                providers.secrets.getUserCrt(alias = userKeyAlias)
            }
            val text: String
            if (crt == null) {
                text = "no user crt $userKeyAlias"
                addCrt.visibility = View.VISIBLE
                deleteCrt.visibility = View.GONE
            } else {
                check(crt is X509Certificate)
                text = """
                    alias: $userKeyAlias
                    serial number: ${crt.serialNumber.toByteArray().toHexString()}
                """.trimIndent()
                addCrt.visibility = View.GONE
                deleteCrt.visibility = View.VISIBLE
            }
            textCrt.text = text
            textCrt.visibility = View.VISIBLE
        } else {
            switchOwner.visibility = View.GONE
            textCrt.visibility = View.GONE
            addCrt.visibility = View.GONE
            deleteCrt.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context: Context = this
        LinearLayout(context).also { root ->
            root.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
            )
            root.orientation = LinearLayout.VERTICAL
            root.gravity = Gravity.CENTER_VERTICAL
            _textOwner = TextView(context).also { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                root.addView(view)
            }
            _switchOwner = Button(context).also { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                root.addView(view)
            }
            _textCrt = TextView(context).also { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                root.addView(view)
            }
            _addCrt = Button(context).also { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                view.text = "add user key $userKeyAlias"
                view.setOnClickListener { _ ->
                    lifecycleScope.launch {
                        withContext(providers.contexts.default) {
                            runCatching {
                                val crt = providers.assets.open("ca.crt").use { src ->
                                    providers.secrets.toCertificate(src = src)
                                }
                                val key = providers.assets.open("ca.key").use { src ->
                                    providers.secrets.toPrivateKey(src = src)
                                }
                                providers.secrets.setUserKey(alias = userKeyAlias, key = key, crt = crt)
                            }
                        }.fold(
                            onSuccess = {
                                onRender()
                            },
                            onFailure = { error ->
                                logger.warning("add crt error: $error")
                            },
                        )
                    }
                }
                root.addView(view)
            }
            _deleteCrt = Button(context).also { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                view.text = "delete user key $userKeyAlias"
                view.setOnClickListener { _ ->
                    lifecycleScope.launch {
                        withContext(providers.contexts.default) {
                            runCatching {
                                providers.secrets.deleteUserKey(alias = userKeyAlias)
                            }
                        }.fold(
                            onSuccess = {
                                onRender()
                            },
                            onFailure = { error ->
                                logger.warning("delete crt error: $error")
                            },
                        )
                    }
                }
                root.addView(view)
            }
            lifecycleScope.launch {
                withContext(providers.contexts.default) {
                    providers.admins.owners.collect { _ ->
                        onRender()
                    }
                }
            }
            setContentView(root)
        }
    }
}
