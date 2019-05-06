
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
Dylan Veraart
3/23/2019
Person.java
Person objects are created when the file being opened has a 'person' line. Persons
can hold single skill string and have a parent port.
 */
class Person extends Thing {

  String skill;
  boolean outerLocked;
  Thread outerLockedBy;
  boolean innerLocked;
  Thread innerLockedBy;

  public Person(Scanner sc) throws InvalidInputException {
    super(sc);
    if (sc.hasNext()) {
      skill = sc.next();
    } else {
      throw new InvalidInputException();
    }
    outerLocked = false;
    innerLocked = false;
  }//Builds Ship when line found in file

  @Override
  public String toString() {
    return "Person: " + super.toString();
  } //returns Person name and index as a string with proper label

  public HashMap<String, String> toMap() {
    HashMap<String, String> retMap = super.toMap();
    retMap.put("Class", "Worker");
    retMap.put("Skill", skill);
    return retMap;
  }

  public synchronized boolean tryOuterLock() {
    if (outerLocked) {
      return false;
    } else {
      outerLock();
      return outerLocked;
    }
  }
  public synchronized boolean tryInnerLock(Thread t) {
    if (innerLocked) {
      return false;
    } else {
      innerLock(t);
      return true;
    }
  }
  public synchronized void outerLock() {
    Thread callingThread = Thread.currentThread();

    while (outerLocked && outerLockedBy != callingThread) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException ex) {

      }
    }
    outerLocked = true;
    outerLockedBy = callingThread;
    notify();
  }

  public synchronized void outerUnlock() {
    if (Thread.currentThread() == outerLockedBy) {
      innerLocked = false;
      outerLocked = false;
      notify();
    }
  }

  

  public synchronized void innerLock(Thread t) {
    Thread callingThread = Thread.currentThread();

    while ((innerLocked && innerLockedBy != callingThread) || outerLockedBy != t || !outerLocked) {
      try {
       System.out.println(callingThread.getName() + " " + Boolean.toString(innerLocked) + " "+ outerLockedBy.getName() + " " + t.getName() + " " +Boolean.toString(outerLocked));
        wait();
      } catch (InterruptedException ex) {

      }
    }
    innerLocked = true;
    innerLockedBy = callingThread;
  }

  public synchronized void innerUnlock() {
    if (Thread.currentThread() == this.innerLockedBy) {

      innerLocked = false;
      notify();
    }
  }
}
