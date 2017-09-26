package ext.deployit.releasehandler.csb;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.xlrelease.domain.Release;

import ext.deployit.releasehandler.csb.util.ReleaseUtil;

public class CreateEventHandler extends ReleaseEventHandler {

	private static final Logger logger = LoggerFactory.getLogger(CreateEventHandler.class);

	private final ReleaseExporter releaseExporter = new CSBReleaseExporter();

	@Override
	public void handleReleaseEvent(List<ConfigurationItem> cis) {

		final Release release = ReleaseUtil.getReleaseFromCIs(cis);

		if (release == null)
			return;

		if (releaseAlreadyHandled(release))
			return;

		logger.info("Exporting created release {}", release.getId());
		addToProcessedRelease(release);

		// TODO: Start in new thread
		try {
			releaseExporter.exportRelease(release);
		} catch (CSBLogException ex) {

		}

	}

}
