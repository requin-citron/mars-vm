package kleman.viard;
import java.io.*;
import java.util.*;

/**
 * Hello world!
 *
 */
 public class ParserFile{
     private String filename;
     public ParserFile(String filename){
       this.filename = filename;
     }
     public void read(){
       String line;
       try {
         File file = new File(this.filename);
         Scanner reader = new Scanner(file);
         while(reader.hasNextLine()){
           line = reader.nextLine();
           System.out.println(line);
         }
         reader.close();
       } catch(Exception e) {
         e.printStackTrace();
       }
     }
 }
