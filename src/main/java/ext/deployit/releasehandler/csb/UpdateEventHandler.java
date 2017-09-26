package ext.deployit.releasehandler.csb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.xlrelease.domain.Release;

import ext.deployit.releasehandler.csb.util.ReleaseUtil;

public class UpdateEventHandler extends ReleaseEventHandler {

	private static final Logger logger = LoggerFactory.getLogger(UpdateEventHandler.class);
	private final ReleaseExporter releaseExporter = new CSBReleaseExporter();

	@Override
	public void handleReleaseEvent(List<ConfigurationItem> cis) {
		final Release release = ReleaseUtil.getReleaseFromCIs(cis);

		if (release == null)
			return;

		if (releaseAlreadyHandled(release))
			return;

		if (release.getStatus().toString().equalsIgnoreCase("Aborted")
				|| release.getStatus().toString().equalsIgnoreCase("Completed")) {
			logger.info("Exporting updated release {}", release.getId());
			addToProcessedRelease(release);
			// TODO: Start in new thread
			releaseExporter.exportRelease(release);
		}
	}

}
