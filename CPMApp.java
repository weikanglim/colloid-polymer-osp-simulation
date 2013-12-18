package org.opensourcephysics.sip.CPM;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.util.Date;

import org.opensourcephysics.controls.AbstractSimulation;
import org.opensourcephysics.controls.SimulationControl;
import org.opensourcephysics.display3d.simple3d.ElementEllipsoid;
import org.opensourcephysics.display3d.simple3d.ElementSphere;
import org.opensourcephysics.frames.Display3DFrame;
import org.opensourcephysics.frames.PlotFrame;
import org.opensourcephysics.numerics.Transformation;
import org.opensourcephysics.numerics.VectorMath;

/**
 * NanoPolyMixApp is a simulation framework for a binary mixture of
 * colloids(nanoparticles) and polymers model.
 * 
 * @author Wei Kang Lim, Alan Denton
 * @version 1.0 11-10-2013
 * 
 */
public class CPMApp extends AbstractSimulation {
	CPM np = new CPM();
	Display3DFrame display3d = new Display3DFrame("3D Frame");
	PlotFrame plotframe = new PlotFrame("Monte Carlo Steps",
			"Number of Intersections", "Number of Intersections");
	double totalIntersections = 0;
	double snapshotIntervals = 1;
	double [] yzRefVector = {0,0,1};
	double [] xyRefVector = {1,0,0};
	double polar;
	double azimuth;
	int writeMode = 0;
	ElementSphere nanoSphere[];
	ElementEllipsoid polySphere[];
	boolean added = false;
	boolean penetrationEnergyToggle;
	BufferedWriter bw1;
	BufferedWriter bw2;
	BufferedWriter bw3;
	BufferedWriter bw4;
	BufferedWriter bw5;
	Date date = new Date();
	Path f1;
	Path f2;
	Path f3;
	Path f4;
	Path f5;
	Path dir;


	/**
	 * Initializes the model.
	 */
	public void initialize() {
		added = false;
		totalIntersections = 0;
		np.nP = control.getInt("N Polymers");
		np.nN = control.getInt("N Nano");
		np.tolerance = control.getDouble("tolerance");
		np.shapeTolerance = control.getDouble("Shape Tolerance");
		np.q = control.getDouble("Polymer Colloid Ratio");
		np.init_eX = control.getDouble("x");
		np.init_eY = control.getDouble("y");
		np.init_eZ = control.getDouble("z");
		np.nano_r = control.getDouble("Nanoparticle radius");
		np.lc = control.getDouble("Lattice constant");
		String configuration = control.getString("initial configuration");
		np.rotMagnitude = control.getDouble("Rotation magnitude");
		np.moveToShapeRatio = control
				.getInt("Trial Moves to Shape Changes Ratio");
		snapshotIntervals = control.getInt("Snapshot Interval");
		penetrationEnergyToggle =control.getBoolean("Penetration Energy");
		writeMode = control.getInt("Write Mode");
		if(!penetrationEnergyToggle){
			System.out.println("Executed!");
			np.Ep = 0;
		}
		np.initialize(configuration);
		if(display3d != null) display3d.dispose(); // closes an old simulation frame is present
		display3d = new Display3DFrame("3D Frame");
		display3d.setPreferredMinMax(0, np.Lx, 0, np.Ly, 0, np.Lz);
		display3d.setSquareAspect(true);

		if (!added) {
			nanoSphere = new ElementSphere[np.nN];
			polySphere = new ElementEllipsoid[np.nP];

			for (int i = 0; i < np.nN; i++) {
				nanoSphere[i] = new ElementSphere();
				display3d.addElement(nanoSphere[i]);
			}

			for (int i = 0; i < np.nP; i++) {
				polySphere[i] = new ElementEllipsoid();
				display3d.addElement(polySphere[i]);
			}

			added = true;
		}

		for (int i = 0; i < np.nN; i++) {
			nanoSphere[i].setSizeXYZ(2 * np.nanos[i].getrX(),
					2 * np.nanos[i].getrY(), 2 * np.nanos[i].getrZ());
			nanoSphere[i].getStyle().setFillColor(Color.BLACK);
			nanoSphere[i].setXYZ(np.nanos[i].getX(), np.nanos[i].getY(),
					np.nanos[i].getZ());
		}

		for (int i = 0; i < np.nP; i++) {
			polySphere[i].setSizeXYZ(2 * np.polymers[i].getrX(),
					2 * np.polymers[i].getrY(), 2 * np.polymers[i].getrZ());
			polySphere[i].getStyle().setFillColor(Color.RED);
			polySphere[i].setXYZ(np.polymers[i].getX(), np.polymers[i].getY(),
					np.polymers[i].getZ());
		}

		plotframe.append(0, np.mcs, np.totalIntersectCount);
	}

