/*
Dylan Veraart
3/23/2019
PortTime.java
Placeholder class for future projects
 */
class PortTime {

  int time;
  public PortTime(){
    time= Integer.parseInt(Long.toString(System.currentTimeMillis()).substring(4));
  }
  private PortTime(int t){
    time=t;
  }
  public PortTime minus(PortTime p){
    return new PortTime(this.time-p.time);
  }
  public String toString(){
    int hours= ((time/1000) / 60) / 60;
    int minutes = (time/1000) / 60;
    int seconds = (time/1000) % 60;
    return String.format("%02d:%02d:%02d", hours,minutes,seconds);
  }
}
