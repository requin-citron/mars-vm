package mars;

import java.io.*;
import java.util.*;
import java.nio.file.*;

public class Player{
  private static int id_ref = 1;
  private int id;
  private List<Instruction> baseProcess =  new ArrayList<Instruction>() ;
  private int getLength(Byte fst){
    int tmp = (fst & 0xF0) >> 4;
    if(tmp ==  0x0 || tmp == 0x4 || tmp == 0x9){
      return 3;
    }
    return 5;
  }
  //constructeur avec un fichier spécifier
  public Player(String filename){
    assert this.id_ref < 6;
    id = this.id_ref;
    this.id_ref++;
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
  //constructeur avec un soket
  //

  public void baseProcessDebug(){
    for(Instruction inst: this.baseProcess){
      inst.debug();
    }
  }
  public List<Instruction> getInstructions() {
    return this.baseProcess;
  }
}
