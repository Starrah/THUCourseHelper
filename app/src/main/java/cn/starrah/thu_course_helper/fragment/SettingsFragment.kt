package cn.starrah.thu_course_helper.fragment

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.*
import cn.starrah.thu_course_helper.R

class SettingsFragment: PreferenceFragmentCompat() {
    lateinit var sp: SharedPreferences
    lateinit var pre_sync_learnx: SwitchPreferenceCompat
    val spListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        if (key == "login_status") pre_sync_learnx.isEnabled = sp.getInt(key, 0) == 2
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        sp = preferenceManager.sharedPreferences

        val pre_login = findPreference<Preference>("login_status")!!
        pre_login.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            LoginDialog(requireActivity()).show()
            true
        }

        pre_sync_learnx = findPreference("sync_learnx_homeworks")!!
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