
import java.awt.GridBagConstraints;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;

public class ShipRunnable implements Runnable {

  private Dock dock;
  private SeaPort port;
  private JPanel panel;
  private ShipThreadPoolExecutor poolExecutor;
  private HashMap<String, Person> workers;
  private boolean allOuter;
  HashSet<String> fullRequirements;

  public ShipRunnable(Dock d, SeaPort p, JPanel pane, ShipThreadPoolExecutor pE) {
    super();
    dock = d;
    port = p;
    panel = pane;
    poolExecutor = pE;
    workers = new HashMap<>();
    allOuter = false;
  }

  @Override
  public void run() {
    synchronized (this) {
      Thread.currentThread().setName(dock.ship.name);
      port.incompletableJobs(dock.ship);
      fullRequirements = dock.ship.getRequirements();
      if (fullRequirements.size() > 0) {
        ArrayList<JobThread> threads = new ArrayList<>();

        for (Job job : dock.ship.jobs) {
          job.parentThread = Thread.currentThread();
          job.parentPanel = panel;
          if (!job.incompletable) {

            HashMap<String, Person> tempWorkers = (HashMap<String, Person>) port.personSkill.clone();
            tempWorkers.keySet().retainAll(job.requirements);
            job.workers = (HashMap<String, Person>) tempWorkers.clone();
            job.parentShip = dock.ship;
          }
          JobThread thread = new JobThread(job, job.name);
          threads.add(thread);
          thread.start();
        }
        try {
          port.priorityQueue.add(Thread.currentThread());
          while (!allOuter) {
            allOuter = true;

            while (port.priorityQueue.indexOf(Thread.currentThread()) != port.priority) {
              System.out.println(dock.ship.name + " " + port.priorityQueue.indexOf(Thread.currentThread()) + " waiting for priority. Current priority: " + port.priority + " Current queue size: " + port.priorityQueue.size());
              try {
                Thread.sleep(1000);
              } catch (InterruptedException ex) {

              }
            }
            System.out.println(dock.ship.name + " Priority acquired");

            for (JobThread jThread : threads) {

              if (!jThread.job.incompletable && jThread.job.duration > 0 && !jThread.job.outer) {
                HashSet<String> tempRequire = new HashSet<>(jThread.job.requirements);
                tempRequire.removeAll(workers.keySet());
                boolean retBool = true;
                for (String requirement : tempRequire) {
                  Person tempPerson = port.personSkill.get(requirement);
                  ArrayList<String> toUnlock = new ArrayList<>();
                  if (tempPerson == null) {
                    System.out.println(requirement + " " + port.name);
                  }
                  retBool = (retBool && tempPerson.tryOuterLock());
                  
                  if (!retBool) {
                    for (String requirementB : toUnlock) {
                      port.personSkill.get(requirementB).outerUnlock();
                    }
                    break;
                  } else {
                    toUnlock.add(requirement);
                  }
                }
                jThread.job.outer = retBool;
                notifyAll();
                allOuter = allOuter && jThread.job.outer;
                HashMap<String, Person> tempWorkers = (HashMap<String, Person>) port.personSkill.clone();
                tempWorkers.keySet().retainAll(tempRequire);
                workers.putAll(tempWorkers);
                if (jThread.job.outer) {
                  System.out.println(jThread.job.name + " Outer acquired");
                }
              }

            }

            port.priority();
            System.out.println(dock.ship.name + " Priority released");
          }
          port.remove(Thread.currentThread());
          for (JobThread jThread : threads) {
            try {
              jThread.join();
              HashSet<String> tempSet = ((HashSet<String>) fullRequirements.clone());
              tempSet.removeAll(dock.ship.getRequirements());
              for (String requirement : tempSet) {
                try {
                  workers.remove(requirement).outerUnlock();
                } catch (NullPointerException e) {

                }
              }
            } catch (InterruptedException ex) {

            }
          }
        } finally {
          for (String requirement : fullRequirements) {
            try {
              port.personSkill.get(requirement).outerUnlock();
            } catch (NullPointerException e) {

            }
          }
        }
      }
      ShipRunnable sRunnable = null;
      try {
        dock.ship = port.que.remove(0);
        poolExecutor.execute(new ShipRunnable(dock, port, panel, poolExecutor));
        dock.ship.dock();
      } catch (ArrayIndexOutOfBoundsException e) {
        poolExecutor.error++;
        if (poolExecutor.error >= port.docks.size()) {
          poolExecutor.shutdown();
        }
      }

    }
  }

}
