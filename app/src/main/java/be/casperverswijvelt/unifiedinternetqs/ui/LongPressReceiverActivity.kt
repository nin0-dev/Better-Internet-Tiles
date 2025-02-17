package be.casperverswijvelt.unifiedinternetqs.ui

import android.app.Activity
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import be.casperverswijvelt.unifiedinternetqs.tiles.InternetTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.MobileDataTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.NFCTileService
import be.casperverswijvelt.unifiedinternetqs.tiles.WifiTileService
import be.casperverswijvelt.unifiedinternetqs.util.getDataEnabled
import be.casperverswijvelt.unifiedinternetqs.util.getWifiEnabled


class LongPressReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val qsTile: ComponentName? =
            intent?.getParcelableExtra(Intent.EXTRA_COMPONENT_NAME)

        val intent = Intent(
            when (qsTile?.className) {
                InternetTileService::class.java.name -> {
                    when {
                        getDataEnabled(applicationContext) -> {
                            Settings.ACTION_NETWORK_OPERATOR_SETTINGS
                        }
                        getWifiEnabled(applicationContext) -> {
                            Settings.Panel.ACTION_WIFI
                        }
                        else -> {
                            Settings.Panel.ACTION_INTERNET_CONNECTIVITY
                        }
                    }
                }
                MobileDataTileService::class.java.name -> {
                    Settings.ACTION_NETWORK_OPERATOR_SETTINGS
                }
                WifiTileService::class.java.name -> {
                    Settings.Panel.ACTION_WIFI
                }
                NFCTileService::class.java.name -> {
                    Settings.ACTION_NFC_SETTINGS
                }
                else -> Settings.ACTION_WIRELESS_SETTINGS
            }
        )

        startActivity(intent)
        finish()
    }
}