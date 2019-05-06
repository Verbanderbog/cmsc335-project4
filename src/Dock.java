
import java.util.HashMap;
import java.util.Scanner;

/*
Dylan Veraart
3/23/2019
Dock.java
Dock objects are created when the file being opened has a 'dock' line. Docks
can hold single ship object and have a parent port.
 */
public class Dock extends Thing {

  Ship ship;

  public Dock(Scanner sc) throws InvalidInputException {
    super(sc);
    if (sc.hasNextInt()) {
      ship = new Ship("ERROR: Unknown Ship", sc.nextInt(), index);
    } else {
      throw new InvalidInputException();
    }

  }//Builds a Dock object from a line in the file with a dummy ship object.
  
  @Override
  public String toString() {
    return "Dock: " + super.toString();
  } //returns dock name and index as a string with proper label
  
  public HashMap<String,String> toMap(){
    HashMap<String,String> retMap = super.toMap();
    retMap.put("Class", "Dock");
    retMap.put("Docked Ship Name & Index", ship.name+" "+Integer.toString(ship.index));
    return retMap;
  }
}
