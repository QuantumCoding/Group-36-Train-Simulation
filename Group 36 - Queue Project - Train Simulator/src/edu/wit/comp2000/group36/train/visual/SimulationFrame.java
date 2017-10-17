package edu.wit.comp2000.group36.train.visual;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.EmptyBorder;

import edu.wit.comp2000.group36.train.Simulation;

public class SimulationFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = -4489738892518264663L;
	public static void main(String[] args) { new SimulationFrame(); }
	
	private static final int MIN_SIMULATION_SPEED = 250;
	private static final int MAX_SIMULATION_SPEED = 10;
	private static final int SIMULATION_SPEED_RANGE = MIN_SIMULATION_SPEED - MAX_SIMULATION_SPEED;
	
	private JButton stepButton;
	private JToggleButton runButton;
	private JCheckBox pauseCheckBox;
	private JSlider speedSlider;
	
	private JPanel simulationPanel;
	private SimulationUI simulationUI;
	
	private boolean pauseSignal;
	private Object simulationLock = new Object();
	private Thread simulationThread = new Thread(() ->  {
		while(true) {
			while(pauseSignal) { 
				synchronized(simulationLock) { 
					try { simulationLock.wait(); } 
					catch(InterruptedException ignore) { }
				}
			}
			
			step();
			
			synchronized(simulationUI) {
				try { simulationUI.wait(); } 
				catch(InterruptedException ignore) { }	
			}
		}
		
	}, "Simulation - Thread");
	
	private Simulation simulation;
	
	public SimulationFrame() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
		simulation = new Simulation();
		simulationThread.setDaemon(true);
		
		setTitle("Group 36 - Train Simulation");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		JPanel contentPanel = new JPanel();
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPanel);
		contentPanel.setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		buttonPanel.setLayout(new BorderLayout(5, 3));
		
		stepButton = new JButton("Step");
		stepButton.setFocusPainted(false);
		buttonPanel.add(stepButton, BorderLayout.EAST);
		
		JSeparator topSeparator = new JSeparator();
		buttonPanel.add(topSeparator, BorderLayout.NORTH);
		
		speedSlider = new JSlider();
		speedSlider.setFocusable(false);
		buttonPanel.add(speedSlider, BorderLayout.CENTER);
		speedSlider.setMinorTickSpacing(5);
		speedSlider.setMajorTickSpacing(10);
		speedSlider.setPaintTicks(true);
		
		JPanel runPanel = new JPanel();
		buttonPanel.add(runPanel, BorderLayout.WEST);
		runPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		runButton = new JToggleButton("Run");
		runButton.setFocusPainted(false);
		runPanel.add(runButton);
		
		pauseCheckBox = new JCheckBox("Pause at Station");
		runPanel.add(pauseCheckBox);
		
		simulationPanel = new JPanel();
		simulationPanel.setUI(simulationUI = new SimulationUI(simulation));
		contentPanel.add(simulationPanel, BorderLayout.CENTER);
		
		runButton.addActionListener(this);
		stepButton.addActionListener(this);
		
		pauseSignal = true;
		simulationThread.start();
		
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	private void step() { 
		long delay = (long) ((1 - speedSlider.getValue() / 100d) * SIMULATION_SPEED_RANGE + MAX_SIMULATION_SPEED);
		
		simulation.step();
		simulationUI.prepSimulation(delay);
		simulationPanel.repaint(); 
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == stepButton) { 
			step(); return; 
		}
		
		if(e.getSource() == runButton) {
			pauseSignal = !runButton.isSelected();
			
			if(!pauseSignal) { 
				synchronized(simulationLock) { simulationLock.notifyAll(); }
			}
			
			return; 
		}
	}
}
