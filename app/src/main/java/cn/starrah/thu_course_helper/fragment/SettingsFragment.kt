package cn.starrah.thu_course_helper.fragment

import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.onlinedata.thu.THUCourseDataSouce
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat() {
    lateinit var sp: SharedPreferences
    lateinit var pf_sync_learnx: SwitchPreferenceCompat
    lateinit var pf_login: Preference
    lateinit var pf_sync_xk: Preference
    val spListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        if (key == "login_status" || key == "login_force_update") {
            val login_status = sp.getInt("login_status", 0)
            pf_login.title =
                if (login_status == 0) resources.getString(R.string.settings_not_login)
                else sp.getString("login_name", "")
            pf_login.summary = when (login_status) {
                1    -> "已登录"
                2    -> "已登录，已保存密码"
                3    -> "登录态过期，请重新登录"
                else -> ""
            }
            pf_sync_learnx.isEnabled = login_status == 2
            pf_sync_learnx.summary = if (!pf_sync_learnx.isEnabled)
                resources.getString(R.string.errmsg_not_save_password) else ""

            pf_sync_xk.isEnabled = ((login_status == 1 && THUCourseDataSouce.isSessionValid) || login_status == 2)
            pf_sync_xk.summary = if (!pf_sync_xk.isEnabled) resources.getString(
                if (login_status != 0) R.string.errmsg_login_session_expired else R.string.errmsg_not_login
            ) else ""

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

        pf_sync_xk = findPreference("sync_XK_btn")!!
        pf_sync_xk.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            lifecycleScope.launch {
                if (sp.getInt("login_status", 0) == 2){
                    TODO("读取保存的密码，然后先login。")
                }
                THUCourseDataSouce.mergeAllCourseFromSchoolSystem(requireActivity())
                Toast.makeText(activity, R.string.sync_XK_success, Toast.LENGTH_SHORT).show()
            }
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