package org.opensourcephysics.sip.CPM;

import org.opensourcephysics.numerics.Matrix3DTransformation;
import org.opensourcephysics.numerics.PBC;
import org.opensourcephysics.numerics.Root;
import org.opensourcephysics.numerics.VectorMath;

public class Polymer extends Particle{
	private static boolean exact;
	private static double tolerance;
	private static double shapeTolerance;
	private static double rotTolerance;
	private static double default_eX;
	private static double default_eY;
	private static double default_eZ;
	private static double q;
	private double eX;
	private double eY;
    private double eZ;
    private final double originAxis[] = {0,0,1};
    private double oldAxis[] = {0,0,1};
    private double newAxis[] = {0,0,1}; // transformation axis
    public double [] closestPoint = new double[3];
    public double [] overlapSphere = new double[3];
    private ESOverlapPolynomial overlapPolynomial;



	public Polymer(double x_, double y_, double z_, double tolerance_, double eX, double eY, double eZ, double q_){
		setX(x_);
		setY(y_);
		setZ(z_);
		seteX(eX);
		seteY(eY);
		seteZ(eZ);
		tolerance = tolerance_;
	}
	
	public Polymer(double x_, double y_, double z_){
		setX(x_);
		setY(y_);
		setZ(z_);
		seteX(default_eX);
		seteY(default_eY);
		seteZ(default_eZ);
	}
	
	/**
	 * Checks if a polymer overlaps a given particle.
	 * @param Particle A particle object.
	 * @return boolean true if an overlap occurs, false otherwise.
	 */
	public boolean overlap(Particle particle) {
		if(particle instanceof Polymer){
			return false;
		} else if(this.equals(particle)){
			return false;
		} else{
			Nano nano = (Nano) particle;
			
			// Check if polymer is spherical. 
			if(this.geteX() == this.geteY() && this.geteX() == this.geteZ()){
				return this.squaredSeparation(nano) < Math.pow(this.getrX() + nano.getrX(), 2); // Calculate the pbc distance directly.
			} else { // Polymer is ellipsoidal				
				if(exact){
					/** Initial filter: **/
					// If a sphere is outside of a sphere formed by the longest radius of the ellipsoid, reject it right away.
					double maxEllipsRadius = Math.max(Math.max(this.getrX(), this.getrY()), this.getrZ());
					if(this.squaredSeparation(nano) >= Math.pow(maxEllipsRadius + nano.getrX(), 2)){ 
						return false;
					}
					
					// If center of ellipsoid is inside the sphere, accept it immediately.
					if(this.squaredSeparation(nano) <= Math.pow(nano.getrX(), 2)){
						return true;
					}
					
					/** Exact solution **/
					Matrix3DTransformation rotationTransformation =  this.getRotationTransformation();
					double [] ellipsEigen = {Math.pow(this.getrX(),2), Math.pow(this.getrY(),2), Math.pow(this.getrZ(),2)}; // ellipsoid eigenvalues
					// sphere distances from center of ellipsoid
					double [] sphereCoord = {PBC.separation(nano.getX()-this.getX(), Particle.getLx()),
										   PBC.separation(nano.getY()-this.getY(), Particle.getLy()),
										   PBC.separation(nano.getZ()-this.getZ(), Particle.getLz())};
					
					if(isRotated()){ // Rotations needed
						sphereCoord = rotationTransformation.inverse(sphereCoord);
					}

					// Update the overlap polynomial with the new ellipsoid shape, and new distance of sphere.
					if(overlapPolynomial == null){ // If the function hasn't been instantiated, instantiate a new object
						overlapPolynomial = new ESOverlapPolynomial(ellipsEigen, sphereCoord);
					} else { // Object instantiated, reuse it instead of creating new references for performance.
						System.out.println("x0 " + sphereCoord[0] + " " +
										   "y0 " + sphereCoord[1] + " " +
										   "z0 " + sphereCoord[2]);
						overlapPolynomial.update(ellipsEigen, sphereCoord);
					}
					
					double yRatio = ellipsEigen[1] / ellipsEigen[0]; //  ratio of the radii, lambda2/lambda1
					double zRatio = ellipsEigen[2] / ellipsEigen[0]; //  lambda3/lambda1
					// solve for root, which must be in between (0, maxEllipsRadius).
					// 0.001 is currently set as the acceptable computational tolerance.
					// x, y, z are the coordinates of the closest point
					
					double max = sphereCoord[0] > 0 ? maxEllipsRadius : 0;
					double min = sphereCoord[0] > 0 ? 0 : -maxEllipsRadius;
					double x = Root.newtonBisection(overlapPolynomial, min , max, 0.0001,20); 
					double y = yRatio*x*sphereCoord[1]/(sphereCoord[0] + (yRatio-1)*x);
					double z = zRatio*x*sphereCoord[2]/(sphereCoord[0] + (zRatio-1)*x);
					double ellipsEquation = Math.pow(x/this.getrX(),2)+Math.pow(y/this.getrY(),2)+Math.pow(z/this.getrZ(),2);
					double [] closest = {x,y,z};

					if(isRotated()){ // Rotations needed
						closest = rotationTransformation.direct(closest);
					}
					
//					System.out.println("Ellipsoid radii: " + this.getrX() + " " + this.getrY() + " " + this.getrZ());
					System.out.println("Closest: " + x + " " + y + " " + z);
					System.out.println("Equation of ellipsoid: " + ellipsEquation);
//					System.out.println("Nano center: " + sphere[0] + " " + sphere[1] + " " + sphere[2]);
					boolean exactOverlap = Math.pow(x-sphereCoord[0], 2) + Math.pow(y-sphereCoord[1], 2) + Math.pow(z-sphereCoord[2], 2) < Math.pow(nano.getrX(),2);
					if(this.geteX() == this.geteY() && this.geteX() == this.geteZ() && exactOverlap && this.squaredSeparation(nano) >= Math.pow(this.getrX() + nano.getrX(), 2)){
						System.out.println("Inconsistency in overlap.");
					}
					
					
					if(exactOverlap){
						overlapSphere[0] = sphereCoord[0];
						overlapSphere[1] = sphereCoord[1];
						overlapSphere[2] = sphereCoord[2];
					closestPoint[0] = closest[0] + this.getX();
					closestPoint[1] = closest[1] + this.getY();
					closestPoint[2] = closest[2] + this.getZ();
					}

					return Math.pow(x-sphereCoord[0], 2) + Math.pow(y-sphereCoord[1], 2) + Math.pow(z-sphereCoord[2], 2) < Math.pow(nano.getrX(),2);
				} else{					
					/**
					 * Inexact overlap algorithm.
					 */
					double [] nanoCoord = {PBC.separation(nano.getX()-this.getX(), Particle.getLx()),
										   PBC.separation(nano.getY()-this.getY(), Particle.getLy()),
										   PBC.separation(nano.getZ()-this.getZ(), Particle.getLz())};
					if(isRotated()){
						Matrix3DTransformation transformation = this.getRotationTransformation();
						transformation.inverse(nanoCoord);
					}
					
					if(Math.pow(nanoCoord[0]/(this.getrX()+nano.getrX()),2) + 
					   Math.pow(nanoCoord[1]/(this.getrY()+nano.getrY()), 2) + 
					   Math.pow(nanoCoord[2]/(this.getrZ()+nano.getrZ()),2) < 1){ // AD
						return true;
					} 
					
					return false;
				}
			}
		}
	}
	
