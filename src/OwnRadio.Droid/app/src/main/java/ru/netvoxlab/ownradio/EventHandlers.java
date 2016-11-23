package ru.netvoxlab.ownradio;

/**
 * Created by a.polunina on 17.11.2016.
 */

public abstract class EventHandlers {

	public abstract void StatusChangedEventHandler (Object sender, EventHandlers eventHundlers );

	public abstract void BufferingEventHandler(Object sender, EventHandlers eventHundlers );

	public abstract void CoverReloadedEventHandler(Object sender, EventHandlers eventHundlers );

	public abstract void PlayingEventHandler(Object sender, EventHandlers eventHundlers );
}
