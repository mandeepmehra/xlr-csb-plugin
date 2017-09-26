package ext.deployit.releasehandler.csb;

import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.security.Permissions;
import com.xebialabs.xlrelease.api.XLReleaseServiceHolder;
import com.xebialabs.xlrelease.api.v1.ConfigurationApi;
import com.xebialabs.xlrelease.domain.Configuration;
import com.xebialabs.xlrelease.domain.Release;
import com.xebialabs.xlrelease.domain.status.ReleaseStatus;

import ext.deployit.releasehandler.csb.domain.CSBLogEntry;
import ext.deployit.releasehandler.csb.util.PingURL;
import ext.deployit.releasehandler.csb.util.ReleaseUtil;

public class CSBReleaseExporter implements ReleaseExporter {

	private final static int CSB_TIMEOUT_MS = 5000;
	private String CSB_URL;
	private String clientName;
	private String CSB_API_TOKEN;
	private final static String CSB_SUCCESS_RESPONSE = "{\"success\":true}";
	private final static String CSB_CONFIG_TYPE = "customerSuccessBox.Config";
	private final static String CSB_CONFIG_TITLE = "CSB";
	private final static String CSB_FEATURE_API = "/api/v1_1/feature";

	private static final Logger logger = LoggerFactory.getLogger(CSBLogEntry.class);

	public void intializeCSBParams() throws CSBLogException {
		final Configuration csbConfig = getCSBConfig();
		if (csbConfig == null) {
			throw new CSBLogException("customerSuccessBox.Config not initialized");
		}
		clientName = csbConfig.getProperty("customerName");
		CSB_URL = csbConfig.getProperty("url");
		CSB_API_TOKEN = csbConfig.getProperty("token");

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

	@Override
	public void exportRelease(final Release release) throws CSBLogException {

		intializeCSBParams();
		CSBLogEntry logEntry = getCSBLogEntryInstance(release);

		try {
			if (isCSBAPIReachable(CSB_URL, CSB_TIMEOUT_MS)) {
				sendToCSBWeb(logEntry);
			} else {
				sendToCSBFile(logEntry);
			}
		} catch (CSBLogException ex) {
			sendToCSBFile(logEntry);
		}
	}

	private boolean isCSBAPIReachable(String url, int timeout) {
		return PingURL.pingURL(url, timeout);
	}

	private void sendToCSBFile(final CSBLogEntry logEntry) {
		MDC.put("CSB_CLIENT_NAME", logEntry.getClientName());
		MDC.put("CSB_USER", logEntry.getUserId());
		MDC.put("CSB_PRODUCT", logEntry.getProduct());
		MDC.put("CSB_MODULE", logEntry.getModule());
		MDC.put("CSB_FEATURE", logEntry.getFeature());
		MDC.put("CSB_TIMESTAMP", logEntry.getTimestamp());
		logger.info("Release event logged to file");
	}

	private void sendToCSBWeb(final CSBLogEntry logEntry) throws CSBLogException {
		try {
			logger.debug("Sending Release event to CSB API : {}{}", CSB_URL, CSB_FEATURE_API);

			Client client = ClientBuilder.newClient();

			String payload = "{\"account_id\": \"" + logEntry.getClientName() + "\",  \"user_id\": \""
					+ logEntry.getUserId() + "\",  \"product_id\": \"" + logEntry.getProduct() + "\",  \"module_id\":\""
					+ logEntry.getModule() + "\",  \"feature_id\": \"" + logEntry.getFeature()
					+ "\",  \"timestamp\": \"" + logEntry.getTimestamp() + "\"}";

			logger.debug("CSB payload : {}", payload);

			Entity<String> payloadEntity = Entity.json(payload);
			Response response = client.target(CSB_URL + CSB_FEATURE_API).request(MediaType.APPLICATION_JSON_TYPE)
					.header("Authorization", "Bearer " + CSB_API_TOKEN).post(payloadEntity);

			String responseBody = response.readEntity(String.class);
			logger.debug("CSB response Status : {}, Body : {}", response.getStatus(), responseBody);

			if (response.getStatus() != 200)
				throw new CSBLogException("Invalid response code received : " + response.getStatus());
			else if (!responseBody.equalsIgnoreCase(CSB_SUCCESS_RESPONSE))
				throw new CSBLogException("Invalid response success message : " + responseBody);

		} catch (Exception ex) {
			throw new CSBLogException(ex.getMessage());
		}
	}

	public CSBLogEntry getCSBLogEntryInstance(final Release release) {
		CSBLogEntry csbLogEntry = new CSBLogEntry();

		String eventType = null;

		if (release.getStatus() == ReleaseStatus.PLANNED)
			eventType = "Release Initiated";
		else if (release.getStatus() == ReleaseStatus.COMPLETED)
			eventType = "Release Completed";
		else if (release.getStatus() == ReleaseStatus.ABORTED)
			eventType = "Release Aborted";
		else
			eventType = "UNKNOWN";

		csbLogEntry.setUserId(Permissions.getAuthenticatedUserName());

		csbLogEntry.setProduct("XL Release");
		csbLogEntry.setModule("Release");
		csbLogEntry.setFeature(eventType);
		csbLogEntry.setTimestamp(ReleaseUtil.getCurrentTimeStampInGMT());
		csbLogEntry.setClientName(clientName);

		return csbLogEntry;
	}

}
