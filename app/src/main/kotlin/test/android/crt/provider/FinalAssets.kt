package test.android.crt.provider

import android.content.Context
import java.io.InputStream

internal class FinalAssets(
    private val context: Context,
) : Assets {
    override fun open(name: String): InputStream {
        return context.assets.open(name)
    }
}
