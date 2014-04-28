package com.controlledsenility.android.glass.six;

import android.app.Application;
import de.yadrone.base.ARDrone;
import de.yadrone.base.IARDrone;

public class SixApplication extends Application {

	/**
	 * The drone is kept in the application context so that all activities use
	 * the same drone instance
	 */
	private IARDrone drone;

	public void onCreate() {
		drone = new ARDrone("192.168.1.1"); // null because of missing
											// video support on Android
	}

	public IARDrone getARDrone() {
		return drone;
	}

}