Group Number: 5

Breakdown of responsibilities:

Justin Whalley #101117127:   Test, JunitTestCases, Common, Timer, TimerController, FloorSubSystem UDP, Elevator faults, GUI
Yisheng Li     #101028686:   Scheduler and its relative classes
Yixiang Huang  #101071528:   ElevatorSubSystem, Common, RPC, Test faults, Floor subsystem, GUI UDP
Gurjit Gill    #101110071:   Elevator, Fileloader errorfile
Everyone: 	   UML



Running Instruction:   

-Import Project
-Run Project/src/test/Test.java



Running GUI: 

-Import Project
-Run Project/src/GUI/GUI.java


Running Junit Instruction:

-Import Project
-Run Project/src/test/JunitTestCases.java

FloorSubsystem:
	- FileLoader.java
		*reads data file
		* reads error file
	- Floor.java
		*represents each floor
	- FloorButton.java
		*represents an up down button
	- FloorSubsystem.java 
		*communicates with scheduler

ElevatorSubsystem:
	- MotorState.java interface
		*Up.java 
		*Down.java
		*idle.java
	- ElevatorButton.java
		*represents a button within the elevator 
	- ElevatorSubsystem.java
		*communicates with scheduler
	- Elevator.java
		*responds to messages for elevator
		* handles faults 
	
Scheduler:
	-Scheduler.java
		*send/receive messages to/from elevator and floor subsystems
		*store states
		*scheduling for elevators
		*handles stuck elevators
	-ElevtState.java
		*store a state of an elevator
	-FloorState.java
		*store a state of a Floor
	

Timer:
    - TimerController.java
        *intermediate between timer and elevator
    - TimerThread.java
        *sleep until interrupted for specified time & notify timer controller on completion

GUI:
    - CommandBridge.java
        *connect GUI to the system by UDP (new GUI fileloader)
    - ElevatorPanel.java
	*construct Elevator Panel that holds elevator panels and buttons
    - ElevatorButtonListener.java
        *Listen for elevator button click
    - FloorButtonListener.java
        *Listen for floor button click
    - GUI.java
        *main function shows whole GUI



test:
	- Test.java
		*create data file
		*initialize FloorSubsystem, Elevator and scheduler
		* creates error file
	- testFile.txt
		*hold the data
	- settings.txt
	    *hold # elev # floor # speed # command
		
Junit:
	- run junit test cases on common and file loader 
	
Common:
	- holds methods that are common between classes(encode/decode messages; decode and print out byte arrays)
