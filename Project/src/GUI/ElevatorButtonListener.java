package GUI;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import FloorSubsystem.GUIFileLoader;

public class ElevatorButtonListener implements ActionListener{
	private int floorNum;
	private int elevNum;
	private CommandBridge commandBridge;
	
	/**
	 * constructor for an elevator button listener
	 * @param floorNum
	 * @param elevNum
	 */
	public ElevatorButtonListener(int floorNum,int elevNum, CommandBridge bridge) {
		this.floorNum = floorNum;
		this.elevNum = elevNum;
		this.commandBridge = bridge;
	}
	
	/**
	 * react to a elevator button click
	 */
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() instanceof JButton) {
			// print to scroll panel, write to command file, change color of button and disable the button
			TextManager.print("Floor: "+floorNum+ " button clicked on elevator "+elevNum);
			// send button
			commandBridge.send(elevNum, floorNum);
			((JButton)e.getSource()).setBackground(Color.YELLOW);
			((JButton)e.getSource()).setEnabled(false);
			
		}
	}
}
