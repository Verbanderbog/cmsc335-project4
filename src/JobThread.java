/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Dylan Veraart
 */
public class JobThread extends Thread {
  Job job;
  public JobThread(Job j){
    super(j);
    job=j;
  }
  public JobThread(Job j,String t){
    super(j,t);
    job=j;
  }
}
