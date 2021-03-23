package mars;

import java.util.*;
import java.io.*;
import java.net.*;

class SharedStuff{
  private List<Player> lst = new ArrayList<Player>();
  private String gameState="";
  private volatile boolean lock=false;
  private boolean isRunning=true;
  public SharedStuff(){
  }
  synchronized  public List<Player> getLstPlayer(){
    return this.lst;
  }
  synchronized  public void add(Player pl){
    this.lst.add(pl);
  }
  public void setGameState(String game){
    this.lock = true;
    this.gameState = game;
    this.lock = false;
  }
  synchronized public String getGameState(){
    while(this.lock == true){}
    return this.gameState;
  }
  public boolean gameIsRunning(){
    return this.isRunning;
  }
}

class SockToPlayer implements Runnable{
  private List<Player> lst = new ArrayList<Player>();
  private Socket cl;
  private SharedStuff share;
  public SockToPlayer(Socket sock, SharedStuff magie){
    this.cl = sock;
    this.share = magie;
  }
  public void run(){
    try {
      OutputStream out;
      DataInputStream in;
      out = this.cl.getOutputStream();
      in = new DataInputStream(new BufferedInputStream(this.cl.getInputStream()));
      out.write("Entrer: votre pseudo \\n votre code\n".getBytes());
      byte[] value = new byte[4096];
      // on lit max la taille du buffer
      int size = in.read(value, 0, 4096);
      value = Arrays.copyOfRange(value,0, size);
      //DEBUG
      //System.out.println(Arrays.toString(value));
      int c = 0;
      String pseudo = "";
      while (c < size && value[c] != "\n".getBytes()[0]){
        pseudo += (char)value[c];
        c++;
      }
      c++;
      byte[] code = new byte[size - c];
      for(int i=c; i<size; i++){
        code[i-c] = value[i];
      }
      this.share.add(new Player(this.cl, code, pseudo));
    } catch(Exception e) {
      System.err.println("Error in SockToPlayer");
      System.exit(1);
    }
    try {
      Thread th = new Thread(new ClientSockHandler(this.cl, this.share));
      th.start();
    } catch(Exception e) {
      System.err.println("Error in SockToPlayer Change Thread");
      System.exit(1);
    }
  }
}

class ClientSockHandler implements Runnable{
  private Socket client;
  private SharedStuff share;
  public ClientSockHandler(Socket cl, SharedStuff sh){
    this.client = cl;
    this.share = sh;
  }
  public void run(){
      try {
        OutputStream out;
        out = this.client.getOutputStream();
        while(this.share.gameIsRunning() == true){
          out.write(this.share.getGameState().getBytes());
          Thread.currentThread().sleep(20);
        }
      } catch(Exception e) {
        try {
          this.client.close();
        } catch(Exception k) {
          System.err.println("Error fermeture socket");
          System.exit(1);
          //c'est moche mais ne vois pas comment faire

        }
      }
      try {
        this.client.close();
      }catch(Exception e) {
        System.err.println("Error fermeture socket");
        System.exit(1);
        //c'est moche mais ne vois pas comment faire

      }
  }
}

class AcceptUser implements Runnable{
  private ServerSocket server;
  private volatile int nb;
  private volatile boolean gameIsStart = false;
  private SharedStuff share;
  public AcceptUser(ServerSocket serv, SharedStuff magie){
    this.server = serv;
    this.share = magie;
  }
  public void changeState(boolean input){
    this.gameIsStart = input;
  }
  public void run(){
    try {
      Socket tmp;
      int c = 0;
      while(true){
        tmp = this.server.accept();
        if(this.gameIsStart == false && c < 5){
          SockToPlayer magie = new SockToPlayer(tmp, this.share);
          Thread th = new Thread(magie);
          th.start();
          c++;
        }else{
          Thread th = new Thread(new ClientSockHandler(tmp, this.share));
          th.start();
        }
      }
    } catch(Exception e) {
      System.err.println("Error in AcceptUser");
      System.exit(1);
    }
  }
}

public class Network{
    private ServerSocket server;
    private List<Socket> clients = new ArrayList<Socket>();
    private boolean gameIsStart = false;
    private int port;
    private SharedStuff share = new SharedStuff();
    private AcceptUser accept;
    public Network(int port) throws IOException{
      try {
        this.server = new ServerSocket(port);
        this.accept = new AcceptUser(this.server, this.share);
        Thread serverThread = new Thread(this.accept);
        serverThread.start();
        while(this.share.getLstPlayer().size() < 2 ){
        }
        // on attend 10 seconde et on run la game
        Thread.currentThread().sleep(10*1000);
        this.accept.changeState(true);
        System.out.println("The Game going to start");
      } catch(Exception e) {
        System.err.println("Error in Network");
      }
      GameArea test = new GameArea(this.share);
      for (Player p : this.share.getLstPlayer()) {
        test.addPlayer(p);
      }
      test.run();
    }
}
