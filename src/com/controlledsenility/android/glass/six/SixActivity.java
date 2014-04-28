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
import de.yadrone.base.navdata.CadType;
import de.yadrone.base.navdata.TrackerData;
import de.yadrone.base.navdata.VisionData;
import de.yadrone.base.navdata.VisionListener;
import de.yadrone.base.navdata.VisionPerformance;
import de.yadrone.base.navdata.VisionTag;

public class SixActivity extends Activity {

	private IARDrone drone = null;
	private Card mCard;
	private GestureDetector mGestureDetector;

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
			drone.getCommandManager().setDetectionType(
					CadType.MULTIPLE_DETECTION_MODE);
			drone.getCommandManager().setDetectionType(
					CadType.ORIENTED_COCARDE_BW);
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

					drone.getNavDataManager().addAttitudeListener(
							new AttitudeListener() {

								@Override
								public void attitudeUpdated(float pitch,
										float roll, float yaw) {
									// Log.v(Constants.TAG, pitch + " " + roll
									// + " " + yaw);

								}

								@Override
								public void attitudeUpdated(float pitch,
										float roll) {
									// Log.v(Constants.TAG, pitch + " " + roll);

								}

								@Override
								public void windCompensation(float pitch,
										float roll) {
									// TODO Auto-generated method stub

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
									// TODO Auto-generated method stub

								}

								@Override
								public void receivedPerformanceData(
										VisionPerformance d) {
									// TODO Auto-generated method stub

								}

								@Override
								public void receivedRawData(float[] vision_raw) {
									// TODO Auto-generated method stub

								}

								@Override
								public void receivedData(VisionData d) {
									// Log.v(Constants.TAG,
									// "State: " + d.getVisionState());

								}

								@Override
								public void receivedVisionOf(float[] of_dx,
										float[] of_dy) {
									// TODO Auto-generated method stub

								}

								@Override
								public void typeDetected(
										int detection_camera_type) {
									// Log.v(Constants.TAG, "Camera Type: "
									// + detection_camera_type);
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
									// TODO Auto-generated method stub

								}

							});
					return true;
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
