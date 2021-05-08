/******************************************************************************
 *                                                                            *
 * Copyright (C) 2021 by nekohasekai <sekai@neko.services>                    *
 * Copyright (C) 2021 by Max Lv <max.c.lv@gmail.com>                          *
 * Copyright (C) 2021 by Mygod Studio <contact-shadowsocks-android@mygod.be>  *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 *  (at your option) any later version.                                       *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program. If not, see <http://www.gnu.org/licenses/>.       *
 *                                                                            *
 ******************************************************************************/

package io.nekohasekai.sagernet.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.SwitchPreference
import com.takisoft.preferencex.PreferenceFragmentCompat
import io.nekohasekai.sagernet.Key
import io.nekohasekai.sagernet.R
import io.nekohasekai.sagernet.bg.BaseService
import io.nekohasekai.sagernet.database.DataStore
import io.nekohasekai.sagernet.database.preference.EditTextPreferenceModifiers
import io.nekohasekai.sagernet.ktx.addOverScrollListener
import io.nekohasekai.sagernet.ktx.isExpert
import io.nekohasekai.sagernet.ktx.remove
import io.nekohasekai.sagernet.ktx.runOnMainDispatcher

class SettingsPreferenceFragment : PreferenceFragmentCompat() {

    private lateinit var isProxyApps: SwitchPreference
    private lateinit var listener: (BaseService.State) -> Unit

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addOverScrollListener(listView)
    }

    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = DataStore.configurationStore
        DataStore.initGlobal()
        addPreferencesFromResource(R.xml.global_preferences)
        val persistAcrossReboot = findPreference<SwitchPreference>(Key.PERSIST_ACROSS_REBOOT)!!
        val directBootAware = findPreference<SwitchPreference>(Key.DIRECT_BOOT_AWARE)!!
        val portSocks5 = findPreference<EditTextPreference>(Key.SOCKS_PORT)!!
        val speedInterval = findPreference<Preference>(Key.SPEED_INTERVAL)!!
        val serviceMode = findPreference<Preference>(Key.SERVICE_MODE)!!
        val allowAccess = findPreference<Preference>(Key.ALLOW_ACCESS)!!
        val requireHttp = findPreference<SwitchPreference>(Key.REQUIRE_HTTP)!!
        val portHttp = findPreference<EditTextPreference>(Key.HTTP_PORT)!!
        val showStopButton = findPreference<SwitchPreference>(Key.SHOW_STOP_BUTTON)!!
        if (Build.VERSION.SDK_INT < 24) {
            showStopButton.isVisible = false
        }
        val securityAdvisory = findPreference<SwitchPreference>(Key.SECURITY_ADVISORY)!!
        val showDirectSpeed = findPreference<SwitchPreference>(Key.SHOW_DIRECT_SPEED)!!
        val ipv6Route = findPreference<Preference>(Key.IPV6_ROUTE)!!
        val preferIpv6 = findPreference<Preference>(Key.PREFER_IPV6)!!
        val domainStrategy = findPreference<Preference>(Key.DOMAIN_STRATEGY)!!
        val domainMatcher = findPreference<Preference>(Key.DOMAIN_MATCHER)!!
        domainMatcher.isVisible = isExpert

        val trafficSniffing = findPreference<Preference>(Key.TRAFFIC_SNIFFING)!!
        val enableMux = findPreference<Preference>(Key.ENABLE_MUX)!!
        val enableMuxForAll = findPreference<Preference>(Key.ENABLE_MUX_FOR_ALL)!!
        val muxConcurrency = findPreference<EditTextPreference>(Key.MUX_CONCURRENCY)!!
        val tcpKeepAliveInterval = findPreference<EditTextPreference>(Key.TCP_KEEP_ALIVE_INTERVAL)!!

        val bypassLan = findPreference<Preference>(Key.BYPASS_LAN)!!

        val forceShadowsocksRust =
            findPreference<SwitchPreference>(Key.FORCE_SHADOWSOCKS_RUST)!!
        forceShadowsocksRust.isVisible = isExpert

        val remoteDns = findPreference<Preference>(Key.REMOTE_DNS)!!
        val enableLocalDns = findPreference<SwitchPreference>(Key.ENABLE_LOCAL_DNS)!!
        val portLocalDns = findPreference<EditTextPreference>(Key.LOCAL_DNS_PORT)!!
        val domesticDns = findPreference<EditTextPreference>(Key.DOMESTIC_DNS)!!

        portLocalDns.setOnBindEditTextListener(EditTextPreferenceModifiers.Port)
        muxConcurrency.setOnBindEditTextListener(EditTextPreferenceModifiers.Port)
        portSocks5.setOnBindEditTextListener(EditTextPreferenceModifiers.Port)
        portHttp.setOnBindEditTextListener(EditTextPreferenceModifiers.Port)

        val currServiceMode = DataStore.serviceMode
        val metedNetwork = findPreference<Preference>(Key.METERED_NETWORK)!!
        if (Build.VERSION.SDK_INT >= 28) {
            metedNetwork.isEnabled = currServiceMode == Key.MODE_VPN
        } else {
            metedNetwork.remove()
        }
        isProxyApps = findPreference(Key.PROXY_APPS)!!
        isProxyApps.isEnabled = currServiceMode == Key.MODE_VPN
        isProxyApps.setOnPreferenceChangeListener { _, newValue ->
            startActivity(Intent(activity, AppManagerActivity::class.java))
            if (newValue as Boolean) DataStore.dirty = true
            newValue
        }

        listener = {
            val stopped = it == BaseService.State.Stopped
            val sMode = DataStore.serviceMode

            runOnMainDispatcher {
                persistAcrossReboot.isEnabled = stopped
                directBootAware.isEnabled = stopped
                serviceMode.isEnabled = stopped
                speedInterval.isEnabled = stopped
                portSocks5.isEnabled = stopped
                requireHttp.isEnabled = stopped
                portHttp.isEnabled = stopped
                showStopButton.isEnabled = stopped
                securityAdvisory.isEnabled = stopped
                showDirectSpeed.isEnabled = stopped
                domainStrategy.isEnabled = stopped
                domainMatcher.isEnabled = stopped
                trafficSniffing.isEnabled = stopped
                enableMux.isEnabled = stopped
                enableMuxForAll.isEnabled = stopped
                muxConcurrency.isEnabled = stopped
                tcpKeepAliveInterval.isEnabled = stopped
                bypassLan.isEnabled = stopped
                forceShadowsocksRust.isEnabled = stopped
                remoteDns.isEnabled = stopped
                enableLocalDns.isEnabled = stopped
                portLocalDns.isEnabled = stopped
                domesticDns.isEnabled = stopped
                ipv6Route.isEnabled = stopped
                preferIpv6.isEnabled = stopped
                allowAccess.isEnabled = stopped

                metedNetwork.isEnabled = sMode == Key.MODE_VPN && stopped
                isProxyApps.isEnabled = sMode == Key.MODE_VPN && stopped

            }
        }

    }

    override fun onResume() {
        super.onResume()

        if (::listener.isInitialized) {
            MainActivity.stateListener = listener
            listener((activity as MainActivity).state)
        }
        if (::isProxyApps.isInitialized) {
            isProxyApps.isChecked = DataStore.proxyApps
        }
    }

    override fun onDestroy() {
        if (MainActivity.stateListener == listener) {
            MainActivity.stateListener = null
        }
        super.onDestroy()
    }
}