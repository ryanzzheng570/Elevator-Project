package Scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import Common.Constant;
import Common.Utility;
import Enum.Direction;
import FloorSubsystem.*;

/**

*/

public class FloorSendThread implements Runnable {
	private Scheduler s;
	private FloorSubsystem floors;
	private DatagramSocket FLOOR_SEND_SOCKET;
	private DatagramPacket FLOOR_SEND_PACKET;
	private InetAddress LOCAL_HOST_ADDRESS;

	public FloorSendThread(Scheduler s, FloorSubsystem floors) {
		this.s = s;
		this.floors = floors;
		try {
			this.LOCAL_HOST_ADDRESS = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			FLOOR_SEND_SOCKET = new DatagramSocket();
		} catch (SocketException e) {
			System.out.println("FLOOR SEND THREAD: COULD NOT CREATE RECEIVE SOCKET");
			e.printStackTrace();
		}
	}

	/**
	 * Waits to receive data from a elevator
	 */
	private void send(FloorCommand command) {
		//update state
		floorSendRequest(command.originFloor, command.directionButton);
		
		// print info
		System.out.println("======================\n");
		System.out.println("FLOOR SEND THREAD: SENDING");
		System.out.println("FLOOR SEND THREAD: Button pressed on Floor #" + command.originFloor);
		System.out.println("FLOOR SEND THREAD: Direction: " + command.directionButton);
		System.out.println("FLOOR SEND THREAD: Destination Floor #" + command.destinationFloor);

		// construct & send packet
		byte[] msg = Utility.deserializeCommandObject(command);
		try {
			FLOOR_SEND_PACKET = new DatagramPacket(msg, msg.length, InetAddress.getLocalHost(), Constant.SCHEDULER_PORT);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}

		// Send the datagram packet to the scheduler via the sendsocket.
		try {
			FLOOR_SEND_SOCKET.send(FLOOR_SEND_PACKET);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("FLOOR SEND THREAD: Packet sent.");
		System.out.println("======================\n");

	}

	public void run() {
		System.out.println("FLOOR SEND THREAD: STARTED");
		while (true) {
			send();
		}
	}
	
	private void floorSendRequest(int currentFloor, Direction direction) {
		
		//update state
		for(Floor floor:floors.getFloors()) {
			if (floor.getFloor()==currentFloor) {				
				if(direction == Direction.UP) floor.pressUpButton();
				else floor.pressDownButton();
			}
			floor.printInfo();
		}
	}
}
