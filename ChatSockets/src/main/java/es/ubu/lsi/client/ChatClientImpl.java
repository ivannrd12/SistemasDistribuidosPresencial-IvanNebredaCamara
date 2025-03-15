package es.ubu.lsi.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.ChatMessage.MessageType;

/**
 * Implementación del cliente de chat basado en sockets TCP.
 */
public class ChatClientImpl implements ChatClient {
	
	private String server;
    private String username;
    private int port;
    private Boolean carryOn = true;
    private int id;
    private Socket socket;
    private ObjectOutputStream  outputStream;
	
    /**
     * Constructor del cliente de chat.
     * @param server Dirección del servidor.
     * @param port Puerto del servidor.
     * @param username Nombre de usuario.
     */
    public ChatClientImpl(String server,int port,String username) {
    	
    	this.server = (server != null) ? server : "localhost";
    	this.port = port;
    	this.username = username;
    }
    
	public boolean start() {
		
		try {
			
			// Conectar al servidor en el puerto 1500
			socket = new Socket(server, port);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
			
			// Recibir el ID asignado por el servidor
            this.id = inputStream.readInt();
            System.out.println("Conectado al servidor como " + username + " (ID: " + id + ")");
			
			// Iniciar el hilo de escucha de mensajes
	        new Thread(new ChatClientListener()).start();
	        
            return true;
	        
		} catch (IOException e) {
			
			System.err.println("Error al conectar con el servidor: " + e.getMessage());
		}
		
		return false;
	}

	public void sendMessage(ChatMessage msg) {
		
		try {
			
			if (outputStream != null) {
				
				outputStream.writeObject(msg);
				outputStream.flush();
			}
			
			
		} catch (IOException e) {
			
			System.err.println("Error al enviar mensaje: " + e.getMessage());
		}
	}

	public void disconnect() {
	
		carryOn = false;
		
		try {
			
			if (outputStream  != null) {
				
				// Enviar mensaje de LOGOUT al servidor antes de cerrar
				ChatMessage logoutMessage = new ChatMessage(id, MessageType.LOGOUT, "logout");
				sendMessage(logoutMessage);
				
			}
			
			if (socket  != null) {
				
				socket.close();
				System.out.println("Desconectado del chat.");
			}
			
			
		} catch (IOException e) {
			
			System.err.println("Error al cerrar la conexión: " + e.getMessage());
        }
		}
		
	public ObjectInputStream getInputStream() {
	    return inputStream;
	}

	public ObjectOutputStream getOutputStream() {
	    return outputStream;
	}
	
	
	/**
     * Clase interna que maneja la escucha de mensajes del servidor.
     */
	private class ChatClientListener implements Runnable {
		
		public void run() {
			
			try (ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream())){
				
				while (carryOn) {
					
					 ChatMessage msg = (ChatMessage) inputStream.readObject();
	                 System.out.println(msg.getId() + ": " + msg.getMessage());
				}
				
				
			} catch (IOException | ClassNotFoundException e) {
				
				 System.err.println("Desconectado del servidor.");
	        }
			
		}
	}
	
	/**
     * Método principal para ejecutar el cliente desde la consola.
     */
	public static void main(String[] args) {
		
		if (args.length < 2) {
			System.out.println("Uso: java es.ubu.lsi.client.ChatClientImpl <servidor> <nickname>");
	        return;
		}
		
		String server = args[0];
	    String username = args[1];
	    int port = 1500;
	    
	    ChatClientImpl client = new ChatClientImpl(server,port,username);
	    
	    if (client.start()) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Cliente conectado. Escriba mensajes para enviar. Escriba 'logout' para salir.");
            
            while (client.carryOn) {
                String msg = scanner.nextLine();
                if (msg.equalsIgnoreCase("logout")) {
                    client.disconnect();
                    break;
                }
                
                // Crear mensaje con el tipo adecuado y enviarlo
                ChatMessage chatMessage = new ChatMessage(client.id, MessageType.MESSAGE,msg);
                client.sendMessage(chatMessage);
            }
            
            scanner.close();
	    }

	}
		
		
}
		
    
