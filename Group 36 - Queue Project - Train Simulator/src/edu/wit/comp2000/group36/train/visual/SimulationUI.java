package edu.wit.comp2000.group36.train.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.plaf.PanelUI;

import edu.wit.comp2000.group36.train.Logger;
import edu.wit.comp2000.group36.train.Simulation;
import edu.wit.comp2000.group36.train.Station;
import edu.wit.comp2000.group36.train.Train;

public class SimulationUI extends PanelUI implements ComponentListener {
	private Simulation simulation;
	private TrainDrawInfo[] trains;
	private StationDrawInfo[] stations;
	
	public SimulationUI(Simulation simulation) {
		this.simulation = simulation;
		this.trains = new TrainDrawInfo[simulation.getTrains().length];
		this.stations = new StationDrawInfo[simulation.getRoute().getStationCount()];
		
		for(int i = 0; i < trains.length; i ++) {
			trains[i] = new TrainDrawInfo(simulation.getTrains()[i]);
		}
		
		for(int i = 0; i < stations.length; i ++) {
			stations[i] = new StationDrawInfo(simulation.getRoute().getStation(i));
		}
	}
	
	private static final int DELAY = 10;
	
	private static final float STEP_SIZE = 3f;
	private static final float STEP_SIZE_SQ = STEP_SIZE * STEP_SIZE;
	
	private static final Path2D TRAIN_SHAPE;
	
	static {
		double ANGLE = Math.toRadians(60);
		double SCALE = 10;
		
		double cos = Math.cos(ANGLE) * SCALE;
		double sin = Math.sin(ANGLE) * SCALE;
		
		TRAIN_SHAPE = new GeneralPath();
		TRAIN_SHAPE.moveTo(   0, -sin);
		TRAIN_SHAPE.lineTo( cos,  0);
		TRAIN_SHAPE.lineTo( cos,  2 * SCALE);
		TRAIN_SHAPE.lineTo(-cos,  2 * SCALE);
		TRAIN_SHAPE.lineTo(-cos,  0);
		TRAIN_SHAPE.closePath();
	}
	
	private ArrayList<Point2D> pointsInt;
	private ArrayList<Point2D> pointsOut;
	private Path2D pathInt;
	private Path2D pathOut;
	
	private float trackSpace;

	private long stepCount, stepLimit;
	
	public void installUI(JComponent c) {
		c.addComponentListener(this);
	}
	
	public void prepSimulation(long milli) {
		System.out.println(stepLimit);
		stepLimit = milli / DELAY;
		stepCount = 0;
		System.out.println(stepLimit);
	}
	
	public void paint(Graphics g, JComponent c) {
		if(pathOut == null) componentResized(new ComponentEvent(c, 0));
		Graphics2D g2d = (Graphics2D) g;
		
		// Draw Tracks
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(3));
		
		g2d.draw(pathOut);
		g2d.draw(pathInt);

//		g2d.setStroke(new BasicStroke(1));
		
		for(TrainDrawInfo train : trains) {
			train.draw(g2d);
		}
		
		for(StationDrawInfo stations : stations) {
			stations.draw(g2d);
		}
		
		if(stepCount ++ < stepLimit) {
			try { Thread.sleep(DELAY); } 
			catch(InterruptedException ignore) { }
			c.repaint();
			return;
		}
		
