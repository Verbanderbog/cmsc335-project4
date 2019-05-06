
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
Dylan Veraart
4/11/2019
World.java
World objects are created by the SeaPortProgram class when a file is opened.
The World constructor is passed a scanner of the opened file and processes it 
into objects of various Thing subclasses. Also contains search functions that
search the file by name, index, skill, parent index, or parent name.

Changes for Project 2 include the removal of helper methods that found the 
parents of new objects and added the object as a child. Instead a HashMap was
implements to maintain a temporary collection of all possible parent objects. 
This HashMap could then be searched by index to find the appropriate parent.
 */
class World extends Thing {

  ArrayList<SeaPort> ports;
  PortTime time;

  public World(Scanner sc) throws InvalidInputException {
    super("World", -1, -1);
    int x = 1;
    ports = new ArrayList<SeaPort>(){
    @Override
    public String toString(){
      return "Ports";
    }
    };;
    HashMap<Integer, Thing> map = new HashMap<>();
    HashMap<Integer, ArrayList<Job>> jobMap = new HashMap<>();
    while (sc.hasNextLine()) {
      try {
        process(sc.nextLine(), map, jobMap);
        x++;
      } catch (InvalidInputException e) {
        throw new InvalidInputException(Integer.toString(x));
      }
    }
    
  }//begins processing the input file by calling process() on each line
  

  
  private void process(String st, HashMap<Integer, Thing> map, HashMap<Integer, ArrayList<Job>> jobMap) throws InvalidInputException {
    Scanner sc = new Scanner(st);
    if (!sc.hasNext()) {
      return;
    }
    switch (sc.next()) {
      case "port":
        SeaPort port = new SeaPort(sc);
        map.put(port.index, port);
        ports.add(port);
        break;
      case "ship":
        Ship ship = new Ship(sc);
        map.put(ship.index, ship);
        shipBuilder(ship, map, jobMap);
        break;
      case "pship":
        PassengerShip pship = new PassengerShip(sc);
        map.put(pship.index, pship);
        shipBuilder(pship, map, jobMap);
        break;
      case "cship":
        CargoShip cship = new CargoShip(sc);
        map.put(cship.index, cship);
        shipBuilder(cship, map, jobMap);
        break;
      case "person":
        Person person = new Person(sc);
        map.put(person.index, person);
        SeaPort tempPortA = (SeaPort) map.get(person.parent);
        if (tempPortA != null) {
          tempPortA.persons.add(person);
          tempPortA.personSkill.put(person.skill, person);
        }
        break;
      case "dock":
        Dock dock = new Dock(sc);
        map.put(dock.index, dock);
        SeaPort tempPortB = (SeaPort) map.get(dock.parent);
        if (tempPortB != null) {
          tempPortB.docks.add(dock);
        }
        Ship tempShip = (Ship) map.get(dock.ship.index);
        if (tempShip != null) {
          dock.ship = tempShip;
          tempShip.dock();
        }
        break;
      case "job":
        Job job = new Job(sc);

        Ship parentShip = ((Ship) map.get(job.parent));
        if (parentShip != null) {
          parentShip.jobs.add(job);
          
        } else {
          ArrayList<Job> tempArr = jobMap.getOrDefault(job.parent, new ArrayList<>());
          tempArr.add(job);
          jobMap.put(job.parent, tempArr);
        }
      case "//":
        break;
      default:
        throw new InvalidInputException();
    }

  }//Helper method used to process each line of the file and creates an 
  //object as dictated by the file.

  public String searchByIndex(String s) {
    int i;
    StringBuilder retString = new StringBuilder();
    try {
      i = Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return "No results found.";
    }
    for (SeaPort port : ports) {
      if (port.index == i) {
        retString.append(port.toString());
      }
      for (Ship ship : port.ships) {
        if (ship.index == i) {
          retString.append(ship.toString());
        }
      }
      for (Dock dock : port.docks) {
        if (dock.index == i) {
          retString.append(dock.toString());
        }
      }
      for (Person person : port.persons) {
        if (person.index == i) {
          retString.append(person.toString());
        }
      }
    }
    if (retString.length() <= 0) {
      retString.append("No results found.");
    }
    return retString.toString();
  }//public fuction used to search the file by index

  public String searchByParentIndex(String s) {
    int i;
    StringBuilder retString = new StringBuilder();
    try {
      i = Integer.parseInt(s);
    } catch (NumberFormatException e) {
      return "No results found.";
    }
    for (SeaPort port : ports) {
      if (port.parent == i) {
        retString.append(port.toString());
      }
      for (Ship ship : port.ships) {
        if (ship.parent == i) {
          retString.append(ship.toString());
        }
      }
      for (Dock dock : port.docks) {
        if (dock.parent == i) {
          retString.append(dock.toString());
        }
      }
      for (Person person : port.persons) {
        if (person.parent == i) {
          retString.append(person.toString());
        }
      }
    }
    if (retString.length() <= 0) {
      retString.append("No results found.");
    }
    return retString.toString();
  }//public fuction used to search the file by parent index

