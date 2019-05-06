
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/*
Dylan Veraart
4/11/2019
SeaPortProgram.java
Creates a GUI that allows the user to load a file describing a hierarchy of 
ports, docks, ships, and workers. The program also tracks various information 
about these objects. The program is able to list all these objects in a 
readable format. Finally the program allows a user to search the hierarchy by 
index, name, skill, parent index, or parent name.

Changes for Project 2 include the addition of functions to list and sort the 
various ArrayLists within the program.
 */
public class SeaPortProgram extends JFrame {

  private World world;
  private DefaultTreeModel model;

  public SeaPortProgram() {
    JTextArea text = new JTextArea();
    JScrollPane scrollPane = new JScrollPane();
    JPanel scrollPanel = new JPanel();
    scrollPanel.setLayout(new GridLayout(0, 1));
    scrollPane.setViewportView(scrollPanel);
    scrollPanel.add(text);
    JPanel tablePanel = new JPanel();
    tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
    JPanel treePanel = new JPanel();
    treePanel.setLayout(new BorderLayout());
    JButton refresh = new JButton("Refresh Tree");
    treePanel.add(refresh, BorderLayout.NORTH);
    model = new DefaultTreeModel(new worldNode("World"));
    JTree tree = new JTree(model);

    treePanel.add(tree, BorderLayout.CENTER);

    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {
        worldNode node = (worldNode) tree.getLastSelectedPathComponent();
        if (node == null) {
          return;
        }
        tablePanel.removeAll();
        switch (node.toString()) {
          case "World":
            String[] headers = new String[]{"Class", "Name", "Index", "Parent", "Number of Ports"};
            String[][] data = new String[1][headers.length];
            for (int i = 0; i < headers.length; i++) {
              data[0][i] = world.toMap().get(headers[i]);
            } 
            tablePanel.add(new JScrollPane(new JTable(data, headers)));
            break;
          case "Ports":
            tablePanel.add(portsTable((ArrayList<SeaPort>) node.getUserObject()));
            break;
          case "Docks":
            tablePanel.add(docksTable((ArrayList<Dock>) node.getUserObject()));
            break;
          case "Queued Ships":
            tablePanel.add(shipsTable((ArrayList<Ship>) node.getUserObject()));
            break;
          case "All Ships":
            tablePanel.add(shipsTable((ArrayList<Ship>) node.getUserObject()));
            break;
          case "Workers":
            tablePanel.add(personsTable((ArrayList<Person>) node.getUserObject()));
            break;
          case "Jobs":
            tablePanel.add(jobsTable((ArrayList<Job>) node.getUserObject()));
            break;
          default:
            if (node.getUserObject() instanceof Dock) {
              Dock dock = ((Dock) node.getUserObject());
              tablePanel.add(docksTable(new ArrayList<Dock>(Arrays.asList(dock))));
            } else if (node.getUserObject() instanceof SeaPort) {
              tablePanel.add(portsTable(new ArrayList<SeaPort>(Arrays.asList((SeaPort) node.getUserObject()))));
            } else if (node.getUserObject() instanceof Ship) {
              tablePanel.add(shipsTable(new ArrayList<Ship>(Arrays.asList((Ship) node.getUserObject()))));
            } else if (node.getUserObject() instanceof Person) {
              tablePanel.add(personsTable(new ArrayList<Person>(Arrays.asList((Person) node.getUserObject()))));
            } else if (node.getUserObject() instanceof Job) {
              tablePanel.add(jobsTable(new ArrayList<Job>(Arrays.asList((Job) node.getUserObject()))));
            }
            break;

        }
        scrollPane.revalidate();
        revalidate();
      }

      private JScrollPane portsTable(ArrayList<SeaPort> ports) {
        Object[] headers = new ArrayList<String>(Arrays.asList("Class", "Name", "Index", "Parent", "Number of Docks", "All Ships", "Queued Ships", "Workers")).toArray();
        return table(ports, headers);
      }

      private JScrollPane personsTable(ArrayList<Person> persons) {
        Object[] headers = new ArrayList<String>(Arrays.asList("Class", "Name", "Index", "Parent", "Skill")).toArray();
        return table(persons, headers);
      }

      private JScrollPane shipsTable(ArrayList<Ship> ships) {
        Object[] headers = new ArrayList<String>(Arrays.asList("Class", "Name", "Index", "Parent", "Dock Wait Time", "Has Docked", "Draft", "Length", "Width", "Weight", "Cargo Value", "Cargo Volume", "Cargo Weight", "Occupied Rooms", "Total Passengers", "Total Rooms")).toArray();
        return table(ships, headers);
      }

      private JScrollPane docksTable(ArrayList<Dock> docks) {
        Object[] headers = new ArrayList<String>(Arrays.asList("Class", "Name", "Index", "Parent", "Docked Ship Name & Index")).toArray();
        return table(docks, headers);
      }

      private JScrollPane jobsTable(ArrayList<Job> jobs) {
        Object[] headers = new ArrayList<String>(Arrays.asList("Class", "Name", "Index", "Parent", "Progress", "Requirements")).toArray();
        return table(jobs, headers);
      }

      private JScrollPane table(ArrayList things, Object[] headers) {
        String[][] data = new String[things.size()][headers.length];
        for (int k = 0; k < things.size(); k++) {
          for (int i = 0; i < headers.length; i++) {
            data[k][i] = ((Thing) things.get(k)).toMap().get(headers[i]);
          }
        }
        return new JScrollPane(new JTable(data, headers));
      }
    });
    treePanel.add(tree);
    JScrollPane treePane = new JScrollPane(treePanel);
    JTextField searchField = new JTextField();
    JComboBox<String> searchType = new JComboBox<>(new String[]{"Index", "Name", "Skill", "Parent Index", "Parent Name"});
    JComboBox<String> listType = new JComboBox<>(new String[]{"Ports", "Queued Ships", "Ships", "Docks", "Persons"});
    JComboBox<Thing> fromParent = new JComboBox<>();
    JComboBox<String> sortBy = new JComboBox<>(new String[]{"Name", "Weight", "Length", "Width", "Draft"});
    JComponent[] enableable = new JComponent[14];
    JPanel allProgressBars = new JPanel();
    allProgressBars.setLayout(new GridBagLayout());

    ActionListener seaportListener = new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
          case "refresh":
            updateTree();
            model.reload();
            validate();
            break;
          case "tree":
            treeVisible();
            updateTree();
            model.reload();
            tree.revalidate();
            treePane.revalidate();
            break;
          case "open":
            textVisible();
            text.setText(chooseFile(enableable, allProgressBars));

            break;
          case "display":
            textVisible();
            text.setText(world.toString());
            break;
          case "search":
            textVisible();
            text.setText(search(String.valueOf(searchType.getSelectedItem()), searchField.getText().trim()));
            break;
          case "listType":

            switch (listType.getSelectedItem().toString()) {
              case "Queued Ships":
                fromParent.removeAllItems();
                for (SeaPort port : world.ports) {
                  fromParent.addItem(port);
                }
                sortBy.removeAllItems();
                sortBy.addItem("Name");
                sortBy.addItem("Weight");
                sortBy.addItem("Length");
                sortBy.addItem("Width");
                sortBy.addItem("Draft");
                fromParent.setEnabled(true);
                sortBy.setEnabled(true);
                break;
              case "Ships":
                fromParent.removeAllItems();
                for (SeaPort port : world.ports) {
                  fromParent.addItem(port);
                }
                sortBy.removeAllItems();
                sortBy.addItem("Name");
                sortBy.addItem("Weight");
                sortBy.addItem("Length");
                sortBy.addItem("Width");
                sortBy.addItem("Draft");
                fromParent.setEnabled(true);
                sortBy.setEnabled(true);

                break;
              case "Ports":
                fromParent.removeAllItems();
                sortBy.removeAllItems();
                sortBy.addItem("Name");
                fromParent.setEnabled(false);
                sortBy.setEnabled(true);
                break;
              case "Docks":
                fromParent.removeAllItems();
                for (SeaPort port : world.ports) {
                  fromParent.addItem(port);
                }
                sortBy.removeAllItems();
                sortBy.addItem("Name");
                fromParent.setEnabled(true);
                sortBy.setEnabled(true);
                break;
              case "Persons":
                fromParent.removeAllItems();
                for (SeaPort port : world.ports) {
                  fromParent.addItem(port);
                }
                sortBy.removeAllItems();
                sortBy.addItem("Name");
                fromParent.setEnabled(true);
                sortBy.setEnabled(true);
                break;
            }
            break;
          case "list":
            textVisible();
            if (fromParent.getSelectedItem() != null) {

              System.out.println(sortBy.getSelectedItem().toString());
              String retString = displayList(listType.getSelectedItem().toString(),
                      ((Thing) fromParent.getSelectedItem()).index,
                      sortBy.getSelectedItem().toString());
              System.out.println(retString);
              text.setText(retString);
            } else {
              String retString = displayList(listType.getSelectedItem().toString(),
                      0,
                      sortBy.getSelectedItem().toString());
              text.setText(retString);
            }
        }
      }

      private void textVisible() {
        treePane.setVisible(false);
        if (scrollPanel.getComponent(0).equals(tablePanel)) {
          tablePanel.setVisible(false);
          scrollPanel.removeAll();
          scrollPanel.add(text);
          scrollPanel.revalidate();
        }
        text.setVisible(true);
        revalidate();
        repaint();
      }

      private void treeVisible() {
        treePane.setVisible(true);

        if (scrollPanel.getComponent(0).equals(text)) {
          text.setVisible(false);
          scrollPanel.removeAll();
          scrollPanel.add(tablePanel);
          scrollPanel.revalidate();
        }
        tablePanel.setVisible(true);
        revalidate();
        repaint();
      }
    };
    refresh.setActionCommand("refresh");
    refresh.addActionListener(seaportListener);
    JMenuBar menuBar = new JMenuBar();
    JMenu fileMenu = new JMenu("File");
    JMenuItem open = new JMenuItem("Open...");
    JMenuItem display = new JMenuItem("Display Overview");
    JMenuItem treeMenu = new JMenuItem("Show World Tree");
    open.setActionCommand("open");
    open.addActionListener(seaportListener);
    display.setActionCommand("display");
    display.addActionListener(seaportListener);
    enableable[0] = display;
    treeMenu.setActionCommand("tree");
    treeMenu.addActionListener(seaportListener);
    enableable[12] = treeMenu;
    enableable[13] = tree;
    fileMenu.add(open);
    fileMenu.add(display);
    fileMenu.add(treeMenu);
    menuBar.add(fileMenu);
    this.setJMenuBar(menuBar);
    BorderLayout borderLayout = new BorderLayout();
    this.setLayout(borderLayout);
    JPanel searchPanel = new JPanel();
    GridBagLayout gridbagLayout = new GridBagLayout();
    searchPanel.setLayout(gridbagLayout);
    GridBagConstraints c = new GridBagConstraints();
    JLabel searchLabel = new JLabel("Search by: ");
    enableable[1] = searchLabel;
    searchPanel.add(searchLabel, c);
    enableable[2] = searchType;
    searchPanel.add(searchType, c);

    searchField.setActionCommand("search");
    searchField.addActionListener(seaportListener);
    enableable[3] = searchField;
    c.weightx = 1;
    c.fill = GridBagConstraints.HORIZONTAL;
    searchPanel.add(searchField, c);
    c.weightx = 0;
    JButton searchButton = new JButton("Search");
    searchButton.setActionCommand("search");
    searchButton.addActionListener(seaportListener);
    enableable[4] = searchButton;
    searchPanel.add(searchButton, c);
    JPanel listPanel = new JPanel();
    listPanel.setLayout(gridbagLayout);
    JLabel listTypeLabel = new JLabel("List All:");
    enableable[5] = listTypeLabel;
    listPanel.add(listTypeLabel, c);
    listType.setActionCommand("listType");
    listType.addActionListener(seaportListener);
    enableable[6] = listType;

    listPanel.add(listType, c);

    JLabel fromLabel = new JLabel("From:");
    enableable[7] = fromLabel;
    listPanel.add(fromLabel, c);
    fromParent.setMaximumRowCount(30);
    enableable[8] = fromParent;
    c.weightx = 1;
    listPanel.add(fromParent, c);
    c.weightx = 0;
    JLabel sortLabel = new JLabel("Sorted by:");
    enableable[9] = sortLabel;
    listPanel.add(sortLabel, c);
    enableable[10] = sortBy;

    listPanel.add(sortBy, c);
    JButton listButton = new JButton("List");
    listButton.setActionCommand("list");
    listButton.addActionListener(seaportListener);
    enableable[11] = listButton;
    listPanel.add(listButton, c);
    JPanel navBarPanel = new JPanel();
    navBarPanel.setLayout(new GridLayout(2, 0));
    navBarPanel.add(searchPanel);
    navBarPanel.add(listPanel);
    this.add(navBarPanel, BorderLayout.NORTH);

    text.setEditable(false);
    this.add(treePane, BorderLayout.WEST);
    treePane.setVisible(false);
    this.add(scrollPane, BorderLayout.CENTER);
    this.add(allProgressBars, BorderLayout.SOUTH);
    enableComponents(enableable, false);
    this.setTitle("Sea Port Program");
  }//Builds GUI elements

  private class worldNode extends DefaultMutableTreeNode {

    private worldNode(Object o) {
      super(o);
    }

    public String toString() {
      if (this.userObject instanceof Thing) {
        return ((Thing) this.userObject).name;
      }
      return this.userObject.toString();

    }
  }

  private void updateTree() {
    worldNode root = new worldNode(world);
    model.setRoot(root);
    worldNode portsNode = new worldNode(world.ports);
    model.insertNodeInto(portsNode, root, root.getChildCount());
    for (SeaPort port : world.ports) {
      worldNode portNode = new worldNode(port);
      model.insertNodeInto(portNode, portsNode, portsNode.getChildCount());
      worldNode docksNode = new worldNode(port.docks);
      model.insertNodeInto(docksNode, portNode, portNode.getChildCount());
      for (Dock dock : port.docks) {
        worldNode dockNode = new worldNode(dock);
        model.insertNodeInto(dockNode, docksNode, docksNode.getChildCount());
        worldNode shipNode = new worldNode(dock.ship);
        model.insertNodeInto(shipNode, dockNode, dockNode.getChildCount());
        updateShipNode(shipNode);
      }
      worldNode queNode = new worldNode(port.que);
      model.insertNodeInto(queNode, portNode, portNode.getChildCount());

      for (Ship ship : port.que) {
        worldNode shipNode = new worldNode(ship);
        model.insertNodeInto(shipNode, queNode, queNode.getChildCount());
        updateShipNode(shipNode);
      }

      worldNode shipsNode = new worldNode(port.ships);
      model.insertNodeInto(shipsNode, portNode, portNode.getChildCount());
      for (Ship ship : port.ships) {
        worldNode shipNode = new worldNode(ship);
        model.insertNodeInto(shipNode, shipsNode, shipsNode.getChildCount());
        updateShipNode(shipNode);
      }
      worldNode personsNode = new worldNode(port.persons);
      model.insertNodeInto(personsNode, portNode, portNode.getChildCount());
      for (Person person : port.persons) {
        worldNode personNode = new worldNode(person);
        model.insertNodeInto(personNode, personsNode, personsNode.getChildCount());
      }
    }

  }

  private void updateShipNode(worldNode root) {
    Ship ship = ((Ship) root.getUserObject());
    worldNode jobsNode = new worldNode(ship.jobs);
    model.insertNodeInto(jobsNode, root, root.getChildCount());
    for (Job job : ship.jobs) {
      worldNode jobNode = new worldNode(job);
      model.insertNodeInto(jobNode, jobsNode, jobsNode.getChildCount());
    }

  }

  private void enableComponents(JComponent[] enableable, boolean y) {
    for (JComponent component : enableable) {
      component.setEnabled(y);
    }
  }//helper method to enable or disable GUI elements based on if a file is loaded

  private int customRound(double d) {
    if (d > 0) {
      return (int) Math.ceil(d);
    } else {
      return (int) Math.floor(d);
    }
  }//Used to round away from 0 for comparator

  private String displayList(String list, int parent, String sort) {
    try {
      Comparator<Ship> compare = null;
      switch (sort) {
        case "Weight":
          compare = new Comparator<Ship>() {
            @Override
            public int compare(Ship o1, Ship o2) {
              return customRound(o2.weight - o1.weight);
            }
          };
          break;
        case "Length":
          compare = new Comparator<Ship>() {
            @Override
            public int compare(Ship o1, Ship o2) {
              return customRound(o2.length - o1.length);
            }
          };
          break;
        case "Width":
          compare = new Comparator<Ship>() {
            @Override
            public int compare(Ship o1, Ship o2) {
              return customRound(o2.width - o1.width);
            }
          };
          break;
        case "Draft":
          compare = new Comparator<Ship>() {
            @Override
            public int compare(Ship o1, Ship o2) {
              return customRound(o2.draft - o1.draft);
            }
          };
          break;
      }
      final ArrayList<Thing> sortedList = new ArrayList<>();
      ArrayList<Ship> sortedListShip = new ArrayList<>();
      switch (list) {
        case "Queued Ships":
          for (SeaPort port : world.ports) {
            if (port.index == parent) {
              sortedListShip = new ArrayList(port.que);
              port.que.forEach((n) -> sortedList.add(n));
            }
          }
          break;
        case "Ships":
          for (SeaPort port : world.ports) {
            if (port.index == parent) {
              sortedListShip = port.ships;
              port.ships.forEach((n) -> sortedList.add(n));
            }
          }
          break;
        case "Ports":
          world.ports.forEach((n) -> sortedList.add(n));

          break;
        case "Docks":
          for (SeaPort port : world.ports) {
            if (port.index == parent) {
              port.docks.forEach((n) -> sortedList.add(n));

            }
          }
          break;
        case "Persons":
          for (SeaPort port : world.ports) {
            if (port.index == parent) {
              port.persons.forEach((n) -> sortedList.add(n));

            }
          }
          break;
      }
      String retString = "";
      if (sort.equals("Name")) {
        Collections.sort(sortedList);
        for (Thing thing : sortedList) {
          retString += thing.toString();
        }
      } else {
        Collections.sort(sortedListShip, compare);
        for (Ship thing : sortedListShip) {
          retString += thing.toString();
        }
      }

      return retString;
    } catch (NullPointerException e) {
      return "Unexpected List Input.";
    }
  }//returns organized text of elements in arraylists

  private String chooseFile(JComponent[] enableable, JPanel progress) {
    try {
      JFileChooser jfc = new JFileChooser(".");
      int result = jfc.showOpenDialog(this);
      File file;
      Scanner sc;
      if (result == JFileChooser.APPROVE_OPTION) {
        try {
          file = jfc.getSelectedFile();
          sc = new Scanner(file);
        } catch (NullPointerException exx) {
          return "Please pick a file.";
        }
        try {
          world = new World(sc);
          enableComponents(enableable, true);
          String retString = world.toString();
          progress.setVisible(true);
          progress.removeAll();
          ((JComboBox) enableable[6]).setSelectedIndex(0);
          GridBagConstraints c = new GridBagConstraints();
          c.fill = GridBagConstraints.HORIZONTAL;
          c.weightx = 1;
          c.gridwidth = GridBagConstraints.REMAINDER;
          JButton hide = new JButton("Hide All Jobs");
          hide.setActionCommand("hide");
          JButton unhide = new JButton("Show Job Progress");
          unhide.setActionCommand("unhide");
          ActionListener hideListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
              switch (e.getActionCommand()) {
                case "hide":
                  unhide.setVisible(true);
                  progress.setVisible(false);
                  remove(progress);
                  add(unhide, BorderLayout.SOUTH);
                  unhide.revalidate();
                  revalidate();
                  break;
                case "unhide":
                  unhide.setVisible(false);
                  progress.setVisible(true);
                  remove(unhide);
                  add(progress, BorderLayout.SOUTH);
                  revalidate();
                  for (Component component : progress.getComponents()) {
                    component.revalidate();
                  }
                  break;
              }
            }
          };
          unhide.addActionListener(hideListener);
          hide.addActionListener(hideListener);
          progress.add(hide, c);
          ArrayList<ShipThreadPoolExecutor> executors = new ArrayList<>();

          for (SeaPort port : world.ports) {
            ShipThreadPoolExecutor poolExecutor = new ShipThreadPoolExecutor(port.docks.size()+port.docks.size()/2, port.docks.size()*2, 365, TimeUnit.DAYS, new LinkedBlockingDeque());
            executors.add(poolExecutor);
            for (Dock dock : port.docks) {

              ShipRunnable sRunnable = new ShipRunnable(dock, port, progress, poolExecutor);
              poolExecutor.execute(sRunnable);
            }

          }
          Thread thread = new Thread() {
            @Override
            public void run() {

              for (ShipThreadPoolExecutor executor : executors) {
                try {
                  executor.awaitTermination(1, TimeUnit.HOURS);
                } catch (InterruptedException ex) {
                  System.out.println("error");
                }
              }
              progress.removeAll();
              progress.revalidate();
            }
          };
          thread.start();
          return retString;
        } catch (InvalidInputException e) {
          enableComponents(enableable, false);
          return "An error in the file \"" + file.toString() + "\" on line " + e.getMessage() + " prevented the file from being read.";
        } finally {
          this.setTitle(file.getName() + " - Sea Port Program");
        }
      } else {
        return "Please pick a file.";
      }
    } catch (FileNotFoundException ex) {
      return "File not found.";
    }

  }//helper method tochoose and load a file and create a world object

  private class hideListener implements ActionListener {

    private Container progressBars;

    @Override
    public void actionPerformed(ActionEvent e) {
      switch (e.getActionCommand()) {
        case "hide":
          break;
        case "unhide":
          break;
      }
    }

  }

  private String search(String type, String value) {
    if (world == null) {
      return "No file is loaded to search";
    }
    switch (type) {
      case "Parent Name":
        return world.searchByParentName(value);
      case "Skill":
        return world.searchBySkill(value);
      case "Parent Index":
        return world.searchByParentIndex(value);
      case "Index":
        return world.searchByIndex(value);
      case "Name":
        return world.searchByName(value);
      default:
        return "Not a valid search type.";
    }
  }//helper method performs a search of given type on given value

  public static void main(String[] args) {
    SeaPortProgram spp = new SeaPortProgram();
    spp.setMinimumSize(new Dimension(720, 500));
    spp.setLocationRelativeTo(null);
    spp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    spp.pack();
    spp.setVisible(true);

  }//Builds and displays GUI
}