		synchronized(this) { this.notifyAll(); }
	}
	
	private class TrainDrawInfo {
		private Train train;
		private Color color;

		private Point2D p0, p1;
		
		private double dx, dy;
		private double angle;

		private int calcLocation;
		
		public TrainDrawInfo(Train train) {
			this.train = train;
			this.color = Color.getHSBColor(Logger.RAND.nextFloat(), 1, 1);
			
			this.calcLocation = -1;
		}
		
		public void draw(Graphics2D g2d) {
			if(train.getLocation() != calcLocation) recalculate();

			double perc = (double) stepCount / Math.max(stepLimit, 1);
			Point2D p = new Point2D.Double(p0.getX() + dx * perc, p0.getY() + dy * perc);
			
			g2d.setColor(color);
			
			g2d.translate(p.getX(), p.getY());
			g2d.rotate(angle);
			g2d.fill(TRAIN_SHAPE);
			g2d.rotate(-angle);
			g2d.translate(-p.getX(), -p.getY());
		}
		
		private void recalculate() {
			ArrayList<Point2D> path = train.isInbound() ? pointsInt : pointsOut;
			
			int length = simulation.getRoute().getLength();
			float locLength = (float) path.size() / length;
			
			int nextLoc = (train.getLocation() + (train.isInbound() ? -1 : 1) + length) % length;
			
			int p0Index = (int) (train.getLocation() * locLength);
			int p1Index = (int) (nextLoc * locLength);
			
			p0Index = (p0Index + path.size()) % path.size();
			p1Index = (p1Index + path.size()) % path.size();
			
			p0 = path.get(p0Index);
			p1 = path.get(p1Index);
			
			dx = p1.getX() - p0.getX();
			dy = p1.getY() - p0.getY();
			
			angle = Math.atan2(p1.getY() - p0.getY(), p1.getX() - p0.getX()) + Math.PI / 2;
			
			calcLocation = train.getLocation();
		}
	}
	
	private class StationDrawInfo {
		private static final int RANGE = 3;
		
		private Station station;
		private Color color;

//		private Shape shape;
		private Point2D point;
		
		public StationDrawInfo(Station station) {
			this.station = station;
			this.color = Color.getHSBColor(0, 0, Logger.RAND.nextFloat());
		}
		
		public void draw(Graphics2D g2d) {
//			if(shape == null) recalculate();
			if(point == null) recalculate();
			
			g2d.setColor(color);
//			g2d.fill(shape);
			
			g2d.fillRect((int) point.getX() - 5, (int) point.getY() - 5, 10, 10);
		}
		
		private void recalculate() {
			ArrayList<Point2D> path = pointsInt;
			int length = simulation.getRoute().getLength();
			float locLength = (float) path.size() / length;
			
			int p0Index = (int) (station.getLocation() * locLength);
			int p1Index = p0Index + RANGE;
			int p2Index = p0Index - RANGE;
			
			p0Index = (p0Index + path.size()) % path.size();
			p1Index = (p1Index + path.size()) % path.size();
			p2Index = (p2Index + path.size()) % path.size();
			
			Point2D p0 = path.get(p0Index);
			Point2D p1 = path.get(p1Index);
			Point2D p2 = path.get(p2Index);
			
			double dist = p2.distanceSq(p1);
			double dx = (p2.getX() - p1.getX()) / dist;
			double dy = (p2.getY() - p1.getY()) / dist;
			
			float space = trackSpace / 2;
			point = new Point2D.Double(p0.getX() - dy * space, p0.getY() + dx * space);
			System.out.println(point);
		}
	}
	
	private void appendShape(Shape shape, Collection<Point2D> points, AffineTransform transform) {
		PathIterator iter = new FlatteningPathIterator(shape.getPathIterator(transform), 0.01f);
		Point2D p0 = null;
        
        float[] coords = new float[6];
        while(!iter.isDone()) {
            iter.currentSegment(coords);
            
            float x = (int) coords[0];
            float y = (int) coords[1];
            
            Point2D p = new Point2D.Float(x, y);
            if(p0 == null) {
            	p0 = p;
            	
            } else {
            	double distSq = p.distanceSq(p0);
            	if(distSq > STEP_SIZE_SQ) {
            		double dist = Math.sqrt(distSq);
            		double dx = p.getX() - p0.getX();
            		double dy = p.getY() - p0.getY();

            		double nx = dx / dist * STEP_SIZE;
            		double ny = dy / dist * STEP_SIZE;
            		
            		for(int i = 0; i < dist / STEP_SIZE; i ++) {
            			points.add(p0 = new Point2D.Double(p0.getX() + nx, p0.getY() + ny));
            		}
            	} 
            }
            
            iter.next();
        }
	}
	
//	private static <T> void flipQueue(Queue<T> flip) {
//		Stack<T> stack = new Stack<>();
//		while(!flip.isEmpty()) stack.push(flip.poll());
//		while(!stack.isEmpty()) flip.add(stack.pop());
//	}

	public void componentResized(ComponentEvent e) {
		pointsInt = new ArrayList<>();
		pointsOut = new ArrayList<>();
		
		pathInt = new GeneralPath();
		pathOut = new GeneralPath();
		
		int width = e.getComponent().getWidth();
		int height = e.getComponent().getHeight();
		
		float outerLength = width * 7f / 8;					// 3/4
		float outerArc = outerLength * 1f / 16;
		float outerCorner = (width - outerLength) / 2;
		outerLength -= outerArc * 2;
		
		float innerLength = width * 4f / 8;					// 5/8
		float innerArc = innerLength * 1f / 16;
		float innerCorner = (width - innerLength) / 2;
		innerLength -= innerArc * 2;
		
		trackSpace = innerCorner - outerCorner;
		
		Arc2D outArc = new Arc2D.Float(outerCorner, outerCorner, outerArc * 2, outerArc * 2, 180, -90, Arc2D.OPEN);
		Line2D outLine = new Line2D.Float(outerCorner + outerArc, outerCorner, outerLength + outerCorner + outerArc, outerCorner);
		
		Arc2D intArc = new Arc2D.Float(innerCorner, innerCorner, innerArc * 2, innerArc * 2, 180, -90, Arc2D.OPEN);
		Line2D intLine = new Line2D.Float(innerCorner + innerArc, innerCorner, innerLength + innerCorner + innerArc, innerCorner);

		AffineTransform transform = new AffineTransform();
		
		for(int i = 0; i < 4; i ++) {
			appendShape(outArc, pointsOut, transform);
			appendShape(outLine, pointsOut, transform);
			
			appendShape(intArc, pointsInt, transform);
			appendShape(intLine, pointsInt, transform);
			
			pathOut.append(outArc.getPathIterator(transform), true);
			pathOut.append(outLine.getPathIterator(transform), true);
			
			pathInt.append(intArc.getPathIterator(transform), true);
			pathInt.append(intLine.getPathIterator(transform), true);
			
			transform.rotate(Math.PI / 2, width / 2, height / 2);
		}
	}

	public void componentMoved(ComponentEvent e) { }
	public void componentShown(ComponentEvent e) { }
	public void componentHidden(ComponentEvent e) { }
	
	public Dimension getPreferredSize(JComponent p) { 
		return new Dimension(500, 500);
//		Dimension base = super.getPreferredSize(p);
//        Container parent = p.getParent();
//        
//        if(parent != null) {
//            base = parent.getSize();
//        } else {
//            return new Dimension(500, 500);
//        }
//        
//        int width = (int) base.getWidth();
//        int height = (int) base.getHeight();
//        
//        int size = width < height ? width : height;
//        return new Dimension(size, size);
	}
}
