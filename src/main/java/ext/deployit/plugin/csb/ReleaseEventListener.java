package ext.deployit.plugin.csb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.engine.spi.event.CisCreatedEvent;
import com.xebialabs.deployit.engine.spi.event.CisUpdatedEvent;
import com.xebialabs.deployit.engine.spi.event.DeployitEventListener;

import ext.deployit.plugin.csb.eventhandler.CreateEventHandler;
import ext.deployit.plugin.csb.eventhandler.ReleaseEventHandler;
import ext.deployit.plugin.csb.eventhandler.UpdateEventHandler;
import nl.javadude.t2bus.Subscribe;

@DeployitEventListener
public class ReleaseEventListener {

	private static final Logger logger = LoggerFactory.getLogger(ReleaseEventListener.class);
	private final ReleaseEventHandler createEventHandler = new CreateEventHandler();
	private final ReleaseEventHandler updateEventHandler = new UpdateEventHandler();

	@Subscribe
	public void receiveCisCreated(CisCreatedEvent createEvent) {
		logger.trace("Received CI created event");
		createEventHandler.handleReleaseEvent(createEvent.getCis());
	}

	@Subscribe
	public void receiveCisUpdated(CisUpdatedEvent updateEvent) {
		logger.trace("Received CI updated event");
		updateEventHandler.handleReleaseEvent(updateEvent.getCis());
	}

}
