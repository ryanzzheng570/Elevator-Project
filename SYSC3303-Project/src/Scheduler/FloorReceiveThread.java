package Scheduler;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import Common.Constant;
import Enum.Direction;
import FloorSubsystem.*;

/**

*/

public class FloorReceiveThread implements Runnable{
	private Scheduler s;
	private FloorSubsystem floors;
	private DatagramSocket FLOOR_RECEIVE_SOCKET;
	private DatagramPacket FLOOR_RECEIVE_PACKET;
	private InetAddress LOCAL_HOST_ADDRESS;
	
	public FloorReceiveThread(Scheduler s, FloorSubsystem floors){
		this.s = s;
		this.floors = floors;
		try {
			this.LOCAL_HOST_ADDRESS = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			FLOOR_RECEIVE_SOCKET = new DatagramSocket(Constant.FLOOR_SYSTEM_PORT, this.LOCAL_HOST_ADDRESS);
		} catch (SocketException e) {
			System.out.println("FLOOR RECEIVE THREAD: COULD NOT CREATE RECEIVE SOCKET");
			e.printStackTrace();
		}
	}
	
	/**
	 * Waits to receive data from a elevator
	 */
	private void receive() {
		byte [] data = new byte[512];
		FLOOR_RECEIVE_PACKET = new DatagramPacket(data, data.length);
		FloorCommand in = null;
		System.out.println("======================\n");
		System.out.println("FLOOR RECEIVE THREAD: RECEIVING");
		
		try {
			FLOOR_RECEIVE_SOCKET.receive(FLOOR_RECEIVE_PACKET);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		System.out.println("FLOOR RECEIVE THREAD: DATA RECEIVED");
		in = Common.Utility.serializeCommandObject(data);
		if (in != null) {
			int arrivingFloor = in.arrivalFloor;
			Direction dir = in.directionLamp;
			System.out.println("Floor: Arriving Floor #" + arrivingFloor);
			System.out.println("Floor: Received Direction: " + dir.toString());
			System.out.println("======================\n");
			
			//update state
			for(Floor floor:floors.getFloors()) {
				if(floor.getFloor() == arrivingFloor) floor.elevatorArriving(dir);
				else floor.elevatorNotArriving(dir);	
				floor.printInfo();
			}


		}
		
		
	}
	
	public void run () {
		System.out.println("FLOOR RECEIVE THREAD: STARTED");
		while (true) {
			receive();
		}
	}
}
