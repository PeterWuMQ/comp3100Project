import java.io.*;  
import java.net.*; 

public class MyClient {  
    public static void main(String[] args) {  
        try{      
            Socket s = new Socket("localhost", 5000);  
            DataInputStream din = new DataInputStream(s.getInputStream());
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            String str = "";
            while(!str.equals("BYE")) {
                str = dout.readUTF();
                System.out.println("SERVER says: " + str);  
                if(str.equals("G'DAY")) {
                    dout.writeUTF("BYE");
                    dout.flush();
                }
            }
            dout.writeUTF("HELO");  
            dout.flush();  
            dout.close();  
            s.close();  
        }catch(Exception e){System.out.println(e);}  
    }  
}  