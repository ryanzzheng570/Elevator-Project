package Common;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import Elevator.ElevatorSend;
import FloorSubsystem.FloorCommand;

public class Utility {
	
	public static byte[] deserializeCommandObject(FloorCommand command) {
		byte[] stream = null;
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(baos);) {
			oos.writeObject(command);
			stream = baos.toByteArray();
			oos.close();
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return stream;
	}

	public static FloorCommand serializeCommandObject(byte[] commandByteArray) {
		FloorCommand command = null;
		
		try (ByteArrayInputStream bais = new ByteArrayInputStream(commandByteArray);
	            ObjectInputStream ois = new ObjectInputStream(bais);) {
	        command = (FloorCommand) ois.readObject();
	        ois.close();
	        bais.close();
	    } catch (IOException e) {
	        // Error in de-serialization
	        e.printStackTrace();
	    } catch (ClassNotFoundException e) {
	        // You are converting an invalid stream to Student
	        e.printStackTrace();
	    }
		return command;
	}
	
	//FIXME: rename function?
	public static ElevatorSend serializeElevatorObject(byte[] commandByteArray) {
		ElevatorSend command = null;
		
		try (ByteArrayInputStream bais = new ByteArrayInputStream(commandByteArray);
	            ObjectInputStream ois = new ObjectInputStream(bais);) {
	        command = (ElevatorSend) ois.readObject();
	        ois.close();
	        bais.close();
	    } catch (IOException e) {
	        // Error in de-serialization
	        e.printStackTrace();
	    } catch (ClassNotFoundException e) {
	        e.printStackTrace();
	    }
		return command;
	}
	
}
