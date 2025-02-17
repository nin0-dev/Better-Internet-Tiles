package be.casperverswijvelt.unifiedinternetqs.ui

import android.app.AlertDialog
import android.app.StatusBarManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import be.casperverswijvelt.unifiedinternetqs.BuildConfig
import be.casperverswijvelt.unifiedinternetqs.R
import be.casperverswijvelt.unifiedinternetqs.tiles.InternetTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.MobileDataTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.NFCTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.WifiTileService
import be.casperverswijvelt.unifiedinternetqs.util.ShizukuUtil
import be.casperverswijvelt.unifiedinternetqs.util.getShellAccessRequiredDialog
import com.jakewharton.processphoenix.ProcessPhoenix
import com.topjohnwu.superuser.Shell

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?
    ) {
        setPreferencesFromResource(R.xml.preference, rootKey)

        findPreference<Preference>(resources.getString(R.string.app_info_key))?.summary =
            "${resources.getString(R.string.app_name)} version ${BuildConfig.VERSION_NAME}"
        findPreference<Preference>(resources.getString(R.string.language_key))?.summary =
            Resources.getSystem().configuration.locales[0].displayLanguage

        val settingsCategory = findPreference<PreferenceCategory>(
            resources.getString(R.string.settings_key)
        )

        // Hide some settings based on android version
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {

            // Hide language settings
            val localeSetting = findPreference<Preference>(
                resources.getString(R.string.language_key)
            )
            localeSetting?.let { settingsCategory?.removePreference(it) }

            // Hide tile-add buttons
            val addTileCategory = findPreference<PreferenceCategory>(
                resources.getString(R.string.add_tiles_key)
            )
            addTileCategory?.let { preferenceScreen.removePreference(it) }
        }
    }

    override fun onPreferenceTreeClick(preference: Preference): Boolean {

        context?.let { context ->
            when (preference.key) {
                resources.getString(R.string.request_shizuku_access_key) -> {
                    if (ShizukuUtil.shizukuAvailable) {
                        if (ShizukuUtil.hasShizukuPermission()) {
                            Toast.makeText(
                                context,
                                R.string.shizuku_access_granted,
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            ShizukuUtil.requestShizukuPermission { granted ->
                                if (granted) {
                                    Toast.makeText(
                                        context,
                                        R.string.shizuku_access_granted,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    getShellAccessRequiredDialog(context).show()
                                }
                            }
                        }
                    } else {
                        getShellAccessRequiredDialog(context).show()
                    }
                    return true
                }
                resources.getString(R.string.request_root_access_key) -> {
                    if (Shell.isAppGrantedRoot() == true) {
                        Toast.makeText(
                            context,
                            R.string.root_access_granted,
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        AlertDialog.Builder(context)
                            .setTitle(R.string.root_access_denied)
                            .setMessage(R.string.root_access_denied_description)
                            .create().show()
                    }


                    return true
                }
                resources.getString(R.string.restart_app_key) -> {
                    ProcessPhoenix.triggerRebirth(context)
                }
                resources.getString(R.string.app_info_key) -> {
                    val intent =
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    intent.data =
                        Uri.fromParts("package", context.packageName, null)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NO_HISTORY or
                                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                    startActivity(intent)
                }
                resources.getString(R.string.add_wifi_tile_key) -> {
                    addTile(
                        context,
                        WifiTileService::class.java,
                        R.string.wifi,
                        R.drawable.ic_baseline_signal_wifi_3_bar_24
                    )
                }
                resources.getString(R.string.add_mobile_data_tile_key) -> {
                    addTile(
                        context,
                        MobileDataTileService::class.java,
                        R.string.mobile_data,
                        R.drawable.ic_baseline_mobile_data_24
                    )
                }
                resources.getString(R.string.add_internet_tile_key) -> {
                    addTile(
                        context,
                        InternetTileService::class.java,
                        R.string.internet,
                        R.drawable.ic_baseline_public_24
                    )
                }
                resources.getString(R.string.add_nfc_tile_key) -> {
                    addTile(
                        context,
                        NFCTileService::class.java,
                        R.string.nfc,
                        R.drawable.baseline_nfc_24
                    )
                }
                resources.getString(R.string.language_key) -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val intent =
                            Intent(Settings.ACTION_APP_LOCALE_SETTINGS)
                        intent.data =
                            Uri.fromParts("package", context.packageName, null)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NO_HISTORY or
                                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                        startActivity(intent)
                    } else {
                        // Should not happen, language setting is hidden
                    }
                }
                else -> {}
            }
        }
        return super.onPreferenceTreeClick(preference)
    }

    private fun <T> addTile(
        context: Context,
        serviceClass: Class<T>,
        titleResourceId: Int,
        iconResourceId: Int
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSystemService(
                context,
                StatusBarManager::class.java
            )?.requestAddTileService(
                ComponentName(
                    context,
                    serviceClass,
                ),
                getString(titleResourceId),
                Icon.createWithResource(context, iconResourceId),
                {},
                {
                    Toast.makeText(
                        context,
                        "${getString(R.string.tile_added_error)} ($it)",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } else {
            // Should not happen, add-tile buttons are hidden
        }
    }
}