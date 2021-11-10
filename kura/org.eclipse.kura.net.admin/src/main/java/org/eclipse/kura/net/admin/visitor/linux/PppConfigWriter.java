/*******************************************************************************
 * Copyright (c) 2011, 2021 Eurotech and/or its affiliates and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *  Eurotech
 *******************************************************************************/
package org.eclipse.kura.net.admin.visitor.linux;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.eclipse.kura.KuraException;
import org.eclipse.kura.KuraIOException;
import org.eclipse.kura.core.net.AbstractNetInterface;
import org.eclipse.kura.core.net.NetworkConfiguration;
import org.eclipse.kura.core.net.NetworkConfigurationVisitor;
import org.eclipse.kura.core.net.modem.ModemInterfaceConfigImpl;
import org.eclipse.kura.executor.CommandExecutorService;
import org.eclipse.kura.linux.net.modem.SupportedUsbModemInfo;
import org.eclipse.kura.linux.net.modem.SupportedUsbModemsInfo;
import org.eclipse.kura.net.NetConfig;
import org.eclipse.kura.net.NetInterfaceAddressConfig;
import org.eclipse.kura.net.NetInterfaceConfig;
import org.eclipse.kura.net.admin.modem.ModemPppConfigGenerator;
import org.eclipse.kura.net.admin.modem.PppPeer;
import org.eclipse.kura.net.admin.modem.SupportedUsbModemsFactoryInfo;
import org.eclipse.kura.net.admin.modem.SupportedUsbModemsFactoryInfo.UsbModemFactoryInfo;
import org.eclipse.kura.net.admin.util.LinuxFileUtil;
import org.eclipse.kura.net.admin.visitor.linux.util.ModemXchangeScript;
import org.eclipse.kura.net.modem.ModemConfig;
import org.eclipse.kura.usb.UsbDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PppConfigWriter implements NetworkConfigurationVisitor {

    private static final Logger logger = LoggerFactory.getLogger(PppConfigWriter.class);
    private static final String WRITING = "Writing {}";

    public static final String OS_PPP_DIRECTORY = "/etc/ppp/";
    public static final String OS_PEERS_DIRECTORY = OS_PPP_DIRECTORY + "peers/";
    public static final String OS_PPP_LOG_DIRECTORY = "/var/log/";
    public static final String OS_SCRIPTS_DIRECTORY = OS_PPP_DIRECTORY + "scripts/";
    public static final String DNS_DELIM = ",";

    public PppConfigWriter() {
        createSystemFolders();
    }

    protected void createSystemFolders() {
        File peersDir = new File(OS_PEERS_DIRECTORY);
        if (!peersDir.exists()) {
            if (peersDir.mkdirs()) {
                logger.debug("Created directory: {}", OS_PEERS_DIRECTORY);
            } else {
                logger.warn("Could not create peers directory: {}", OS_PEERS_DIRECTORY);
            }
        }

        File scriptsDir = new File(OS_SCRIPTS_DIRECTORY);
        if (!scriptsDir.exists()) {
            if (scriptsDir.mkdirs()) {
                logger.debug("Created directory: {}", OS_SCRIPTS_DIRECTORY);
            } else {
                logger.warn("Could not create scripts directory: {}", OS_SCRIPTS_DIRECTORY);
            }
        }
    }

    @Override
    public void setExecutorService(CommandExecutorService executorService) {
        // Not needed
    }

    @Override
    public void visit(NetworkConfiguration config) throws KuraException {
        List<NetInterfaceConfig<? extends NetInterfaceAddressConfig>> updatedNetInterfaceConfigs = config
                .getModifiedNetInterfaceConfigs();
        for (NetInterfaceConfig<? extends NetInterfaceAddressConfig> netInterfaceConfig : updatedNetInterfaceConfigs) {
            if (netInterfaceConfig instanceof ModemInterfaceConfigImpl) {
                writeConfig((ModemInterfaceConfigImpl) netInterfaceConfig);
            }
        }
    }

    private void writeConfig(ModemInterfaceConfigImpl modemInterfaceConfig) throws KuraException {
        String oldInterfaceName = modemInterfaceConfig.getName();
        String newInterfaceName = modemInterfaceConfig.getName();

        if (!((AbstractNetInterface<?>) modemInterfaceConfig).isInterfaceEnabled()) {
            logger.info("Network interface status for {} is {} - not overwriting hostapd configuration file",
                    oldInterfaceName, ((AbstractNetInterface<?>) modemInterfaceConfig).getInterfaceStatus());
            return;
        }

        ModemConfig modemConfig = getModemConfig(modemInterfaceConfig);

        // Use the ppp number for the interface name, if configured
        int pppNumber = getPppNumber(modemConfig);
        if (pppNumber >= 0) {
            newInterfaceName = "ppp" + pppNumber;
            modemInterfaceConfig.setName(newInterfaceName);
        }

        // Save the status and priority
        Class<? extends ModemPppConfigGenerator> configClass = null;
        UsbDevice usbDevice = modemInterfaceConfig.getUsbDevice();
        int baudRate = -1;
        if (usbDevice != null) {
            SupportedUsbModemInfo modemInfo = SupportedUsbModemsInfo.getModem(usbDevice);
            UsbModemFactoryInfo usbFactoryInfo = SupportedUsbModemsFactoryInfo.getModem(modemInfo);
            if (usbFactoryInfo != null) {
                configClass = usbFactoryInfo.getConfigGeneratorClass();
            }
            baudRate = 921600;
        }

        String pppPeerFilename = formPeerFilename(usbDevice);
        removeOldSymbolicLinks(oldInterfaceName, newInterfaceName, pppPeerFilename);

        if (configClass != null) {
            writePppConfigFiles(modemConfig, pppNumber, configClass, usbDevice, baudRate);
        }
    }

    private void writePppConfigFiles(ModemConfig modemConfig, int pppNumber,
            Class<? extends ModemPppConfigGenerator> configClass, UsbDevice usbDevice, int baudRate) {
        try {
            String pppPeerFilename = formPeerFilename(usbDevice);
            String pppLogfile = formPppLogFilename(usbDevice);
            String chatFilename = formChatFilename(usbDevice);
            String disconnectFilename = formDisconnectFilename(usbDevice);
            String chapAuthSecretsFilename = formChapAuthSecretsFilename();
            String papAuthSecretsFilename = formPapAuthSecretsFilename();
            ModemPppConfigGenerator scriptGenerator = configClass.newInstance();

            if (modemConfig != null) {
                logger.debug("Writing connect scripts for ppp{} using {}", pppNumber, configClass);

                logger.debug(WRITING, pppPeerFilename);
                PppPeer pppPeer = scriptGenerator.getPppPeer(getDeviceId(usbDevice), modemConfig, pppLogfile,
                        chatFilename, disconnectFilename);
                pppPeer.setBaudRate(baudRate);
                pppPeer.write(pppPeerFilename, chapAuthSecretsFilename, papAuthSecretsFilename);

                if (pppNumber >= 0) {
                    logger.debug("Linking peer file using ppp number: {}", pppNumber);
                    String symlinkFilename = formPeerLinkAbsoluteName(pppNumber);
                    LinuxFileUtil.createSymbolicLink(pppPeerFilename, symlinkFilename);
                } else {
                    logger.error("Can't create symbolic link to {}, invalid ppp number: {}", pppPeerFilename,
                            pppNumber);
                }

                logger.debug(WRITING, chatFilename);
                ModemXchangeScript connectScript = scriptGenerator.getConnectScript(modemConfig);
                connectScript.writeScript(chatFilename);

                logger.debug(WRITING, disconnectFilename);
                ModemXchangeScript disconnectScript = scriptGenerator.getDisconnectScript(modemConfig);
                disconnectScript.writeScript(disconnectFilename);
            } else {
                logger.error("Error writing connect scripts - modemConfig is null");
            }
        } catch (Exception e) {
            logger.error("Could not write modem config", e);
        }
    }

    private void removeOldSymbolicLinks(String oldInterfaceName, String newInterfaceName, String pppPeerFilename)
            throws KuraIOException {
        // Cleanup values associated with the old name if the interface name has changed
        if (!oldInterfaceName.equals(newInterfaceName)) {
            try {
                // Remove the old ppp peers symlink
                logger.debug("Removing old symlinks to {}", pppPeerFilename);
                removeSymbolicLinks(pppPeerFilename, OS_PEERS_DIRECTORY);
            } catch (IOException e) {
                throw new KuraIOException(e);
            }
        }
    }

    private ModemConfig getModemConfig(ModemInterfaceConfigImpl modemInterfaceConfig) {
        // Get the config
        ModemConfig modemConfig = null;
        List<NetConfig> netConfigs = modemInterfaceConfig.getNetConfigs();
        Optional<NetConfig> netConfig = netConfigs.stream().filter(ModemConfig.class::isInstance).findFirst();
        if (netConfig.isPresent()) {
            modemConfig = (ModemConfig) netConfig.get();
        }
        return modemConfig;
    }

    private int getPppNumber(ModemConfig modemConfig) {
        int pppNum = -1;
        if (modemConfig != null) {
            pppNum = modemConfig.getPppNumber();
        }
        return pppNum;
    }

    public String formPeerFilename(UsbDevice usbDevice) {
        StringBuilder buf = new StringBuilder();
        buf.append(OS_PEERS_DIRECTORY);
        buf.append(formBaseFilename(usbDevice));
        return buf.toString();
    }

    public String formPppLogFilename(UsbDevice usbDevice) {
        StringBuilder buf = new StringBuilder();
        buf.append(OS_PPP_LOG_DIRECTORY);
        buf.append("kura-");
        buf.append(formBaseFilename(usbDevice));
        return buf.toString();
    }

    public String formChatFilename(UsbDevice usbDevice) {
        StringBuilder buf = new StringBuilder();
        buf.append(OS_SCRIPTS_DIRECTORY);
        buf.append("chat");
        buf.append('_');
        buf.append(formBaseFilename(usbDevice));
        return buf.toString();
    }

    public String formPeerLinkName(int pppUnitNo) {
        StringBuilder peerLinkName = new StringBuilder();
        peerLinkName.append("ppp");
        peerLinkName.append(pppUnitNo);

        return peerLinkName.toString();
    }

    public String formPeerLinkAbsoluteName(int pppUnitNo) {
        StringBuilder peerLink = new StringBuilder();
        peerLink.append(OS_PEERS_DIRECTORY);
        peerLink.append(formPeerLinkName(pppUnitNo));
        return peerLink.toString();
    }

    public String formDisconnectFilename(UsbDevice usbDevice) {
        StringBuilder buf = new StringBuilder();
        buf.append(OS_SCRIPTS_DIRECTORY);
        buf.append("disconnect");
        buf.append('_');
        buf.append(formBaseFilename(usbDevice));
        return buf.toString();
    }

    public String formChapAuthSecretsFilename() {
        return OS_PPP_DIRECTORY + "chap-secrets";
    }

    public String formPapAuthSecretsFilename() {
        return OS_PPP_DIRECTORY + "pap-secrets";
    }

    private String formBaseFilename(UsbDevice usbDevice) {
        StringBuilder sb = new StringBuilder();

        if (usbDevice != null) {
            SupportedUsbModemInfo modemInfo = SupportedUsbModemsInfo.getModem(usbDevice);
            if (modemInfo != null) {
                sb.append(modemInfo.getDeviceName());
                sb.append('_');
                sb.append(usbDevice.getUsbPort());
            }
        }
        return sb.toString();
    }

    private String getDeviceId(UsbDevice usbDevice) {
        StringBuilder sb = new StringBuilder();
        if (usbDevice != null) {
            SupportedUsbModemInfo modemInfo = SupportedUsbModemsInfo.getModem(usbDevice);
            if (modemInfo != null) {
                sb.append(modemInfo.getDeviceName());
            }
        }

        return sb.toString();
    }

    // Delete all symbolic links to the specified target file in the specified
    // directory
    private void removeSymbolicLinks(String target, String directory) throws IOException {
        File targetFile = new File(target);
        File dir = new File(directory);
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.getAbsolutePath().equals(targetFile.getAbsolutePath())) {
                    // this is the target file
                    continue;
                }

                if (file.getCanonicalPath().equals(targetFile.getAbsolutePath())) {
                    logger.debug("Deleting {}", file.getAbsolutePath());
                    file.delete();
                }
            }
        }
    }
}
