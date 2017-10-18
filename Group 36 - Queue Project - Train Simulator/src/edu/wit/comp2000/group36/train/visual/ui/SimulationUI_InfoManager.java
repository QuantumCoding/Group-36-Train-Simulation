package edu.wit.comp2000.group36.train.visual.ui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;

import edu.wit.comp2000.group36.train.visual.InfoUI;
import edu.wit.comp2000.group36.train.visual.InfoUI.Descriptable;
import edu.wit.comp2000.group36.train.visual.ui.SimulationUIComponents.StationDrawInfo;
import edu.wit.comp2000.group36.train.visual.ui.SimulationUIComponents.TrainDrawInfo;

public class SimulationUI_InfoManager implements MouseMotionListener, MouseListener {
	private SimulationUI ui;
	
	private Object infoObj;
	private JPanel info;
	
	public SimulationUI_InfoManager(SimulationUI ui) {
		this.ui = ui;
	}
	
	public void mouseMoved(MouseEvent e) {
		JPanel panel = ((JPanel) e.getComponent());
		Descriptable select = getSelectedDescriptable(e.getX(), e.getY());
		
		if(select != null && select == infoObj) {
			if(info == null) return;
			info.setLocation(e.getX(), e.getY());
			panel.repaint();
			return;
		}

		if(select != null) {
			if(info != null) panel.remove(info);
			if(select.getInfoPanel() != null) return;
			
			infoObj = select;
			info = new JPanel();
			info.setUI(new InfoUI(select));
			info.setLocation(e.getX(), e.getY());
			info.setSize(info.getPreferredSize());
			panel.add(info);
			panel.setComponentZOrder(info, 0);
			panel.repaint();
			return;
		}
		
		if(info != null) {
			panel.remove(info);
			infoObj = null;
			info = null;
			
			panel.repaint();
		}
	}
	
	public void mouseClicked(MouseEvent e) {
		JPanel panel = ((JPanel) e.getComponent());
		Descriptable select = getSelectedDescriptable(e.getX(), e.getY());
		
		if(e.getButton() == MouseEvent.BUTTON1) {
			if(select != null && select == infoObj) {
				info.setLocation(e.getX(), e.getY());
				panel.repaint();
				
				select.setInfoPanel(info);
				infoObj = null;
				info = null;
				return;
			}
		}
		
		if(e.getButton() == MouseEvent.BUTTON3) {
			if(select != null && select.getInfoPanel() != null) {
				JPanel infoPanel = select.getInfoPanel();
				panel.remove(infoPanel);
				panel.repaint();
				select.setInfoPanel(null);
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
