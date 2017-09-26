package ext.deployit.releasehandler.csb;

import com.xebialabs.xlrelease.domain.Release;

public interface ReleaseExporter {

	// private static final Logger logger =
	// LoggerFactory.getLogger(ReleaseExporter.class);
	//
	// public void exportRelease(final Release release) {
	// MDC.put("CLIENT_NAME", "Freedom Mortgage");
	// logger.info("Release Initialized");
	// }

	public void exportRelease(final Release release);

}
