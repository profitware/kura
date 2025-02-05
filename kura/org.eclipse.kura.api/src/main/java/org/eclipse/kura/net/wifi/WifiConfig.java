/*******************************************************************************
 * Copyright (c) 2011, 2022 Eurotech and/or its affiliates and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *  Eurotech
 *  Sterwen-Technology
 ******************************************************************************/
package org.eclipse.kura.net.wifi;

import java.util.Arrays;
import java.util.List;

import org.eclipse.kura.configuration.Password;
import org.eclipse.kura.net.NetConfig;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Configuration for a wifi interface based on IPv4 addresses.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
@ProviderType
public class WifiConfig implements NetConfig {

    /** Mode for the configuration **/
    private WifiMode mode;

    /** SSID of the the wifi interface **/
    private String ssid;

    /** Channel(s) supported by the wifi interface **/
    private int[] channels;

    /** Security mode of the interface **/
    private WifiSecurity security;

    /** Supported pairwise ciphers **/
    private WifiCiphers pairwiseCiphers;

    /** Supported group ciphers **/
    private WifiCiphers groupCiphers;

    /** The passkey for the wifi interface **/
    private Password passkey;

    /** The hardware mode **/
    private String hwMode;

    /** Radio mode **/
    private WifiRadioMode radioMode;

    /** Background scan **/
    private WifiBgscan bgscan;

    /** Ping Access Point **/
    private boolean pingAccessPoint = false;

    /** Ignore SSID **/
    private boolean ignoreSSID;

    /** The driver of the wifi interface **/
    private String driver;

    /** Channels Frequencies **/
    List<WifiChannel> channelFrequencies;

    /** Wifi Country Code **/
    private String wifiCountryCode;

    public WifiConfig() {
        super();
    }

    /**
     * @deprecated since 2.3. Use {@link WifiConfig(WifiMode mode, String ssid, int[] channels, WifiSecurity security,
     *             String passkey, String hwMode, WifiBgscan bgscan)}
     */
    @SuppressWarnings("checkstyle:parameterNumber")
    @Deprecated
    public WifiConfig(WifiMode mode, String ssid, int[] channels, WifiSecurity security, String passkey, String hwMode,
            boolean broadcast, WifiBgscan bgscan) {
        this(mode, ssid, channels, security, passkey, hwMode, bgscan);
    }

    /**
     * @since 2.3
     * 
     * @param mode
     *            for the configuration as {@link WifiMode}
     * @param ssid
     *            of the the wifi interface
     * @param channels
     *            supported by the wifi interface
     * @param security
     *            mode of the interface as {@link WifiSecurity}
     * @param passkey
     *            for the wifi interface
     * @param hwMode
     *            the hardware mode
     * @param bgscan
     *            the background scan
     */
    public WifiConfig(WifiMode mode, String ssid, int[] channels, WifiSecurity security, String passkey, String hwMode,
            WifiBgscan bgscan) {
        super();

        this.mode = mode;
        this.ssid = ssid;
        this.channels = channels;
        this.security = security;
        this.passkey = new Password(passkey);
        this.hwMode = hwMode;
        this.bgscan = bgscan;
        this.channelFrequencies = null;
        this.wifiCountryCode = null;
    }

    public WifiMode getMode() {
        return this.mode;
    }

    public void setMode(WifiMode mode) {
        this.mode = mode;
    }

    public String getSSID() {
        return this.ssid;
    }

    public void setSSID(String ssid) {
        this.ssid = ssid;
    }

