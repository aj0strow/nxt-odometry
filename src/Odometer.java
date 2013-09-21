import lejos.nxt.NXTRegulatedMotor;

/*
*  the Odometer is programmed for theta = 0.0 rad corresponding to the
*  + x axis and for the angle to increase to 2π going counter-clockwise. 
*/
	  

public class Odometer extends Thread {
	// odometer update period, in ms
	private static final long ODOMETER_PERIOD = 25;
	
	// x and y in centimeters (cm), theta in radians
	private double x, y, theta;
		
	// the wheel radius and separation between the middle of the wheels
	private double radius, separation;
	
	// the previous tacho meter counts
	private double leftCount, rightCount;	
	
	// motors
	private final NXTRegulatedMotor leftMotor, rightMotor;

	// lock object for mutual exclusion
	private Object lock;

	// default constructor
	public Odometer(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor, double radius, double separation) {
		this(leftMotor, rightMotor, radius, separation, 0.0, 0.0, 90.0);
	}
	
	// pass in the start position (x, y) and angle (theta)
	public Odometer(NXTRegulatedMotor leftMotor, NXTRegulatedMotor rightMotor, double radius,
			double separation, double x, double y, double theta) {
		this.leftMotor = leftMotor;
		this.rightMotor = rightMotor;
		this.radius = radius;
		this.separation = separation;
		this.x = x;
		this.y = y;
		this.theta = theta;
		lock = new Object();
	}

	// run method (required for Thread)
	public void run() {
		long updateStart, updateEnd;

		while (true) {
			updateStart = System.currentTimeMillis();
			
			double newLeftCount = leftMotor.getTachoCount();
			double newRightCount = rightMotor.getTachoCount();
						
			double deltaLeftCount = newLeftCount - leftCount;
			double deltaRightCount = newRightCount - rightCount;
			
			leftCount = newLeftCount;
			rightCount = newRightCount;
			
			double leftArcDistance = deltaLeftCount * radius;
			double rightArcDistance = deltaRightCount * radius;
			
			double deltaTheta = (leftArcDistance - rightArcDistance) / separation;
			double displacement = (leftArcDistance + rightArcDistance) / 2.0;
			
			double currentTheta = getTheta();
			
			double deltaX = displacement * Math.cos(currentTheta + deltaTheta / 2);
			double deltaY = displacement * Math.sin(currentTheta + deltaTheta / 2);
			
			setX(getX() + deltaX);
			setY(getY() + deltaY);
			setTheta(currentTheta + deltaTheta);

			// this ensures that the odometer only runs once every period
			updateEnd = System.currentTimeMillis();
			if (updateEnd - updateStart < ODOMETER_PERIOD) {
				try {
					Thread.sleep(ODOMETER_PERIOD - (updateEnd - updateStart));
				} catch (InterruptedException e) {
					// there is nothing to be done here because it is not
					// expected that the odometer will be interrupted by
					// another thread
				}
			}
		}
	}

	// accessors
	public void getPosition(double[] position, boolean[] update) {
		if (update[0]) position[0] = getX();
		if (update[1]) position[1] = getY();
		if (update[2]) position[2] = getTheta();
	}
	
	// mutators
	public void setPosition(double[] position, boolean[] update) {
		if (update[0]) setX(position[0]);
		if (update[1]) setY(position[1]);
		if (update[2]) setTheta(position[2]);
	}
	
	public double getX() {
		double result;
		synchronized (lock) { result = x; }
		return result;
	}

	public double getY() {
		double result;
		synchronized (lock) { result = y; }
		return result;
	}

	public double getTheta() {
		double result;
		synchronized (lock) { result = theta; }
		return result;
	}

	public void setX(double x) {
		synchronized (lock) { this.x = x; }
	}

	public void setY(double y) {
		synchronized (lock) { this.y = y; }
	}

	public void setTheta(double theta) {
		synchronized (lock) { this.theta = theta; }
	}
}