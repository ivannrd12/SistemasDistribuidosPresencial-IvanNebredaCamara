package es.ubu.lsi.client;

import java.io.*;
import java.net.*;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import es.ubu.lsi.common.ChatMessage;
import es.ubu.lsi.common.ChatMessage.MessageType;

/**
 * Implementación del cliente de un sistema de chat basado en sockets TCP.
 * Se conecta al servidor, envía y recibe mensajes, y permite bloquear/desbloquear usuarios.
 * 
 * Gestiona un hilo para la escucha continua de mensajes del servidor.
 * Utiliza flujos de entrada/salida con objetos Java serializados.
 * 
 * El cliente también permite comandos especiales como "logout", "ban" y "unban".
 * 
 * @author ...
 */
public class ChatClientImpl implements ChatClient {
	
	/** Dirección IP o nombre del servidor. */
	private String server;
	/** Nombre de usuario del cliente. */
    private String username;
    /** Puerto de conexión al servidor. */
    private int port;
    /** Bandera para controlar si el cliente sigue activo. */
    private Boolean carryOn = true;
    /** Identificador asignado por el servidor. */
    private int id;
    /** Socket de conexión. */
    private Socket socket;
    /** Flujo de salida para enviar mensajes al servidor. */
    private ObjectOutputStream outputStream;
    /** Flujo de entrada para recibir mensajes del servidor. */
    private ObjectInputStream inputStream;
    /** Indica si el cliente está listo tras la configuración inicial. */
    private boolean isConfigured = false;
    /** Lista de usuarios bloqueados por el cliente. */
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
    
    /**
     * Inicia la conexión con el servidor, configura los flujos y lanza el hilo de escucha.
     * 
     * @return true si la conexión se ha establecido correctamente, false en caso contrario.
     */
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

	/**
	 * Envía un mensaje al servidor.
	 * 
	 * @param msg Objeto ChatMessage que se desea enviar.
	 */
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

	/**
	 * Desconecta el cliente del servidor y cierra todos los recursos.
	 */
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
	 * Hilo interno encargado de recibir y mostrar los mensajes enviados desde el servidor.
	 * Ignora los mensajes de usuarios bloqueados y gestiona el mensaje de apagado del servidor.
	 */
	private class ChatClientListener implements Runnable {
		
		/**
	     * Ejecuta el hilo de escucha de mensajes.
	     * Recibe el ID, configura el cliente y muestra mensajes en consola.
	     */
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
						
						System.out.println("\r" + msg.getMessage()); // \r borra línea actual
						System.out.print("> ");
						
	                }
					
				}
					
			} catch (IOException | ClassNotFoundException e) {
				
				if (carryOn) {
					
				System.err.println("[ERR] Desconectado del servidor.");
				System.exit(0);  // Salir del programa
				}
	        }
			
		}
	}
	
	/**
	 * Bloquea los mensajes de un usuario dado e informa al servidor.
	 * 
	 * @param username Nombre del usuario a bloquear.
	 */
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
	
	/**
	 * Desbloquea los mensajes de un usuario dado e informa al servidor.
	 * 
	 * @param username Nombre del usuario a desbloquear.
	 */
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
	 * Método principal para ejecutar el cliente desde consola usando Maven.
	 * 
	 * Uso esperado:
	 * - mvn exec:java@run-client [servidor] nickname
	 * 
	 * @param args Argumentos de la línea de comandos (servidor y nickname).
	 */
	public static void main(String[] args) {
		
		String server = "localhost";  // Valor por defecto
	    String username;
	    int port = 1500;
	    ChatMessage chatMessage;
	    
	    if (args.length == 1) {
	    	
	        username = args[0];
	        
	    } else if (args.length == 2) {
	    	
	        server = args[0];
	        username = args[1];
	        
	    } else {
	    	
	        System.out.println("Uso: mvn exec:java@run-client [servidor] nickname");
	        return;
	    }
	    
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
		
    
