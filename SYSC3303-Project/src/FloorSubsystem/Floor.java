package FloorSubsystem;

//Floor.java
//The Floor class controls the state of the individual floors

import java.util.ArrayList;

import Enum.Direction;
import Enum.FloorArrivalSensor;

public class Floor {

	/**
	 * floor number 
	 */
	private int floor;
	
	/**
	 * floor lamp for up Button
	 */
	private Direction upButton;
	
	
	/**
	 * floor lamp for down button
	 */
	private Direction downButton;
	
	/**
	 * arrival sensor
	 */
	private FloorArrivalSensor sensor;

	
	/**
	 * floor button lamps indicating which buttons have been pressed
	 */
	private ArrayList<Direction> floorButtonLamps;
	
	/**
	 * floor elevator direction lamp
	 */
	private Direction floorDirectionLamp;
	
	
	/**
	 * Constructor.
	 * 
	 * @param int floor: The floor number
	 */
	public Floor(int floor){
		
		this.floor = floor;
		
		//initial state
		floorButtonLamps = new ArrayList<Direction>();
		floorDirectionLamp = null;
		sensor = FloorArrivalSensor.OFF;
		
		//up&down buttons
		if(floor >= FloorSubsystem.MIN && floor <= FloorSubsystem.MAX) {
		
			if(floor == FloorSubsystem.MIN) {
				downButton = null;
				upButton = Direction.UP;
			}
			
			else if(floor == FloorSubsystem.MAX) {
				upButton = null;
				downButton = Direction.DOWN;
			}
			
			else {
				upButton = Direction.UP;
				downButton = Direction.DOWN;
			}
		}
		
	}
	
	
	/**
	 * Press the up button and add floorlamp to list
	 * 
	 * @return void
	 */
	public void pressUpButton() {
		if(upButton != null) {
			floorButtonLamps.add(upButton);		
		}
	}
	
	/**
	 * Press the down button and add floorlamp to list
	 * 
	 * @return void
	 */
	public void pressDownButton() {
		if(downButton != null) {
			floorButtonLamps.add(downButton);
		}
	}
	
	/**
	 * update state if elevator not present
	 * 
	 * @return void
	 */
	public void elevatorNotArriving(Direction direction) {
		floorDirectionLamp = null;
		sensor = FloorArrivalSensor.OFF;
	}
	
	/**
	 * update state when elevator arriving to floor
	 * 
	 * @return void
	 */
	public void elevatorArriving(Direction direction) {
		//remove floor button lamps upon arrival
		if(direction == Direction.UP) {
			floorButtonLamps.remove(Direction.UP);
		}
		else if(direction == Direction.DOWN) {
			floorButtonLamps.remove(Direction.DOWN);
		};
		
		//turn sensor on
		sensor = FloorArrivalSensor.ON;
		
		//set direction lamp to direction
		floorDirectionLamp = direction;
		
		System.out.println("Floor: Elevator arriving to Floor #"+getFloor()+"\n");
	}
	
	/**
	 * get the Floor Number
	 * 
	 * @return int
	 */
	public int getFloor() {
		return floor;
	}
	
	/**
	 * get the Arrival sensor
	 * 
	 * @return FloorArrivalSensor
	 */
	public FloorArrivalSensor getFloorArrivalSensor() {
		return sensor;
	}
	
	/**
	 * get direction lamp
	 * 
	 * @return Direction
	 */
	public Direction getDirectionLamp() {
		return floorDirectionLamp;	
	}
	
	/**
	 * get floorLamps
	 * 
	 * @return Direction
	 */
	public ArrayList<Direction> getFloorButtonLamps() {
		return floorButtonLamps;	
	}
	
	/**
	 * get upButton
	 * 
	 * @return Direction
	 */
	public Direction getUpButton() {
		if(upButton == null) return null;
		return upButton;	
	}
	
	/**
	 * get downButton
	 * 
	 * @return Direction
	 */
	public Direction getDownButton() {
		if(downButton == null) return null;
		return downButton;
	}
	
	/**
	 * print the state of the floors
	 * 
	 * @return void
	 */
	public void printInfo() {
		
		// print info
		System.out.println("======================");
		System.out.println("State for Floor #"+getFloor());
		System.out.println("Button(s) Pressed:");
		for(Direction dir: floorButtonLamps) {
			System.out.println(dir.toString()+" Button");
		}
		System.out.println("Sensor status: "+getFloorArrivalSensor().toString());
		if(getDirectionLamp() != null) {
			System.out.println("Elevator direction lamp: "+getDirectionLamp().toString());
		}
		System.out.println("======================\n");
	}
	
	
}
