package GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FloorButtonListener implements ActionListener{
	private int floorNum;
	private boolean up;
	
	public FloorButtonListener(int floorNum, boolean up) {
		this.floorNum = floorNum;
		this.up = up;
	}
	
	public void actionPerformed(ActionEvent e) {
		System.out.print("Floor: "+floorNum+ " " + (up? "up ":"down ")+" button clicked");
		TextManager.print("Floor: "+floorNum+ " " + (up? "up ":"down ")+" button clicked");
	}
	
}
