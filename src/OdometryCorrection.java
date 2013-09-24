import lejos.nxt.*;
/* 
 * OdometryCorrection.java
 */

public class OdometryCorrection extends Thread {
	private static final long CORRECTION_PERIOD = 10;
	private static final int LIGHT_THRESHOLD = 400;	
	
	private static final double CLOSE_LINE = 15.0;
	private static final double FAR_LINE = 45.0;
	private static final double LINE_SEPARATION = FAR_LINE - CLOSE_LINE;
	
	private static final double SENSOR_DISTANCE = 11.6;
	
	private final LightSensor lightSensor;
	private final Odometer odometer;
	
	private boolean frozen = false;
	private boolean wasLine = false;
	// x, y, theta of last line crossing
	private double[] previous;
	private double[] current;
	
	// constructor
	public OdometryCorrection(Odometer odometer, LightSensor lightSensor) {
		this.odometer = odometer;
		this.lightSensor = lightSensor;
	}

	// run method (required for Thread)
	public void run() {
		Sound.setVolume(Sound.VOL_MAX);
		long correctionStart, correctionEnd;

		while (true) {
			correctionStart = System.currentTimeMillis();
			
			if (!frozen) {
				boolean isLine = lightSensor.readNormalizedValue() < LIGHT_THRESHOLD;
				if (isLine && !wasLine) {
					Sound.twoBeeps();
					savePosition();
					correctOdometer();
				}
				wasLine = isLine;
			}
			
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
	
	public void freeze() {
		frozen = true;
	}
	
	public void unfreeze() {
		previous = null;
		current = null;
		frozen = false;
	}
	
	private void savePosition() {
		if (current != null) previous = current.clone();
		current = odometer.getPosition();
	}
		
	private void correctOdometer() {
		double x, y, theta = current[2];

		if (approximately(0, theta)) {
			x = outboundLine() + SENSOR_DISTANCE;
			y = deviationDisplacement(1, 0.0);
		} else if (approximately(Math.PI / 2, theta)) {
			x = deviationDisplacement(0, Math.PI / 2);
			y = - outboundLine() - SENSOR_DISTANCE;
		} else if (approximately(Math.PI, theta)) {
  			x = inboundLine() - SENSOR_DISTANCE;
  			y = deviationDisplacement(1, Math.PI);
  		} else {
			x = deviationDisplacement(0, 3 * Math.PI / 2);
			y = - inboundLine() + SENSOR_DISTANCE;
  		}
		
		double dx = x - current[0];
		double dy = y - current[1];
		
		if (dx != 0) odometer.setX(odometer.getX() + dx);
		if (dy != 0) odometer.setY(odometer.getY() + dy);
	}
	
	private double deviationDisplacement(int xOrY, double expected) {
		if (isFirstLine() || !wasStraightPath()) {
			return current[xOrY];
		} else {
			return previous[xOrY] + deviationDistance(expected);
		}
	}
	
	private double deviationDistance(double expected) {
		return Math.tan(averageTheta() - expected) * LINE_SEPARATION;
	}
	
	private double averageTheta() {
		double angleSum = previous[2] + current[2];
		if (Math.abs(previous[2] - current[2]) > Math.PI) {
			angleSum += Math.PI * 2;
		}
		return angleSum / 2.0;
	}
	
	private boolean wasStraightPath() {
		return Math.abs(previous[2] - current[2]) < (Math.PI * 2) / 1500.0;
	}
			
	private boolean approximately(double target, double actual) {
		return Math.abs(target - actual) < Math.PI / 4;
	}
	
	private double outboundLine() {
		return isFirstLine() ? CLOSE_LINE : FAR_LINE;
	}
	
	private double inboundLine() {
		return isFirstLine() ? FAR_LINE : CLOSE_LINE;
	}
	
	private boolean isFirstLine() {
		return previous == null;
	}
}