	/** Performs a shape change
	 * 
	 */
	public void shapeChange(){
		double newEX = this.geteX() + 10 * shapeTolerance * 2. * (Math.random() - 0.5);
		double newEY = this.geteY() + 3 * shapeTolerance * 2. * (Math.random() - 0.5);
		double newEZ = this.geteZ() + shapeTolerance * 2. * (Math.random() - 0.5);
		
		// Reject changes for negative radii eigenvalue immediately
		if(newEY < 0 || newEY < 0 || newEZ < 0){
			return;
		}
		
		this.seteX(newEX);
		this.seteY(newEY);
		this.seteZ(newEZ);
	}
	
	/**
	 * Performs a rotation on the polymer.
	 */
	public void rotate(){
		this.setOldAxis(this.getNewAxis());
		double [] a = this.getOldAxis();
		double [] v = new double[3];
		
		// generate randomly oriented vector v 
		for(int i = 0 ; i < v.length; i++){
			v[i] = Math.random() - 0.5;
		}

		// normalize new (randomly oriented) vector v 
		VectorMath.normalize(v);
				
		// addition of the old and new vector 
		// Note: rotTolerance, which replaces rotMagnitude, should be << 1 (e.g. 0.1)
		for(int i = 0; i < v.length; i++){
			a[i] = a[i] + rotTolerance*v[i];
		}

		// normalize result
		VectorMath.normalize(a);
		this.setNewAxis(a);

	}
	
	/**
	 * Moves the polymer with the tolerance specified by Polymer.tolerance.
	 */
	public void move(){
		super.move(getTolerance());
	}
	
	public static void setTolerance(double tolerance) {
		Polymer.tolerance = tolerance;
	}
	
	public static double getDefault_eX() {
		return default_eX;
	}

