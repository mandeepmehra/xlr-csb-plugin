package ext.deployit.releasehandler.csb;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.List;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.xlrelease.domain.Release;

public abstract class ReleaseEventHandler {

	private static final Cache<String, Boolean> RELEASES_SEEN = CacheBuilder.newBuilder().maximumSize(1000)
			.expireAfterWrite(10, SECONDS).<String, Boolean>build();

	public abstract void handleReleaseEvent(List<ConfigurationItem> cis);
	
	public boolean releaseAlreadyHandled(final Release release) {
		boolean releaseSeen = false;
		if (RELEASES_SEEN.getIfPresent(release.getId()) != null) {
			releaseSeen = true;
		}
		return releaseSeen;
	}

	public void addToProcessedRelease(final Release release) {
		RELEASES_SEEN.put(release.getId(), true);
	}

}
