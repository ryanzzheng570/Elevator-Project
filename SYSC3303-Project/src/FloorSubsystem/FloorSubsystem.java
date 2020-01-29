package FloorSubsystem;

// FloorSubsystem.java
// This class is the FloorSubsystem for a Floor-Scheduler-Elevator system
// It sends and receives Datagram Packets to/from the scheduler

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

import Common.Constant;
import Common.Utility;
import Enum.Direction;

public class FloorSubsystem {

	/**
	 * Sockets & Packets
	 */
	private DatagramPacket sendPacket, receivePacket;
	private DatagramSocket sendSocket, receiveSocket;

	/**
	 * Constants
	 */
	private static final int INPUT_DATA_LENGTH = 1024;

	/**
	 * Top floor
	 */
	public static final int MAX = 6;
	
	/**
	 * Bottom floor
	 */
	public static final int MIN = 1;
	
	
	/**
	 * Floors
	 */
	private ArrayList<Floor> floors;

	/**
	 * Constructor.
	 * 
	 * Create the floors & send/receive sockets
	 */
	public FloorSubsystem() {
		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets.
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(Constant.FLOOR_SYSTEM_PORT);

			// create floors
			floors = new ArrayList<Floor>();
			for(int i = 1; i <= MAX; i ++) {
				floors.add(new Floor(i));
			}

		} catch (SocketException se) {
			System.out.println("Cannot create socket");
			se.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Send the packet to the scheduler
	 * 
	 * @return void
	 */
	private void send(FloorCommand command) {
		// update the state of the floor
		floorSendRequest(command.originFloor, command.directionButton);

		// print info
		System.out.println("======================");
		System.out.println("Floor: Sending Packet");
		System.out.println("Sending packet to Scheduler");
		System.out.println("Button pressed on Floor #" + command.originFloor);
		System.out.println("Direction: " + command.directionButton);
		System.out.println("Destination Floor #" + command.destinationFloor);
		

		// construct & send packet
		byte[] msg = Utility.deserializeCommandObject(command);
		try {
			sendPacket = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), Constant.SCHEDULER_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Send the datagram packet to the scheduler via the sendsocket.
		try {
			sendSocket.send(sendPacket);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Floor: Packet sent.");
		System.out.println("======================\n");
	}

	/**
	 * Update the state of the floor where the request was made
	 * 
	 * @return void
	 */
	private void floorSendRequest(int currentFloor, Direction direction) {
		
		//update state
		for(Floor floor:floors) {
			if (floor.getFloor()==currentFloor) {				
				if(direction == Direction.UP) floor.pressUpButton();
				else floor.pressDownButton();
			}
			floor.printInfo();
		}
	}

	/**
	 * Update the state of the floor where the request was received
	 * 
	 * @return ArrayList<String>
	 */
	private void floorReceiveRequest(int arrivingFloor, Direction direction) {
		
		//update state
		for(Floor floor:floors) {
			if(floor.getFloor() == arrivingFloor) floor.elevatorArriving(direction);
			else floor.elevatorNotArriving(direction);	
			floor.printInfo();
		}

	}

	/**
	 * Wait to receive the packet from the scheduler
	 * 
	 * @return void
	 */
	private void receive() {
		// receive packet
		System.out.println("======================");
		System.out.println("Floor: Receiving...");
		
		//create datapacket
		byte[] inputDataByte = new byte[INPUT_DATA_LENGTH];
		receivePacket = new DatagramPacket(inputDataByte, inputDataByte.length);
		try {
			receiveSocket.receive(receivePacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		//print info
		System.out.println("Floor: Received packet from Floor Scheduler");
		FloorCommand command = Utility.serializeCommandObject(receivePacket.getData());
		int arrivingFloor = command.arrivalFloor;
		Direction dir = command.directionLamp;
		System.out.println("Floor: Arriving Floor #" + arrivingFloor);
		System.out.println("Floor: Received Direction: " + dir.toString());
		System.out.println("======================\n");
		
		// update floor state
		floorReceiveRequest(arrivingFloor, dir);

	}

	/**
	 * Parse the input text and return the commands in arraylist of strings
	 * 
	 * @return ArrayList<String>
	 */
	public static ArrayList<String> getCommands() {
		String pattern = "\\d\\d(:)\\d\\d(:)\\d\\d(\\.)\\d\\d\\d\\s\\d\\s(UP|DOWN)\\s\\d";
		String inputFileName = "input.txt";
		ArrayList<String> commands = new ArrayList<String>();

		// prepare the input file
		System.out.println("Input File Name: input.txt");

		File input = new File(inputFileName);
		Scanner inputFile;
		try {
			inputFile = new Scanner(input);
			while (inputFile.hasNextLine()) {
				String line = inputFile.nextLine();
				if (line.matches(pattern)) {
					commands.add(line);
				}
			}
			inputFile.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(0);
		}

		return commands;
	}

	/**
	 * Send the commands to the scheduler and wait to receive commands from
	 * scheduler
	 * 
	 * @return void
	 */
	public static void main(String args[]) {
		FloorSubsystem system = new FloorSubsystem();
		ArrayList<String> commands = getCommands();

		//send commands to scheduler
		for (int i = 0; i < commands.size(); i++) {

			// parse the commands
			String[] split = commands.get(i).split("\\s");
			String timestamp = split[0];
			int floor = Integer.valueOf(split[1]);
			String floorButton = split[2];
			int carButton = Integer.valueOf(split[3]);

			// create command & send to scheduler
			FloorCommand command = new FloorCommand(floor, Direction.valueOf(floorButton), carButton);
			system.send(command);
			
			// sleep
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		//receive packages from scheduler
		while (true) {
			system.receive();
		}
	}

	public ArrayList<Floor> getFloors() {
		return floors;
	}



}
