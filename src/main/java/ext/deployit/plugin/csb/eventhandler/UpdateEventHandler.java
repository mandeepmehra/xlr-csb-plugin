package ext.deployit.plugin.csb.eventhandler;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.xlrelease.domain.Release;

import ext.deployit.plugin.csb.exception.CSBPluginException;
import ext.deployit.plugin.csb.exporter.CSBReleaseExporter;
import ext.deployit.plugin.csb.util.XLUtil;

public class UpdateEventHandler extends ReleaseEventHandler {

	private static final Logger logger = LoggerFactory.getLogger(UpdateEventHandler.class);
	private final CSBReleaseExporter releaseExporter = new CSBReleaseExporter();
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

	@Override
	public void handleReleaseEvent(List<ConfigurationItem> cis) {
		final Release release = XLUtil.getReleaseFromCIs(cis);

		if (release == null)
			return;

		if (releaseAlreadyHandled(release))
			return;

		if (release.getStatus().toString().equalsIgnoreCase("Aborted")
				|| release.getStatus().toString().equalsIgnoreCase("Completed")) {
			logger.info("Exporting updated release {}", release.getId());
			addToProcessedRelease(release);
			
			// TODO: Start in new thread
			try {
				releaseExporter.exportRelease(release);
			} catch (CSBPluginException ex) {
				logger.error("Error in exporting release", ex);
			}
		}
	}

}
