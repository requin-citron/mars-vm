package mars;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.net.*;

public class Player{
  private static int id_ref = 1;
  private int id;
  private boolean state = true;
  private List<Instruction> baseProcess =  new ArrayList<Instruction>() ;
  private String name;
  private Socket curr;
  private boolean isNetwork=false;

  private int getLength(Byte fst){
    int tmp = (fst & 0xF0) >> 4;
    if(tmp ==  0x0 || tmp == 0x4 || tmp == 0x9){
      return 3;
    }
    return 5;
  }
  //constructeur avec un fichier spécifier
  public Player(String filename, String name){
    assert this.id_ref < 6;
    this.id = this.id_ref;
    this.id_ref++;
    this.name = name;
    try {
      byte[] content = Files.readAllBytes(Paths.get(filename));
      //parse file
      int c = 0;
      int tmp = 0;
      Instruction inst = null;
      while (c < content.length){
        tmp = this.getLength(content[c]);
        inst = new Instruction(Arrays.copyOfRange(content, c,c+tmp), this.id);
        this.baseProcess.add(inst);
        c += tmp;
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  public Player(Socket curr, byte[] bytecode, String name){
    assert this.id_ref < 6;
    this.id = this.id_ref;
    this.id_ref++;
    this.name = name;
    this.curr = curr;
    try {
      String str = "vos etes :"+Instruction.userColor[this.id-1]+this.name+Instruction.ANSI_RESET;
      this.curr.getOutputStream().write(str.getBytes());
      byte[] content = bytecode;
      //parse file
      int c = 0;
      int tmp = 0;
      Instruction inst = null;
      while (c < content.length){
        tmp = this.getLength(content[c]);
        inst = new Instruction(Arrays.copyOfRange(content, c,c+tmp), this.id);
        this.baseProcess.add(inst);
        c += tmp;
      }
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  public static void reset(){
    id_ref = 1;
  }
  //constructeur avec un soket
  public void loose(){
    this.state = false;
  }
  public boolean getState(){
    return this.state;
  }
  public int getId(){
    return this.id;
  }
  public String getName(){
    return this.name;
  }
  public void baseProcessDebug(){
    for(Instruction inst: this.baseProcess){
      inst.debug();
    }
  }
  public List<Instruction> getInstructions() {
    return this.baseProcess;
  }
}
