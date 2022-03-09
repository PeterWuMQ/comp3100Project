import java.io.*;
import java.net.*;

public class MyServer {
    public static void main(String[] args) {
        try{
            ServerSocket ss = new ServerSocket(5000);
            Socket s = ss.accept();
            DataInputStream din = new DataInputStream(s.getInputStream());
            
        }catch(Exception e){System.out.println(e);}
    }
}
