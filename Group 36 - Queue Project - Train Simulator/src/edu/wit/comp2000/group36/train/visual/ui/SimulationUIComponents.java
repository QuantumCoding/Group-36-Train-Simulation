package edu.wit.comp2000.group36.train.visual.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import edu.wit.comp2000.group36.train.Logger;
import edu.wit.comp2000.group36.train.Station;
import edu.wit.comp2000.group36.train.Train;
import edu.wit.comp2000.group36.train.visual.InfoUI.Descriptable;

public class SimulationUIComponents {
	static class TrainDrawInfo implements Descriptable {
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
		
		private SimulationUI ui;
		private JPanel infoPanel;
		private double accDX, accDY;
		
		private Train train;
		private Color color;

		private Point2D p0, p1;
		private Point2D p;
		
		private double dx, dy;
		private double angle;

		private int calcLocation;
		private int lastOnboard;
		
		public TrainDrawInfo(Train train, SimulationUI ui) {
			this.ui = ui;
			this.train = train;
			this.color = Color.getHSBColor(Logger.RAND.nextFloat(), 1, 1);
			
			this.calcLocation = -1;
		}
		
		public void draw(Graphics2D g2d) {
			if(train.getLocation() != calcLocation) recalculate();

			Point2D last = p;
			double perc = Math.min(ui.stepCount / Math.max(ui.stepLimit, 1), 1);
			p = new Point2D.Double(p0.getX() + dx * perc, p0.getY() + dy * perc);
			
			g2d.setColor(color);
			
			g2d.translate(p.getX(), p.getY());
			g2d.rotate(angle);
			g2d.fill(TRAIN_SHAPE);
			g2d.rotate(-angle);
			g2d.translate(-p.getX(), -p.getY());
			
			if(infoPanel != null) {
				accDX += p.getX() - last.getX();
				accDY += p.getY() - last.getY();
				
				int cx = (int) (accDX - accDX  % 1);
				int cy = (int) (accDY - accDY  % 1);
				
				Point2D loc = infoPanel.getLocation();
				loc = new Point2D.Double(loc.getX() + cx, loc.getY() + cy);
				infoPanel.setLocation((int) loc.getX(), (int) loc.getY());
				
				accDX %= 1;
				accDY %= 1;
			}
			
			int onboard = train.getPassengerCount() - train.getJustBoarded();
			if(onboard < lastOnboard) {
				for(int i = 0, limit = lastOnboard - onboard; i < limit; i ++) {
					double angle = Math.toRadians(Math.random() * 360);
					double speed = Math.random() + .5;
					ui.addParticle(new Particle(p.getX(), p.getY(), Math.cos(angle) * speed, Math.sin(angle) * speed));
				}
			}
			lastOnboard = onboard;
		}
		
		private void recalculate() {
			ArrayList<Point2D> path = train.isInbound() ? ui.pointsInt : ui.pointsOut;
			
			int length = ui.simulation.getRoute().getLength();
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

		public String[] getInfo() {
			return new String[] {
				train.toString(),
				"Onboard: " + train.getPassengerCount(),
				"Location: " + train.getLocation(),
				train.isInbound() ? "-- Inbound --" : "-- Outbound --"
			};
		}

		public JPanel getInfoPanel() { return infoPanel; }
		public void setInfoPanel(JPanel panel) { this.infoPanel = panel; }
	}
	
	static class StationDrawInfo implements Descriptable {
		private static final int RANGE = 3;
		
		private SimulationUI ui;
		private JPanel infoPanel;
		
		private int lastWaitingCount;
		private Station station;
		private Color color;

		private Shape shape;
		
		public StationDrawInfo(Station station, SimulationUI ui) {
			this.ui = ui;
			this.station = station;
			this.color = Color.getHSBColor(0, 0, Logger.RAND.nextFloat() / 2);
		}
		
