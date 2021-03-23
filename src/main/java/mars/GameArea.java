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
  private int MAX_CLOCK = 10000;
  private int count = 0;
  private SharedStuff share;
  private boolean network = false;
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
  public GameArea(SharedStuff sh){
    this.share = sh;
    this.network = true;
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
    String output = "";
    //this.debug();
    this.sched = new Scheduler(this.joeursLst, this.memory, this.slot);
    this.count = 0;
    while(this.nbPlayerAlive()>1 && this.count < this.MAX_CLOCK){
      output = "\033[H\033[2J";
      output+="Nb Alive : "+this.nbPlayerAlive()+"\n";
      output+=this.getState();
      if(this.network==false)System.out.println(output);
      else this.share.setGameState(output);
      this.sched.next();
      this.count++;
    }
    List<Player> winner = new ArrayList<Player>();
    String chaine = "";
    if(this.count == this.MAX_CLOCK){
      int tab[] = new int[this.joeursLst.size()];
      for(int i = 0; i<tab.length; i++) tab[i]=0;
      for (Instruction inst : this.memory ) {
          if(inst != null){
            tab[inst.getId()-1]++;
          }
      }
      int max = -1;
    for(int ind = 0; ind<tab.length;ind++){
      if(tab[ind]>=max){
        if(tab[ind] != max) winner = new ArrayList<Player>();
        max = tab[ind];
        for (Player p : this.joeursLst) {
          if(p.getId()==ind+1) winner.add(p);
        }
      }
    }

    for (Player p : winner) {
      chaine = "The winner is: "+Instruction.userColor[p.getId()-1]+p.getName()+Instruction.ANSI_RESET + " avec "+max+"cases";
      System.out.println(chaine);
      if(this.network == true) this.share.setGameState(this.share.getGameState()+"\n"+chaine+"\n");
    }
    }else{
      for(Player p: this.joeursLst){
        if(p.getState()) winner.add(p);
      }
      chaine = "The winner is: "+Instruction.userColor[winner.get(0).getId()-1]+winner.get(0).getName()+Instruction.ANSI_RESET;
      System.out.println(chaine);
      if(this.network == true) this.share.setGameState(this.share.getGameState()+"\n"+chaine+"\n");
    }
    return this.getState();
  }
}