	public static void setDefault_eX(double defaulteX_) {
		Polymer.default_eX = defaulteX_;
	}
	public static void setDefault_eY(double defaulteY_) {
		Polymer.default_eY = defaulteY_;
	}
	public static void setDefault_eZ(double defaulteZ_) {
		Polymer.default_eZ = defaulteZ_;
	}
	public void seteX(double eX_){
		eX = eX_;
		setrX(toRadius(eX));
	}
	
	public void seteY(double eY_){
		eY = eY_;
		setrY(toRadius(eY));
	}
	
	public void seteZ(double eZ_){
		eZ = eZ_;
		setrZ(toRadius(eZ));
	}
	public void setOldAxis(double [] axis){
		if(axis.length == 3){
			this.oldAxis = axis;
		}
	}

	public static void setQ(double q_){
		Polymer.q = q_;
	}
	public void setNewAxis(double newAxis[]) {
		this.newAxis = newAxis;
	}


  // -------------------------------------
  // Getter methods
  // -------------------------------------
	public Matrix3DTransformation getRotationTransformation(){
		Matrix3DTransformation transform;
		if(!isRotated()){
			double [][] identity = {
					{1, 0, 0},
					{0, 1, 0},
					{0, 0, 1}
			};
			 transform = new Matrix3DTransformation(identity);
		} else{
			double [] originAxis = {0,0,1};
			transform = Matrix3DTransformation.createAlignmentTransformation(originAxis, newAxis);
		}
		return transform;
	}

	
  public double[] getNewAxis() {
	  	double [] returnAxis = new double[newAxis.length];
	  	for(int i =0; i < newAxis.length; i++){
	  		returnAxis[i] = newAxis[i];
	  	}
		return returnAxis;
	}


	public double geteX(){
		return eX;
	}
	
	public double geteY(){
		return eY;
	}
	
	public double geteZ(){
		return eZ;
	}
	public double[] getOldAxis(){
		double a[] = new double[oldAxis.length];		
		for(int i = 0; i < oldAxis.length; i ++){
			a[i] = oldAxis[i];
		}
		return a;
	}
	public static double getQ(){
		return Polymer.q;
	}
	public static double getDefault_eZ() {
		return default_eZ; // AD: eY --> eZ
	}
	public static double getTolerance() {
		return tolerance;
	}
	public static double getDefault_eY() {
		return default_eY;
	}
	
	/**
	 * Converts radius eigenvalue to radius.
	 * @param ei Eigenvalue
	 * @param v enumeration representing the radius axis
	 * @return The radius
	 */
	public double toRadius(double ei) {
		return (q/2.) * Math.sqrt(18. * ei);
	}
	
	public boolean updateRotation(){
		for(int i = 0; i < oldAxis.length; i++){
			if(oldAxis[i] != newAxis[i]) return true;
		}
		return false;
	}
	
	public boolean isRotated(){
		for(int i = 0; i < originAxis.length; i++){
			if(newAxis[i] != originAxis[i]) return true;
		}
		return false; 
	}
	
	public static double getRotTolerance() {
		return rotTolerance;
	}

	public static void setRotTolerance(double rotTolerance) {
		Polymer.rotTolerance = rotTolerance;
	}
	
	public static double getShapeTolerance() {
		return shapeTolerance;
	}

	public static void setShapeTolerance(double shapeTolerance) {
		Polymer.shapeTolerance = shapeTolerance;
	}
	
	public static boolean getExact(){
		return exact;
	}
	
	public static void setExact(boolean exact){
		Polymer.exact = exact;
	}

	public String toString(){
		return "oldAxis: " + oldAxis[0] + ", " + oldAxis[1] + ", " + oldAxis[2] + "\n" +
			   "newAxis: " + newAxis[0] + ", " + newAxis[1] + ", " + newAxis[2];
	}
	
	public String toPovray(){
		double [] oldAxis = this.getOldAxis();
		double [] newAxis = this.getNewAxis();
		String pString = String.format("sphere{\n"
				+ "\t<%.3f, %.3f, %.3f>, 1\n"
				+ "\tscale <%.3f, %.3f, %.3f>\n"
				+ "\ttexture {\n"
				+ "\t		pigment { color Red }\n"
				+ "\t}\n"
				+ "Reorient_Trans(<%.3f, %.3f, %.3f>, <%.3f, %.3f, %.3f>)"
				+ "}", 
				this.getX(), this.getY(), this.getZ(),
				this.getrX(), this.getrY(), this.getrZ(),
				oldAxis[0], oldAxis[1], oldAxis[2],
				newAxis[0], newAxis[1], newAxis[2]);
		return pString;
	}
}
