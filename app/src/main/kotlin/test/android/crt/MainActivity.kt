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
    //
    private var _textUserCrt: TextView? = null
    private var _addUserCrt: TextView? = null
    private var _deleteUserCrt: TextView? = null
    //
    private var _textCaCrt: TextView? = null
    private var _addCaCrt: TextView? = null
    private var _deleteCaCrt: TextView? = null

    private fun getSerialNumber(): ByteArray {
        val crt = providers.assets.open("ca.crt").use { src ->
            providers.secrets.toCertificate(src = src)
        }
        check(crt is X509Certificate)
        return crt.serialNumber.toByteArray()
    }

    private suspend fun onRender() = withContext(providers.contexts.main) {
        val isDeviceOwner = providers.admins.owners.value
        //
        val textOwner = _textOwner ?: TODO()
        val switchOwner = _switchOwner ?: TODO()
        val textUserCrt = _textUserCrt ?: TODO()
        val addUserCrt = _addUserCrt ?: TODO()
        val deleteUserCrt = _deleteUserCrt ?: TODO()
        //
        val textCaCrt = _textCaCrt ?: TODO()
        val addCaCrt = _addCaCrt ?: TODO()
        val deleteCaCrt = _deleteCaCrt ?: TODO()
        //
        textOwner.text = "owner: $isDeviceOwner"
        if (isDeviceOwner) {
            switchOwner.text = "remove admin"
            switchOwner.setOnClickListener { _ ->
                providers.admins.update(isDeviceOwner = false)
            }
            switchOwner.visibility = View.VISIBLE
            //
            val userCrt = withContext(providers.contexts.default) {
                providers.secrets.getUserCrt(alias = userKeyAlias)
            }
            val userText: String
            if (userCrt is X509Certificate) {
                userText = """
                    alias: $userKeyAlias
                    serial number: ${userCrt.serialNumber.toByteArray().toHexString()}
                """.trimIndent()
                addUserCrt.visibility = View.GONE
                deleteUserCrt.visibility = View.VISIBLE
            } else {
                userText = "no user crt $userKeyAlias"
                addUserCrt.visibility = View.VISIBLE
                deleteUserCrt.visibility = View.GONE
            }
            textUserCrt.text = userText
            textUserCrt.visibility = View.VISIBLE
            //
            val serialNumber = withContext(providers.contexts.default) {
                getSerialNumber()
            }
            val caCrt = withContext(providers.contexts.default) {
                providers.secrets.getCaCrt(serialNumber = serialNumber)
            }
            val caText: String
            if (caCrt is X509Certificate) {
                caText = """
                    ca
                    serial number: ${serialNumber.toHexString()}
                """.trimIndent()
                addCaCrt.visibility = View.GONE
                deleteCaCrt.text = "delete ca crt ${serialNumber.toHexString()}"
                deleteCaCrt.visibility = View.VISIBLE
            } else {
                caText = "no ca crt ${serialNumber.toHexString()}"
                addCaCrt.text = "add ca crt ${serialNumber.toHexString()}"
                addCaCrt.visibility = View.VISIBLE
                deleteCaCrt.visibility = View.GONE
            }
            textCaCrt.text = caText
            textCaCrt.visibility = View.VISIBLE
        } else {
            switchOwner.visibility = View.GONE
            //
            textUserCrt.visibility = View.GONE
            addUserCrt.visibility = View.GONE
            deleteUserCrt.visibility = View.GONE
            //
            textCaCrt.visibility = View.GONE
            addCaCrt.visibility = View.GONE
            deleteCaCrt.visibility = View.GONE
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
            //
            _textUserCrt = TextView(context).also { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                root.addView(view)
            }
            _addUserCrt = Button(context).also { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                view.text = "add user key $userKeyAlias"
                view.setOnClickListener { _ ->
                    lifecycleScope.launch {
                        withContext(providers.contexts.default) {
                            runCatching {
                                val crt = providers.assets.open("foo.crt").use { src ->
                                    providers.secrets.toCertificate(src = src)
                                }
                                val key = providers.assets.open("foo.key").use { src ->
                                    providers.secrets.toPrivateKey(src = src)
                                }
                                providers.secrets.setUserKey(alias = userKeyAlias, key = key, crt = crt)
                            }
                        }.fold(
                            onSuccess = {
                                onRender()
                            },
                            onFailure = { error ->
                                logger.warning("add user key error: $error")
                            },
                        )
                    }
                }
                root.addView(view)
            }
            _deleteUserCrt = Button(context).also { view ->
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
            //
            _textCaCrt = TextView(context).also { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                root.addView(view)
            }
            _addCaCrt = Button(context).also { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                view.setOnClickListener { _ ->
                    lifecycleScope.launch {
                        withContext(providers.contexts.default) {
                            runCatching {
                                val crt = providers.assets.open("ca.crt").use { src ->
                                    providers.secrets.toCertificate(src = src)
                                }
                                providers.secrets.setCaCrt(crt = crt)
                            }
                        }.fold(
                            onSuccess = {
                                onRender()
                            },
                            onFailure = { error ->
                                logger.warning("add ca crt error: $error")
                            },
                        )
                    }
                }
                root.addView(view)
            }
            _deleteCaCrt = Button(context).also { view ->
                view.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                view.setOnClickListener { _ ->
                    lifecycleScope.launch {
                        withContext(providers.contexts.default) {
                            runCatching {
                                val crt = providers.assets.open("ca.crt").use { src ->
                                    providers.secrets.toCertificate(src = src)
                                }
                                providers.secrets.deleteCaCrt(crt = crt)
                            }
                        }.fold(
                            onSuccess = {
                                onRender()
                            },
                            onFailure = { error ->
                                logger.warning("delete ca crt error: $error")
                            },
                        )
                    }
                }
                root.addView(view)
            }
            //
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
