package utool.plugin.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * The observable object that is responsible for notifying observers of changes
 * @author Justin Kreier
 * @version 2/3/2013
 * @param <E> The type of object being observed
 */
public class Observable<E>{

	/**
	 * The objects observing the observable
	 */
	private List<Observer> observers;

	/**
	 * True if the object being observed has been changed since its last update
	 */
	private boolean hasChanged;

	/**
	 * The object being observed
	 */
	private E observableObject;

	/**
	 * Constructor
	 * @param observableObject The object being observed
	 */
	public Observable(E observableObject){
		this.observableObject = observableObject;
		observers = new ArrayList<Observer>();
		hasChanged = false;
	}

	/**
	 * Registers an observer
	 * @param o The new observer
	 */
	public void registerObserver(Observer o){
		observers.add(o);
		updateObserver(o);
	}

	/**
	 * Removes an observer
	 * @param o The observer to remove
	 */
	public void deleteObserver(Observer o){
		observers.remove(o);
	}

	/**
	 * Removes all observers
	 */
	public void deleteObservers(){
		observers.clear();
	}

	/**
	 * Updates all observers
	 */
	public void updateObservers(){
		if (hasChanged){
			hasChanged = false;
			for (int i = 0; i < observers.size(); i++){
				if(observers.get(i)!=null)
				{
					observers.get(i).updateObserver(observableObject);
				}
			}
		}
	}

	/**
	 * Updates a single observer
	 * @param o The observer to update
	 */
	public void updateObserver(Observer o){
		if(o!=null)
			o.updateObserver(observableObject);
	}

	/**
	 * Called when the Observable has changed
	 */
	public void notifyChanged(){
		hasChanged = true;
		updateObservers();
	}

}
