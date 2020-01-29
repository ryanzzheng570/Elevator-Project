package FloorSubsystem;

//FloorCommand.java
//FloorCommand class will be sent from/to the FloorSubsystem indicating requests & arrivals

import java.io.Serializable;

import Enum.Direction;


@SuppressWarnings("serial")
public class FloorCommand implements Serializable {
	public int originFloor;
	public Direction directionButton;
	public int destinationFloor;
	
	public int arrivalFloor = -1;
	
	public Direction directionLamp;
	
	/**
	 * Constructor.
	 * 
	 * @param int requestedFloor
	 * @param FloorButton direction
	 * @param int destinationFloor
	 */
	public FloorCommand(int requestedFloor, Direction direction, int destinationFloor) {
		this.originFloor = requestedFloor;
		this.directionButton = direction;
		this.destinationFloor = destinationFloor;
	}
	
	/**
	 * Constructor.
	 * 
	 * @param int arrivingFloor
	 */
	public FloorCommand(int arrivingFloor) {
		this.arrivalFloor = arrivingFloor;
	}
	
	/**
	 * Constructor
	 * 
	 * @param int arrivingFloor
	 * @param direction 
	 */
	public FloorCommand(int arrivingFloor, Direction direction) {
		this.arrivalFloor = arrivingFloor;
		this.directionLamp = direction;
	}
	
}
