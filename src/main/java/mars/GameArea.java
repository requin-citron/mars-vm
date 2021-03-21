package mars;

import java.io.*;
import java.util.*;


public class GameArea{
  private int memorySize;
  private volatile boolean gameIsStart = false;
  private volatile Instruction[] memory;
  private List<Player> joeursLst = new ArrayList<Player>();
  private int[] slot = new int[5];
  private Scheduler sched;
  private String ljust(String str, int len){
    while(str.length() < len){
      str+= " ";
    }
    return str;
  }
  private void init(){
    int c = 0;
    int addr = 0;
    int quanta = this.memorySize/6;
    while(c<5){
      addr = (int)(Math.random()*quanta + 500+quanta*c ) % this.memorySize ;
      this.slot[c] = addr;
      c++;
    }
  }
  private void instructionCpy(Player j, int addr){
    int c=0;
    for(Instruction i: j.getInstructions()){
      this.memory[addr+c] = i;
      c++;
    }
  }
  public GameArea(){
    this.memorySize = 8000;
    this.memory= new Instruction[this.memorySize];
    this.init();
  }
  public GameArea(int size){
    if((size%100) != 0){
      System.err.println("Invalid memory size\n it should be % 10");
      System.exit(1);
    }
    this.memorySize = size;
    this.memory= new Instruction[this.memorySize];
    this.init();
  }
  public void addPlayer(Player j){
    if(this.gameIsStart  == true){
      System.err.println("Game is already start");
      System.exit(1);
    }
    this.joeursLst.add(j);
  }
  public void debug(){
    for (int i = 0; i<this.memorySize; i++) {
      if(this.memory[i] != null){
        System.out.print(i+": ");
        this.memory[i].debug();
      }
    }
  }
  public String getState(){
    // 0 pour case vide
    // O pour code
    // # pour dat
    String ret = new String("");
    for(int i = 0; i < (this.memorySize/100); i+= 1){
      ret += this.ljust(String.valueOf(i*100), 4)+": ";
      //add magie
      Instruction tmp;
      for (int j=0; j<100 ; j++) {
        tmp = this.memory[i*100 + j];
        if(tmp != null) ret += tmp.getState();
        else ret+= "0";
      }
      ret += "\n";
    }
    return ret;
  }
  public void print(){
    System.out.println(this.getState());
  }
  public int nbPlayerAlive(){
    int c = 0;
    for (Player p :  this.joeursLst) {
      if(p.getState()) c++;
    }
    return c;
  }
  public String run(){
    Collections.shuffle(joeursLst);
    for(int i=0; i<this.joeursLst.size(); i++){
      this.instructionCpy(this.joeursLst.get(i), this.slot[i]);
    }
    this.debug();
    this.sched = new Scheduler(this.joeursLst, this.memory, this.slot);
    while(this.nbPlayerAlive()>1){
      System.out.println("Nb Alive : "+this.nbPlayerAlive());
      this.sched.next();
      this.print();
    }
    //DEBUG
    int c = 0;
    for(Player p: this.joeursLst){
      if(p.getState()) System.out.println("Le gagant est Player : "+c);
      c++;
    }
    return this.getState();
  }
}
