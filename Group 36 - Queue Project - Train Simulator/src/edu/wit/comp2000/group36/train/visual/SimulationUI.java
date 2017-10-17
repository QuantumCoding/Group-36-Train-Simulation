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
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Queue;
import java.util.Stack;

import javax.swing.JComponent;
import javax.swing.plaf.PanelUI;

import edu.wit.comp2000.group36.train.Simulation;

public class SimulationUI extends PanelUI implements ComponentListener {
	private Simulation simulation;
	
	public SimulationUI(Simulation simulation) {
		this.simulation = simulation;
	}
	
	private static final float STEP_SIZE = 3f;
	private static final float STEP_SIZE_SQ = STEP_SIZE * STEP_SIZE;
	
	private Queue<Point2D> pointsInt;
	private Queue<Point2D> pointsOut;
	private Path2D pathInt;
	private Path2D pathOut;
	
	public void installUI(JComponent c) {
		c.addComponentListener(this);
	}
	
	public void paint(Graphics g, JComponent c) {
		if(pathOut == null) componentResized(new ComponentEvent(c, 0));
		Graphics2D g2d = (Graphics2D) g;
		
		int width = c.getWidth();
		int height = c.getHeight();

		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(3));
		
		g2d.draw(pathOut);
		g2d.draw(pathInt);
		
		g2d.setColor(Color.RED);
		g2d.setStroke(new BasicStroke(1));
		
		Point2D p = pointsInt.poll();
		pointsInt.add(p);
		g2d.fillRect((int) p.getX() - 4, (int) p.getY() - 4, 8, 8);

		g2d.setColor(Color.BLUE);
		
		p = pointsOut.poll();
		pointsOut.add(p);
		g2d.fillRect((int) p.getX() - 4, (int) p.getY() - 4, 8, 8);
		
		try { Thread.sleep(10); } catch(InterruptedException e) { }
		c.repaint();
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
	
	private static <T> void flipQueue(Queue<T> flip) {
		Stack<T> stack = new Stack<>();
		while(!flip.isEmpty()) stack.push(flip.poll());
		while(!stack.isEmpty()) flip.add(stack.pop());
	}
	
	public Dimension getPreferredSize(JComponent c) { return new Dimension(500, 500); }

	public void componentResized(ComponentEvent e) {
		pointsInt = new ArrayDeque<>();
		pointsOut = new ArrayDeque<>();
		
		pathInt = new GeneralPath();
		pathOut = new GeneralPath();
		
		int width = e.getComponent().getWidth();
		int height = e.getComponent().getHeight();
		
		float outerLength = width * 3f / 4;
		float outerArc = outerLength * 1f / 16;
		float outerCorner = (width - outerLength) / 2;
		outerLength -= outerArc * 2;
		
		float innerLength = width * 5f / 8;
		float innerArc = innerLength * 1f / 16;
		float innerCorner = (width - innerLength) / 2;
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
			
			transform.rotate(Math.PI / 2, width / 2, height / 2);
		}
		
		flipQueue(pointsOut);
	}

	public void componentMoved(ComponentEvent e) { }
	public void componentShown(ComponentEvent e) { }
	public void componentHidden(ComponentEvent e) { }
}