	/**
	 * Does a simulation step.
	 */
	public void doStep() {
		// Initialize files for writing output data
<<<<<<< Updated upstream
		if(np.mcs == 0 && snapshotIntervals > 0){
<<<<<<< Updated upstream
			initializeWrite();
=======
			dir = Paths.get("data");
			f1 = Paths.get("data/" + date + " x .dat");
			f2 = Paths.get("data/" + date + " y .dat");
			f3 = Paths.get("data/" + date + " z .dat");
			
			try {
				if (Files.notExists(dir, LinkOption.values())) {
					Files.createDirectory(dir);
				}
				Charset ascii = Charset.forName("US-ASCII");
				StandardOpenOption append = StandardOpenOption.APPEND;
				if(f1.toFile().exists()){
					bw1 = Files.newBufferedWriter(f1, ascii, append);
				} else{
					bw1 = Files.newBufferedWriter(f1, ascii);
				}
				
				if(f2.toFile().exists()){
					bw2 = Files.newBufferedWriter(f2, ascii, append);
				} else{
					bw2 = Files.newBufferedWriter(f2, ascii);
				}
				
				if(f3.toFile().exists()){
					bw3 = Files.newBufferedWriter(f3, ascii, append);
				} else{
					bw3 = Files.newBufferedWriter(f3, ascii);
				}
				
				DecimalFormat largeDecimal = new DecimalFormat("0.##E0");
				DecimalFormat threeDecimal = new DecimalFormat("#0.###");
				String configurations = "# Number of Polymers:" + np.nP +
						"\n# Number of Nanoparticles: "+np.nN +
						"\n# Move Tolerance: "+threeDecimal.format(np.tolerance)+
						"\n# Shape Change Tolerance: "+threeDecimal.format(np.shapeTolerance)+
						"\n# Nanoparticle Radius :"+threeDecimal.format(np.nano_r) + 
						"\n# Polymer Colloid Ratio: "+threeDecimal.format(np.q)+
						"\n# Lattice Constant: " +threeDecimal.format(np.lc)+
						"\n# Rotation Tolerance: "+threeDecimal.format(np.rotMagnitude)+
						"\n# Trial Moves to Attempt Shape Change Ratio: "+np.moveToShapeRatio+
						"\n# Snapshot Interval: "+largeDecimal.format(this.snapshotIntervals)+
						"\n# Penetration Energy On: " + this.penetrationEnergyToggle
						;
				bw1.write(configurations);
				bw2.write(configurations);
				bw3.write(configurations);
				bw1.newLine();
				bw2.newLine();
				bw3.newLine();		
					bw1.flush();
					bw2.flush();
					bw3.flush();

			} catch (IOException e) {
				e.printStackTrace();
			}
=======
		if(np.mcs == 0 && writeMode > 0){
			initializeWrite();
>>>>>>> Stashed changes
>>>>>>> Stashed changes
		}
		
		np.step();

		for (int i = 0; i < np.nN; i++) {
			nanoSphere[i].setXYZ(np.nanos[i].getX(), np.nanos[i].getY(),
					np.nanos[i].getZ());
		}
		for (int i = 0; i < np.nP; i++) {
			polySphere[i].setXYZ(np.polymers[i].getX(), np.polymers[i].getY(),
					np.polymers[i].getZ());
			polySphere[i].setSizeXYZ(2 * np.polymers[i].getrX(),
					2 * np.polymers[i].getrY(), 2 * np.polymers[i].getrZ());
//			if(np.polymers[i].isRotated()){
//				polySphere[i].setTransformation(np.polymers[i].getTransformation());
//			}
		}

		if ((writeMode == 1 || writeMode == 3) && np.mcs % snapshotIntervals == 0 && np.mcs >= 50000) {
			for (Polymer poly : np.polymers) {
				try {
						bw1.write(String.valueOf(poly.geteX()) + "\n");
						bw2.write(String.valueOf(poly.geteY()) + "\n");
						bw3.write(String.valueOf(poly.geteZ()) + "\n");
					}
				 catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		if ((writeMode == 2 || writeMode == 3) && np.mcs % snapshotIntervals == 0 && np.mcs > 1) {
			for (Polymer poly : np.polymers) {
				try {
					double[] yzVector = {0, poly.getTransformAxis()[1],poly.getTransformAxis()[2]};
					double[] xyVector = {poly.getTransformAxis()[0], poly.getTransformAxis()[1], 0};
					yzVector = VectorMath.normalize(yzVector);
					xyVector = VectorMath.normalize(xyVector);
					polar = Math.acos(VectorMath.dot(yzRefVector, yzVector));
					azimuth = Math.acos(VectorMath.dot(xyRefVector, xyVector));
					bw4.write(String.valueOf(polar + "\n"));
					bw5.write(String.valueOf(azimuth + "\n"));
					}
				 catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		
		if(writeMode > 0 && np.mcs % 100*snapshotIntervals == 0){
			try {
				switch(writeMode){
				case 1:
					bw1.flush();
					bw2.flush();
					bw3.flush();
					break;
				case 2:
					bw4.flush();
					bw5.flush();
					break;
				case 3:
					bw1.flush();
					bw2.flush();
					bw3.flush();
					bw4.flush();
					bw5.flush();
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		plotframe.append(0, np.mcs, np.totalIntersectCount);
		totalIntersections += np.totalIntersectCount;
		display3d.setMessage("Number of mcs steps: " + np.mcs);
	}

	/**
	 * Resets the colloid polymer mixture model to its default state.
	 */
	public void reset() {
		enableStepsPerDisplay(true);
		control.setValue("N Polymers", 64);
		control.setValue("N Nano", 0);
		control.setValue("tolerance", 0.1);
		control.setValue("Shape Tolerance", 0.001);
		control.setValue("Nanoparticle radius", 0.1);
		control.setValue("x", 0.005);
		control.setValue("y", 0.005);
		control.setValue("z", 0.005);
		control.setValue("Polymer Colloid Ratio", 5);
		control.setValue("Lattice constant", 10);
		control.setValue("initial configuration", "square");
		control.setValue("Rotation magnitude", 1);
		control.setValue("Trial Moves to Shape Changes Ratio", 1);
		control.setValue("Snapshot Interval", 1000);
		control.setValue("Penetration Energy", false);
		control.setValue("Write Mode", 2);
		initialize();
	}

	public void stop() {
		double averageIntersections = totalIntersections / np.mcs;
		
		double volSpheres = np.nP
				* (4 / 3d * Math.PI * np.polymers[0].getrX()
						* np.polymers[0].getrY() * np.polymers[0].getrZ())
				* np.nN;
		double volFract = volSpheres / (np.Lx * np.Ly * np.Lz);
		control.println("Average no. of Intersections: " + averageIntersections);
		control.println("Vol fraction: " + volFract);
		
		if(snapshotIntervals > 0 && writeMode > 0){
			try {
				switch(writeMode){
				case 1:
					bw1.close();
					bw2.close();
					bw3.close();
					break;
				case 2:
					bw4.close();
					bw5.close();
					break;
				case 3: 
					bw1.close();
					bw2.close();
					bw3.close();
					bw4.close();
					bw5.close();
					break;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
<<<<<<< Updated upstream
=======
	
	public void initializeWrite(){
		DecimalFormat largeDecimal = new DecimalFormat("0.##E0");
		DecimalFormat threeDecimal = new DecimalFormat("#0.###");
		String configurations = "# Number of Polymers:" + np.nP +
				"\n# Number of Nanoparticles: "+np.nN +
				"\n# Move Tolerance: "+threeDecimal.format(np.tolerance)+
				"\n# Shape Change Tolerance: "+threeDecimal.format(np.shapeTolerance)+
				"\n# Nanoparticle Radius :"+threeDecimal.format(np.nano_r) + 
				"\n# Polymer Colloid Ratio: "+threeDecimal.format(np.q)+
				"\n# Lattice Constant: " +threeDecimal.format(np.lc)+
				"\n# Rotation Tolerance: "+threeDecimal.format(np.rotMagnitude)+
				"\n# Trial Moves to Attempt Shape Change Ratio: "+np.moveToShapeRatio+
				"\n# Snapshot Interval: "+largeDecimal.format(this.snapshotIntervals)+
				"\n# Penetration Energy On: " + this.penetrationEnergyToggle
				;

		switch(writeMode){
		case 1:
			dir = Paths.get("data");
			f1 = Paths.get("data/" + date + " x .dat");
			f2 = Paths.get("data/" + date + " y .dat");
			f3 = Paths.get("data/" + date + " z .dat");
			
			try {
				if (Files.notExists(dir, LinkOption.values())) {
					Files.createDirectory(dir);
				}
				Charset ascii = Charset.forName("US-ASCII");
				bw1 = Files.newBufferedWriter(f1, ascii, f1.toFile().exists()? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
				bw2 = Files.newBufferedWriter(f2, ascii, f2.toFile().exists()? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
				bw3 = Files.newBufferedWriter(f3, ascii, f3.toFile().exists()? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
				
				bw1.write(configurations);
				bw2.write(configurations);
				bw3.write(configurations);
				
				bw1.newLine();
				bw2.newLine();
				bw3.newLine();	
				
				bw1.flush();
				bw2.flush();
				bw3.flush();

			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 2:
			dir = Paths.get("data");
			f4 = Paths.get("data/" + date + " polar.dat");
			f5 = Paths.get("data/" + date + " azimuth.dat");
			try {
				if (Files.notExists(dir, LinkOption.values())) {
					Files.createDirectory(dir);
				}
				Charset ascii = Charset.forName("US-ASCII");
				bw4 = Files.newBufferedWriter(f4, ascii, f4.toFile().exists()? StandardOpenOption.APPEND : StandardOpenOption.CREATE );
				bw5 = Files.newBufferedWriter(f5, ascii, f5.toFile().exists()? StandardOpenOption.APPEND : StandardOpenOption.CREATE );
				
				bw4.write(configurations);
				bw5.write(configurations);
				
				bw4.newLine();
				bw5.newLine();
				
				bw4.flush();
				bw5.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
		case 3:
			dir = Paths.get("data");
			f1 = Paths.get("data/" + date + " x .dat");
			f2 = Paths.get("data/" + date + " y .dat");
			f3 = Paths.get("data/" + date + " z .dat");
			f4 = Paths.get("data/" + date + " polar.dat");
			f5 = Paths.get("data/" + date + " azimuth.dat");

			try {
				if (Files.notExists(dir, LinkOption.values())) {
					Files.createDirectory(dir);
				}
				Charset ascii = Charset.forName("US-ASCII");
				bw1 = Files.newBufferedWriter(f1, ascii, f1.toFile().exists()? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
				bw2 = Files.newBufferedWriter(f2, ascii, f2.toFile().exists()? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
				bw3 = Files.newBufferedWriter(f3, ascii, f3.toFile().exists()? StandardOpenOption.APPEND : StandardOpenOption.CREATE);
				bw4 = Files.newBufferedWriter(f4, ascii, f4.toFile().exists()? StandardOpenOption.APPEND : StandardOpenOption.CREATE );
				bw5 = Files.newBufferedWriter(f5, ascii, f5.toFile().exists()? StandardOpenOption.APPEND : StandardOpenOption.CREATE );

				bw1.write(configurations);
				bw2.write(configurations);
				bw3.write(configurations);
				bw4.write(configurations);
				bw5.write(configurations);
				
				bw1.newLine();
				bw2.newLine();
				bw3.newLine();		
				bw4.newLine();
				bw5.newLine();

				bw1.flush();
				bw2.flush();
				bw3.flush();
				bw4.flush();
				bw5.flush();

			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			default:
				return;
		}
	}
<<<<<<< Updated upstream
	
	public void initializeWrite(){
			dir = Paths.get("data");
			f1 = Paths.get("data/" + date + " x .dat");
			f2 = Paths.get("data/" + date + " y .dat");
			f3 = Paths.get("data/" + date + " z .dat");
			
			try {
				if (Files.notExists(dir, LinkOption.values())) {
					Files.createDirectory(dir);
				}
				Charset ascii = Charset.forName("US-ASCII");
				StandardOpenOption append = StandardOpenOption.APPEND;
				if(f1.toFile().exists()){
					bw1 = Files.newBufferedWriter(f1, ascii, append);
				} else{
					bw1 = Files.newBufferedWriter(f1, ascii);
				}
				
				if(f2.toFile().exists()){
					bw2 = Files.newBufferedWriter(f2, ascii, append);
				} else{
					bw2 = Files.newBufferedWriter(f2, ascii);
				}
				
				if(f3.toFile().exists()){
					bw3 = Files.newBufferedWriter(f3, ascii, append);
				} else{
					bw3 = Files.newBufferedWriter(f3, ascii);
				}
				
				DecimalFormat largeDecimal = new DecimalFormat("0.##E0");
				DecimalFormat threeDecimal = new DecimalFormat("#0.###");
				String configurations = "# Number of Polymers:" + np.nP +
						"\n# Number of Nanoparticles: "+np.nN +
						"\n# Move Tolerance: "+threeDecimal.format(np.tolerance)+
						"\n# Shape Change Tolerance: "+threeDecimal.format(np.shapeTolerance)+
						"\n# Nanoparticle Radius :"+threeDecimal.format(np.nano_r) + 
						"\n# Polymer Colloid Ratio: "+threeDecimal.format(np.q)+
						"\n# Lattice Constant: " +threeDecimal.format(np.lc)+
						"\n# Rotation Tolerance: "+threeDecimal.format(np.rotMagnitude)+
						"\n# Trial Moves to Attempt Shape Change Ratio: "+np.moveToShapeRatio+
						"\n# Snapshot Interval: "+largeDecimal.format(this.snapshotIntervals)+
						"\n# Penetration Energy On: " + this.penetrationEnergyToggle
						;
				bw1.write(configurations);
				bw2.write(configurations);
				bw3.write(configurations);
				bw1.newLine();
				bw2.newLine();
				bw3.newLine();		
					bw1.flush();
					bw2.flush();
					bw3.flush();

			} catch (IOException e) {
				e.printStackTrace();
			}
	}
=======
>>>>>>> Stashed changes
>>>>>>> Stashed changes

	/**
	 * Starts the Java application.
	 * 
	 * @param args
	 *            command line parameters
	 */
	public static void main(String[] args) { // set up animation control
												// structure using this class
		@SuppressWarnings("unused")
		SimulationControl control = SimulationControl.createApp(new CPMApp());
	}
}
