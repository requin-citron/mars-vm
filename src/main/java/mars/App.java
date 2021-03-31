package mars;
import org.apache.commons.cli.*;


public class App
{
    public static void main( String[] args ) throws Exception{
        Options options = new Options();
        options.addOption("p", "port", true, "Chose port default port is 7070");
        options.addOption("d", "host", true, "Chose IP default listen on all interfaces");
        options.addOption("h", "help", false, "Show help");
        CommandLineParser parser  = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        if(cmd.hasOption("h")){
          HelpFormatter formatter = new HelpFormatter();
          formatter.printHelp("mars-vm", options);
          System.exit(0);
        }
        int port=7070;
        if(cmd.hasOption("p")){
          port = Integer.parseInt(cmd.getOptionValue("p"));
        }
        String host = "0.0.0.0";
        if(cmd.hasOption("d")){
          host = cmd.getOptionValue("d");
        }
        // if(args.length<1){
        //   System.out.println("Usage: corefile.cor");
        //   System.exit(1);
        // }
        // String path = args[0];
        // GameArea test = new GameArea();
        // Player j1 = new Player("/home/poney/script/java/info4B/asm-parser/target/algo1.cor", "Kevin");
        // test.addPlayer(j1);
        // test.addPlayer(new Player("/home/poney/script/java/info4B/asm-parser/target/algo2.cor", "Kevin"));
        // //test.addPlayer(new Player("/home/poney/script/java/info4B/asm-parser/target/out.cor"));
        // //j1.baseProcessDebug();
        // test.debug();
        // test.run();
        // //test.print();
        System.out.println("Server is Up");
        while(true){
          Network test = new Network(host, port);
          Player.reset();
        }
    }
}
