/**
 * 
 */
package GUI;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.*;

/**
 * @author jcwha
 *
 */
public class ElevatorPanel extends JPanel {

	private int elevNum;
	private JLabel curFloorLabel;
	private JLabel stateLabel;
	private JLabel destLabel;
	
	
	public ElevatorPanel(int elevNum) {
		this.elevNum = elevNum;
		setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),BorderFactory.createLineBorder(Color.BLACK)));
		setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		JPanel elevPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		elevPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		JLabel elevatorLabel = new JLabel("Elevator "+elevNum);
		elevatorLabel.setBorder(new EmptyBorder(10,10,10,10));
		elevPanel.add(elevatorLabel);
		add(elevPanel,BorderLayout.WEST);
		
		JPanel faultPanel = new JPanel(new FlowLayout());
		faultPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		JButton faultButton = new JButton("Fault");
		faultButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TextManager.print("Fault generated on elevator "+elevNum);
			}
		});
		faultButton.setBackground(Color.RED);
		faultPanel.add(faultButton);
		add(faultPanel, BorderLayout.CENTER);
		
		JPanel floorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		floorPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		curFloorLabel = new JLabel("Current Floor: " + 1);
		curFloorLabel.setBorder(new EmptyBorder(10,10,10,10));
		floorPanel.add(curFloorLabel);
		add(floorPanel);
		
		JPanel destPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		destPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		destLabel = new JLabel("Destination Floor: " + 1);
		destLabel.setBorder(new EmptyBorder(10,10,10,10));
		destPanel.add(destLabel);
		add(destPanel);
		
		JPanel statePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		statePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE,30));
		stateLabel = new JLabel("State: " + "Up");
		stateLabel.setBorder(new EmptyBorder(10,10,10,10));
		statePanel.add(stateLabel);
		add(statePanel);
		
		JPanel buttonPanel = new JPanel();
		GridLayout layout = new GridLayout((int)Math.ceil(GUI.FLOORS/6),GUI.FLOORS >= 6 ? 6 : GUI.FLOORS);
		layout.setHgap(20);
		layout.setVgap(20);
		buttonPanel.setLayout(layout);
		for(int i = 0; i < GUI.FLOORS; i++) {
			JButton button = new JButton(Integer.toString(i+1));
			button.setBounds(0, 0, 30, 25);
			button.setBorder(new RoundedBorder(5));
			button.setForeground(Color.BLACK);
			button.addActionListener(new ElevatorButtonListener(i+1,elevNum));
			buttonPanel.add(button);
		}
		add(buttonPanel);
	}
	
	public void update(int cur,int dest, String state) {
		curFloorLabel = new JLabel("Current Floor: " + cur);
		destLabel = new JLabel("Destination Floor: " + dest);
		stateLabel = new JLabel("State: " + state);
	}
}
