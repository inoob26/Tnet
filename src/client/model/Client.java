package client.model;

import org.apache.commons.net.telnet.TelnetClient;

import java.io.*;
import java.nio.charset.Charset;


public class Client implements Runnable {
    private static TelnetClient client;
    private static InputStream inputStream;
    private static OutputStream outputStream;

    public static void main(String[] args) {
        connect("10.192.27.70",23);
    }

    public static void connect(String address, int port){
        client = new TelnetClient();

        try{
            client.connect(address,port);

            Thread reader = new Thread(new Client());
            reader.start();

            outputStream = client.getOutputStream();

            listenConsole(outputStream);

        }catch (Exception ex){
            ex.printStackTrace();
            System.err.println("Exception while connecting:" + ex.getMessage());
        }

    }

    public static void listenConsole(OutputStream outputStream){
        //listen console
        boolean end_loop = false;
        byte[] buff = new byte[3072];
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
                            System.err.println("IOException " + ioe.getMessage());
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
        //end listen
    }

    public static void shutDownPortCisco(OutputStream outputStream){
        String[] strings = {
                "nik\r\n",
                "vsteep12a\r\n",
                "ena\r\n",
                "starforvard\r\n",
                "conf t\r\n",
                "int fa0/5\r\n",
                "no shutdown\r\n",
                "end\r\n",
                "sh int status\r\n"
        };

        sendCommandScriptToServer(strings,outputStream);
    }

    public static void sendCommandScriptToServer(String[] strings,OutputStream outputStream){
        byte[] buff;
        for (int i = 0; i < strings.length;i++){
            buff = strings[i].getBytes(Charset.defaultCharset());
            try{
                outputStream.write(buff,0, strings[i].length());
                outputStream.flush();//turnoff write mode
            }catch (IOException e) {
                System.err.println("Exception while sending command " + e.getMessage());
            }
        }
    }

    @Override
    public void run() {
        inputStream = client.getInputStream();

        try
        {
            byte[] buff = new byte[3072];
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
}
