package edu.wit.comp2000.group36.train.visual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;
import javax.swing.plaf.PanelUI;

import edu.wit.comp2000.group36.train.Station;
import edu.wit.comp2000.group36.train.Train;

public class InfoUI extends PanelUI {
	private static final int PADDING = 5;
	private static final Font FONT = new Font("", Font.PLAIN, 12*2);
	private static final FontRenderContext FRC = new FontRenderContext(null, true, true);
	private String[] info;
	
	public InfoUI(Object obj) {
		this.info = getInfoBlock(obj);
	}
	
	public void paint(Graphics g, JComponent c) {
		Graphics2D g2d = (Graphics2D) g;
		
		int width = c.getWidth() - 1;
		int height = c.getHeight() - 1;
		
		g2d.setColor(new Color(210, 180, 140));
		g2d.fillRect(0, 0, width, height);
		
		g2d.setFont(FONT);
		g2d.setColor(Color.BLACK);
		int x = PADDING, y = -PADDING;
		for(String line : info) {
			y += FONT.getStringBounds(line, FRC).getHeight();
			g2d.drawString(line, x, y);
		}
		
		g2d.setColor(Color.BLACK);
		g2d.drawRect(0, 0, width, height);
	}
	
	private String[] getInfoBlock(Object obj) {
		if(obj instanceof Station) {
			Station station = (Station) obj;
			return new String[] {
				station.toString(),
				"Location: " + station.getLocation()
			};
		}
		
		if(obj instanceof Train) {
			Train train = (Train) obj;
			return new String[] {
				train.toString(),
				"Location: " + train.getLocation()
			};
		}
		
		return new String[] { obj.toString() };
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
}
