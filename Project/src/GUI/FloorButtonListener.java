package GUI;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import FloorSubsystem.GUIFileLoader;

public class FloorButtonListener implements ActionListener{
	private int floorNum;
	private boolean up;
	private CommandBridge commandBridge;
	
	/**
	 * constructor for a floor button listener
	 * @param floorNum
	 * @param up
	 */
	public FloorButtonListener(int floorNum, boolean up, CommandBridge bridge) {
		this.floorNum = floorNum;
		this.up = up;
		this.commandBridge = bridge;
	}
	
	/**
	 * react to a floor button click
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton) {
			//print to scroll panel, add to command file, set color to yellow and disable button
			TextManager.print("Floor: "+floorNum+ " " + (up? "up ":"down ")+" button clicked");
			commandBridge.send(floorNum, up ? 1 : 0);
			((JButton)e.getSource()).setForeground(Color.YELLOW);
			((JButton)e.getSource()).setEnabled(false);
		}
	}
	
}
