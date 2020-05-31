package cn.starrah.thu_course_helper.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import cn.starrah.thu_course_helper.R

class SettingsFragment : PreferenceFragmentCompat() {
    lateinit var sp: SharedPreferences
    lateinit var pf_sync_learnx: SwitchPreferenceCompat
    lateinit var pf_login: Preference
    val spListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        if (key == "login_status") {
            val login_status = sp.getInt(key, 0)
            pf_login.title =
                if (login_status == 0) resources.getString(R.string.settings_not_login)
                else sp.getString("login_name", "")
            pf_login.summary = when (login_status) {
                1 -> "已登录"
                2 -> "已登录，已保存密码"
                else -> ""
            }
            pf_sync_learnx.isEnabled = login_status == 2
            if (!pf_sync_learnx.isEnabled) pf_sync_learnx.summary = resources.getString(R.string.errmsg_open_sync_learnx)

        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        sp = preferenceManager.sharedPreferences

        pf_login = findPreference<Preference>("login_status")!!
        pf_login.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            LoginDialog(requireActivity()).show()
            true
        }

        pf_sync_learnx = findPreference("sync_learnx_homeworks")!!

        spListener.onSharedPreferenceChanged(sp, "login_status")

    }

    override fun onPause() {
        super.onPause()
        sp.unregisterOnSharedPreferenceChangeListener(spListener)
    }

    override fun onResume() {
        super.onResume()
        sp.registerOnSharedPreferenceChangeListener(spListener)
    }
}