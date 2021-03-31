package GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ElevatorButtonListener implements ActionListener{
	private int floorNum;
	private int elevNum;
	
	public ElevatorButtonListener(int floorNum,int elevNum) {
		this.floorNum = floorNum;
		this.elevNum = elevNum;
	}
	
	public void actionPerformed(ActionEvent e) {
		System.out.print("Floor: "+floorNum+ " button clicked on elevator "+elevNum);
		TextManager.print("Floor: "+floorNum+ " button clicked on elevator "+elevNum);
	}
}
