package Scheduler;

import java.net.DatagramSocket;
import java.util.LinkedList;

public class SchedulerTestable extends Scheduler {
	
	public SchedulerTestable() {
		super();
	}
	
	public LinkedList<Integer> getElevatorUpQueue(){
		return elevator_up_queue;
	}
	
	public LinkedList<Integer> getElevatorDownQueue() {
		return elevator_down_queue;
	}
	
	public DatagramSocket getFloorReceiveSocket() {
		return FLOOR_RECEIVE_SOCKET;
	}
	
	public DatagramSocket getFloorSendSocket() {
		return FLOOR_SEND_SOCKET;
	}
	
	public DatagramSocket getElevatorReceiveSocket() {
		return ELEVATOR_RECEIVE_SOCKET;
	}
	
	public DatagramSocket getElevatorSendSocket() {
		return ELEVATOR_SEND_SOCKET;
	}
	
	public void addAndSortQueue(int num) {
		super.addAndSortQueue(num);
	}

	public void connect() {
		super.listen();
	}
}
