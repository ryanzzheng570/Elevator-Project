package Scheduler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.ArrayList;
import java.util.LinkedList;

import Common.Constant;
import Common.Utility;
import Elevator.ElevatorCommand;
import Elevator.ElevatorSend;
import Enum.*;
import FloorSubsystem.FloorCommand;
/**
 * Class:								Scheduler
 * Description:							Controller module to manage elevators
 * 
 * @author								Calvin Soong, Zihao Zheng
 * @version 							0.0.1
 * @since 								2019/01/31
 */
public class Scheduler {
	protected final int INPUT_DATA_LENGTH = 1024;

	protected boolean isConnectedToFloorSystem = false;
	protected boolean isConnectedToElevatorSystem = false;

	protected InetAddress LOCAL_HOST_ADDRESS;

	protected DatagramSocket FLOOR_RECEIVE_SOCKET, FLOOR_SEND_SOCKET;
	protected DatagramSocket ELEVATOR_RECEIVE_SOCKET, ELEVATOR_SEND_SOCKET;

	protected LinkedList<Integer> elevator_up_queue;
	protected LinkedList<Integer> elevator_down_queue;
	protected int currentFloor;
	protected Direction currentDirection;
	
	protected ArrayList<Integer> elevators;

	/**
	 * Initialize the Scheduler and assign the local host address
	 */
	public Scheduler() {
		try {
			elevator_up_queue = new LinkedList<Integer>();
			elevator_down_queue = new LinkedList<Integer>();
			currentFloor = 0;
			currentDirection = null;
			elevators = new ArrayList<Integer>();
			
			this.LOCAL_HOST_ADDRESS = InetAddress.getLocalHost();
		} catch (IOException e) {
			System.out.println("Not able to obtain local host address");
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Try to connect to the floor subsystem
	 */
	protected void connectToFloorSystem() {
		try {
			System.out.println("Scheduler: connecting to Floor Subsystem");
			this.FLOOR_RECEIVE_SOCKET = new DatagramSocket(Constant.SCHEDULER_PORT, InetAddress.getLocalHost());
			this.FLOOR_SEND_SOCKET = new DatagramSocket();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		this.isConnectedToElevatorSystem = true;
		System.out.println("Connected to Floor Subsystem");
	}

	/**
	 * Try connect to the elevator subsystem
	 */
	protected void connectToElevatorSystem() {
		//Create send/receive sockets
		try {
			System.out.println("Scheduler: Creating Elevator subsystem daemon");
			ELEVATOR_RECEIVE_SOCKET = new DatagramSocket(Constant.ELEVATOR_SYSTEM_PORT, this.LOCAL_HOST_ADDRESS);
			ELEVATOR_SEND_SOCKET = new DatagramSocket();
			System.out.println("Scheduler: Elevator subsystem daemon created");
		} catch (IOException e) {
			System.out.println("Scheduler: Elevator subsystem daemon creation FAILED: IOException");
			e.printStackTrace();
			System.exit(1);
		} 
		//Knock on each elevator to find them
		for(int i = Constant.ELEVATOR_MIN_PORT; i <= Constant.ELEVATOR_MAX_PORT; i++) {
			try {
				//Send floor info at Elevator
				System.out.println("Scheduler: Checking for Elevator on PORT=" + i);
				String floorNumberString = Integer.toString(Constant.FLOOR_TOTAL);
				byte[] data = floorNumberString.getBytes();
				DatagramPacket outPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), i);
				ELEVATOR_SEND_SOCKET.send(outPacket);
				//Wait for response
				ELEVATOR_RECEIVE_SOCKET.setSoTimeout(2000);
				byte[] buffer = new byte[10];
				DatagramPacket inPacket = new DatagramPacket(buffer, buffer.length);
				ELEVATOR_RECEIVE_SOCKET.receive(inPacket);
				String elevatorResponseString = new String(buffer,0,buffer.length);
				//OK response and we're good to go
				if(elevatorResponseString.trim().equals("OK")){
					elevators.add(i);
				}
				else {
					throw new RuntimeException("BAD RESPONSE FROM ELEVATOR");
				}
				System.out.println("Scheduler: Elevator found on PORT=" + i);
				ELEVATOR_RECEIVE_SOCKET.setSoTimeout(0);
			} catch(SocketTimeoutException e) {
				System.out.println("Scheduler: Elevator subsystem ports retrieval FAILED: SocketTimeOutException");
			} catch(NumberFormatException e) {
				System.out.println("Scheduler: Elevator subsystem ports retrieval FAILED: NumberFormatException");
			} catch(IOException e) {
				System.out.println("Scheduler: Elevator subsystem ports retrieval FAILED: IOException");
			} catch(RuntimeException e) {
				System.out.println("Scheduler: Elevator subsystem ports retrieval FAILED: NOT OK RESPONSE FROM ELEVATOR");
			}
		}
		if(elevators.size() > 0) {
			this.isConnectedToElevatorSystem = true;
			System.out.println("Connected to Elevator Subsystems");
		}
		else {
			System.out.println("Cannot connect to Elevator Subsystems");
			System.exit(1);
		}
	}

	/**
	 * Establish the connection to elevator and floor subsystem if not connected
	 */
	protected void listen() {
		if (!isConnectedToElevatorSystem) {
			connectToElevatorSystem();
		}

		if (!isConnectedToFloorSystem) {
			connectToFloorSystem();
		}

		while (true) {
			// Implement the algorithm to schedule the elevator
			receiveFloorSignal();
			receiveElevatorSignal();
		}
	}

	/**
	 * Decompose the data sent from the floor sub system and generate appropriate
	 * signal for the elevator
	 */
	protected void receiveFloorSignal() {
		
		// Receive the input data from the floor subsystem
		byte[] inputDataByte = new byte[INPUT_DATA_LENGTH];
		DatagramPacket inputPacket = new DatagramPacket(inputDataByte, inputDataByte.length);
		try {
			this.FLOOR_RECEIVE_SOCKET.setSoTimeout(1000);

			this.FLOOR_RECEIVE_SOCKET.receive(inputPacket);
		} catch (IOException e) {
			//e.printStackTrace();
			//System.exit(1);
			return;
		}

		System.out.println("======================");
		System.out.println("Received packet from Floor Subsytem");
		FloorCommand command = Utility.serializeCommandObject(inputPacket.getData());
		System.out.println("Button pressed floor#: " + command.originFloor);
		System.out.println("Direction: " + command.directionButton);
		System.out.println("Destination Floor: " + command.destinationFloor);

		/*
		 * Determine whether the received floor is in the queue 
		 * Open the door if the arrival floor matches the current floor in the queue
		 */
		if (command.arrivalFloor != -1) {
			elevatorStopAtFloor(command.arrivalFloor);
		} else {
			// If there is an empty/idle elevator, use it
			if (elevator_up_queue.size() == 0 && elevator_down_queue.size() == 0) {	
				//We have to pickup the person at origin floor first
				if (currentFloor < command.originFloor) {
					sendToElevatorSystem("DIRECTION_UP");
					currentDirection = Direction.UP;
				}
				else {
					sendToElevatorSystem("DIRECTION_DOWN");
					currentDirection = Direction.UP;
				}
			} 
			
			addAndSortQueue(command.originFloor);
			addAndSortQueue(command.destinationFloor);
			System.out.println("Scheduler: current direction: " + currentDirection);
		}
	}

	/**
	 * Decompose the data sent from the elevator subsystem
	 */
	protected void receiveElevatorSignal() {
		byte[] inputDataByte = new byte[INPUT_DATA_LENGTH];
		DatagramPacket inputPacket = new DatagramPacket(inputDataByte, inputDataByte.length);

		try {
			this.ELEVATOR_RECEIVE_SOCKET.receive(inputPacket);
		} catch (IOException e) {
			//e.printStackTrace();
			//System.exit(1);
			return;
		}

		System.out.println("======================");
		System.out.println("Received packet from Elevator Subsytem");

		ElevatorSend command = Utility.serializeElevatorObject(inputPacket.getData());
		System.out.println("Scheduler: Elevator's current floor: " + command.floor);
		System.out.println("Scheduler: Elevator state: " + command.state);

		// Keep the current floor as a global in the scheduler
		currentFloor = command.floor;

		for (int i = 0; i < command.buttons.length; i++) {
			if(command.buttons[i] == Toggle.ON) {
				System.out.println("Scheduler: Pressed floors in elevator: " + i);
				addAndSortQueue(i);
			}
		}

		// Send command to elevator depending on its current state
		switch (command.state) {
		case STANDBY:
			LinkedList<Integer> currentQueue = elevator_up_queue;
			if (this.currentDirection == Direction.DOWN) {
				currentQueue = elevator_down_queue;
			}

			if (command.floor == currentQueue.getFirst()) {
				if(this.currentDirection == Direction.DOWN) {
					elevator_down_queue.removeFirst();
				} else {
					elevator_up_queue.removeFirst();
				}
				sendToElevatorSystem("OPEN_DOOR");
			} else if (currentQueue.isEmpty()) {
				String direction = "";
				
				//Switch direction
				if (currentDirection == Direction.UP) {
					direction = "DIRECTION_DOWN";
					currentDirection = Direction.DOWN;
				}
				else {
					direction = "DIRECTION_UP";
					currentDirection = Direction.UP;
				}
				sendToElevatorSystem(direction);
			}
			break;

		case WAITING_TO_START:
			sendToElevatorSystem("START_MOTOR");
			break;

		case MOVING:
			// Tell elevator to stop if it is in the queue
			elevatorStopAtFloor(command.floor);
			break;

		case OPEN:
			// Tell elevator to close door
			sendToElevatorSystem("CLOSE_DOOR");
			break;

		default:
			System.out.println("Scheduler: Received invalid elevator state");
		}
	}

	/**
	 * Convert ElevatorCommand to byte array
	 * 
	 * @param ec The ElevatorCommand object
	 * @return byte array
	 */
	protected static byte[] toStream(ElevatorCommand EC) {
		byte[] stream = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			oos.writeObject(EC);
			stream = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream;
	}

	/**
	 * Convert FloorCommand to byte array
	 * 
	 * TODO: duplicate of above function
	 * @param ec The FloorCommand object
	 * @return byte array
	 */
	protected static byte[] floorToStream(FloorCommand EC) {
		byte[] stream = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			oos.writeObject(EC);
			stream = baos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream;
	}
	
	/**
	 * Send a packet to the elevator
	 */
	protected void sendToElevatorSystem(String cmd) {
		byte[] sendData = new byte[INPUT_DATA_LENGTH];
		ElevatorCommand sendCmd = null;

		/*
		 * TO SEND: - Direction elevator should move if stationary - elevator door to
		 * close - when door closed, command motor to move
		 */
		switch (cmd) {
		case "CLOSE_DOOR":
			System.out.println("Scheduler: Sending command CLOSE_DOOR");
			sendCmd = new ElevatorCommand(ElevatorDoorCommand.CLOSE);
			break;

		case "OPEN_DOOR":
			System.out.println("Scheduler: Sending command OPEN_DOOR");
			sendCmd = new ElevatorCommand(ElevatorDoorCommand.OPEN);
			break;

		case "START_MOTOR":
			System.out.println("Scheduler: Sending command START_MOTOR");
			sendCmd = new ElevatorCommand(ElevatorMotorCommand.START);
			break;

		case "STOP_MOTOR":
			System.out.println("Scheduler: Sending command STOP_MOTOR");
			sendCmd = new ElevatorCommand(ElevatorMotorCommand.STOP);
			break;

		case "DIRECTION_UP":
			System.out.println("Scheduler: Sending command DIRECTION_UP");
			sendCmd = new ElevatorCommand(Direction.UP);
			break;

		case "DIRECTION_DOWN":
			System.out.println("Scheduler: Sending command DIRECTION_DOWN");
			sendCmd = new ElevatorCommand(Direction.DOWN);
			break;
		default:
			System.out.println("Scheduler: Received an invalid command to send to elevator.");
			break;
		}
		sendData = toStream(sendCmd);

		try {
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), elevators.get(0));
			ELEVATOR_SEND_SOCKET.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		/** Confirm the packet was received **/
		byte[] okByte = new byte[INPUT_DATA_LENGTH];
		DatagramPacket okPacket = new DatagramPacket(okByte, okByte.length);

		try {
			ELEVATOR_RECEIVE_SOCKET.receive(okPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		System.out.println("Received packet from Elevator Subsytem");
		String ok_response = new String(okPacket.getData()).trim();
		System.out.println("Scheduler: ok_response: " + ok_response);
	}

	/**
	 * Send a packet to the floor when the elevator 
	 * has arrived at the floor
	 */
	protected void sendToFloorSystem() {
		byte[] sendData = new byte[INPUT_DATA_LENGTH];

		System.out.println("Scheduler: Sending packet to floor");
		System.out.println("Scheduler: Current floor: " + currentFloor);
		System.out.println("Scheduler: Current Direction: " + currentDirection);

		FloorCommand cmd = new FloorCommand(currentFloor, currentDirection);
		sendData = floorToStream(cmd);
		
		try {
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getLocalHost(), Constant.FLOOR_SYSTEM_PORT);
			FLOOR_SEND_SOCKET.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Put the new floor request into the queue and sort
	 */
	protected void addAndSortQueue(int floorNum) {
		if (floorNum < 0 || floorNum > Constant.FLOOR_TOTAL) {
			System.out.println("Scheduler: Floor num is invalid");
			return;
		}
		
		if (currentDirection == Direction.UP) {
			if (!elevator_up_queue.contains(floorNum)) {
				elevator_up_queue.add(floorNum);
				Collections.sort(elevator_up_queue);	
				
				System.out.println("Scheduler: Current up queue: " + elevator_up_queue);
			}
		}
		else {
			if (!elevator_down_queue.contains(floorNum)) {
				elevator_down_queue.add(floorNum);
				Collections.sort(elevator_down_queue);
				//Reverse to have queue start from largest to smallest
				Collections.reverse(elevator_down_queue);
				System.out.println("Scheduler: Current down queue: " + elevator_down_queue);
			}	
		}
	}

	/**
	 * If the floor senses a elevator, check if the elevator should stop at this
	 * floor
	 * 
	 * @param floorNum  The floor number that sensed an elevator
	 */
	protected void elevatorStopAtFloor(int floorNum) {
		// Whenever elevator approaches floor, this is it's current floor
		currentFloor = floorNum;

		// Check if this floor is included in the elevator's queue
		if (currentDirection == Direction.UP) {
			if (elevator_up_queue.contains(floorNum)) {
				System.out.println("Scheduler: Up queue contains floor num, stopping motor");
				sendToElevatorSystem("STOP_MOTOR");
				sendToFloorSystem();
			}
		} else {
			if (elevator_down_queue.contains(floorNum)) {
				System.out.println("Scheduler: Down queue contains floor num, stopping motor");
				sendToElevatorSystem("STOP_MOTOR");
				sendToFloorSystem();
			}
		}
	}

	/**
	 * Run the Scheduler program
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		Scheduler scheduler = new Scheduler();
		scheduler.listen();
	}
}