  public String searchByParentName(String s) {
    ArrayList<Thing> search = new ArrayList<>();
    for (SeaPort port : ports) {
      if (port.name.equalsIgnoreCase(s)) {
        search.add(port);
      }
      for (Ship ship : port.ships) {
        if (ship.name.equalsIgnoreCase(s)) {
          search.add(ship);
        }
      }
      for (Dock dock : port.docks) {
        if (dock.name.equalsIgnoreCase(s)) {
          search.add(dock);
        }
      }
      for (Person person : port.persons) {
        if (person.name.equalsIgnoreCase(s)) {
          search.add(person);
        }
      }
    }
    StringBuilder retString = new StringBuilder();
    for (Thing term : search) {
      for (SeaPort port : ports) {
        if (port.parent == term.index) {
          retString.append(port.toString());
        }
        for (Ship ship : port.ships) {
          if (ship.parent == term.index) {
            retString.append(ship.toString());
          }
        }
        for (Dock dock : port.docks) {
          if (dock.parent == term.index) {
            retString.append(dock.toString());
          }
        }
        for (Person person : port.persons) {
          if (person.parent == term.index) {
            retString.append(person.toString());
          }
        }
      }
    }
    if (retString.length() <= 0) {
      retString.append("No results found.");
    }
    return retString.toString();
  }//public fuction used to search the file by parent name

  public String searchBySkill(String s) {
    StringBuilder retString = new StringBuilder();
    for (SeaPort port : ports) {
      for (Ship ship : port.ships) {
        for (Job job : ship.jobs) {
          for (String requirement : job.requirements) {
            if (requirement.equalsIgnoreCase(s)) {
              retString.append(job.toString());
            }
          }
        }
      }
      for (Person person : port.persons) {
        if (person.skill.equalsIgnoreCase(s)) {
          retString.append(person.toString());
        }
      }
    }
    if (retString.length() <= 0) {
      retString.append("No results found.");
    }
    return retString.toString();
  }//public fuction used to search the file by skill

  public String searchByName(String s) {
    StringBuilder retString = new StringBuilder();
    for (SeaPort port : ports) {
      if (port.name.equalsIgnoreCase(s)) {
        retString.append(port.toString());
      }
      for (Ship ship : port.ships) {
        if (ship.name.equalsIgnoreCase(s)) {
          retString.append(ship.toString());
        }
      }
      for (Dock dock : port.docks) {
        if (dock.name.equalsIgnoreCase(s)) {
          retString.append(dock.toString());
        }
      }
      for (Person person : port.persons) {
        if (person.name.equalsIgnoreCase(s)) {
          retString.append(person.toString());
        }
      }
    }
    if (retString.length() <= 0) {
      retString.append("No results found.");
    }
    return retString.toString();
  }//public fuction used to search the file by name

  @Override
  public String toString() {
    StringBuilder retString = new StringBuilder();
    retString.append(">>>>> The world:\n\n\n");
    for (SeaPort port : ports) {
      retString.append(port.toString());
      for (Dock dock : port.docks) {
        retString.append(" ");
        retString.append(dock.toString());
        retString.append("  Ship: ");
        retString.append(dock.ship.toString());
        retString.append("\n");
      }
      retString.append(" --List of all ships in queue:\n");
      for (Ship ship : port.que) {
        retString.append("  >");
        retString.append(ship.toString());
      }
      retString.append("\n --List of all ships\n");
      for (Ship ship : port.ships) {
        retString.append("  >");
        retString.append(ship.toString());
      }
      retString.append("\n --List of all persons\n");
      for (Person person : port.persons) {
        retString.append("  >");
        retString.append(person.toString());
      }
      retString.append("\n");
    }
    return retString.toString();
  }//Builds the display string for the whole file based on the object structure

  private void shipBuilder(Ship s, HashMap<Integer, Thing> map, HashMap<Integer, ArrayList<Job>> jobMap) {
    if ((map.get(s.parent) instanceof Dock)) {
      Dock dock = (Dock) map.get(s.parent);
      dock.ship = s;
      s.dock();
      SeaPort port = (SeaPort) map.get(dock.parent);
      port.ships.add(s);
    } else if (map.get(s.parent) instanceof SeaPort) {
      SeaPort port = (SeaPort) map.get(s.parent);
      port.que.add(s);
      port.ships.add(s);
    }
    s.jobs.addAll(jobMap.getOrDefault(s.index, new ArrayList<>()));
    jobMap.remove(s.index);
  }//Helper method when processing ship lines in process()
  public HashMap<String,String> toMap(){
    HashMap<String,String> retMap = super.toMap();
    retMap.put("Class", "World");
    retMap.put("Number of Ports", Integer.toString(ports.size()));
    return retMap;
  }

}
