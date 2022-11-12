import java.io.*;
import java.net.*;
import  java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

class ClientInfo extends Thread{
    String name;
    Socket socket;

    InetSocketAddress isa;
    // 소켓 -> 서버
    BufferedReader reader;																// 데이터를 읽어옴
    // 서버 -> 소켓
    PrintWriter writer ;
    Map<String, ChatRoomInfo> chatRoomMap;
    ChatRoomInfo chatRoomInfo;
    public ClientInfo(Socket socket, InetSocketAddress isa, BufferedReader reader, PrintWriter writer, Map<String, ChatRoomInfo> chatRoomMap){
        System.out.println("NEW CLIENT");
        this.socket = socket;
        this.isa = isa;
        this.reader = reader;
        this.writer = writer;
        this.chatRoomMap = chatRoomMap;
    }
    public void run(){
        while(true){
            try {
                String line = reader.readLine(); // 서버 클래스 안의 line변수 안에 "안녕하세요!"가 저장됨

                System.out.println(line);
                String[] commandList = line.split(" ");
                if(commandList[0].equals("#CREATE")){
                    if(chatRoomMap.get(commandList[1])==null){
                        // #CREATE room1 Kain
                        writer.println("SUCCESS");
                        writer.flush();
                        this.name = commandList[2];
                        chatRoomInfo = new ChatRoomInfo(commandList[1]);
                        chatRoomMap.put(commandList[1], chatRoomInfo);
                        chatRoomInfo.clients.add(this);
                    } else{
                        writer.println("FAILED");
                        writer.flush();
                    }
                } else if(commandList[0].equals("#JOIN")){
                    // #JOIN room1 Kain
                    if(commandList.length<3){
                        writer.println("FAILED");
                        writer.flush();
                    }
                    if(chatRoomMap.get(commandList[1])!=null){
                        writer.println("SUCCESS");
                        writer.flush();
                        this.name = commandList[2];
                        chatRoomInfo = chatRoomMap.get(commandList[1]);
                        chatRoomInfo.clients.add(this);
                    } else{
                        writer.println("FAILED");
                        writer.flush();
                    }
                } else if(commandList[0].equals("#EXIT")){
                    chatRoomInfo.clients.remove(this);
                    chatRoomInfo = null;
                    writer.println("SUCCESS");
                    writer.flush();
                } else if(commandList[0].equals("#STATUS")){
                    writer.println("RoomName: "+chatRoomInfo.roomName+"\n Members:\n"+chatRoomInfo.clients.toString());
                    writer.flush();
                } else if(commandList[0].equals("#WRITE")){

                    writer.println("WRITE");
                    writer.flush();
                } else if(commandList[0].equals("#READ")){

                    writer.println("READ");
                    writer.flush();
                }
                else{
                    writer.println(line);
                    writer.flush();
                }
                // 서버 -> 소켓
                // 바깥으로 보냄

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
class ChatRoomInfo{
    String roomName;
    ArrayList<ClientInfo> clients;
    public ChatRoomInfo(String roomName){
        this.roomName = roomName;
        clients = new ArrayList<>();
    }
}

public class Server extends Thread{
    public static void main(String[] args) throws IOException {
        Map<String, ChatRoomInfo> chatRoomMap = new HashMap<>();
        ArrayList<ClientInfo> clients = new ArrayList<>();
        int port1, port2;
        if(args.length == 3){
            port1 = Integer.parseInt(args[0]);
            port2 = Integer.parseInt(args[1]);
        } else{
            port1 = 2020;
            port2 = 2021;
        }
        ServerSocket serverSocket1 = new ServerSocket(port1); // 포트번호를 9500번으로 지정
        ServerSocket serverSocket2 = new ServerSocket(port2); // 포트번호를 9500번으로 지정
        while(true){
            System.out.println("연결을 기다리는 중...");
            Socket socket = serverSocket1.accept();
            InetSocketAddress isa = (InetSocketAddress) socket.getRemoteSocketAddress();
            System.out.println("연결 수락됨" + isa.getHostName());
            // 소켓 -> 서버
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));																	// 데이터를 읽어옴
            //String line = reader.readLine(); // 서버 클래스 안의 line변수 안에 "안녕하세요!"가 저장됨
            // 서버 -> 소켓
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream())); // 소켓으로 데이터를
            // 바깥으로 보냄
            //writer.println(line);
            //writer.flush();
            ClientInfo client = new ClientInfo(socket, isa,reader, writer, chatRoomMap);
            client.start();
            clients.add(client);
        }
    }
}
