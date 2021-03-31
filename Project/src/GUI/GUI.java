package GUI;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.basic.BasicArrowButton;

import common.Common;
import common.RPC;

public class GUI extends JFrame{

	private File instructionFile = new File("src/test/settings.txt");
	private static int ROWS;
	private static int ELEVATORS;
	private static int ELEV_ERR;
	public static int FLOORS;
	public static int SPEED;
	private ElevatorPanel[] elevatorPanels;
	private RPC transmitter;
	public static JTextArea textPanel;
	public static BasicArrowButton[] upButtons;
	public static BasicArrowButton[] downButtons;
	
	/**
	 * constructor for GUI 
	 */
	public GUI() {
		//read the settings file 
		getSettings();
		try {
			// initialize communication between scheduler and GUI
			transmitter = new RPC(InetAddress.getLocalHost(), 5, 6);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//create an array of panels for elevators
		elevatorPanels = new ElevatorPanel[ELEVATORS];
		
		//set the size of the frame to max
		setExtendedState(JFrame.MAXIMIZED_BOTH); 
		
		// set title, close program on GUI exit
		setTitle("Elevator System");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		//split the frame into 2 rows
		setLayout(new GridLayout(2,1));
		
		// add elevator panels to each elecator section
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1,ELEVATORS));
		for(int i = 0; i < ELEVATORS; i++) {
			ElevatorPanel panel = new ElevatorPanel(i+1);
			topPanel.add(panel);
			elevatorPanels[i] = panel;
		}
        
		// split the bottom panel into 3 so the floor buttons take 66% of the bottom and the scroll panel take 33%
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(1,3));
		for(JPanel panel: floorPanel()) {
			bottomPanel.add(panel);
		}
        bottomPanel.add(textPanel());
		
        // add top and bottom panels to the frame
        add(topPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // dont allow resizing of the frame and show frame on screen
        setResizable(false);
        setVisible(true);
        
        // create and run a thread to continously wait on a message from scheduler and update GUI based on message
        Thread thread = new Thread() {
        	public void run() {
        		while(true) {
	       			byte[] msg = transmitter.receivePacket();
	       			int[] decodedMsg = Common.decode(msg);
	       			if(Common.findType(msg) == Common.TYPE.ELEV_ERROR) {
	       				elevatorPanels[decodedMsg[0]].update(decodedMsg[1],decodedMsg[3],"FAULT");
	       			} else if (Common.findType(msg) != Common.TYPE.CONFIRMATION) {
		       			String state;
		       			if(decodedMsg[1] == 1) {
		       				state = "Up";
		       			} else if (decodedMsg[1]==0) {
		       				state = "Idle";
		        		} else {
		        			state = "Down";
		        		}
		        		elevatorPanels[decodedMsg[0]].update(decodedMsg[1],decodedMsg[3],state);
	        		}
	        	
        		}
        	}
        };
        thread.start();
        
        TextManager.print("Program now ready\n\n");
	}
	
	/**
	 * read and save settings from settings.txt
	 */
	private void getSettings() {
		try {
			Scanner scanner = new Scanner(instructionFile);
			while(scanner.hasNextLine()) {
				String line = scanner.nextLine();
				String[] splitStr = line.trim().split("\\s+");

				// Get value
				int value = Integer.parseInt(splitStr[1]);
				// Assign value to its according variable
				switch(splitStr[0].trim()){
					case "ELEVATORS:" 	-> ELEVATORS 	= value;
					case "ROWS:"		-> ROWS 		= value;
					case "FLOORS:"		-> FLOORS 		= value;
					case "SPEED:"		-> SPEED 		= value;
					case "ELEV_ERR:"	-> ELEV_ERR		= value;
					default -> System.out.println("Unexpected item in settings file.");
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * create the panels containing the floor up and down buttons
	 * @return JPanel[] 
	 */
	private JPanel[] floorPanel() {
		JPanel[] panels = new JPanel[2];
		panels[0] = new JPanel();
		panels[1] = new JPanel();
		// set the grid to be at max 8 floor buttons wide
		panels[0].setLayout(new GridLayout((int)Math.ceil((double)FLOORS/8.0), FLOORS >= 4 ? 4 : FLOORS));
		panels[1].setLayout(new GridLayout((int)Math.ceil((double)FLOORS/8.0), FLOORS >= 4 ? (FLOORS >= 8 ? 4 : FLOORS-4) : 1));
		upButtons = new BasicArrowButton[FLOORS];
		downButtons = new BasicArrowButton[FLOORS];
		for(int i = 0; i < FLOORS; i++) {
			JPanel tempPanel = new JPanel(new GridLayout(3,1));
			//center panel
			tempPanel.setLayout(new BoxLayout(tempPanel,BoxLayout.Y_AXIS));
			// add border with padding 
			tempPanel.setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10),BorderFactory.createLineBorder(Color.BLACK)));
			// create an up arrow button
			BasicArrowButton upButton = new BasicArrowButton(BasicArrowButton.NORTH);
			upButton.setBounds(0, 0, 30, 25);
			upButton.setBorder(new RoundedBorder(5));
			upButton.setForeground(Color.BLUE);
			upButton.addActionListener(new FloorButtonListener(i+1,true));
			upButtons[i] = upButton;
			// add a label stating the floor number
			JLabel label = new JLabel(Integer.toString(i+1), SwingConstants.CENTER);
			// create a down arrow button
			BasicArrowButton downButton = new BasicArrowButton(BasicArrowButton.SOUTH);
			downButton.setBounds(0, 0, 30, 25);
			downButton.setBorder(new RoundedBorder(5));
			downButton.setForeground(Color.BLUE);
			downButton.addActionListener(new FloorButtonListener(i+1,false));
			downButtons[i] = downButton;
			// add buttons and label to the temp panel
			tempPanel.add(upButton);
			tempPanel.add(label, BorderLayout.CENTER);
			tempPanel.add(downButton);
			// add to the left or right panel depending on where the index is
			if(i%8 < 4) {
				panels[0].add(tempPanel);
			} else {
				panels[1].add(tempPanel);
			}
		}
		
		return panels;
	}
	
	/**
	 * create a scrollable text panel to write to
	 * @return JPanel
	 */
	private JPanel textPanel() {
		textPanel = new JTextArea();
		// dont allow user editing
		textPanel.setEditable(false);
		JScrollPane scrollPane = new JScrollPane(textPanel);
		scrollPane.setPreferredSize(new Dimension(400,400));
		JPanel panel = new JPanel();
		TextManager.print("Messages will appear here\nProgram starting...\n");
		panel.add(scrollPane);
		return panel;
	}
	
	public static void main(String[] args) {
		new GUI();
       
	}

}
