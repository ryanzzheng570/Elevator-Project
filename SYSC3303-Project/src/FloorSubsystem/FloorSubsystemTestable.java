package FloorSubsystem;

import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import Common.Constant;
import Enum.Direction;

public class FloorSubsystemTestable {

	/**
	 * Sockets & Packets
	 */
	private DatagramSocket sendSocket, receiveSocket;

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

	public FloorSubsystemTestable() {

		try {
			// Construct a datagram socket and bind it to any available
			// port on the local host machine. This socket will be used to
			// send and receive UDP Datagram packets.
			sendSocket = new DatagramSocket();
			receiveSocket = new DatagramSocket(Constant.FLOOR_SYSTEM_PORT);

			// create floors
			floors = new ArrayList<Floor>();
			for (int i = 1; i <= MAX; i++) {
				floors.add(new Floor(i));
			}

		} catch (SocketException se) {
			System.out.println("Cannot create socket");
			se.printStackTrace();
			System.exit(1);
		}

	}
	
	/**
	 * Update the state of the floor where the request was made
	 * 
	 * @return void
	 */
	public void floorSendRequest(int currentFloor, Direction direction) {

		//update state
		for(Floor floor:floors) {
			if (floor.getFloor()==currentFloor) {				
				if(direction == Direction.UP) floor.pressUpButton();
				else floor.pressDownButton();
				
			}
		}

	}

	/**
	 * Update the state of the floor where the request was received
	 * 
	 * @return ArrayList<String>
	 */
	public void floorReceiveRequest(int arrivingFloor, Direction direction) {
		
		//update state
		for(Floor floor:floors) {
			if(floor.getFloor() == arrivingFloor) floor.elevatorArriving(direction);
			else floor.elevatorNotArriving(direction);	
			
		}

	}

	/**
	 * get send socket
	 * 
	 * @return DatagramSocket
	 */
	public DatagramSocket getSendSocket() {
		return sendSocket;
	}

	/**
	 * get receive socket
	 * 
	 * @return DatagramSocket
	 */
	public DatagramSocket getReceiveSocket() {
		return receiveSocket;
	}

	/**
	 * get floor arraylist
	 * 
	 * @return ArrayList<Floor>
	 */
	public ArrayList<Floor> getFloors() {
		return floors;
	}

}
