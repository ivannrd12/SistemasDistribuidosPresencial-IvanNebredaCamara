package es.ubu.lsi.client;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

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
    private Set<String> blockedUsers = new HashSet<>();
	
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
			
			System.err.println("[ERR] Error al conectar con el servidor: " + e.getMessage());
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
			
			System.err.println("[ERR] Error al enviar mensaje: " + e.getMessage());
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
			
			System.err.println("[ERR] Error al cerrar la conexión: " + e.getMessage());
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
					
					// Manejar el apagado del servidor
	                if (msg.getType() == ChatMessage.MessageType.SHUTDOWN) {
	                    System.out.println("[SERVIDOR] " + msg.getMessage());
	                    System.out.println("Desconectando...");
	                    System.exit(0);  // Salir del programa
	                }
					
					// Extraer el nombre del usuario del mensaje
	                String[] parts = msg.getMessage().split(":", 2);
	                if (parts.length < 2) continue;  // Mensaje mal formateado
	                
	                String senderUsername = parts[0].trim();

					
					// Verificar si el mensaje viene de un usuario bloqueado
	                if (blockedUsers.contains(senderUsername)) {
	                    continue; // Ignorar el mensaje si el usuario está bloqueado
	                }
					
					synchronized (System.out) {	// Bloquear la impresión para evitar superposición
						
						System.out.println(msg.getMessage());
						
	                }
					
				}
					
			} catch (IOException | ClassNotFoundException e) {
				
				 System.err.println("[ERR] Desconectado del servidor.");
				 System.exit(0);  // Salir del programa
	        }
			
		}
	}
	
	public void banUser(String username) {
		
	    if (!blockedUsers.contains(username)) {
	        blockedUsers.add(username);
	        System.out.println(username + " ha sido bloqueado.");
	        
	        // Enviar mensaje al servidor notificando el baneo
	        ChatMessage banMessage = new ChatMessage(this.id, ChatMessage.MessageType.MESSAGE, 
	                this.username + " ha bloqueado a " + username);
	        sendMessage(banMessage);
	        
	    } else {
	    	
	        System.out.println(username + " ya está bloqueado.");
	    }
	    
	}
	
	public void unbanUser(String username) {
	    if (blockedUsers.contains(username)) {
	        blockedUsers.remove(username);
	        System.out.println(username + " ha sido desbloqueado.");
	        
	        // Enviar mensaje al servidor notificando el desbloqueo
	        ChatMessage unbanMessage = new ChatMessage(this.id, ChatMessage.MessageType.MESSAGE, 
	                this.username + " ha desbloqueado a " + username);
	        sendMessage(unbanMessage);
	        
	    } else {
	    	
	        System.out.println(username + " no está en la lista de bloqueados.");
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
                    
                } else if (msg.startsWith("ban ")) {
                	
                	String userToBan = msg.substring(4).trim();
                	client.banUser(userToBan);
                    continue;
                    
                } else if (msg.startsWith("unban ")) {
                	
                	String userToUnban = msg.substring(6).trim();
                	client.unbanUser(userToUnban);
                    continue;
                }
                
                // Crear mensaje con el tipo adecuado y enviarlo
                chatMessage = new ChatMessage(client.id, MessageType.MESSAGE,msg);
                client.sendMessage(chatMessage);
            }
            
            scanner.close();
	    }

	}
		
		
}
		
    
