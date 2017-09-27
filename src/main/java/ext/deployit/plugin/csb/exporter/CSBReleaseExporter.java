package ext.deployit.plugin.csb.exporter;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.security.Permissions;
import com.xebialabs.xlrelease.api.XLReleaseServiceHolder;
import com.xebialabs.xlrelease.api.v1.ConfigurationApi;
import com.xebialabs.xlrelease.domain.Configuration;
import com.xebialabs.xlrelease.domain.Release;
import com.xebialabs.xlrelease.domain.status.ReleaseStatus;

import ext.deployit.plugin.csb.domain.CSBConfiguration;
import ext.deployit.plugin.csb.domain.CSBLogEntry;
import ext.deployit.plugin.csb.exception.CSBPluginException;
import ext.deployit.plugin.csb.util.PingURL;
import ext.deployit.plugin.csb.util.XLUtil;

public class CSBReleaseExporter {

	private final static int CSB_TIMEOUT_MS = 5000;
	private final static String CSB_CONFIG_TYPE = "customerSuccessBox.Config";
	private final static String CSB_CONFIG_TITLE = "CSB";
	private final static String AUTH_TOKEN = "authToken";
	private final static String CUSTOMER_NAME = "customerName";
	private final static String API_URL = "url";

	private static final Logger logger = LoggerFactory.getLogger(CSBReleaseExporter.class);

	private CSBWebExporter csbWebExporter = new CSBWebExporter();
	private CSBFileExporter csbFileExporter = new CSBFileExporter();

	public CSBConfiguration intializeCSBConfiguration() throws CSBPluginException {

		CSBConfiguration csbConfiguration = new CSBConfiguration();

		final Configuration csbConfig = getCSBConfig();
		if (csbConfig == null) {
			throw new CSBPluginException("customerSuccessBox.Config not initialized");
		}
		csbConfiguration.setClientName(csbConfig.getProperty(CUSTOMER_NAME));
		csbConfiguration.setUrl(csbConfig.getProperty(API_URL));
		csbConfiguration.setAuthToken(csbConfig.getProperty(AUTH_TOKEN));
		return csbConfiguration;
	}

	public Configuration getCSBConfig() {
		final ConfigurationApi configurationApi = XLReleaseServiceHolder.getConfigurationApi();
		List<Configuration> configurations = configurationApi.searchByTypeAndTitle(CSB_CONFIG_TYPE, CSB_CONFIG_TITLE);
		Configuration csbConfig = null;
		for (Configuration config : configurations) {
			csbConfig = config;
			break;
		}
		return csbConfig;
	}

	public void exportRelease(final Release release) throws CSBPluginException {

		CSBConfiguration csbConfiguration = intializeCSBConfiguration();
		CSBLogEntry logEntry = getCSBLogEntryInstance(csbConfiguration, release);

		try {
			if (isCSBAPIReachable(csbConfiguration.getUrl(), CSB_TIMEOUT_MS)) {
				sendToCSBWeb(csbConfiguration, logEntry);
			} else {
				sendToCSBFile(csbConfiguration, logEntry);
			}
		} catch (CSBPluginException ex) {
			logger.info("API not reachable, logging release event to file");
			sendToCSBFile(csbConfiguration, logEntry);
		}
	}

	private boolean isCSBAPIReachable(String url, int timeout) {
		return PingURL.pingURL(url, timeout);
	}

	private void sendToCSBFile(final CSBConfiguration csbConfiguration, final CSBLogEntry logEntry) {
		logger.info("Logging release event to file");
		csbFileExporter.logEntry(logEntry);
	}

	private void sendToCSBWeb(final CSBConfiguration csbConfiguration, final CSBLogEntry logEntry)
			throws CSBPluginException {

		logger.info("Sending Release event to CSB API : {}", csbConfiguration.getUrl());
		csbWebExporter.logEntry(csbConfiguration, logEntry);
	}

	public CSBLogEntry getCSBLogEntryInstance(final CSBConfiguration csbConfiguration, final Release release)
			throws CSBPluginException {
		CSBLogEntry csbLogEntry = new CSBLogEntry();

		String eventType = null;

		if (release.getStatus() == ReleaseStatus.PLANNED)
			eventType = "Release Initiated";
		else if (release.getStatus() == ReleaseStatus.COMPLETED)
			eventType = "Release Completed";
		else if (release.getStatus() == ReleaseStatus.ABORTED)
			eventType = "Release Aborted";
		else
			throw new CSBPluginException("Unkown release status : " + release.getStatus());

		csbLogEntry.setUserId(Permissions.getAuthenticatedUserName());

		csbLogEntry.setProduct("XL Release");
		csbLogEntry.setModule("Release");
		csbLogEntry.setFeature(eventType);
		csbLogEntry.setTimestamp(XLUtil.getCurrentTimeStampInGMT());
		csbLogEntry.setClientName(csbConfiguration.getClientName());

		return csbLogEntry;
	}

}
