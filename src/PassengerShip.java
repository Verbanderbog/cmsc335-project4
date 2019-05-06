
import java.util.*;

/*
Dylan Veraart
3/23/2019
PassengerShip.java
PassengerShip objects are child classes of Ship objects and are created when 
'pship' lines in the open file are processed by the World object.
 */
public class PassengerShip extends Ship {

  int numberOfOccupiedRooms, numberOfPassenger, numberOfRooms;

  public PassengerShip(Scanner sc) throws InvalidInputException {
    super(sc);
    if (sc.hasNextInt()) {
      numberOfPassenger = sc.nextInt();
    }
    if (sc.hasNextInt()) {
      numberOfRooms = sc.nextInt();
    }
    if (sc.hasNextInt()) {
      numberOfOccupiedRooms = sc.nextInt();
    } else {
      throw new InvalidInputException();
    }
  }//Builds PassengerShip when line found in file

  @Override
  public String toString() {
    return "Passenger " + super.toString();
  }//returns PassengerShip name and index as a string with proper label
  
  public HashMap<String,String> toMap(){
    HashMap<String,String> retMap = super.toMap();
    retMap.put("Class", "Passenger Ship");
    retMap.put("Occupied Rooms", Integer.toString(numberOfOccupiedRooms));
    retMap.put("Total Passengers", Integer.toString(numberOfPassenger));
    retMap.put("Total Rooms", Integer.toString(numberOfRooms));

    return retMap;
  }
}
