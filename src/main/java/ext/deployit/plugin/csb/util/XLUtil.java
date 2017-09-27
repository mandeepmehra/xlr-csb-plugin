package ext.deployit.plugin.csb.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.xebialabs.deployit.plugin.api.reflect.Type;
import com.xebialabs.deployit.plugin.api.udm.ConfigurationItem;
import com.xebialabs.xlrelease.domain.Release;

public class XLUtil {


	public static boolean isConfigItemARelease(ConfigurationItem configItem) {
		boolean isRelease = false;

		if (configItem.getType().instanceOf(Type.valueOf("xlrelease.Release"))) {
			isRelease = true;
		}
		return isRelease;
	}

	public static Release getReleaseFromCIs(List<ConfigurationItem> configItems) {
		Release release = null;
		for (ConfigurationItem configurationItem : configItems) {
			if (isConfigItemARelease(configurationItem)) {
				release = (Release) configurationItem;
			}
		}
		return release;
	}


	public static String getCurrentTimeStampInGMT() {
		final Date currentTime = new Date();
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		return sdf.format(currentTime);
	}
}
