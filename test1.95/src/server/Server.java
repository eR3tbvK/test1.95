package server;

import client.Login;
import client.ServerObject;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.JPanel;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;



public class Server {

	ArrayList<ObjectOutputStream> clientOutputStreams;
	ArrayList<String> usernames;
	ArrayList<ServerObject> clientObjects;
	ArrayList<Integer> xCoordinates;
	ArrayList<Integer> yCoordinates;
	ObjectOutputStream outStream;
	
	public static void main(String[] args){
		ApplicationContext factory = new ClassPathXmlApplicationContext("spring.xml");
		//new ChatServer().go();  //Old way of doing it.
		//Server server = new Server();	//New and improved way
		Server server = factory.getBean("server",Server.class);
		server.go();
	}

	public class ClientHandler implements Runnable {
		ObjectInputStream inStream;
		public ClientHandler(Socket clientSocket){
			try{
				Socket sock = clientSocket;
				inStream = new ObjectInputStream(sock.getInputStream());
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
		//The object comes in from a client
		public void run(){
			Object clientObject = null;
			try{
				while ((clientObject = inStream.readUnshared()) != null){
					//System.out.println(System.currentTimeMillis());
					ServerObject serverObject = (ServerObject) clientObject;
					//the casted fressh new object that came in
					serverObject.setArrayList(usernames);
					Boolean undefinedUser = serverObject.getUsername().equals("undefined");
					int clientIndex = usernames.indexOf(serverObject.getUsername());

					if(undefinedUser){
						tellThisGuy(clientObjects,usernames.size());
					}
					if(!undefinedUser && serverObject.getArrayList().indexOf(serverObject.getUsername()) < 0){
						System.out.println("New User Logged in: " + serverObject.getUsername());

						usernames.add(serverObject.getUsername());
						clientIndex = usernames.indexOf(serverObject.getUsername()); //update clientIndex since we just added to usernames
						
						clientObjects.add(serverObject);
						tellThisGuy(clientObjects,clientIndex);
						xCoordinates.add(serverObject.getXCoordinate());
						yCoordinates.add(serverObject.getYCoordinate());
					}
					else if(!undefinedUser && clientIndex >= 0){
						clientObjects.set(clientIndex, serverObject);
						xCoordinates.set(clientIndex,serverObject.getXCoordinate());
						yCoordinates.set(clientIndex,serverObject.getYCoordinate());
					}
					serverObject.setArrayList(usernames);

					tellEveryone(serverObject);
				}
			}
			catch(SocketException e){
				System.err.println("User Logged out");
				removeLoggedOutUsers();

			}
			catch(IndexOutOfBoundsException ex){
				System.err.println("IndexOutOfBoundsException caught");
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}


		public void go(){
			clientOutputStreams = new ArrayList<ObjectOutputStream>();
			usernames = new ArrayList<String>();
			clientObjects = new ArrayList<ServerObject>();
			xCoordinates = new ArrayList<Integer>();
			yCoordinates = new ArrayList<Integer>();

			try{
				@SuppressWarnings("resource")
				ServerSocket serverSock = new ServerSocket(5000);

				System.out.println("Server Up");

				while(true){
					Socket clientSocket = serverSock.accept();
		            outStream = new ObjectOutputStream(clientSocket.getOutputStream());
		            clientOutputStreams.add(outStream);
					Thread t = new Thread(new ClientHandler(clientSocket));
					t.start();
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}

		public void removeLoggedOutUsers(){
				Iterator<ObjectOutputStream> it = clientOutputStreams.iterator();
				ObjectOutputStream out = null;
				ServerObject conTest = new ServerObject();

					try{

							while(it.hasNext()){
								out = (ObjectOutputStream) it.next();
								int removedIndex = clientOutputStreams.indexOf(out);
								conTest.setArrayList(usernames);
								if(usernames.size() < removedIndex) 
								{
									conTest.setUsername(usernames.get(removedIndex));
									conTest.setXCoordinate(xCoordinates.get(removedIndex));
									conTest.setYCoordinate(yCoordinates.get(removedIndex));
								}
									
								try{
									synchronized(out){out.writeUnshared(conTest);}
								}catch(SocketException e){
									System.err.println("Removing terminated user from clientOutputStreams the index is:" + clientOutputStreams.indexOf(out));

									
								//if(usernames.size() < removedIndex){
									usernames.remove(removedIndex);;
									clientObjects.remove(removedIndex);;
									xCoordinates.remove(removedIndex);;
									yCoordinates.remove(removedIndex);;
									//}
									clientOutputStreams.remove(out);

								}
								out.reset();
							}
						
					}
					catch(SocketException e){

					}
					catch(Exception e){
						e.printStackTrace();
					}
		}
		
		//The object gets sent to the ONE client it has One Job Oneee Job
		public void tellThisGuy(ArrayList<ServerObject> clientObjects, int thisInt) throws IOException{
			Iterator<ServerObject> clientObject = clientObjects.iterator();
			ObjectOutputStream thisGuy = null;
			thisGuy = clientOutputStreams.get(thisInt);
		
			while(clientObject.hasNext()){
				synchronized(thisGuy){	thisGuy.writeUnshared(clientObject.next());}
				thisGuy.reset();
			}	
		}
		
		//The object gets sent out to every client
		public void tellEveryone(Object one) throws IOException{
			Iterator<ObjectOutputStream> it = clientOutputStreams.iterator();
			ObjectOutputStream out = null;

			while(it.hasNext()){
				out = (ObjectOutputStream) it.next();
				synchronized(out){out.writeUnshared(one);}
				out.reset();
			}

		}
}