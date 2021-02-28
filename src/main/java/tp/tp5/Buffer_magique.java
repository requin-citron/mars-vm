import java.util.*;

class Buffer{
  private Object[] lst;
  public Buffer(int n){
    this.lst = new Object[n];
  }
  synchronized void ecrire(Object el, int ind){
    while(this.lst[ind] != null){
      try {
        this.wait();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    this.lst[ind] = el;
  }
  synchronized Object lecture(int ind){
    while(this.lst[ind] == null){
      try {
        this.wait();
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
    return this.lst[ind];
  }
}

class Writer implements Runnable{
  private Object[] lst;
  private int n;
  private Buffer buff;
  public Writer(Buffer buff, Object[] lst, int n){
    this.lst = lst;
    this.n = n;
    this.buff = buff;
  }
  public void run(){
      for (int i=0; i<n; i++) {
          this.buff.ecrire(this.lst[i], i);
      }
  }
}

class Lecteur implements Runnable{
  private Buffer buff;
  private int end;
  private int start;
  public Lecteur(Buffer buff, int start, int end){
    this.buff = buff;
    this.end = end;
    this.start = start;
  }
  public void run(){
    Object tmp;
      for (int i=this.start; i<this.end+1; i++) {
          tmp = this.buff.lecture(i);
          System.out.println(tmp.toString());
      }
  }
}


public class Buffer_magique{
  public static void main(String[] args) {
    System.out.println("hello world");
  }
}
