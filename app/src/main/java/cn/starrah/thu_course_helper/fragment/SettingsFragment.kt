package cn.starrah.thu_course_helper.fragment

import android.app.AlertDialog
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import androidx.preference.*
import cn.starrah.thu_course_helper.R
import cn.starrah.thu_course_helper.data.database.CREP
import cn.starrah.thu_course_helper.onlinedata.backend.*
import cn.starrah.thu_course_helper.utils.setLastSyncExamDate
import cn.starrah.thu_course_helper.utils.setLastSyncHomeworkDatetime
import cn.starrah.thu_course_helper.utils.shouldSyncExam
import cn.starrah.thu_course_helper.utils.startDownloadIntent
import com.alibaba.fastjson.JSON
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class SettingsFragment : PreferenceFragmentCompat() {
    lateinit var sp: SharedPreferences
    lateinit var pf_sync_hmex: MultiSelectListPreference
    lateinit var pf_login: Preference
    lateinit var pf_sync_xk: Preference
    lateinit var pf_term: ListPreference
    lateinit var pf_check_version: Preference
    lateinit var pf_feedback: Preference
    lateinit var pf_backup: ListPreference
    val spListener = SharedPreferences.OnSharedPreferenceChangeListener { sp, key ->
        if (key == "login_status" || key == "login_force_update") {
            val login_status = sp.getInt("login_status", 0)
            pf_login.title =
                if (login_status == 0) resources.getString(R.string.status_not_login)
                else sp.getString("login_name", "")
            pf_login.summary = when (login_status) {
                1    -> resources.getString(R.string.status_login)
                2    -> resources.getString(R.string.status_login_save)
                4    -> resources.getString(R.string.status_login_session_expire)
                else -> ""
            }

            pf_sync_hmex.isEnabled = login_status == 2
            pf_sync_hmex.summary = if (!pf_sync_hmex.isEnabled)
                resources.getString(R.string.errmsg_not_save_password)
            else ""

            pf_sync_xk.isEnabled =
                ((login_status == 1 || login_status == 2) && CREP.onlineCourseDataSource?.isSessionValid == true)
            pf_sync_xk.summary = if (!pf_sync_xk.isEnabled) resources.getString(
                when (login_status) {
                    1    -> R.string.errmsg_login_no_saved_password
                    2, 4 -> R.string.errmsg_login_session_expired
                    else -> R.string.errmsg_not_login
                }
            )
            else ""

        }
        if (key == "term_id") {
            val value = sp.getString("term_id", "")
            val currentTerm = if (CREP.initialized) CREP.term else null
            if (value != currentTerm?.termId) {
                lifecycleScope.launch {
                    try {
                        Toast.makeText(
                            activity,
                            R.string.info_change_term_process,
                            Toast.LENGTH_SHORT
                        ).show()
                        val resp = BackendAPITermData(value)
                        CREP.initializeTerm(requireActivity(), resp.termData)
                        sp.edit {
                            putString("currentTerm", JSON.toJSONString(resp.termData))
                            putString(
                                "lastHandChangeTermTime", LocalDate.now().format(
                                    DateTimeFormatter.ISO_DATE
                                )
                            )
                        }
                        pf_term.title =
                            (if (CREP.initialized) CREP.term else null)?.chineseName ?: ""
                        Toast.makeText(
                            activity,
                            R.string.info_change_term_success,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    catch (e: Exception) {
                        //操作失败，回滚修改操作
                        pf_term.value = currentTerm?.termId
                        Toast.makeText(
                            activity,
                            R.string.errmsg_change_term_fail,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    @Suppress("USELESS_CAST", "UNCHECKED_CAST")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        sp = preferenceManager.sharedPreferences

        pf_login = findPreference<Preference>("login_status")!!
        pf_login.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val the_dialog = LoginDialog(requireActivity())
            the_dialog.show()

            true
        }

        pf_sync_xk = findPreference("sync_XK_btn")!!
        pf_sync_xk.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            lifecycleScope.launch {
                Toast.makeText(activity, R.string.sync_XK_process, Toast.LENGTH_SHORT).show()
                val onlineData = CREP.onlineCourseDataSource?.loadAllCourses(
                    CREP.term,
                    mapOf("context" to requireActivity())
                )
                onlineData?.let { CREP.onlineCourseDataSource?.applyLoadedCourses(onlineData) }
                Toast.makeText(activity, R.string.sync_XK_success, Toast.LENGTH_SHORT).show()
            }
            true
        }

        pf_sync_hmex = findPreference("sync_hmex")!!
        pf_sync_hmex.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { pref, newValue ->
                lifecycleScope.launch {
                    if (resources.getString(R.string.settings_open_sync_homework) in newValue as Set<String> &&
                        sp.getInt("login_status", 0) == 2) {
                        sp.edit(commit = true) {
                            remove("lastSyncHWTime")
                        }
                        CREP.onlineCourseDataSource!!.loadData(
                            CREP.term,
                            mapOf(
                                "homework" to true,
                                "activity" to requireActivity(),
                                "username" to sp.getString("login_name", null)!!,
                                "password" to CREP.getUserPassword(requireActivity()),
                                "onlyUnsubmitted" to true,
                                "apply" to true
                            )
                        )
                        setLastSyncHomeworkDatetime(requireActivity())
                    }
                    else {
                        // 删除现存的所有作业数据
                        CREP.deleteItems(CREP.helper_findHomeworkItems())
                    }
                    if (resources.getString(R.string.settings_open_sync_exam) in newValue as Set<String> &&
                        sp.getInt("login_status", 0) == 2) {
                        sp.edit(commit = true) {
                            remove("lastSyncExamTime")
                        }
                        CREP.onlineCourseDataSource!!.loadData(
                            CREP.term, mapOf(
                                "exam" to true,
                                "username" to sp.getString("login_name", null)!!,
                                "password" to CREP.getUserPassword(requireActivity()),
                                "apply" to true
                            )
                        )
                        setLastSyncExamDate(requireActivity())
                    }
                    //暂时规定如果取消自动同步期末考试，期末考试数据也不会自动删除吧。
                }
                true
            }

        pf_term = findPreference("term_id")!!
        pf_term.title = (if (CREP.initialized) CREP.term else null)?.chineseName ?: ""
        val termsList =
            JSON.parseArray(sp.getString("available_terms", "[]"), TermDescription::class.java)
        pf_term.entries = termsList.map { it.name }.toTypedArray()
        pf_term.entryValues = termsList.map { it.id }.toTypedArray()

        pf_check_version = findPreference("check_version")!!
        val cvsum1 = resources.getString(R.string.settings_found_new_version_1)
        val cvsum2 = resources.getString(R.string.settings_found_new_version_2)
        val currentVersion = requireActivity().packageManager.getPackageInfo(
            requireActivity().packageName,
            0
        ).versionName!!
        val latestVersion = sp.getString("latest_version", null)
        val latestVersionUrl = sp.getString("latest_version_url", null)

        // 首先检查sharedPreference中是否有新版本的信息、是否当前版本已经不是最新
        if (latestVersion != null && latestVersion != currentVersion && latestVersionUrl != null) {
            // 如果是的话那么就设置summary提醒文字；
            pf_check_version.summary = "${cvsum1}${latestVersion}${cvsum2}"
        }
        // 否则，提醒文字留空

        pf_check_version.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            lifecycleScope.launch {
                // 需要先访问后端获取数据
                val resp = BackendAPICheckVersion()
                sp.edit {
                    putString("latest_version", resp.versionName)
                    putString("latest_version_url", resp.url)
                }
                if (resp.versionName != currentVersion) {
                    // 发现新版本，就设置提醒文字，同时开启下载
                    pf_check_version.summary = "${cvsum1}${latestVersion}${cvsum2}"
                    startDownloadIntent(
                        requireActivity(),
                        Uri.parse(sp.getString("latest_version_url", null))
                    )
                }
                else {
                    // 否则设置提醒文字“已是最新版本”
                    pf_check_version.summary =
                        resources.getString(R.string.settings_no_found_new_version)
                }
            }
            true
        }

        pf_feedback = findPreference("dull_feedback")!!
        pf_feedback.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            true
        }

        pf_backup = findPreference("dull_backup")!!
        pf_backup.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { pf, newValue ->
                if (newValue == resources.getString(R.string.backup_download_title)) {
                    AlertDialog.Builder(activity).setTitle(R.string.warning)
                        .setMessage(R.string.remind_download_warning)
                        .setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }
                        .setPositiveButton(R.string.confirm) { dialog, which ->
                            dialog.dismiss()
                            lifecycleScope.launch {
                                try {
                                    BackendAPIDownloadMyData(
                                        requireActivity(),
                                        CREP.onlineCourseDataSource!!.doSomething(
                                            "VPNCookie",
                                            sp.getString("login_name", null)!!,
                                            CREP.getUserPassword(requireActivity())
                                        )
                                    )
                                    Toast.makeText(
                                        activity,
                                        R.string.remind_backup_download_success,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                catch (e: Exception) {
                                    Toast.makeText(
                                        activity,
                                        e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .show()
                }
                else if (newValue == resources.getString(R.string.backup_upload_title)) {
                    AlertDialog.Builder(activity).setTitle(R.string.warning)
                        .setMessage(R.string.remind_upload_warning)
                        .setNegativeButton(R.string.cancel) { dialog, which -> dialog.cancel() }
                        .setPositiveButton(R.string.confirm) { dialog, which ->
                            dialog.dismiss()
                            lifecycleScope.launch {
                                try {
                                    BackendAPIUploadMyData(
                                        requireActivity(),
                                        CREP.onlineCourseDataSource!!.doSomething(
                                            "VPNCookie",
                                            sp.getString("login_name", null)!!,
                                            CREP.getUserPassword(requireActivity())
                                        )
                                    )
                                    Toast.makeText(
                                        activity,
                                        R.string.remind_backup_upload_success,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                catch (e: Exception) {
                                    Toast.makeText(
                                        activity,
                                        e.message,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                        .show()
                }
                false
            }

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