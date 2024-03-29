package mars;

import java.util.*;
import java.io.*;
import java.net.*;

class SharedStuff{
  private List<Player> lst = new ArrayList<Player>();
  private String gameState="";
  private volatile boolean lock=false;
  private volatile boolean lock1 = false;
  private boolean isRunning=true;
  private int secondBeforStart = 10;
  public SharedStuff(){
  }
  synchronized  public List<Player> getLstPlayer(){
    return this.lst;
  }
  synchronized  public void add(Player pl){
    this.lst.add(pl);
  }
  public void setSeconde(int time){
    this.lock1 = true;
      System.out.println(time);
      this.secondBeforStart=time;
    this.lock1 = false;
  }
  public int getSeconde(){
    while(this.lock1 == true){}
    return this.secondBeforStart;
  }
  public void setGameState(String game){
    this.lock = true;
    this.gameState = game;
    this.lock = false;
  }
  public String getGameState(){
    while(this.lock == true){}
    return this.gameState;
  }
  public boolean gameIsRunning(){
    return this.isRunning;
  }
  synchronized public void changeGameRunning(boolean state){
    this.isRunning = state;
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
  private boolean inList(List<Player> lst, String name){
    for (Player p:lst ) {
      if(p.getName().equals(name)) return true;
    }
    return false;
  }
  public void run(){
    try {
      OutputStream out;
      DataInputStream in;
      out = this.cl.getOutputStream();
      in = new DataInputStream(new BufferedInputStream(this.cl.getInputStream()));
      out.write(this.share.getGameState().getBytes());
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
      //check if pseudo is already taken
      String magie = "";
      int counttmp = 1;
      while(this.inList(this.share.getLstPlayer(), pseudo+magie)){
        magie = String.valueOf(counttmp);
        counttmp++;
      }
      pseudo = pseudo+magie;
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
    Socket tmp;
    int c = 0;
    this.share.setGameState("\033[H\033[2 \nEEntrer: votre pseudo \\n votre code\nnb player: "+(c+1)+"\n");
    try {
      boolean firstTime = true;
      while(this.share.gameIsRunning() == true){
        tmp = this.server.accept();
        if(this.gameIsStart == false && c < 5){
          SockToPlayer magie = new SockToPlayer(tmp, this.share);
          Thread th = new Thread(magie);
          th.start();
          if(firstTime == true){
            this.share.setGameState("\033[H\033[2 \nEEntrer: votre pseudo \\n votre code\nnb player: "+(c+1)+"\n");
            firstTime = false;
          }else{
            this.share.setGameState("\033[H\033[2 \nEEntrer: votre pseudo \\n votre code\nnb player: "+(c+1)+"\n");
          }
          c++;
        }else{
          Thread th = new Thread(new ClientSockHandler(tmp, this.share));
          th.start();
        }
      }
    } catch(Exception e) {
      System.out.println("Server reboot");
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
    public Network(String host, int port) throws IOException{
      try {
        this.server = new ServerSocket();
        this.server.bind(new InetSocketAddress(host, port));
        this.accept = new AcceptUser(this.server, this.share);
        Thread serverThread = new Thread(this.accept);
        serverThread.start();
        while(this.share.getLstPlayer().size() < 2 ){
        }
        // on attend 10 seconde et on run la game
        String prev = this.share.getGameState();
        for (int i=0; i<10; i++) {
          Thread.currentThread().sleep(1000);
          //\r pour clean la ligne
          this.share.setGameState(prev+"Start dans "+(10-i)+" \n");
        }
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
      this.share.setGameState(this.share.getGameState()+"\nReconnectez vous pour participer a la prochaine game\n");
      try {
        Thread.currentThread().sleep(1000);
      } catch(Exception e) {
        System.err.println("Error in sleep");
        System.exit(1);
      }
      this.share.changeGameRunning(false);
      this.server.close();
    }
}
