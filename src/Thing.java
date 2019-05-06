
import java.util.*;

/*
Dylan Veraart
3/23/2019
Thing.java
Serves as a super class for most other classes with the exception
of the main class (SeaPortProgram), InvalidInputException, and PortTime.
It establishes that each class, at the least, has a name, index, and parent.
I'm uncertain why the class implements Compareable but assume 
this has to do with later projects.
 */
class Thing implements Comparable<Thing> {

  int index;
  String name;
  int parent;

  public Thing(String n, int i, int p) {
    name = n;
    index = i;
    parent = p;
  }//Used to instantiate incomplete ships for docks and the World class

  public Thing(Scanner sc) throws InvalidInputException {
    if (sc.hasNext()) {
      name = sc.next();
    }
    if (sc.hasNextInt()) {
      index = sc.nextInt();
    }
    if (sc.hasNextInt()) {
      parent = sc.nextInt();
    } else {
      throw new InvalidInputException();
    }
  }//Constructor used when parsing a the file. 
  //Only called by super in constructors.

  @Override
  public int compareTo(Thing o) {
    return name.compareTo(o.name);
  }//Beyond the scope of this week's project. Assumed to be relevant later

  @Override
  public String toString() {
    return name + " " + Integer.toString(index) + "\n";
  }//Returns the name and index of the Thing as a string

  public HashMap<String, String> toMap() {
    HashMap<String, String> retMap = new HashMap<String, String>();
    retMap.put("Class", "Thing");
    retMap.put("Index", Integer.toString(index));
    retMap.put("Name", name);
    retMap.put("Parent", Integer.toString(parent));
    return retMap;
  }
}
