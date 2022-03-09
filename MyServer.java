import java.io.*;
import java.net.*;

public class MyServer {
    public static void main(String[] args) {
        try{
            ServerSocket ss = new ServerSocket(5000);
            Socket s = ss.accept();
            DataInputStream din = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            String str = "";
            while(!str.equals("BYE")) {
                str = din.readUTF();
                System.out.println("CLIENT says: " + str);
                if(str.equals("HELO")) {
                    System.out.println("sending G'DAY");
                    dout.writeUTF("G'DAY");
                    dout.flush();
                }
            }
            System.out.println("sending BYE");
            dout.writeUTF("BYE");
            dout.flush();
            System.out.println("closing connection to client");
            din.close();
            s.close();
            ss.close();
        }catch(Exception e){System.out.println(e);}
    }
}
