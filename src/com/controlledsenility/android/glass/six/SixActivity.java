package com.controlledsenility.android.glass.six;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.google.android.glass.app.Card;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;
import com.google.android.glass.touchpad.GestureDetector.ScrollListener;

import de.yadrone.base.IARDrone;
import de.yadrone.base.command.DetectionType;
import de.yadrone.base.command.VisionTagType;
import de.yadrone.base.navdata.AttitudeListener;
import de.yadrone.base.navdata.BatteryListener;
import de.yadrone.base.navdata.ControlState;
import de.yadrone.base.navdata.DroneState;
import de.yadrone.base.navdata.StateListener;
import de.yadrone.base.navdata.TrackerData;
import de.yadrone.base.navdata.VisionData;
import de.yadrone.base.navdata.VisionListener;
import de.yadrone.base.navdata.VisionPerformance;
import de.yadrone.base.navdata.VisionTag;

public class SixActivity extends Activity {

	private IARDrone drone = null;
	private Card mCard;
	private GestureDetector mGestureDetector;
	private static boolean isEmergency = false;
	private static boolean isFlying = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		SixApplication app = (SixApplication) getApplication();
		drone = app.getARDrone();

		mCard = new Card(this);
		mCard.setText("Six");
		setContentView(mCard.getView());

		// always keep the screen on while this app is running
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// listen for gestures
		mGestureDetector = createGestureDetector(this);

		try {
			drone.start();
			VisionTagType[] types = { VisionTagType.ORIENTED_ROUNDEL };
			drone.getCommandManager().setDetectionType(
					DetectionType.HORIZONTAL, types);
			mCard.setFootnote("Initialized drone");
			setContentView(mCard.getView());
		} catch (Exception exc) {
			exc.printStackTrace();

			if (drone != null)
				drone.stop();
		}
	}

	private GestureDetector createGestureDetector(Context context) {

		GestureDetector gestureDetector = new GestureDetector(context);

		// Create a base listener for generic gestures
		gestureDetector.setBaseListener(new GestureDetector.BaseListener() {
			@Override
			public boolean onGesture(Gesture gesture) {
				if (gesture == Gesture.LONG_PRESS) {

					drone.getNavDataManager().addStateListener(
							new StateListener() {

								@Override
								public void stateChanged(DroneState state) {
									isEmergency = state.isEmergency();
									isFlying = state.isFlying();
								}

								@Override
								public void controlStateChanged(
										ControlState state) {
								}

							});

					drone.getNavDataManager().addAttitudeListener(
							new AttitudeListener() {

								@Override
								public void attitudeUpdated(float pitch,
										float roll, float yaw) {
								}

								@Override
								public void attitudeUpdated(float pitch,
										float roll) {
								}

								@Override
								public void windCompensation(float pitch,
										float roll) {
								}

							});

					drone.getNavDataManager().addVisionListener(
							new VisionListener() {

								@Override
								public void tagsDetected(VisionTag[] tags) {

									for (int i = 0; i < tags.length; i++) {
										Log.v(Constants.TAG,
												"Tags: " + tags[i].toString());
									}
								}

								@Override
								public void trackersSend(
										TrackerData trackersData) {
								}

								@Override
								public void receivedPerformanceData(
										VisionPerformance d) {
								}

								@Override
								public void receivedRawData(float[] vision_raw) {
								}

								@Override
								public void receivedData(VisionData d) {
								}

								@Override
								public void receivedVisionOf(float[] of_dx,
										float[] of_dy) {
								}

								@Override
								public void typeDetected(
										int detection_camera_type) {
								}

							});

					drone.getNavDataManager().addBatteryListener(
							new BatteryListener() {

								@Override
								public void batteryLevelChanged(
										final int percentage) {

									runOnUiThread(new Runnable() {

										@Override
										public void run() {
											mCard.setFootnote("Battery: "
													+ percentage);
											setContentView(mCard.getView());

										}
									});

								}

								@Override
								public void voltageChanged(int vbat_raw) {
								}

							});
					return true;
				} else if (gesture == Gesture.TAP) {

					// just ensure we never reset emergency flag when the drone
					// is in the air
					if (isEmergency && !isFlying) {
						drone.reset();
					}

					drone.takeOff();
				} else if (gesture == Gesture.TWO_TAP) {
					drone.landing();
				}
				return false;
			}
		});

		gestureDetector.setScrollListener(new ScrollListener() {

			@Override
			public boolean onScroll(float displacement, float delta,
					float velocity) {

				return false;
			}
		});

		return gestureDetector;
	}

	/*
	 * Send generic motion events to the gesture detector
	 */
	@Override
	public boolean onGenericMotionEvent(MotionEvent event) {
		if (mGestureDetector != null) {
			return mGestureDetector.onMotionEvent(event);
		}
		return false;
	}

}
