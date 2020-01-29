package Test;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import Elevator.ElevatorSubsystem;
import FloorSubsystem.FloorSubsystem;
import Scheduler.Scheduler;
import Scheduler.SchedulerTestable;

public class SchedulerTest {
	private Scheduler scheduler;
	
	private static SchedulerTestable schedulerTestable;
	
	/**
	 * Initialize the scheduler testable to test the scheduler functions
	 */
	@BeforeClass
	public static void beforeClass() {
		schedulerTestable = new SchedulerTestable();
	}
	
	/**
	 * Test if Floor and Elevator is not running whether the scheduler will be connected or not
	 */
	@Test
	public void connectionTest() {
		schedulerTestable.connect();
		Assert.assertTrue(!schedulerTestable.getFloorSendSocket().isConnected());
	}
	
	/*
	 * Add a negative floor value to queue
	 */
	@Test
	public void addNegativeToQueueTest() {
		schedulerTestable.addAndSortQueue(-1);
		Assert.assertTrue(schedulerTestable.getElevatorDownQueue().size() == 0);
	}
	
	/*
	 * Add a max floor value to queue
	 */
	@Test
	public void addMaxToQueueTest() {
		schedulerTestable.addAndSortQueue(100);
		Assert.assertTrue(schedulerTestable.getElevatorDownQueue().size() == 0);
	}
	
	/*
	 * Add a normal floor value to queue
	 */
	@Test
	public void addFloorToQueueTest() {
		schedulerTestable.addAndSortQueue(3);
		Assert.assertTrue(schedulerTestable.getElevatorDownQueue().getFirst() == 3);
	}
	
	/*
	 * Add two floors to queue and sort
	 */
	@Test
	public void addTwoFloorsToQueueTest() {
		schedulerTestable.addAndSortQueue(1);
		schedulerTestable.addAndSortQueue(3);
		//Down queue is sorted greatest to least
		Assert.assertTrue(schedulerTestable.getElevatorDownQueue().getFirst() == 3);
		Assert.assertTrue(schedulerTestable.getElevatorDownQueue().get(1) == 1);
	}
	
	public static void main(String[] args) {
		Result result = JUnitCore.runClasses(SchedulerTest.class);
		for(Failure f : result.getFailures()) {
			System.out.println("JUnit Test Failed: " + f.toString());
		}
		System.out.println("JUnit Sucess Result: " + result.wasSuccessful());
	}
}
