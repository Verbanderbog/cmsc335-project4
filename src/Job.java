
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;


/*
Dylan Veraart
3/23/2019
Job.java
Placeholder class for future projects
 */
class Job extends Thing implements Runnable {
  
  double duration;
  ArrayList<String> requirements;
  JPanel parentPanel;
  private final ProgressPanel panel;
  private boolean pauseFlag;
  boolean incompletable;
  Thread parentThread;
  Ship parentShip;
  HashMap<String, Person> workers;
  boolean outer;
  boolean inner;
  
  public Job(Scanner sc) throws InvalidInputException {
    super(sc);
    incompletable = false;
    outer = false;
    inner = false;
    requirements = new ArrayList<String>() {
      @Override
      public String toString() {
        return "Requirements";
      }
      
    };
    if (sc.hasNextDouble()) {
      duration = sc.nextDouble();
      while (sc.hasNext()) {
        requirements.add(sc.next());
      }
    } else {
      throw new InvalidInputException();
    }
    panel = new ProgressPanel();
    pauseFlag = false;
  }
  
  public void incompletable() {
    System.out.println("Incompletable: " + this.name);
    incompletable = true;
    panel.incompletable();
  }
  
  private void pause() {
    pauseFlag = true;
  }
  
  private synchronized void resume() {
    pauseFlag = false;
    notify();
  }
  
  private String elementsAsString() {
    String retString = "";
    for (String s : requirements) {
      retString += (s + " \n");
    }
    return retString;
  }
  
  public HashMap<String, String> toMap() {
    HashMap<String, String> retMap = super.toMap();
    retMap.put("Class", "Port");
    retMap.put("Progress", (duration <= 0) ? "Finished" : "In Progress/Pending");
    retMap.put("Requirements", elementsAsString());
    return retMap;
  }
  
  @Override
  public void run() {
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.weightx = 1;
    c.gridwidth = GridBagConstraints.REMAINDER;
    parentPanel.add(panel, c);
    parentPanel.revalidate();
    if (!incompletable) {
      
      try {
        System.out.println("Started run: " + name);
        synchronized (this) {
          while (!outer) {
            System.out.println(name + " waiting for outer");
            wait();
          }
        }
        
        while (!inner) {
          synchronized (this) {
            while (parentShip.priority != parentShip.jobs.indexOf(this)) {
              System.out.println(name + " " + parentShip.jobs.indexOf(this) + " waiting for priority. Priority is " + Integer.toString(parentShip.priority));
              Thread.sleep(1000);
            }
          }
          System.out.println(name + " priority acquired");
          inner = true;
          for (String requirement : requirements) {
            inner = inner && workers.get(requirement).tryInnerLock(parentThread);
            if (inner) {
              panel.black(requirement);
            } else {
              panel.redout();
              for (String requirementB : requirements) {
                workers.get(requirementB).innerUnlock();
              }
              Thread.sleep(1000);
              break;
            }
          }
          if (inner){
            System.out.println(name + " inner acquired");
          }
          parentShip.priority();
        }
        while (duration > 0) {
          Thread.sleep(100);
          duration -= 0.1;
          panel.update();
          synchronized (this) {
            while (pauseFlag) {
              wait();
            }
          }
        }
        System.out.println("Stopped run: " + name);
      } catch (InterruptedException ex) {
        
      } finally {
        for (String requirement : requirements) {
          workers.get(requirement).innerUnlock();
        }
        
      }
      
    } else {
      
      try {
        Thread.sleep(2000);
      } catch (InterruptedException ex) {
        
      }
    }
    panel.end();
    
  }
  
  private class ProgressPanel extends JPanel implements ActionListener {
    
    JButton pause;
    JButton stop;
    JProgressBar bar;
    JPanel labelsPanel;
    HashMap<String, JLabel> labels;
    
    private ProgressPanel() {
      bar = new JProgressBar(0, (int) Math.ceil(duration * 1000));
      bar.setValue(0);
      pause = new JButton("Pause");
      pause.setActionCommand("pause");
      pause.addActionListener(this);
      pause.setFont(new Font("Arial", Font.BOLD, 11));
      stop = new JButton("Stop");
      stop.setActionCommand("stop");
      stop.addActionListener(this);
      stop.setFont(new Font("Arial", Font.BOLD, 11));
      labelsPanel = new JPanel();
      labels = new HashMap<>();
      for (String requirement : requirements) {
        JLabel tempLabel = new JLabel(requirement);
        tempLabel.setForeground(Color.RED);
        labelsPanel.add(tempLabel);
        labels.put(requirement, tempLabel);
      }
      this.setLayout(new GridBagLayout());
      GridBagConstraints c = new GridBagConstraints();
      c.gridy = 0;
      add(pause, c);
      add(stop, c);
      add(new JLabel(name + ": "), c);
      c.fill = GridBagConstraints.HORIZONTAL;
      c.weightx = 1;
      add(bar, c);
      c.fill = GridBagConstraints.NONE;
      c.weightx = 0;
      add(labelsPanel, c);
    }
    
    private void redout() {
      for (JLabel label : labels.values()) {
        label.setForeground(Color.RED);
      }
      
    }
    
    private void black(String s) {
      labels.get(s).setForeground(Color.BLACK);
    }
    
    private void incompletable() {
      pause.setEnabled(false);
      bar.setEnabled(false);
      bar.setString("UNCOMPLETEABLE");
      bar.setStringPainted(true);
    }
    
    private void update() {
      bar.setValue(bar.getMaximum() - (int) (duration * 1000));
    }
    
    private void end() {
      this.removeAll();
      this.revalidate();
      parentPanel.remove(this);
      parentPanel.revalidate();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
      switch (e.getActionCommand()) {
        case "pause":
          pause.setText("Resume");
          pause.setActionCommand("resume");
          pause();
          break;
        case "resume":
          pause.setText("Pause");
          pause.setActionCommand("pause");
          resume();
          break;
        case "stop":
          duration = 0;
          incompletable = true;
          end();
          resume();
          break;
      }
    }
  }
}
