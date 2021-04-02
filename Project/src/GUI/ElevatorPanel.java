/**
 * 
 */
package GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.*;

import FloorSubsystem.GUIFileLoader;

/**
 * @author jcwha
 *
 */
public class ElevatorPanel extends JPanel {

	private int elevNum;
	private JLabel curFloorLabel;
	private JLabel stateLabel;
	private JLabel destLabel;
	private JButton faultButton;
	private JButton[] buttons;
	private boolean faultState = false;
	private boolean buttonsActive = false;
	private int fontSize = 20;
	
	/**
	 * constructor for an elevator panel
	 * @param elevNum
	 */
	public ElevatorPanel(int elevNum) {
		this.elevNum = elevNum;
		// add border and padding 
		setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),BorderFactory.createLineBorder(Color.BLACK)));
		// center the panel
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		// add title to the panel (Elevator elevatorNumber)
		JPanel elevPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		elevPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		JLabel elevatorLabel = new JLabel("Elevator "+elevNum);
		elevatorLabel.setBorder(new EmptyBorder(10,10,10,10));
		elevPanel.add(elevatorLabel);
		add(elevPanel,BorderLayout.WEST);
		
		// add a fault button to the elevator
		JPanel faultPanel = new JPanel(new FlowLayout());
		faultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		faultButton = new JButton("Fault");
		faultButton.addActionListener(new ActionListener() {
			//react to a click
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() instanceof JButton) {
					// print to scroll panel, write to command file, change background to signify button is disabled and set button disabled (no double faulting)
					TextManager.print("Fault generated on elevator "+elevNum);
					GUIFileLoader.writeToFile(0, elevNum, -1);
					((JButton)e.getSource()).setBackground(Color.GRAY);
					((JButton)e.getSource()).setEnabled(false);
				}
			}
		});
		faultButton.setBackground(Color.RED);
		faultPanel.add(faultButton);
		add(faultPanel, BorderLayout.CENTER);
		
		// create a panel containing the currrent floor label 
		JPanel floorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		floorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		curFloorLabel = new JLabel("Current Floor: " + 1);
		curFloorLabel.setBorder(new EmptyBorder(10,10,10,10));
		floorPanel.add(curFloorLabel);
		add(floorPanel);
		
		// create a panel with the destination floor label
		JPanel destPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		destPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		destLabel = new JLabel("Destination Floor: " + 1);
		destLabel.setBorder(new EmptyBorder(10,10,10,10));
		destPanel.add(destLabel);
		add(destPanel);
		
		// create a panel with the current state label
		JPanel statePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		stateLabel = new JLabel("State: " + "Idle");
		stateLabel.setBorder(new EmptyBorder(10,10,10,10));
		statePanel.add(stateLabel);
		add(statePanel);
		
		// create a panel containing buttons for each floor
		JPanel buttonPanel = new JPanel();
		GridLayout layout = new GridLayout((int)Math.ceil(GUI.FLOORS/6),GUI.FLOORS >= 6 ? 6 : GUI.FLOORS);
		layout.setHgap(20);
		layout.setVgap(20);
		buttons = new JButton[GUI.FLOORS];
		buttonPanel.setLayout(layout);
		for(int i = 0; i < GUI.FLOORS; i++) {
			JButton button = new JButton(Integer.toString(i+1));
			button.setBounds(0, 0, 30, 25);
			button.setBorder(new RoundedBorder(5));
			button.setForeground(Color.BLACK);
			button.addActionListener(new ElevatorButtonListener(i+1,elevNum));
			buttons[i] = button;
			buttonPanel.add(button);
		}
		Helper.turnAllButtonsOff(buttons);
		add(buttonPanel);
		
		curFloorLabel.setFont(new Font("Serif",Font.PLAIN,fontSize));
		destLabel.setFont(new Font("Serif",Font.PLAIN,fontSize));
		stateLabel.setFont(new Font("Serif",Font.PLAIN,fontSize));
		elevatorLabel.setFont(new Font("Serif",Font.PLAIN,fontSize));
	}
	
	/**
	 * update the labels and button states 
	 * @param cur
	 * @param dest
	 * @param state
	 */
	public void update(int cur,int dest, String state) {
		// disable all elevator buttons when in fault state
		if(state.equals("FAULT")) {
			Helper.turnAllButtonsOff(buttons);
			buttonsActive = false;
			faultState = true;
			stateLabel.setText("State: " + state);
			return;
		}
		// reenable fault button and all elevator buttons when in non fault state
		if(state.equals("Recovered")) {
			buttonsActive = true;
			System.out.println("RECOVERING");
			faultButton.setEnabled(true);
			faultButton.setBackground(Color.RED);
			Helper.turnAllButtonsOn(buttons);
			stateLabel.setText("State: " + "Idle");
			return;
		}
		// update labels
		curFloorLabel.setText("Current Floor: " + cur);
		destLabel.setText("Destination Floor: " + (dest == -1 ? cur : dest));
		stateLabel.setText("State: " + state);
		
		if(state.equals("Idle") && !GUIFileLoader.elevHasCommand(elevNum)) {
			buttonsActive = false;
			Helper.turnAllButtonsOff(buttons);
		}
		
		
		// reenable elevator button and reset color if floor reached
		if(!buttons[cur-1].isEnabled() && buttonsActive) {
			buttons[cur-1].setEnabled(true);
			buttons[cur-1].setBackground(null);
		}
	}
	
	/**
	 * turn on all buttons for floor reached
	 */
	public void personIn() {
		buttonsActive = true;
		Helper.turnAllButtonsOn(buttons);
	}
}
