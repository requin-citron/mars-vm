package mars;

import java.util.*;
import java.io.*;

public class Instruction{
  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLACK = "\u001B[30m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  public static final String ANSI_BLUE = "\u001B[34m";
  public static final String ANSI_PURPLE = "\u001B[35m";
  public static final String ANSI_CYAN = "\u001B[36m";
  public static final String ANSI_WHITE = "\u001B[37m";
  private static final String[] userColor = new String[] {
    Instruction.ANSI_RED,
    Instruction.ANSI_GREEN,
    Instruction.ANSI_YELLOW,
    Instruction.ANSI_BLUE,
    Instruction.ANSI_PURPLE,
    Instruction.ANSI_CYAN,
  };
  private static String[] instructionSet = new String[] {
    "DAT", // 0: valeur non executable B est la valeur de la donnée
    "MOV", // 1: transfere le contenue de l'addresse A a l'addresse B
    "ADD", // 2: ajoute le contenue de l'addresse  A a l'addresse B
    "SUB", // 3: soustrai le contenue de l'addresse A a l'addresse B
    "JMP", // 4: transfere l'execution a l'addresse A
    "JMZ", // 5: transfere l'execution  a l'addresse A si le contenue de l'addresse B == 0
    "JMG", // 6: transfere l'execution a l'addresse A si le contenue de l'addresse B > 0
    "DJZ", // 7: retranche 1 du contenue de l'addresse B et saute a A si le result ==0
    "CMP", // 8: compare le contenue de l'addresse A et le contenue de l'addresse B si ils sont different sauté a l'instruction suivante
    "SPL", // 9: fork
  };
  private static String[] addrSymb = new String[] {
    "#", // 0:
    " ", // 1:
    "@", // 2:
  };
  private String label;
  private int userId;
  private boolean isValid;
  private int type;
  private int addrAtype;
  private int addrBtype;
  private int operandeA;
  private int operandeB;
  private boolean shortOp;
  private int processNegetif(int input){
    if(input>=8191){
      return -1*((0x3fff - input)+1);
    }
    return input;
  }
  public Instruction(byte[] input, int userid){
    this.isValid = true;
    this.userId = userid;
    int[] conversion = new int[input.length];
    for (int i = 0; i<input.length; i++) {
      conversion[i] = input[i]&0xFF;
    }
    this.type = (conversion[0]&0xF0) >> 4;
    this.label = this.instructionSet[this.type];
    this.addrAtype = conversion[0]&0xF;
    if(input.length == 5){
        this.addrBtype = (conversion[1]&0xF0) >> 4;
        this.operandeA = (conversion[1]&0xF) << 10;
        this.operandeA += (conversion[2])<<2;
        this.operandeA += ((conversion[3] &0xC0) >> 6);
        this.operandeB = (conversion[3]&0x3F) << 8;
        this.operandeB += conversion[4];
        this.shortOp = false;
        this.operandeB = this.processNegetif(this.operandeB);
    } else{//3 petite instruction
      this.operandeA = conversion[1]<<6;
      this.operandeA += (conversion[2]&0x3F);
      this.shortOp = true;
    }
    this.operandeA = this.processNegetif(this.operandeA);
    if(this.type == 0){
      this.isValid = false;
    }
  }
  public int getA(){
    return this.operandeA;
  }
  public int getB(){
    return this.operandeB;
  }
  public int getaddrAtype(){
    return this.addrAtype;
  }
  public int getaddrBtype(){
    return this.addrBtype;
  }
  public int getType(){
    return this.type;
  }
  public boolean checkValid(){
    return this.isValid;
  }
  public void printDebug(){
    System.out.println(this.label);
  }
  public String getState(){
    String ret = new String("");
    if(this.userId<0){
      return ret + "0";
    }
    if(this.type == 0){
      ret += this.userColor[this.userId-1] +"#"+this.ANSI_RESET;
    }else{
      ret += this.userColor[this.userId-1] +"O"+this.ANSI_RESET;
    }
    return ret;
  }
  public void print(){
    System.out.print(this.getState());
  }
  public void debug(){
    if(this.shortOp){
      System.out.println(this.userColor[this.userId-1]+ this.label +" "+this.operandeA+this.ANSI_RESET);
    }else{
      System.out.println(this.userColor[this.userId-1] + this.label +" "+this.addrSymb[this.addrAtype]+this.operandeA + ", "+this.addrSymb[this.addrBtype]+this.operandeB+this.ANSI_RESET);
    }

  }
}
