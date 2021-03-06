/*
 * Lab2.java
 */
import lejos.nxt.*;

public class Lab2 {
	private static NXTRegulatedMotor LEFT_MOTOR = Motor.A, RIGHT_MOTOR = Motor.B;
	private static double WHEEL_RADIUS = 2.8;
	private static double WHEEL_SEPARATION = 16.0;
	
	public static void main(String[] args) {
		int buttonChoice;

		// some objects that need to be instantiated
		Odometer odometer = new Odometer(LEFT_MOTOR, RIGHT_MOTOR, WHEEL_RADIUS, WHEEL_SEPARATION);
		OdometryDisplay odometryDisplay = new OdometryDisplay(odometer);
		LightSensor lightSensor = new LightSensor(SensorPort.S2);
		OdometryCorrection odometryCorrection = new OdometryCorrection(odometer, lightSensor);
		final SquareDriver driver = new SquareDriver(LEFT_MOTOR, RIGHT_MOTOR, WHEEL_RADIUS, 
				WHEEL_SEPARATION, odometryCorrection);

		do {
			// clear the display
			LCD.clear();

			// ask the user whether the motors should drive in a square or float
			LCD.drawString("< Left | Right >", 0, 0);
			LCD.drawString("       |        ", 0, 1);
			LCD.drawString(" Float | Drive  ", 0, 2);
			LCD.drawString("motors | in a   ", 0, 3);
			LCD.drawString("       | square ", 0, 4);

			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {
			for (NXTRegulatedMotor motor : new NXTRegulatedMotor[] { LEFT_MOTOR, RIGHT_MOTOR }) {
				motor.forward();
				motor.flt();
			}

			// start only the odometer and the odometry display
			odometer.start();
			odometryDisplay.start();
		} else {
			// start the odometer, the odometry display and (possibly) the
			// odometry correction
			odometer.start();
			odometryDisplay.start();
			odometryCorrection.start();

			// spawn a new Thread to avoid SquareDriver.drive() from blocking
			(new Thread() {
				public void run() {
					driver.drive();
				}
			}).start();
		}
		
		while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		System.exit(0);
	}
}