
package Test;


import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import Enum.Direction;
import Enum.FloorArrivalSensor;
import FloorSubsystem.Floor;
import FloorSubsystem.FloorSubsystem;
import FloorSubsystem.FloorSubsystemTestable;


public class FloorSubsystemTest {

	private static FloorSubsystemTestable floorTest;
	
	
	/**
	 * Create object FloorSubsystemTestable
	 * 
	 */
	@BeforeClass
	public static void beforeClass() {
		floorTest = new FloorSubsystemTestable();
	}

	/**
	 * check socket connection if scheduler not enabled
	 * 
	 */
	@Test
	public void connectionSendSocket() {
		
		Assert.assertFalse(floorTest.getSendSocket().isConnected());
	}
	
	/**
	 * check socket connection if scheduler not enabled
	 * 
	 */
	@Test
	public void connectionReceiveSocket() {
		
		Assert.assertFalse(floorTest.getReceiveSocket().isConnected());
	}
	
	/**
	 * check if floor objects have been created
	 * 
	 */
	@Test
	public void floorsCreated() {
		Assert.assertTrue(floorTest.getFloors().size() == FloorSubsystem.MAX);
	}
	
	
	/**
	 * check initial state of floor ONE
	 * 
	 */
	@Test
	public void floorOneInitialState() {
		Floor floor = floorTest.getFloors().get(FloorSubsystem.MIN-1);
		Assert.assertTrue(floor.getFloor()==1 && floor.getFloorArrivalSensor() == FloorArrivalSensor.OFF && floor.getDirectionLamp() == null && floor.getFloorButtonLamps().isEmpty()
				&& floor.getUpButton() == Direction.UP && floor.getDownButton() == null);
		
	}
	
	/**
	 * check initial state of floor SIX
	 * 
	 */
	@Test
	public void floorSixInitialState() {
		Floor floor = floorTest.getFloors().get(FloorSubsystem.MAX-1);
		Assert.assertTrue(floor.getFloor()==6 && floor.getFloorArrivalSensor() == FloorArrivalSensor.OFF && floor.getDirectionLamp() == null && floor.getFloorButtonLamps().isEmpty()
				&& floor.getUpButton() == null && floor.getDownButton() == Direction.DOWN);
	}
	
	/**
	 * check state of floor which request was made
	 * 
	 */
	@Test
	public void sendRequest() {
		int requestedFloor = 2;
		Direction dir = Direction.UP;
		floorTest.floorSendRequest(2, dir);
		Floor floorTwo = floorTest.getFloors().get(requestedFloor - 1);
		
		Assert.assertTrue(floorTwo.getFloorButtonLamps().contains(dir));
	}
	
	/**
	 * check state of floor when elevator arrives
	 * 
	 */
	@Test
	public void elevatorArrived() {
		int arrivingFloor = 2;
		Direction dir = Direction.UP;
		floorTest.floorReceiveRequest(arrivingFloor, dir);
		Floor floorTwo = floorTest.getFloors().get(arrivingFloor - 1);
		
		Assert.assertTrue(!(floorTwo.getFloorButtonLamps().contains(dir)) && (floorTwo.getFloorArrivalSensor()==FloorArrivalSensor.ON) &&  (floorTwo.getDirectionLamp()==dir));
	}

	/**
	 * check state of floor when elevator has not arrived
	 * 
	 */
	@Test
	public void elevatorNotArrived() {
		int arrivingFloor = 4;
		Direction dir = Direction.UP;
		floorTest.floorReceiveRequest(arrivingFloor, dir);
		Floor floorTwo = floorTest.getFloors().get(1);
		
		Assert.assertTrue((floorTwo.getFloorArrivalSensor()==FloorArrivalSensor.OFF) &&  (floorTwo.getDirectionLamp()==null));
	}
	
	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(FloorSubsystemTest.class);
		for (Failure f : result.getFailures()) {
			System.out.println("JUnit Test Failed: " + f.toString());
		}
		System.out.println("JUnit Success Result: " + result.wasSuccessful());
	}

}
