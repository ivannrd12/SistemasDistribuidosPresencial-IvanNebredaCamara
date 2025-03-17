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
    private ObjectOutputStream outputStream;
    private ObjectInputStream inputStream;
    private boolean isConfigured = false; // Indica si el cliente está listo
	
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
			
			System.out.println("Intentando conectar al servidor en " + server + ":" + port);
			// Conectar al servidor en el puerto 1500
			socket = new Socket(server, port);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
	
			// Iniciar el hilo de escucha de mensajes
	        new Thread(new ChatClientListener()).start();
	        
	        // esperar al hilo de escucha
	        synchronized (this) {
	            while (!isConfigured) {
	                wait(); // Esperar hasta que `ChatClientListener` confirme que todo está listo
	            }
	        }
	        
	        // Enviar el nickname al servidor antes de continuar
	     	ChatMessage loginMessage = new ChatMessage(id, MessageType.MESSAGE,username);
	     	sendMessage(loginMessage);        
	        
            return true;
	        
		} catch (IOException | InterruptedException e) {
			
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
			
	/**
     * Clase interna que maneja la escucha de mensajes del servidor.
     */
	private class ChatClientListener implements Runnable {
		
		public void run() {
			
			try {
				
				inputStream = new ObjectInputStream(socket.getInputStream());
				// Recibir el ID asignado por el servidor
	            id = inputStream.readInt();
	            System.out.println("Conectado al servidor como " + username + " (ID: " + id + ")");
	            
	            // Notificar al que todo está listo
	            synchronized (ChatClientImpl.this) { // Bloqueo sobre la instancia principal
	                isConfigured = true;
	                ChatClientImpl.this.notify();  // Notificar al `wait()` en `start()`
	            }
				
				while (carryOn) {
					
					ChatMessage msg = (ChatMessage) inputStream.readObject();
					synchronized (System.out) {	// Bloquear la impresión para evitar superposición
						
						System.out.println(msg.getId() + ": " + msg.getMessage());
	                }
					
					
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
	    ChatMessage chatMessage;
	    
	    ChatClientImpl client = new ChatClientImpl(server,port,username);
	    
	    
	    
	    if (client.start()) {
            Scanner scanner = new Scanner(System.in);
            System.out.println("Cliente "+username+": Escriba mensajes para enviar. Escriba 'logout' para salir.");
            
            while (client.carryOn) {
            	
            	System.out.print("> ");
                String msg = scanner.nextLine();
                if (msg.equalsIgnoreCase("logout")) {
                    client.disconnect();
                    break;
                }
                
                // Crear mensaje con el tipo adecuado y enviarlo
                chatMessage = new ChatMessage(client.id, MessageType.MESSAGE,msg);
                client.sendMessage(chatMessage);
            }
            
            scanner.close();
	    }

	}
		
		
}
		
    
