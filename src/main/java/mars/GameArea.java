package mars;

import java.io.*;
import java.util.*;


public class GameArea{
  private int memorySize;
  private boolean gameIsStart = false;
  private Instruction[] memory;
  private String ljust(String str, int len){
    while(str.length() < len){
      str+= " ";
    }
    return str;
  }
  private void init(){
    for (int i=0; i<this.memorySize; i++) {
      if((i%2) == 0){
        this.memory[i] = new Instruction(null, 0);
      }else{
        this.memory[i] = new Instruction(null, 1);
      }
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
  public void print(){
    for(int i = 0; i < (this.memorySize/100); i+= 1){
      System.out.print(this.ljust(String.valueOf(i*100), 4)+": ");
      //add magie
      for (int j=0; j<100 ; j++) {
        this.memory[i*100 + j].print();
      }
      System.out.println("");
    }
  }
}
