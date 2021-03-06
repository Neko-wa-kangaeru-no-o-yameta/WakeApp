package indi.hitszse2020g6.wakeapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import indi.hitszse2020g6.wakeapp.mainPage.MainPageEventList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        findPreference<Preference>("clear_event_table")?.setOnPreferenceClickListener {
            MainPageEventList.eventList.clear()
            GlobalScope.launch(Dispatchers.IO) {
                MainPageEventList.DAO.deleteAllEvents()
            }
            true
        }
        findPreference<Preference>("clear_course_table")?.setOnPreferenceClickListener {
            CourseList.deleteAllCourse()
            true
        }

        findPreference<Preference>("whiteList")?.setOnPreferenceClickListener{
            startActivity(Intent(context,ChooseWhiteListActivity::class.java))
            true
        }

        findPreference<Preference>("custom_theme")?.setOnPreferenceClickListener {
            startActivity(Intent(context,ChooseCustomTheme::class.java))
            true
        }

        findPreference<Preference>("setScheduleTime")?.setOnPreferenceClickListener {
            startActivity(Intent(context,ChooseScheduleTime::class.java))
            true
        }

        findPreference<Preference>("help_sec")?.setOnPreferenceClickListener {
            startActivity(Intent(context,HelpGuideActivity::class.java))
            true
        }
    }
}
