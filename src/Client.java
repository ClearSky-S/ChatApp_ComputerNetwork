
import java.io.*;
import java.net.*;
import  java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Scanner;

class ClientChatReciver extends Thread {
    BufferedReader reader;
    PrintWriter writer;
    public ClientChatReciver(Socket socket) throws IOException {
        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
    }
    synchronized void recive() throws IOException {
        writer.println("#READ");  //서버로 데이터를 전송한다.
        writer.flush();   //버퍼 안에 있는 값들을 전부 비워준다.
        String line = reader.readLine();
        System.out.println(line);  //서버와 통신이 완료되어 "안녕하세요"라는 값을 가지고 온다.

    }
    @Override
    public void run() {
        super.run();
        String line;
        while (true) {
            try {
                recive();
                sleep(3000);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
    public class Client {
    public static void main(String[] args) throws IOException {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        Socket socket = null;
        InetAddress serverAddress;
        int port1, port2;
        if(args.length == 3){
            serverAddress = InetAddress.getByName(args[0]);
            port1 = Integer.parseInt(args[1]);
            port2 = Integer.parseInt(args[2]);
        } else{
            serverAddress = InetAddress.getByName("127.0.0.1");
            port1 = 2020;
            port2 = 2021;
        }
        try {
            socket = new Socket(serverAddress, port1);
            System.out.println("연결성공");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientChatReciver clientChatReciver = new ClientChatReciver(socket);
        clientChatReciver.start();

        System.out.println("데이터 전송 완료!");
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while(true){
            //클라이언트 -> 소켓

            writer.println(inFromUser.readLine());  //서버로 데이터를 전송한다.
            writer.flush();   //버퍼 안에 있는 값들을 전부 비워준다.
            String line = reader.readLine();
            System.out.println("데이터 받기 성공! (발신기):" + line);  //서버와 통신이 완료되어 "안녕하세요"라는 값을 가지고 온다.


        }

    }
}
