
import java.util.*;

/*
Dylan Veraart
3/23/2019
Ship.java
Ship objects are created in 3 primary ways. The file being opened has a 'ship' 
line, the PassengerShip or CargoShip objects call the ship constructor as a 
super, or a default Ship object is constructed for a new Dock object.
Ships can have either docks or ports as parents.
 */
class Ship extends Thing {

  PortTime arrivalTime, dockTime;
  double draft, length, weight, width;
  ArrayList<Job> jobs;
  int priority;

  public Ship(Scanner sc) throws InvalidInputException {
    super(sc);
    jobs = new ArrayList<>();
    if (sc.hasNextDouble()) {
      weight = sc.nextDouble();
    }
    if (sc.hasNextDouble()) {
      length = sc.nextDouble();
    }
    if (sc.hasNextDouble()) {
      width = sc.nextDouble();
    }
    if (sc.hasNextDouble()) {
      draft = sc.nextDouble();
    } else {
      throw new InvalidInputException();
    }
    arrivalTime = new PortTime();
    jobs = new ArrayList<Job>() {
      @Override
      public String toString() {
        return "Jobs";
      }
    };;
  }//Builds Ship when line found in file

  public Ship(String n, int i, int p) {
    super(n, i, p);
    arrivalTime = new PortTime();
    jobs = new ArrayList<Job>();
  }//Used for building blank Ships for Dock objects

  public void dock() {
    System.out.println("Docking: " + name);
    dockTime = new PortTime();
  }

  public synchronized void priority() {
    priority++;
    if (priority >= jobs.size()) {
      priority = 0;
    }
    try {
      while (jobs.get(priority).inner || jobs.get(priority).incompletable || jobs.get(priority).duration <= 0) {
        priority++;
      }
    } catch (IndexOutOfBoundsException e) {
      if (priority >= jobs.size()) {
        priority = 0;
      }
    }
    notifyAll();
  }

  @Override
  public String toString() {
    return "Ship: " + super.toString();
  } //returns Ship name and index as a string with proper label

  public HashMap<String, String> toMap() {
    HashMap<String, String> retMap = super.toMap();
    retMap.put("Class", "Ship");
    //retMap.put("Needs to Dock", (jobs.size()>0) ? "Yes":"No");
    retMap.put("Dock Wait Time", (dockTime != null) ? dockTime.minus(arrivalTime).toString() : (new PortTime()).minus(arrivalTime).toString());
    retMap.put("Has Docked", (dockTime != null) ? "Yes" : "No");
    retMap.put("Draft", Double.toString(draft));
    retMap.put("Length", Double.toString(length));
    retMap.put("Weight", Double.toString(weight));
    retMap.put("Width", Double.toString(width));
    return retMap;
  }

  public HashSet<String> getRequirements() {
    HashSet<String> setArr = new HashSet<>();
    for (Job job : jobs) {
      if (job.duration > 0 && !job.incompletable) {
        setArr.addAll(job.requirements);
      }
    }
    return setArr;
  }

}
