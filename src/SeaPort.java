
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
Dylan Veraart
3/23/2019
SeaPort.java
Seaports are created when 'port' lines in the open file are processed by the 
World object. These objects store various child docks, ships and persons.
 */
public class SeaPort extends Thing {

  ArrayList<Dock> docks;
  Vector<Ship> que;
  ArrayList<Ship> ships;
  ArrayList<Person> persons;
  HashMap<String, Person> personSkill;
  Vector<Thread> priorityQueue;
  int priority;

  public SeaPort(Scanner sc) throws InvalidInputException {
    super(sc);
    docks = new ArrayList<Dock>() {
      @Override
      public String toString() {
        return "Docks";
      }
    };
    que = new Vector<Ship>() {
      @Override
      public String toString() {
        return "Queued Ships";
      }
    };
    ships = new ArrayList<Ship>() {
      @Override
      public String toString() {
        return "All Ships";
      }
    };
    persons = new ArrayList<Person>() {
      @Override
      public String toString() {
        return "Workers";
      }
    };
    personSkill = new HashMap<>();
    priorityQueue = new Vector<>();
    priority = 0;
  }//Builds SeaPort when line found in file

  @Override
  public String toString() {
    return "SeaPort: " + super.toString() + "\n\n";
  }//returns SeaPort name and index as a string with proper label

  public HashMap<String, String> toMap() {
    HashMap<String, String> retMap = super.toMap();
    retMap.put("Class", "Port");
    retMap.put("Number of Docks", Integer.toString(docks.size()));
    retMap.put("All Ships", Integer.toString(ships.size()));
    retMap.put("Queued Ships", Integer.toString(que.size()));
    retMap.put("Workers", Integer.toString(persons.size()));
    return retMap;
  }

  public void incompletableJobs(Ship ship) {
    for (int i = 0; i < ship.jobs.size(); i++) {
      if (!personSkill.keySet().containsAll(ship.jobs.get(i).requirements)) {
        ship.jobs.get(i).incompletable();
      }
    }
  }
  public synchronized void remove(Thread t){
    priorityQueue.remove(t);
    if (priority >= priorityQueue.size()) {
      priority = 0;
    }
  }
  public synchronized void priority() {
    ++priority;
    if (priority >= priorityQueue.size()) {
      priority = 0;
    }
    notifyAll();
  }

  public synchronized boolean getWorkers(HashSet<String> requirements) {
    if (priority >= priorityQueue.size()) {
      priority = 0;
      notifyAll();
    }
    while (priorityQueue.indexOf(Thread.currentThread()) != priority) {
      if (priorityQueue.indexOf(Thread.currentThread()) == -1) {
        System.out.println(priorityQueue.size());
      }
      try {
        wait();
      } catch (InterruptedException ex) {

      }
    }
    boolean retBool = true;
    for (String requirement : requirements) {
      Person tempPerson = personSkill.get(requirement);
      if (tempPerson == null) {
        System.out.println(requirement + " " + name);
      }
      retBool = tempPerson.tryOuterLock();
      if (!retBool) {
        for (String requirementB : requirements) {
          personSkill.get(requirementB).outerUnlock();
        }
        break;
      }
    }

    return retBool;
  }
}
