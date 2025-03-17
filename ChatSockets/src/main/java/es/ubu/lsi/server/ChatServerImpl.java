package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

import es.ubu.lsi.client.ChatClientImpl;
import es.ubu.lsi.common.ChatMessage;

/**
 * Implementación del servidor de chat basado en sockets TCP.
 */
public class ChatServerImpl implements ChatServer {

	private static final int DEFAULT_PORT = 1500;
	private static int clientId;
	private static SimpleDateFormat sdf;
	private int port;
	private boolean alive;
	private ServerSocket serverSocket;
	private Socket clientSocket;
	private final Map<Integer, ServerThreadForClient> clients = new ConcurrentHashMap<>();
	private final Map<Integer, String> clientUsernames = new ConcurrentHashMap<>();
	
	
	/**
     * Constructor del servidor de chat.
     * @param port Puerto en el que se ejecutará el servidor.
     */
	public ChatServerImpl(int port) {
		
		this.port = port;
		this.alive = true;
		this.clientId = 0;
		
	}
	
	@Override
	public void startup() {
		
		try {
			
			// Crear el servidor de sockets en el puerto 1500
	        serverSocket = new ServerSocket(DEFAULT_PORT);
	        System.out.println("[LOG] Servidor iniciado en el puerto " + DEFAULT_PORT);
	        //Añadimos el usuario -1 que es el servidor para el shutdown
	        clientUsernames.put(-1,"localhost");
	        
	        // Bucle principal para aceptar clientes
	        while (alive) {
	        	
	        	// Verificar si el servidor sigue activo antes de aceptar conexiones
	        	if (!alive) break;
	        	// Aceptar nueva conexión
	        	clientSocket = serverSocket.accept();
	        	
	        	if (!alive) break;
	                      		
	        	// Crear y lanzar el hilo para gestionar al cliente
	        	ServerThreadForClient clientThread = new ServerThreadForClient();
	        	new Thread(clientThread).start();
	                
	        }
			
		} catch (IOException e) {
			
			if (alive) {
				
                System.err.println("[ERR] Error aceptando conexión: " + e.getMessage());
            }
			
		}
		
	}

	@Override
	public void shutdown() {
		
		alive = false;
		
		try {
			
			// Notificar a todos los clientes conectados sobre el apagado del servidor
			ChatMessage shutdownMessage = new ChatMessage(-1, ChatMessage.MessageType.SHUTDOWN,"El servidor se está apagando...");
		    broadcast(shutdownMessage);
		    
		    // Cerrar todas las conexiones activas
		    for (ServerThreadForClient client : new ArrayList<>(clients.values())) {
		    	
	            client.interrupt();  // Matar el hilo del cliente
	            remove(client.id);  // Eliminar cliente del mapa
	        }
	        clients.clear(); // Limpiar la lista de clientes
	        
	        // Cerrar el socket del servidor
	        if (serverSocket != null) {
	            serverSocket.close();
	        }
	        
	        if (clientSocket != null) {
	        	clientSocket.close();
	        }
	        
	        System.out.println("[LOG] Servidor apagado correctamente.");
		    
		} catch (IOException e) {
			
			 System.err.println("[ERR] Error al apagar el servidor: " + e.getMessage());
		}
		
	}

	@Override
	public void broadcast(ChatMessage message) {
		
		String senderUsername = clientUsernames.get(message.getId());
	    String formattedMessage = senderUsername + ": " + message.getMessage();
	    ChatMessage newMessage = new ChatMessage(message.getId(), message.getType(), formattedMessage);
		
		for (ServerThreadForClient client : clients.values()) {
			
			try {
				
				client.outputStream.writeObject(newMessage);
	            client.outputStream.flush();
	            
			} catch (IOException e) {
				
				System.err.println("[ERR] Error al enviar mensaje a " + client.username);
			}
			
	    }
	}

	@Override
	public void remove(int id) {
		
		ServerThreadForClient client = clients.remove(id);
		
		if (client != null) {
			
			try {
				
				if (client.socket != null) client.socket.close();
				if (client.inputStream != null) client.inputStream.close();
	            if (client.outputStream != null) client.outputStream.close();
	            
	            System.out.println("[LOG] Cliente con ID " + id + " eliminado del servidor y conexión cerrada.");
				
			} catch (IOException e) {
				
				 System.err.println("[ERR] Error al cerrar conexión con el cliente " + id);
			}
			
		}
	}
	
	/**
     * Clase interna que maneja la comunicación con un cliente.
     */
	private class ServerThreadForClient extends Thread {
		
		private int id;
		private String username;
		private int port;
		private Socket socket;
		private ObjectInputStream inputStream;
	    private ObjectOutputStream outputStream;
		
		
		public void run() {
			
			try {
				
				// Obtener el socket del cliente aceptado por el servidor
	            socket = clientSocket;
	            // Asignar un ID al cliente
	            this.id = clientId;
	           
	            outputStream = new ObjectOutputStream(socket.getOutputStream());
	            inputStream = new ObjectInputStream(socket.getInputStream());
	            
	            // Enviar el ID al cliente
	            outputStream.writeInt(id);
	            outputStream.flush();
	            clients.put(clientId,this);
	            ++clientId;
	            
	            ChatMessage msg;
	            
	            // Recibir el primer mensaje con el nombre del usuario
	            ChatMessage firstMessage = (ChatMessage) inputStream.readObject();
	            this.username = firstMessage.getMessage();
	            clientUsernames.put(id,username);
	            
	            
	            
	            
	            System.out.println("[LOG] Cliente " + username + " conectado con ID: " + id);

	            
	            while (alive) {
	            	
	            	// Leer el mensaje del cliente
	                msg = (ChatMessage) inputStream.readObject();
	                
	                // Si el cliente envía LOGOUT, eliminarlo y cerrar su conexión
	                if (msg.getType() == ChatMessage.MessageType.LOGOUT || !alive) {
	                	
	                	System.out.println("[LOG] Cliente " + username + " se ha desconectado.");
	                    remove(id);
	                    break;
	                    
	                } else if (msg.getMessage().contains("ha bloquedo a") || msg.getMessage().contains("ha desbloquedo a")) {
	                	
	                	System.out.println("[LOG] " + msg.getMessage());
	                	continue;
	                }
	                
	                // Si es un mensaje normal, enviarlo a todos los clientes
	                System.out.println(username + ": " + msg.getMessage());
	                broadcast(msg);
	                
	            }
				
			} catch (IOException | ClassNotFoundException e) {
				
				if (alive) {
					
		            System.err.println("[ERR] Error en la comunicación con " + username);
		        }
				
				
			} finally {
				
				remove(id);
			}
		}
		
	}
	
	public static void main(String[] args) {
		
		final ChatServerImpl server = new ChatServerImpl(1500); // Instanciar el servidor
		
		// Iniciar el servidor en un hilo separado usando Runnable en Java 7
	    Thread serverThread = new Thread(new Runnable() {
	        @Override
	        public void run() {
	            server.startup();
	        }
	    });
	    
	    serverThread.start(); // Iniciar el servidor en un hilo separado

		// Permitir apagar el servidor manualmente desde la consola
	    Scanner scanner = new Scanner(System.in);
	    System.out.println("Escribe 'shutdown' para apagar el servidor.");
	    System.out.print("> ");
	    while (scanner.hasNext()) {
	        String command = scanner.nextLine();
	        if (command.equalsIgnoreCase("shutdown")) {
	            server.shutdown();
	            break;
	        }
	    }
	    
	    scanner.close();
	}

}
