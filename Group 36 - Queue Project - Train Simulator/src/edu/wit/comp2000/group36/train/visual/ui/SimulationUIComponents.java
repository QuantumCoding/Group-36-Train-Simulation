package edu.wit.comp2000.group36.train.visual.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.ImageIcon;

import edu.wit.comp2000.group36.train.Logger;
import edu.wit.comp2000.group36.train.Station;
import edu.wit.comp2000.group36.train.Train;
import edu.wit.comp2000.group36.train.visual.ui.InfoPanel.Descriptable;

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
		private InfoPanel infoPanel;
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
					double speed = (Math.random() + .5) * 10 + 5;
					ui.addParticle(new Particle(p.getX(), p.getY(), Math.cos(angle) * speed, Math.sin(angle) * speed));
				}
			}
			lastOnboard = onboard;
		}
		
		public void drawInfo(Graphics2D g2d) {
			if(infoPanel != null) infoPanel.paint(g2d);
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

		public InfoPanel getInfoPanel() { return infoPanel; }
		public void setInfoPanel(InfoPanel panel) { this.infoPanel = panel; }
	}
	
	static class StationDrawInfo implements Descriptable {
		private static final int RANGE = 5;
		
		private SimulationUI ui;
		private InfoPanel infoPanel;
		
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
			
			g2d.setColor(Color.RED);
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
		
		public void drawInfo(Graphics2D g2d) {
			if(infoPanel != null) infoPanel.paint(g2d);
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
			
			Point2D pIn = ui.pointsInt.get(getIndexForLocation(station.getLocation(), true));
			Point2D pOut = ui.pointsOut.get(getIndexForLocation(station.getLocation(), false));
			double size = pIn.distance(pOut) / 3;
			
			Area a = new Area(recalculateIsland(true, -size));
			a.add(new Area(recalculateIsland(false, size)));
			shape = a;
		}
		
		public Shape recalculateIsland(boolean inside, double size) {
			ArrayList<Point2D> src = inside ? ui.pointsInt : ui.pointsOut;
			
			ArrayList<Point2D> points = new ArrayList<>();
			int count = src.size();
			
			for(int i = -RANGE; i <= RANGE; i ++) {
				int index = getIndexForLocation(station.getLocation() + i, inside);
				
				Point2D p0 = src.get((index - 10 + count) % count);
				Point2D p2 = src.get((index + 10 + count) % count);
				
				double length = p0.distance(p2);
				double dy = ( p2.getX() - p0.getX()) / length;
				double dx = (-p2.getY() + p0.getY()) / length;
				
				Point2D p = src.get(index);
				points.add(new Point2D.Double(p.getX() - dx * size * 1, p.getY() - dy * size * 1));
				points.add(new Point2D.Double(p.getX() - dx * size * 3, p.getY() - dy * size * 3));
			}

			GeneralPath path = new GeneralPath();
			
			Point2D start = points.get(0);
			path.moveTo(start.getX(), start.getY());
			
			for(int i = 2; i < points.size(); i += 2) {
				Point2D p = points.get(i);
				path.lineTo(p.getX(), p.getY());
			}
			
			for(int i = points.size() - 1; i >= 1; i -= 2) {
				Point2D p = points.get(i);
				path.lineTo(p.getX(), p.getY());
			}
			
			path.closePath();
			return path;
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

		public InfoPanel getInfoPanel() { return infoPanel; }
		public void setInfoPanel(InfoPanel panel) { this.infoPanel = panel; }
	}
	
	static class Particle {
		public static enum ParticleType {
			Add, Rem, Happy;
		}
		
		private static final Image ADD = new ImageIcon(Particle.class.getResource("add.png")).getImage();
		private static final Image REM = new ImageIcon(Particle.class.getResource("rem.png")).getImage();
		private static final Image OFF = new ImageIcon(Particle.class.getResource("off.png")).getImage();
		
		private static final float DEVIATION = 60f;		
		private static final float DEVIATION_SPEED = 10;
		private static final float DEVIATION_ROT = (float) Math.PI / 8 * 3;
		
		private static final float DEVIATION_AGE = 5;
		
		private static final float BASE_AGE = 10;
		
		private double x, y, r;
		private double dx, dy, dr;
		
		private ParticleType type;
		private double age, maxAge;
		
		public Particle(double x, double y, double dx, double dy) {
			this.x = x; 	this.y = y;
			this.dx = dx; 	this.dy = dy;
			this.type = ParticleType.Happy;
			
			this.dr = (Math.random() - .5) * DEVIATION_ROT;
			
			this.age = (long) (BASE_AGE + (Math.random() - .5) * DEVIATION_AGE);
			this.maxAge = 3;
			
			age /= 2; maxAge *= 2;
		}
		
		public Particle(double x, double y, boolean add) {
			this.x = x; this.y = y;

			double devSpeed = Math.random() * DEVIATION_SPEED + 10f;
			double devAng = Math.toRadians((Math.random() - .5) * DEVIATION + 90);
			if(add) devAng += Math.PI;
			
			this.type = add ? ParticleType.Add : ParticleType.Rem;
			
			this.dx = Math.cos(devAng) * devSpeed;
			this.dy = Math.sin(devAng) * devSpeed;
			
			this.dr = (Math.random() - .5) * DEVIATION_ROT;

			this.age = (long) (BASE_AGE + (Math.random() - .5) * DEVIATION_AGE);
			this.maxAge = BASE_AGE;
		}
		
		public void draw(Graphics2D g2d) {
//			g2d.setColor(dy > 0 ? Color.RED : Color.GREEN);
			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) Math.min(1, age / maxAge)));
//			g2d.fillRect((int) x - 5, (int) y - 5, 10, 10);
			
			g2d.rotate(r, x - 8, y - 8);
			g2d.drawImage(type == ParticleType.Happy ? OFF : 
				type == ParticleType.Add ? ADD : REM, (int) x - 8, (int) y - 8, null);
			g2d.rotate(-r, x - 8, y - 8);

			g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
		}
		
		public void simulate(double delta) {
			x += dx * delta;
			y += dy * delta;
			
			r += dr * delta;
			
			age -= delta;
		}
		
		public boolean isDead() { return age < 0; }
	} 
}