    public String getDriver() {
        return this.driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public int[] getChannels() {
        return this.channels;
    }

    public void setChannels(int[] channels) {
        this.channels = channels;
    }

    public WifiSecurity getSecurity() {
        return this.security;
    }

    public void setSecurity(WifiSecurity security) {
        this.security = security;
    }

    public WifiCiphers getPairwiseCiphers() {
        return this.pairwiseCiphers;
    }

    public void setPairwiseCiphers(WifiCiphers pairwise) {
        this.pairwiseCiphers = pairwise;
    }

    public WifiCiphers getGroupCiphers() {
        return this.groupCiphers;
    }

    public void setGroupCiphers(WifiCiphers group) {
        this.groupCiphers = group;
    }

    public Password getPasskey() {
        return this.passkey;
    }

    public void setPasskey(String key) {
        if (key != null) {
            Password psswd = new Password(key);
            this.passkey = psswd;
        }
    }

    public String getHardwareMode() {
        return this.hwMode;
    }

    public void setHardwareMode(String hwMode) {
        this.hwMode = hwMode;
    }

    public WifiRadioMode getRadioMode() {
        return this.radioMode;
    }

    public void setRadioMode(WifiRadioMode radioMode) {
        this.radioMode = radioMode;
    }

    /**
     * @deprecated since 2.3
     */
    @Deprecated
    public boolean getBroadcast() {
        return false;
    }

    /**
     * @deprecated since 2.3
     */
    @Deprecated
    public void setBroadcast(boolean broadcast) {
        // Do nothing...
    }

    public WifiBgscan getBgscan() {
        return this.bgscan;
    }

    public void setBgscan(WifiBgscan bgscan) {
        this.bgscan = bgscan;
    }

    public boolean pingAccessPoint() {
        return this.pingAccessPoint;
    }

    public void setPingAccessPoint(boolean pingAP) {
        this.pingAccessPoint = pingAP;
    }

    public boolean ignoreSSID() {
        return this.ignoreSSID;
    }

    public void setIgnoreSSID(boolean ignoreSSID) {
        this.ignoreSSID = ignoreSSID;
    }

    /**
     * Get the Wi-Fi country code
     * 
     * @return Wi-Fi country code in ISO 3166-1 alpha-2
     * @since 2.2
     */
    public String getWifiCountryCode() {
        return wifiCountryCode;
    }

    /**
     * Set the Wi-Fi country code
     * 
     * @param wifiCountryCode
     *            Wi-Fi Country code in ISO 3166-1 alpha-2 format
     * @since 2.2
     */
    public void setWifiCountryCode(String wifiCountryCode) {
        this.wifiCountryCode = wifiCountryCode;
    }

    /**
     * Get the list of Wi-Fi channels and frequencies
     * 
     * @return List of Wi-Fi channels and frequencies
     * @since 2.2
     */
    public List<WifiChannel> getChannelFrequencies() {
        return channelFrequencies;
    }

    /**
     * Set the list of channel frequencies
     * 
     * @param channelFrequencies
     *            list of Wi-Fi channels and relative frequencies
     * @since 2.2
     */
    public void setChannelFrequencies(List<WifiChannel> channelFrequencies) {
        this.channelFrequencies = channelFrequencies;
    }

    @Override
    public int hashCode() {
        final int prime = 29;
        int result = super.hashCode();

        result = prime * result + (this.mode == null ? 0 : this.mode.hashCode());
        result = prime * result + (this.ssid == null ? 0 : this.ssid.hashCode());
        result = prime * result + (this.driver == null ? 0 : this.driver.hashCode());
        result = prime * result + (this.wifiCountryCode == null ? 0 : this.wifiCountryCode.hashCode());

        if (this.channels != null) {
            for (int channel : this.channels) {
                result = prime * result + channel;
            }
        } else {
            result = prime * result;
        }

        if (this.channelFrequencies != null) {
            for (WifiChannel wc : this.channelFrequencies) {
                result = prime * result + wc.hashCode();
            }
        } else {
            result = prime * result;
        }

        result = prime * result + (this.security == null ? 0 : this.security.hashCode());
        result = prime * result + (this.passkey == null ? 0 : this.passkey.hashCode());
        result = prime * result + (this.hwMode == null ? 0 : this.hwMode.hashCode());
        result = prime * result + (this.radioMode == null ? 0 : this.radioMode.hashCode());

        result = prime * result + (this.pairwiseCiphers == null ? 0 : WifiCiphers.getCode(this.pairwiseCiphers));

        result = prime * result + (this.groupCiphers == null ? 0 : WifiCiphers.getCode(this.groupCiphers));

        if (this.bgscan != null) {

            result = prime * result
                    + (this.bgscan.getModule() == null ? 0 : WifiBgscanModule.getCode(this.bgscan.getModule()));

            result = prime * result + this.bgscan.getRssiThreshold();

            result = prime * result + this.bgscan.getShortInterval();

            result = prime * result + this.bgscan.getLongInterval();
        } else {
            result = prime * result;
        }

        result = prime * result + (this.pingAccessPoint ? 1 : 0);

        result = prime * result + (this.ignoreSSID ? 1 : 0);

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof WifiConfig)) {
            return false;
        }

