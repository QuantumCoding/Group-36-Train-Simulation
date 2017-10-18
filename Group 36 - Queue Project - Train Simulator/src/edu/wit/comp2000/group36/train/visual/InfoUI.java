package edu.wit.comp2000.group36.train.visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.plaf.PanelUI;

public class InfoUI extends PanelUI {
	private static final int PADDING = 5;
	private static final Font FONT = new Font("", Font.PLAIN, 12);
	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
	
	private Descriptable src;
	private String[] info;
	
	private int shiftX, shiftY;
	
	public InfoUI(Descriptable src) {
		this.src = src;
		info = src.getInfo();
	}
	
	public void paint(Graphics g, JComponent c) {
		Graphics2D g2d = (Graphics2D) g;
		
		info = src.getInfo();
		c.setSize(getPreferredSize(c));
		
		int width = c.getWidth();
		int height = c.getHeight();
		
		Point location = c.getLocation();
		Dimension limit = c.getParent().getSize();
		
		if(location.getX() + width > limit.getWidth()) {
			shiftX = -width;
			c.setLocation(location.x + shiftX, location.y);
		} else if(location.getX() + width + shiftX < 0) {
			shiftX = width;
			c.setLocation(location.x + shiftX, location.y);
		}

		location = c.getLocation();
		if(location.getY() + height > limit.getHeight()) {
			shiftY = -height;
			c.setLocation(location.x, location.y + shiftY);
			
		} else if(location.getY() + height + shiftY < 0) {
			shiftY = height;
			c.setLocation(location.x, location.y + shiftY);
		}
		
		width -= 1; height -= 1;
		
		g2d.setColor(new Color(210, 180, 140));
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
	}

	public Dimension getPreferredSize(JComponent c) {
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
		
		public JPanel getInfoPanel();
		public void setInfoPanel(JPanel panel);
	}
}
