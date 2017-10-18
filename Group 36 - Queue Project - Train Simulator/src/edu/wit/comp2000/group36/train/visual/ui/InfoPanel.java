package edu.wit.comp2000.group36.train.visual.ui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class InfoPanel {
	
	private static final int PADDING = 5;
	private static final Font FONT = new Font("", Font.PLAIN, 12);
	private static final Color BG_COLOR = new Color(210, 180, 140);
	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);

	private Descriptable src;
	private Point2D location;
	
	public InfoPanel(Descriptable src) {
		this.src = src;
	}

	public void setLocation(double x, double y) { this.location = new Point2D.Double(x, y); }
	public void setLocation(Point2D p) { this.location = p; }
	
	public Point2D getLocation() { return location; }
	
	public void paint(Graphics2D g2d) {
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .8f));
		String[] info = src.getInfo();
		Dimension size = getPreferredSize(info);
		
		Rectangle b = g2d.getDeviceConfiguration().getBounds();
		double shiftX = 0, shiftY = 0;
		
		if(location.getX() + size.getWidth() > b.getWidth()) {
			shiftX = -size.getWidth();
		}
		
		if(location.getY() + size.getHeight() > b.getHeight()) {
			shiftY = -size.getHeight();
		}
		
		g2d.translate(location.getX() + shiftX, location.getY() + shiftY);
		
		int width = size.width;
		int height = size.height;
		
		g2d.setColor(BG_COLOR);
		g2d.fillRect(0, 0, width, height);
		
		g2d.setFont(FONT);
		g2d.setColor(Color.BLACK);
		int x = PADDING, y = 0;
		for(String line : info) {
			y += FONT.getStringBounds(line, FRC).getHeight();
			g2d.drawString(line, x, y);
		}
		
		g2d.setColor(Color.BLACK);
		g2d.drawRect(0, 0, width, height);
		
		g2d.translate(-(location.getX() + shiftX), -(location.getY() + shiftY));
		g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
	}
	
	public Dimension getPreferredSize(String[] info) {
		double width = 0, height = 0;
		
		for(String line : info) {
			Rectangle2D bound = FONT.getStringBounds(line, FRC);
			width = Math.max(width, bound.getWidth());
			height += bound.getHeight();
		}
		
		return new Dimension((int) (width + PADDING * 2), (int) (height + PADDING * 2));
	}
	
	public static interface Descriptable {
		public String[] getInfo();
		
		public InfoPanel getInfoPanel();
		public void setInfoPanel(InfoPanel panel);
	}
}
