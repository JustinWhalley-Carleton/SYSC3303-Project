package GUI;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.plaf.basic.BasicArrowButton;

import ElevatorSubsystem.Elevator;
import ElevatorSubsystem.ElevatorSubsystem;
import FloorSubsystem.FloorSubSystem;
import FloorSubsystem.GUIFileLoader;
import Scheduler.Scheduler;
import common.Common;
import common.RPC;
import test.Test;

public class GUI extends JFrame{

	private static final boolean CREATE_ALL_SUBSYSTEMS = true;
	
	private File instructionFile = new File("src/test/settings.txt");
	private static int ROWS;
	private static int ELEVATORS;
	private static int ELEV_ERR;
	public static int FLOORS;
	public static double SPEED;
	public static ElevatorPanel[] elevatorPanels;
	private RPC transmitter;
	public static JTextArea textPanel;
	public static BasicArrowButton[] upButtons;
	public static BasicArrowButton[] downButtons;
	private Scheduler scheduler;
	private ElevatorSubsystem elev;
	private FloorSubSystem floor;
	private Thread floorThread;
	private Thread elevatorThread;
	private Thread schedulerThread;
	// Ports
	private static int SCHEDULER_RECV_GUI_PORT;
	private static int GUI_RECV_SCHEDULER_PORT;
	// Command Bridges
	private CommandBridge commandBridge_fault;
	private CommandBridge commandBridge_floor;
	private CommandBridge commandBridge_button;

	/**
	 * constructor for GUI 
	 */
	public GUI(boolean show) {
		// Read the settings file
		getSettings();
		GUIFileLoader.deleteFile();
		try {
			new Test(true).readSettings();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// Initialize communication between scheduler and GUI
		try {
			transmitter = new RPC(InetAddress.getLocalHost(), SCHEDULER_RECV_GUI_PORT, GUI_RECV_SCHEDULER_PORT);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Initialize Command bridge
		commandBridge_fault = new CommandBridge(CommandBridge.TYPE.FAULT, true);
		commandBridge_floor = new CommandBridge(CommandBridge.TYPE.FLOOR_BUTTON, true);
		commandBridge_button = new CommandBridge(CommandBridge.TYPE.ELEV_BUTTON, true);


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
			ElevatorPanel panel = new ElevatorPanel(i+1, commandBridge_fault, commandBridge_button);
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
        if(show) setVisible(true);
        
        // create and run a thread to continously wait on a message from scheduler and update GUI based on message
        Thread thread = new Thread() {
        	public void run() {
        		while(true) {
	       			byte[] msg = transmitter.receivePacket();
	       			int[] decodedMsg = Common.decode(msg);
	       			if(decodedMsg.length == 3) {
	       				elevatorPanels[decodedMsg[0]-1].personIn();
	       				System.out.println(decodedMsg[2]);
	       				if(decodedMsg[2]==1) {
	       					upButtons[decodedMsg[1]-1].setEnabled(true);
	       				} else {
	       					downButtons[decodedMsg[1]-1].setEnabled(true);
	       				}
	       			} else if(Common.findType(msg) == Common.TYPE.ELEV_ERROR) {
	       				if(Common.ELEV_ERROR.decode(msg)== Common.ELEV_ERROR.RECOVER) {
	       					System.out.println("Trying to recover");
	       					elevatorPanels[decodedMsg[0]-1].update(decodedMsg[1],decodedMsg[3],"Recovered");
	       					TextManager.print("Elevator "+decodedMsg[0]+" recovered from fault");
	       				} else {
	       					elevatorPanels[decodedMsg[0]-1].update(decodedMsg[1],decodedMsg[3],"FAULT");
	       				}
	       			} else if (Common.findType(msg) != Common.TYPE.CONFIRMATION) {
		       			String state;
		       			System.out.println("DECODED: "+ decodedMsg[0] + " " + decodedMsg[1] + " " + decodedMsg[2] + " " + decodedMsg[3]);
		       			if(decodedMsg[2] == 1) {
		       				state = "Up";
		       			} else if (decodedMsg[2]==0) {
		       				state = "Idle";
		        		} else {
		        			state = "Down";
		        		}
		        		elevatorPanels[decodedMsg[0]-1].update(decodedMsg[1],decodedMsg[3],state);
	        		}
	        	
        		}
        	}
        };
        thread.start();
        
        try {
        	if(CREATE_ALL_SUBSYSTEMS) {
	        	scheduler = new Scheduler(ELEVATORS,FLOORS,false);
	        	floor = new FloorSubSystem(true);
	        	elev = new ElevatorSubsystem(ELEVATORS,true,false);
	        	schedulerThread = new Thread(scheduler);
	        	elevatorThread = new Thread(elev);
	        	floorThread = new Thread(floor);
				schedulerThread.start();
				elevatorThread.start();
				floorThread.start();
        	}
			TextManager.print("Program now ready\n\n");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
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

				if (splitStr.length == 1 || line.contains("/")){
					// Empty line or Comment in setting file.
					continue;
				}

				// Get value
				int value = splitStr[1].contains(".") ? 0 : Integer.parseInt(splitStr[1]);
				// Assign value to its according variable
				switch(splitStr[0].trim()){
					case "ELEVATORS:" 	-> ELEVATORS 	= value;
					case "ROWS:"		-> ROWS 		= value;
					case "FLOORS:"		-> FLOORS 		= value;
					case "SPEED:"		-> SPEED 		= Double.parseDouble(splitStr[1]);
					case "ELEV_ERR:"	-> ELEV_ERR		= value;
					// GUI Ports
					case "GUI_RECV_SCHEDULER_PORT:"		-> GUI_RECV_SCHEDULER_PORT 		= value;
					case "SCHEDULER_RECV_GUI_PORT:"		-> SCHEDULER_RECV_GUI_PORT		= value;
					// Unsupported settings
					default -> {}
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
			upButton.addActionListener(new FloorButtonListener(i+1,true, commandBridge_floor));
			upButtons[i] = upButton;
			// add a label stating the floor number
			JLabel label = new JLabel(Integer.toString(i+1), SwingConstants.CENTER);
			// create a down arrow button
			BasicArrowButton downButton = new BasicArrowButton(BasicArrowButton.SOUTH);
			downButton.setBounds(0, 0, 30, 25);
			downButton.setBorder(new RoundedBorder(5));
			downButton.setForeground(Color.BLUE);
			downButton.addActionListener(new FloorButtonListener(i+1,false, commandBridge_floor));
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
	
	public ElevatorSubsystem getElev() {
		return elev;
	}
	
	public Scheduler getScheduler() {
		return scheduler;
	}
	
	public FloorSubSystem getFloor() {
		return floor;
	}
	
	public void stop() {
		elevatorThread.interrupt();
		schedulerThread.interrupt();
		floorThread.interrupt();
	}
	public static void main(String[] args) {
		new GUI(true);
	}

}
