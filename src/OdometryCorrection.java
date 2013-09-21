import lejos.nxt.*;
/* 
 * OdometryCorrection.java
 */

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private static final int LIGHT_THRESHOLD = 400;
	
	private static final int[] LINE_DISPLACEMENTS = { 15, 45, 75 };
	
	// distance from sensor to actual coordinates from odometer
	private static final double SENSOR_DISTANCE = 11.6;
	
	private final LightSensor lightSensor;
	private final Odometer odometer;
	
	boolean wasLine;
	
	// constructor
	public OdometryCorrection(Odometer odometer, LightSensor lightSensor) {
		this.odometer = odometer;
		this.lightSensor = lightSensor;
	}

	// run method (required for Thread)
	public void run() {
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();
			
			boolean isLine = lightSensor.readNormalizedValue() < LIGHT_THRESHOLD;
			if (isLine && !wasLine) applyCorrection();
			wasLine = isLine;

			// this ensure the odometry correction occurs only once every period
			correctionEnd = System.currentTimeMillis();
			if (correctionEnd - correctionStart < CORRECTION_PERIOD) {
				try {
					Thread.sleep(CORRECTION_PERIOD
							- (correctionEnd - correctionStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometry correction will be
					// interrupted by another thread
				}
			}
		}
	}
	
	private void applyCorrection() {
		double theta = odometer.getTheta();
				
		if (approximately(0, theta)) {
			double x = odometer.getX();
			if (x < LINE_DISPLACEMENTS[1]) {
				x = LINE_DISPLACEMENTS[0] + SENSOR_DISTANCE;
			} else if (x < LINE_DISPLACEMENTS[2]) {
				x = LINE_DISPLACEMENTS[1] + SENSOR_DISTANCE;
			} else {
				x = LINE_DISPLACEMENTS[2] + SENSOR_DISTANCE;
			}
			odometer.setX(x);
		} else if (approximately(Math.PI / 2, theta)) {
			double y = odometer.getY();
			if (y < LINE_DISPLACEMENTS[1]) {
				y = LINE_DISPLACEMENTS[0] + SENSOR_DISTANCE;
			} else if (y < LINE_DISPLACEMENTS[2]) {
				y = LINE_DISPLACEMENTS[1] + SENSOR_DISTANCE;
			} else {
				y = LINE_DISPLACEMENTS[2] + SENSOR_DISTANCE;
			}
			odometer.setY(y);
		} else if (approximately(Math.PI, theta)) {
			double x = odometer.getX();
			if (x < LINE_DISPLACEMENTS[0]) {
				x = LINE_DISPLACEMENTS[0] - SENSOR_DISTANCE;
			} else if (x < LINE_DISPLACEMENTS[1]) {
				x = LINE_DISPLACEMENTS[1] - SENSOR_DISTANCE;
			} else {
				x = LINE_DISPLACEMENTS[2] - SENSOR_DISTANCE;
			}
			odometer.setX(x);
		} else {
			double y = odometer.getY();
			if (y < LINE_DISPLACEMENTS[0]) {
				y = LINE_DISPLACEMENTS[0] - SENSOR_DISTANCE;
			} else if (y < LINE_DISPLACEMENTS[1]) {
				y = LINE_DISPLACEMENTS[1] - SENSOR_DISTANCE;
			} else {
				y = LINE_DISPLACEMENTS[2] - SENSOR_DISTANCE;
			}
			odometer.setY(y);
		}
	}
			
	private boolean approximately(double target, double actual) {
		return Math.abs(target - actual) < Math.PI / 4;
	}
}