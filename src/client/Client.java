package client;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.*;
import java.nio.charset.Charset;
/**
 * Created by inoob on 05/06/16.
 */
public class Client implements Runnable {
    private static TelnetClient client;
    private static InputStream inputStream;
    private static OutputStream outputStream;

    public static void main(String[] args) {
        connect("192.168.1.227",23);
    }

    public static void connect(String address, int port){
        client = new TelnetClient();
        boolean end_loop = false;
        try{
            client.connect(address,port);

            Thread reader = new Thread(new Client());
            reader.start();

            outputStream = client.getOutputStream();

            byte[] buff = new byte[1024];
            int ret_read = 0;

            do {//reading console
                try{
                    ret_read = System.in.read(buff);
                    if(ret_read > 0){
                        String line = new String(buff,0,ret_read);
                        if(line.matches("^\\^[A-Z^]\\r?\\n?$")){
                            byte toSend = buff[1];
                            if(toSend == '^'){
                                outputStream.write(toSend);
                            }else {
                                outputStream.write(toSend - 'A' + 1);
                            }
                            outputStream.flush();//turnoff write mode
                        }else {
                            try{
                                outputStream.write(buff,0,ret_read);
                                outputStream.flush();//turnoff write mode
                            }catch (IOException ioe){
                                end_loop = true;
                            }catch (NullPointerException npe){
                                System.err.println("disconnect from server: " + npe.getMessage());
                                System.exit(1);
                            }
                        }
                    }
                }catch (IOException ioe){
                    System.err.println("Exception while reading keyboard:" + ioe.getMessage());
                    end_loop = true;
                }
            }while((ret_read > 0) && (end_loop == false));


        }catch (Exception ex){
            ex.printStackTrace();
            System.err.println("Exception while connecting:" + ex.getMessage());
        }

    }

    @Override
    public void run() {
        inputStream = client.getInputStream();

        try
        {
            byte[] buff = new byte[1024];
            int ret_read = 0;

            do
            {
                ret_read = inputStream.read(buff);
                if(ret_read > 0)
                {
                    System.out.print(new String(buff, 0, ret_read));
                }
            }
            while (ret_read >= 0);
        }
        catch (IOException e)
        {
            System.err.println("Exception while reading socket:" + e.getMessage());
        }

        try
        {
            client.disconnect();
        }
        catch (IOException e)
        {
            System.err.println("Exception while closing telnet:" + e.getMessage());
        }
    }

    /*public static void main(String[] args) {

        TelnetClient client = new TelnetClient();
        String host = "192.168.1.227";


        Scanner sc = new Scanner(System.in);
        try {
            client.connect(host);
            inputStream = client.getInputStream();



            if(client.isConnected()){
                System.out.println("we connected to server =) ");
                readInputStream(inputStream,1024);
                String s1 = sc.nextLine();

                client.disconnect();
            }else{
                System.out.println("It is bad =(");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }*/

    public static void readInputStream(InputStream is, int bufferSize) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        final int CHARS_PER_PAGE = 5000; //counting spaces
        StringBuilder builder = new StringBuilder(CHARS_PER_PAGE);
        try {
            //for(String line=bufferedReader.readLine(); line!=null; line=bufferedReader.readLine()) {
            String line=bufferedReader.readLine();
            builder.append(line);
                //builder.append('\n');
                System.out.println(builder.toString());
            //}
        } catch (Exception ex){
            ex.printStackTrace();
        }

        //return builder.toString();
    }
}