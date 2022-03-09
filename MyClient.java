import java.io.*;  
import java.net.*; 

public class MyClient {  
    public static void main(String[] args) {  
        try{      
            Socket s = new Socket("10.6.22.16", 5000);  
            DataInputStream din = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            String str = "";
            dout.writeUTF("HELO");
            dout.flush();
            System.out.println("sending: HELO");
            while(!str.equals("BYE")) {
                str = din.readUTF();
                System.out.println("SERVER says: " + str);  
                if(str.equals("G'DAY")) {
                    System.out.println("sending: BYE");
                    dout.writeUTF("BYE");
                    dout.flush();
                }
            }
            System.out.println("closing connection to server");
            dout.close();  
            s.close();  
        }catch(Exception e){System.out.println(e);}  
    }  
}  