        WifiConfig other = (WifiConfig) obj;

        if (!compare(this.mode, other.mode)) {
            return false;
        }
        if (!compare(this.ssid, other.ssid)) {
            return false;
        }
        if (!compare(this.driver, other.driver)) {
            return false;
        }
        if (!Arrays.equals(this.channels, other.channels)) {
            return false;
        }
        if (!compare(this.wifiCountryCode, other.wifiCountryCode)) {
            return false;
        }
        if (!compare(this.security, other.security)) {
            return false;
        }
        if (!compare(this.pairwiseCiphers, other.pairwiseCiphers)) {
            return false;
        }
        if (!compare(this.groupCiphers, other.groupCiphers)) {
            return false;
        }
        if (!compare(this.passkey.toString(), other.passkey.toString())) {
            return false;
        }
        if (!compare(this.hwMode, other.hwMode)) {
            return false;
        }
        if (!compare(this.radioMode, other.radioMode)) {
            return false;
        }
        if (!compare(this.bgscan, other.bgscan)) {
            return false;
        }
        if (this.pingAccessPoint != other.pingAccessPoint()) {
            return false;
        }
        if (this.ignoreSSID != other.ignoreSSID()) {
            return false;
        }
        return true;
    }

    private boolean compare(Object obj1, Object obj2) {
        return obj1 == null ? obj2 == null : obj1.equals(obj2);
    }

    @Override
    public boolean isValid() {
        return this.mode != null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WifiConfig [");
        if (this.mode != null) {
            sb.append("mode: ").append(this.mode).append(" :: ");
        }
        if (this.ssid != null) {
            sb.append("ssid: ").append(this.ssid).append(" :: ");
        }
        sb.append("ignoreSSID: ").append(this.ignoreSSID).append(" :: ");

        if (this.driver != null) {
            sb.append("driver: ").append(this.driver).append(" :: ");
        }
        if (this.channels != null && this.channels.length > 0) {
            sb.append("channels: ");
            for (int i = 0; i < this.channels.length; i++) {
                sb.append(this.channels[i]);
                if (i + i < this.channels.length) {
                    sb.append(",");
                }
            }
            sb.append(" :: ");
        }
        if (this.security != null) {
            sb.append("security: ").append(this.security).append(" :: ");
        }
        if (this.pairwiseCiphers != null) {
            sb.append("pairwiseCiphers: ").append(this.pairwiseCiphers).append(" :: ");
        }
        if (this.groupCiphers != null) {
            sb.append("groupCiphers: ").append(this.groupCiphers).append(" :: ");
        }
        if (this.passkey != null) {
            sb.append("passkey: ").append(this.passkey).append(" :: ");
        }
        if (this.hwMode != null) {
            sb.append("hwMode: ").append(this.hwMode).append(" :: ");
        }
        if (this.radioMode != null) {
            sb.append("radioMode: ").append(this.radioMode).append(" :: ");
        }
        if (this.bgscan != null) {
            sb.append("bgscan: ").append(this.bgscan).append(" :: ");
        }
        if (this.wifiCountryCode != null) {
            sb.append("countryCode: ").append(this.wifiCountryCode).append(" :: ");
        }
        if (this.channelFrequencies != null) {
            sb.append("channelFrequencies: ");
            int i = 0;
            for (WifiChannel wc : this.channelFrequencies) {
                sb.append(wc);
                if (i++ != this.channelFrequencies.size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
