package mars;

import java.io.*;
import java.util.*;


//on peut changer facilement l'ordonanceur ici

public class Context{
    private Player curr;
    private List<Integer> threadLst = new ArrayList<Integer>();
    private Instruction[] memory;
    private int index;
    private int pc;
    private static int processNegatif(int input){
        if(input < 0){
          int ret = input*-1;
          if(8191 < ret){
            ret = ret - 8191-1;
          }
          ret = ((~ret)&0x3fff)+1 ;
          return ret;
        }
        if(8191 < input){
          input = input - 8191-1;
        }
        return input;
    }
    private byte[] makeDat(int value){
      int a = this.processNegatif(value);
      byte[] array = new byte[3];
      array[0] = (byte)((0<<4) | 1);
      array[1] = (byte)(a>>6);
      array[2] = (byte)(a&0x3f);
      return array;
    }
    private int getFromHa(int index){
      int ret = index+this.pc;
      if(ret < 0) ret = this.memory.length + ret;
      return ret;
    }
    private int getFromAt(int index){
      Instruction tmp = this.memory[index+this.pc];
      if(tmp == null) return -1;
      if(tmp.getType() != 0) return -1;
      int ret = index+this.pc+tmp.getA();
      if(ret<0) ret = this.memory.length + ret;
      return ret;
    }

    public Context(Player input, Instruction[] memory, int start){
      this.curr = input;
      this.memory = memory;
      this.threadLst.add(start);
      this.index = 0;
    }
    public Player getPlayer(){
      return this.curr;
    }
    public boolean process(Instruction curri){
      if(curri == null) return false;
      int opcode = curri.getType();
      if(opcode == 0) return false;
      //process A
      int typeA = curri.getaddrAtype();
      Instruction newA;
      int index = -1;
      int indnewA = -1;
      if(typeA == 0){ // "#"
        newA = new Instruction(this.makeDat(curri.getA()), this.curr.getId());
      }else if(typeA == 1){ // " "
        index = curri.getA();
        indnewA = this.getFromHa(index);
        newA = this.memory[indnewA];
        if(newA==null) return false;
      }else{ // @
        index = curri.getA();
        indnewA = this.getFromAt(index);
        if(indnewA == -1) return false;
        newA = this.memory[indnewA];
        if(newA==null) return false;
      }
      indnewA = indnewA % this.memory.length;

      //check B
      int indnewB = -1;
      int typeB = -1;
      int index1 = -1;
      Instruction newB = null;
      if(!(opcode == 4 || opcode == 9)){
        typeB = curri.getaddrBtype();
        index1 = curri.getB();
        if(typeB == 0){
          newB = new Instruction(this.makeDat(curri.getB()), this.curr.getId());
        }
        else if(typeB == 1){// " "
          indnewB = this.getFromHa(index1) ;
        }
        else{//@
          indnewB = this.getFromAt(index1) ;
          if(indnewB==-1) return false;
        }
        indnewB = indnewB % this.memory.length;
        newB = this.memory[indnewB];
      }
      if(opcode == 1){ // mov
          if(typeB == 0) return false;
          this.memory[indnewB]  = newA;
      }else if (opcode == 2){ // ADD
          if(typeB == 0) return false;
          if(newA.getType() != 0) return false;
          this.memory[indnewB] = new Instruction(this.makeDat(newA.getA() + newB.getA()) , this.curr.getId());
          System.out.println("dat value : "+this.memory[indnewB].getA());
      }else if(opcode == 3){ //SUB
          if(typeB == 0) return false;
          if(newA.getType() != 0) return false;
          this.memory[indnewB] = new Instruction(this.makeDat( newB.getA() - newA.getA()) , this.curr.getId());
      }else if(opcode == 4) { // JMP
          if(this.memory[indnewA].getType() == 0) return false;
          this.threadLst.set(this.index, indnewA);
      }else if(opcode == 5){ // JMZ
          if(typeB != 0) return false;
          if(this.memory[indnewA].getType() == 0) return false;
          if(newB.getA() == 0){
            this.threadLst.set(this.index, indnewA);
          }
      }else if(opcode == 6){ // JMG
          if(typeB != 0) return false;
          if(this.memory[indnewA].getType() == 0) return false;
          if(newB.getA() > 0){
            this.threadLst.set(this.index, indnewA);
          }
      }else if(opcode == 7){ // DJZ
          if(typeB != 0) return false;
          this.memory[indnewB] = new Instruction(this.makeDat(newB.getA()-1 ), this.curr.getId());
          if(this.memory[indnewB].getA() == 0){
            this.threadLst.set(this.index, newA.getA());
          }
      }else if(opcode == 8){ //CMP
          if(typeB != 0) return false;
          if(typeA != 0) return false;
          if(newB.getA() == newB.getB()){
            this.threadLst.set(this.index, this.pc+2);
          }
      }else if(opcode == 9){ // FORK
          typeA = this.memory[indnewA].getType();
          if(typeA == 0) return false;
          this.threadLst.add(indnewA);
      }else{
        return false;
      }
      return true;
    }
    //process un tour de player
    //true si c'est bon
    //false si c'est faux
    public boolean next(){
      this.pc = this.threadLst.get(this.index);
      Instruction curr = this.memory[this.pc];
      if(curr == null) return false;
      this.threadLst.set(this.index, this.pc+1);
      System.out.println("DEBUG EXEC + Thread nb "+this.index+" thread size "+this.threadLst.size());
      curr.debug();
      boolean ret = process(curr);
      if(ret == false){
        System.out.println("remove thread "+this.threadLst.size());
        this.threadLst.remove(this.threadLst.get(this.index));
        System.out.println("remove thread "+this.threadLst.size());
        this.index = (this.index)%this.threadLst.size();
      }else{
        this.index = (this.index+1)%this.threadLst.size();
      }
      if(this.threadLst.size() == 0){
        this.curr.loose();
        return false;
      };
      return true;
    }

}
