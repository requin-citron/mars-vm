package mars;

import java.io.*;
import java.util.*;


//on peut changer facilement l'ordonanceur ici
public class Scheduler{
  List<Context> ctxLst = new ArrayList<Context>();
  int timePerPlayer;
  public Scheduler(List<Player> lstj, Instruction[] memory, int[] slot){
    for(int i = 0; i<lstj.size(); i++){
      this.ctxLst.add(new Context(lstj.get(i), memory, slot[i]));
    }
    this.timePerPlayer = 1;
  }
  public void next(){
    Context ctx = null;
    boolean flag = true;
    for (int i = 0; i<this.ctxLst.size(); i++) {
        for (int o=0; o<this.timePerPlayer; o++) {
            ctx = this.ctxLst.get(i);
            flag = ctx.next();
            if(flag == false){
              this.ctxLst.remove(ctx);
              break;
            }
        }
    }
  }
}
