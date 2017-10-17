package edu.wit.comp2000.group36.train.visual;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.PanelUI;

import edu.wit.comp2000.group36.train.Logger;
import edu.wit.comp2000.group36.train.Simulation;
import edu.wit.comp2000.group36.train.Station;
import edu.wit.comp2000.group36.train.Train;

public class SimulationUI extends PanelUI implements ComponentListener, MouseMotionListener {
	private Simulation simulation;
	private TrainDrawInfo[] trains;
	private StationDrawInfo[] stations;
	
	private float stepMulti;
	
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
		
		stepMulti = (float) simulation.getRoute().getLength() / 100;
		stepMulti = 1;
	}
	
	private static final int DELAY = 10;
	
	private static final float STEP_SIZE = .1f;
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
	
	private double stepCount, stepLimit;
	
	public void installUI(JComponent c) {
		c.addComponentListener(this);
		c.addMouseMotionListener(this);
		
		c.setLayout(null);
	}
	
	public void prepSimulation(long milli) {
		stepLimit = milli / DELAY;
		stepCount = 0;
	}
	
	public void paint(Graphics g, JComponent c) {
		if(pathOut == null) componentResized(new ComponentEvent(c, 0));
		Graphics2D g2d = (Graphics2D) g;
		
		g2d.setColor(Color.WHITE);
		g2d.fillRect(0, 0, c.getWidth(), c.getHeight());
		
		int size = Math.min(c.getWidth(), c.getHeight());
		if(c.getWidth() < c.getHeight()) {
			g2d.translate(0, (c.getHeight() - size) / 2);
		} else {
			g2d.translate((c.getWidth() - size) / 2, 0);
		}

		for(StationDrawInfo stations : stations) {
			stations.draw(g2d);
		}
		
		// Draw Tracks
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(3));
		
		g2d.draw(pathOut);
		g2d.draw(pathInt);
		
		for(TrainDrawInfo train : trains) {
			train.draw(g2d);
		}
		
		if((stepCount += stepMulti) < stepLimit + stepMulti) {
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
		private Point2D p;
		
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

			double perc = Math.min(stepCount / Math.max(stepLimit, 1), 1);
			p = new Point2D.Double(p0.getX() + dx * perc, p0.getY() + dy * perc);
			
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
		
		public boolean contains(int x, int y) {
			AffineTransform transform = new AffineTransform();
			transform.translate(p.getX(), p.getY());
			transform.rotate(angle);
			try { transform.invert(); } catch(NoninvertibleTransformException e) { }
			
			return TRAIN_SHAPE.getBounds().contains(transform.transform(new Point2D.Float(x, y), null));
		}
	}
	
	private class StationDrawInfo {
		private static final int RANGE = 3;
		
		private Station station;
		private Color color;

		private Shape shape;
		
		public StationDrawInfo(Station station) {
			this.station = station;
			this.color = Color.getHSBColor(0, 0, Logger.RAND.nextFloat() / 2);
		}
		
		public void draw(Graphics2D g2d) {
			if(shape == null) recalculate();
			
			g2d.setColor(color);
			g2d.fill(shape);
		}
		
		private void recalculate() {
			GeneralPath path = new GeneralPath();
			for(int i = -RANGE; i <= RANGE; i ++) {
				Point2D p = pointsInt.get(getIndexForLocation(station.getLocation() + i, true));
				
				if(i == -RANGE) path.moveTo(p.getX(), p.getY());
				else path.lineTo(p.getX(), p.getY());
			}
			
			for(int i = RANGE; i >= -RANGE; i --) {
				Point2D p = pointsOut.get(getIndexForLocation(station.getLocation() + i, false));
				path.lineTo(p.getX(), p.getY());
			}
//			path.closePath();
			shape = path;
		}
		
		private int getIndexForLocation(int location, boolean inbound) {
			ArrayList<Point2D> path = inbound ? pointsInt : pointsOut;
			int length = simulation.getRoute().getLength();
			float locLength = (float) path.size() / length;
			
			return ((int) (location * locLength) + path.size()) % path.size();
		}
		
		public boolean contains(int x, int y) {
			return shape.contains(x, y);
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
		
		int size = Math.min(e.getComponent().getWidth(), e.getComponent().getHeight());
		
		float outerLength = size * 7f / 8;					// 3/4
		float outerArc = outerLength * 2f / 16;
		float outerCorner = (size - outerLength) / 2;
		outerLength -= outerArc * 2;
		
		float innerLength = size * 4f / 8;					// 5/8
		float innerArc = innerLength * 2f / 16;
		float innerCorner = (size - innerLength) / 2;
		innerLength -= innerArc * 2;
		
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
			
			transform.rotate(Math.PI / 2, size / 2, size / 2);
		}
		
		for(StationDrawInfo stations : stations) {
			stations.recalculate();
		}
	}

	private Object infoObj;
	private JPanel info;
	
	public void mouseMoved(MouseEvent e) {
		System.out.println(infoObj);
		
		for(StationDrawInfo station : stations) {
			if(station.contains(e.getX(), e.getY())) {
				if(infoObj == station) {
					info.setLocation(e.getX(), e.getY());
					e.getComponent().repaint();
					return;
				
				} else {
					if(info != null) ((JPanel) e.getComponent()).remove(info);
					
					infoObj = station;
					info = new JPanel();
					info.setUI(new InfoUI(station.station));
					info.setSize(info.getPreferredSize());
					info.setLocation(e.getX(), e.getY());
					((JPanel) e.getComponent()).add(info);

					e.getComponent().repaint();
					return;
				}
			}
		}
		
		for(TrainDrawInfo train : trains) {
			if(train.contains(e.getX(), e.getY())) {
				if(infoObj == train) {
					info.setLocation(e.getX(), e.getY());
					e.getComponent().repaint();
					return;
				
				} else {
					if(info != null) ((JPanel) e.getComponent()).remove(info);
					
					infoObj = train;
					info = new JPanel();
					info.setUI(new InfoUI(train.train));
					info.setSize(info.getPreferredSize());
					info.setLocation(e.getX(), e.getY());
					((JPanel) e.getComponent()).add(info);

					e.getComponent().repaint();
					return;
				}
			}
		}
		
		if(info != null) ((JPanel) e.getComponent()).remove(info);
		infoObj = null;
		info = null;
		
		e.getComponent().repaint();
	}

	public void mouseDragged(MouseEvent e) {}
	public void componentMoved(ComponentEvent e) { }
	public void componentShown(ComponentEvent e) { }
	public void componentHidden(ComponentEvent e) { }
	
	public Dimension getPreferredSize(JComponent p) { return new Dimension(500, 500); }
}
