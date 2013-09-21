/*
 * SquareDriver.java
 */
import lejos.nxt.*;

public class SquareDriver {
	private static final int FORWARD_SPEED = 250;
	private static final int ROTATE_SPEED = 150;
	
	private final NXTRegulatedMotor leftMotor, rightMotor;
	
	
	// right and left radius are wheel radii, separation is the distance
	// between the middle of the left wheel to the middle of the right wheel. 
	private final double leftRadius, rightRadius, separation;
	
	public SquareDriver(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor, 
			double leftRadius, double rightRadius, double separation) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.leftRadius = leftRadius;
		this.rightRadius = rightRadius;
		this.separation = separation;
	}
	
	public void drive() {
		for (NXTRegulatedMotor motor : new NXTRegulatedMotor[] { leftMotor, rightMotor }) {
			motor.stop();
			motor.setAcceleration(3000);
		}
		
		// wait 2 seconds
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// there is nothing to be done here because it is not expected that
			// the odometer will be interrupted by another thread
		}

		for (int i = 0; i < 4; i++) {
			// drive forward two tiles
			leftMotor.setSpeed(FORWARD_SPEED);
			rightMotor.setSpeed(FORWARD_SPEED);

			leftMotor.rotate(convertDistance(leftRadius, 60.96), true);
			rightMotor.rotate(convertDistance(rightRadius, 60.96), false);

			// turn 90 degrees clockwise
			rotate(90.0);
		}
	}
	
	private void rotate(double degrees) {
		leftMotor.setSpeed(ROTATE_SPEED);
		rightMotor.setSpeed(ROTATE_SPEED);
		leftMotor.rotate(convertAngle(leftRadius, separation, degrees), true);
		rightMotor.rotate(-convertAngle(rightRadius, separation, degrees), false);
		
	}

	private int convertDistance(double radius, double distance) {
		return (int) ((180.0 * distance) / (Math.PI * radius));
	}
	
	private int convertAngle(double radius, double width, double angle) {
		return convertDistance(radius, Math.PI * width * angle / 360.0);
	}
}