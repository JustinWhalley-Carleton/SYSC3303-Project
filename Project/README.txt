Group Number: 5

Breakdown of responsibilities:

Justin Whalley #101117127:   Test
Yisheng Li     #101028686:   Scheduler	
Yixiang Huang  #101071528:   FloorSubSystem
Gurjit Gill    #101110071:   ElevatorSubsystem
Everyone: 	            UML



Running Instruction:   

-Import Project
-Run Project/src/test/Test.java

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
	- Elevator.java
		*communicates with scheduler
	
Scheduler:
	- pass the message from elevator to floor and vice versa

test:
	- Test.java
		*create data file
		*initialize FloorSubsystem, Elevator and scheduler
	- testFile.txt
		*hold the data
