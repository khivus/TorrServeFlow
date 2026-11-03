package io.github.khivus.torrserveflow.ui.fragments.donate

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import io.github.khivus.torrserveflow.R
import io.github.khivus.torrserveflow.app.App
import io.github.khivus.torrserveflow.ui.fragments.TSFragment

class DonateFragment : TSFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val vi = inflater.inflate(R.layout.donate_fragment, container, false)

        vi.findViewById<Button>(R.id.btnBoosty)?.setOnClickListener {
            val link = "https://boosty.to/yourok"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivitySafely(intent)
        }

        vi.findViewById<Button>(R.id.btnYandex)?.setOnClickListener {
            val link = "https://yoomoney.ru/to/410013733697114"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivitySafely(intent)
        }

        vi.findViewById<Button>(R.id.btnTB)?.setOnClickListener {
            val link = "https://www.tbank.ru/cf/742qEMhKhKn"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(link))
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivitySafely(intent)
        }

        vi.findViewById<Button>(R.id.btnTelegram)?.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.addCategory(Intent.CATEGORY_BROWSABLE)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.data = Uri.parse("https://t.me/torrserve")
            startActivitySafely(intent)
        }

        vi.findViewById<ImageView>(R.id.ivTelegram)?.apply {
            alpha = 0.6f
            setOnFocusChangeListener { _, hasFocus ->
                alpha = if (hasFocus)
                    1.0f
                else
                    0.6f
            }
            setOnClickListener {
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.addCategory(Intent.CATEGORY_BROWSABLE)
                intent.data = Uri.parse("https://t.me/torrserve")
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivitySafely(intent)
            }
        }
        return vi
    }

    private fun startActivitySafely(intent: Intent): Boolean {
        try {
            if (intent.resolveActivity(App.context.packageManager) != null) {
                App.context.startActivity(intent)
                return true
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
        // Maybe will be added a Toast to notify user
        return false
    }

}
