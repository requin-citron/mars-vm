package mars;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        if(args.length<1){
          System.out.println("Usage: corefile.cor");
          System.exit(1);
        }
        String path = args[0];
        GameArea test = new GameArea();
        Player j1 = new Player("/home/poney/script/java/info4B/asm-parser/target/out.cor");
        test.addPlayer(j1);
        test.run();
        test.print();
    }
}