		public void draw(Graphics2D g2d) {
			if(shape == null) recalculate();
			
			g2d.setColor(color);
			g2d.fill(shape);
			
			int waitingCount = station.getInboundWaiting() + station.getOutboundWaiting();
			if(lastWaitingCount != waitingCount) {
				Rectangle2D bound = shape.getBounds2D();
				for(int i = 0, limit = Math.abs(waitingCount - lastWaitingCount); i < limit; i ++) {
					ui.addParticle(new Particle(bound.getCenterX(), bound.getCenterY(), waitingCount > lastWaitingCount));
				}
			}
			
			lastWaitingCount = waitingCount;
		}
		
		public void recalculate() {
			GeneralPath path = new GeneralPath();
			for(int i = -RANGE; i <= RANGE; i ++) {
				Point2D p = ui.pointsInt.get(getIndexForLocation(station.getLocation() + i, true));
				
				if(i == -RANGE) path.moveTo(p.getX(), p.getY());
				else path.lineTo(p.getX(), p.getY());
			}
			
			for(int i = RANGE; i >= -RANGE; i --) {
				Point2D p = ui.pointsOut.get(getIndexForLocation(station.getLocation() + i, false));
				path.lineTo(p.getX(), p.getY());
			}
//			path.closePath();
			shape = path;
		}
		
		private int getIndexForLocation(int location, boolean inbound) {
			ArrayList<Point2D> path = inbound ? ui.pointsInt : ui.pointsOut;
			int length = ui.simulation.getRoute().getLength();
			float locLength = (float) path.size() / length;
			
			return ((int) (location * locLength) + path.size()) % path.size();
		}
		
		public boolean contains(int x, int y) {
			return shape.contains(x, y);
		}

		public String[] getInfo() {
			return new String[] {
				station.toString(),
				"Inbound: " + station.getInboundWaiting(),
				"Outbound: " + station.getOutboundWaiting(),
				"Location: " + station.getLocation()
			};
		}

		public JPanel getInfoPanel() { return infoPanel; }
		public void setInfoPanel(JPanel panel) { this.infoPanel = panel; }
	}
	
	static class Particle {
		public static enum ParticleType {
			Add, Rem, Happy;
		}
		
		private static final Image ADD = new ImageIcon(Particle.class.getResource("add.png")).getImage();
		private static final Image REM = new ImageIcon(Particle.class.getResource("rem.png")).getImage();
		private static final Image OFF = new ImageIcon(Particle.class.getResource("off.png")).getImage();
		
		private static final float DEVIATION = 60f;		
		private static final float DEVIATION_SPEED = 2;
		
		private double x, y;
		private double dx, dy;
		
		private ParticleType type;
		private long life;
		
		public Particle(double x, double y, double dx, double dy) {
			this.x = x; 	this.y = y;
			this.dx = dx; 	this.dy = dy;
			this.type = ParticleType.Happy;
			
			this.life = (long) (100 + (Math.random() - .5) * 25);
		}
		
		public Particle(double x, double y, boolean add) {
			this.x = x; this.y = y;

			double devSpeed = (Math.random()) * DEVIATION_SPEED + .5f;
			double devAng = Math.toRadians((Math.random() - .5) * DEVIATION + 90);
			if(add) devAng += Math.PI;
			
			this.type = add ? ParticleType.Add : ParticleType.Rem;
			
			this.dx = Math.cos(devAng) * devSpeed;
			this.dy = Math.sin(devAng) * devSpeed;
		
			this.life = (long) (100 + (Math.random() - .5) * 25);
		}
		
		public void draw(Graphics2D g2d) {
//			g2d.setColor(dy > 0 ? Color.RED : Color.GREEN);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1, life / 100f)));
//			g2d.fillRect((int) x - 5, (int) y - 5, 10, 10);
			
			g2d.drawImage(type == ParticleType.Happy ? OFF : 
				type == ParticleType.Add ? ADD : REM, (int) x - 8, (int) y - 8, null);
		}
		
		public void simulate() {
			x += dx;
			y += dy;
			
			life --;
		}
		
		public boolean isDead() { return life < 0; }
	} 
}
