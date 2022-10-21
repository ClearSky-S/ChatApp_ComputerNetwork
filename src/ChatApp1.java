import java.io.*;
import java.net.*;
import  java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

class ChatRoom{
    String roomName;
    InetAddress multicastAddress;
    int port;
    public ChatRoom(String roomName, int port) throws UnknownHostException, NoSuchAlgorithmException {
        this.roomName = roomName;
        this.multicastAddress = InetAddress.getByName(getMulticastAddress(roomName));
        System.out.println("Room address: "+ multicastAddress.toString());
        this.port = port;
    }
    public static String getMulticastAddress(String roomName) throws NoSuchAlgorithmException {
        MessageDigest sh= MessageDigest.getInstance("SHA-256");
        sh.update(roomName.getBytes());
        return "225."+ (128+sh.digest()[0]) + "."+(128+sh.digest()[1]) + "."+(128+sh.digest()[2]);
    }
}

class Sender extends Thread{
    ChatRoom chatRoom;
    BufferedReader inFromUser;
    DatagramSocket senderSocket;
    String userName;
    public Sender(ChatRoom chatRoom, BufferedReader inFromUser,String userName) throws IOException {
        this.chatRoom = chatRoom;
        this.inFromUser = inFromUser;
        senderSocket = new DatagramSocket();
        this.userName = userName;
    }

    public void run() {
        byte[] sendData;
        String sentence;

        while(true){
            try {
                sentence = inFromUser.readLine();
            } catch (IOException e) {
                System.out.println("input error");
                continue;
            }
            if(sentence.equals("#EXIT")){
                break;
            }
            // 청크 단위로 나누고 보내야함
            sendData = sentence.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, chatRoom.multicastAddress, chatRoom.port);
            try {
                senderSocket.send(sendPacket);
            } catch (IOException e) {
                System.out.println("send error");
                continue;
            }
        }
    }
}
class Receiver extends Thread {
    ChatRoom chatRoom;
    MulticastSocket receiverSocket;

    public Receiver(ChatRoom chatRoom) throws IOException {
        this.chatRoom = chatRoom;
        receiverSocket = new MulticastSocket(chatRoom.port);
        receiverSocket.joinGroup(chatRoom.multicastAddress);
    }
    public void run(){
        while(true){
            byte[] buffer = new byte[512];
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            try {
                receiverSocket.receive(receivePacket);
            } catch (IOException e) {
                System.out.println("Receiver error");
                continue;
            }
            byte[] receiveData = new byte[receivePacket.getLength()];
            System.arraycopy(receivePacket.getData(), receivePacket.getOffset(), receiveData, 0, receivePacket.getLength());
            System.out.println(new String(receiveData));
        }
    }
}

public class ChatApp1 {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException, InterruptedException {
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Input port number: ");
        int port = Integer.parseInt(inFromUser.readLine());
        System.out.print("Welcome to Chat App!\n");
        System.out.print("Enter chat room name: ");
        String roomName = inFromUser.readLine();
        InetAddress	localAddress = InetAddress.getLocalHost();
        ChatRoom chatRoom = new ChatRoom(roomName, port);

        System.out.print("Enter your name: ");
        String userName = inFromUser.readLine();

        Thread receiver = new Receiver(chatRoom);
        Thread sender = new Sender(chatRoom, inFromUser, userName);
        receiver.start();
        sender.start();
        sender.join();
        System.out.println("end");

    }
}
