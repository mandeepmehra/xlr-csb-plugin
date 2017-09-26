package ext.deployit.releasehandler.csb;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xebialabs.deployit.engine.spi.event.CisCreatedEvent;
import com.xebialabs.deployit.engine.spi.event.CisUpdatedEvent;
import com.xebialabs.deployit.engine.spi.event.DeployitEventListener;

import nl.javadude.t2bus.Subscribe;

@DeployitEventListener
public class ReleaseEventListener {

	private static final Logger logger = LoggerFactory.getLogger(ReleaseEventListener.class);
	private final CreateEventHandler createEventHandler = new CreateEventHandler();
	private final UpdateEventHandler updateEventHandler = new UpdateEventHandler();
	private static final ExecutorService EXECUTOR_SERVICE = Executors.newSingleThreadExecutor();

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
