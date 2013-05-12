package utool.plugin.observer;

/**
 * Observer pattern observer interface
 * @author Justin Kreier
 * @version 1/26/2013
 */
public interface Observer {

	/**
	 * Called by the observable when an observer needs to be updated
	 * @param observedObject The object being observed
	 */
	public void updateObserver(Object observedObject);
}
