package me.pieking.game.net;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import me.pieking.game.Game;
import me.pieking.game.Logger;
import me.pieking.game.Utils;
import me.pieking.game.Logger.ExitState;

public class Client {
	
	private static final String SERVER_IP = "*Server IP here*";
	private static final int SERVER_PORT = 1337;
	private static Socket sock;
	private static DataOutputStream out;
	private static DataInputStream in;
	private static Thread listeningThread;
	private static boolean running = true;
	private static long lastPing = System.currentTimeMillis();
	private static SocketStatus status = SocketStatus.DISCONNECTED;
	private static String myIp;
	
	public static void tick(){
		if(status != SocketStatus.CONNECTED){
			if(Game.getTime() % 240 == 0){
				new Thread(Client::init).start(); // lovely
			}
			
			return;
		}
		
		if(Game.getTime() % 120 == 0){
			write("keepalive");
		}
	}
	
	public static void write(String msg){
		if(status != SocketStatus.CONNECTED) return;
		
		try {
			byte[] data = Utils.compress(msg);
			out.writeInt(data.length);
			out.write(data);
			out.flush();
		} catch (Exception e) {
			Logger.error("Could not write \"" + msg + "\" to " + sock.getInetAddress().getHostAddress() + ": " + e.getMessage());
		}
	}

	public static boolean init(){
		try {
			sock = new Socket();
			sock.connect(new InetSocketAddress(SERVER_IP, SERVER_PORT), 2000);
			
			listen();
			
			status = SocketStatus.CONNECTED;
			lastPing = System.currentTimeMillis();
			return true;
		}catch (UnknownHostException e) {
			Logger.info("Could not connect: " + e.getMessage(), Logger.VB_NET);
			return false;
		}catch (IOException e) {
			if(e.getMessage().equals("connect timed out")) return false;
			Logger.info("Could not connect: " + e.getMessage(), Logger.VB_NET);
			return false;
		}
	}

	private static void listen() {
		try {
			out = new DataOutputStream(sock.getOutputStream());
			in = new DataInputStream(sock.getInputStream());
		}catch (IOException e1) {}
		
		listeningThread = new Thread(() -> {
			w: while(status == SocketStatus.CONNECTED){
				
				try {
					
					if(System.currentTimeMillis() - lastPing > 5000) disconnect(SocketStatus.TIMED_OUT);
					
					if(in.available() > 4){
    					int length = in.readInt();
    					
//    					System.out.println(length);
    					
    					if(length > in.available() || length < 0){
    						//obviously corrupt
    					}else{
        					byte[] data = new byte[length];
        					in.readFully(data);
        					try{
        						String msg = Utils.decompress(data);
        						recieve(msg);
        					}catch(Exception e){
        						System.out.println("I caught this:");
        						e.printStackTrace();
        						lastPing = System.currentTimeMillis();
        					}
    					}
					}
				}catch(SocketException | EOFException e){
					disconnect(SocketStatus.ERROR);
					close();
					e.printStackTrace();
					break w;
				}catch (Exception e) {
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(10);
				}catch (InterruptedException e) {}
			}
		});
		listeningThread.start();
	}

	private static void close() {
		running = false;
		status = SocketStatus.DISCONNECTED;
		
		try{ out.close(); }catch (Exception e) {}
		try{ in.close();  }catch (Exception e) {}
	}

	private static void disconnect(SocketStatus status) {
		JOptionPane.showMessageDialog(null, "Lost connection from server: " + status);
		Client.status = status;
	}

	private static void recieve(String msg) {
		
		lastPing = System.currentTimeMillis();
		
		String type = msg.split("(?<!\\\\)\\|")[0];
		List<String> args = Arrays.asList();
		try{
			args = Arrays.asList(msg.substring(type.length()+1).split("(?<!\\\\)\\|"));
		}catch(IndexOutOfBoundsException e){}
	
//		System.out.println(msg);
//		System.out.println(type + " " + args + " " + msg.substring(type.length()));
		
		switch(type){
			case "keepalive": break;
			case "IP":
				myIp = args.get(0);
				break;
			case "msg": {
				String fromIp = args.get(0);
				String msgp = args.get(1).replace("\\|", "|");
				Game.getMainConsole().write("[" + fromIp + "] " + msgp);
			} break;
			case "ace": {
				try{
    				String fromIp = args.get(0);
    				String msgp = args.get(1).replace("\\|", "|");
    				
//    				System.out.println(msgp);
    				
    				Game.getMainConsole().runDasic(msgp);
				}catch (Exception e) {
					e.printStackTrace();
				}
			} break;
		}
		
	}
	
	public static String myIp(){
		return myIp;
	}
	
	public static enum SocketStatus {
		CONNECTED, DISCONNECTED, TIMED_OUT, ERROR;
	}
	
	public static SocketStatus getStatus(){
		return status;
	}
	
}
