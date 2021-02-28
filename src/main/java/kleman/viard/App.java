package kleman.viard;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        ParserFile parser = new ParserFile("/home/poney/script/java/info4B/mars-vm/src/test/files/test.txt");
        parser.read();
    }
}
