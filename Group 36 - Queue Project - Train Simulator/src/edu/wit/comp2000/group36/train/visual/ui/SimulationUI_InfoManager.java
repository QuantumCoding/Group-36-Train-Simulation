package edu.wit.comp2000.group36.train.visual.ui;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import edu.wit.comp2000.group36.train.visual.ui.InfoPanel.Descriptable;
import edu.wit.comp2000.group36.train.visual.ui.SimulationUIComponents.StationDrawInfo;
import edu.wit.comp2000.group36.train.visual.ui.SimulationUIComponents.TrainDrawInfo;

public class SimulationUI_InfoManager implements MouseMotionListener, MouseListener {
	private SimulationUI ui;
	
	private Object infoObj;
	protected InfoPanel info;
	
	public SimulationUI_InfoManager(SimulationUI ui) {
		this.ui = ui;
	}
	
	private Point convert(Point p) {
		return new Point((int)((p.x - ui.shiftX) / (float) ui.scale * 500), (int)((p.y - ui.shiftY) / (float) ui.scale * 500));
	} 
	
	public void mouseMoved(MouseEvent e) {
		JPanel panel = ((JPanel) e.getComponent());
		Point p = convert(e.getPoint());
		
		Descriptable select = getSelectedDescriptable(p.x, p.y);
		
		if(select != null && select == infoObj) {
			if(info == null) return;
			info.setLocation(p.x, p.y);
			panel.repaint();
			return;
		}

		if(select != null) {
			info = null;
			if(select.getInfoPanel() != null) return;
			
			infoObj = select;
			info = new InfoPanel(select);
			info.setLocation(p.x, p.y);
			
			panel.repaint();
			return;
		}
		
		if(info != null) {
			infoObj = null;
			info = null;
			
			panel.repaint();
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		JPanel panel = ((JPanel) e.getComponent());
		Point p = convert(e.getPoint());
		
		Descriptable select = getSelectedDescriptable(p.x, p.y);
		
		if(e.getButton() == MouseEvent.BUTTON1) {
			if(select != null && select == infoObj) {
				info.setLocation(p.x, p.y);
				select.setInfoPanel(info);
				panel.repaint();
				
				infoObj = null;
				info = null;
				return;
			}
		}
		
		if(e.getButton() == MouseEvent.BUTTON3) {
			if(select != null && select.getInfoPanel() != null) {
				select.setInfoPanel(null);
				mouseMoved(e);
				return;
			} 
		}
	}
	
	private Descriptable getSelectedDescriptable(int x, int y) {
		for(TrainDrawInfo train : ui.trains) {
			if(train.contains(x, y)) {
				return train;
			}
		}
		
		for(StationDrawInfo station : ui.stations) {
			if(station.contains(x, y)) {
				return station;
			}
		}
		
		return null;
	}

	public void mouseDragged(MouseEvent e) {}

	public void mousePressed(MouseEvent e) { }
	public void mouseReleased(MouseEvent e) { }
	public void mouseEntered(MouseEvent e) { }
	public void mouseExited(MouseEvent e) { }
}
