Group Number: 5

Breakdown of responsibilities:

Justin Whalley #101117127:   Test, JunitTestCases, Common
Yisheng Li     #101028686:   Scheduler
Yixiang Huang  #101071528:   FloorSubSystem
Gurjit Gill    #101110071:   ElevatorSubsystem
Everyone: 	            UML



Running Instruction:   

-Import Project
-Run Project/src/test/Test.java

Running Junit Instruction:

-Import Project
-Run Project/src/test/JunitTestCases.java

FloorSubsystem:
	- FileLoader.java
		*reads data file
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
	
Scheduler:
	-Scheduler.java
		*send/receive messages to/from elevator and floor subsystems
		*store states
	-ElevtState.java
		*store a state of an elevator
	-FloorState.java
		*store a state of a Floor

test:
	- Test.java
		*create data file
		*initialize FloorSubsystem, Elevator and scheduler
	- testFile.txt
		*hold the data
		
Junit:
	- run junit test cases on common and file loader 
	
Common:
	- holds methods that are common between classes(encode/decode messages)
