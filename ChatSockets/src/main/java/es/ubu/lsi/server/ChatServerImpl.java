package es.ubu.lsi.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Map;
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
	private final Map<Integer, ServerThreadForClient> clients = new ConcurrentHashMap<>();
	
	/**
     * Constructor del servidor de chat.
     * @param port Puerto en el que se ejecutará el servidor.
     */
	public ChatServerImpl(int port) {
		
		this.port = port;
		
	}
	
	@Override
	public void startup() {
		try {
			
			// Crear el servidor de sockets en el puerto 1500
	        serverSocket = new ServerSocket(DEFAULT_PORT);
	        this.alive = true;
	        System.out.println("Servidor iniciado en el puerto " + DEFAULT_PORT);
	        
	        // Bucle principal para aceptar clientes
	        while (alive) {
	        	
	        	try {
	        		
	        		// Aceptar nueva conexión
	                Socket clientSocket = serverSocket.accept();
	                
	                // Asignar un ID único al cliente
	                int assignedId = ++clientId;
	                
	                // Crear y lanzar el hilo para gestionar al cliente
	                ServerThreadForClient clientThread = new ServerThreadForClient();
	                clients.put(assignedId, clientThread);
	                new Thread(clientThread).start();
	                
	                System.out.println("Nuevo cliente conectado con ID: " + assignedId);
	        		
	        	} catch (IOException e) {
	        		
	        		if (alive) {
	                    System.err.println("Error aceptando conexión: " + e.getMessage());
	                }
	        		
	        	}
	        	
	        }
			
		} catch (IOException e) {
			
			System.err.println("Error iniciando el servidor: " + e.getMessage());
		}
		
	}

	@Override
	public void shutdown() {
		
		alive = false;
		
		try {
			
			// Notificar a todos los clientes conectados sobre el apagado del servidor
			ChatMessage shutdownMessage = new ChatMessage(-1, ChatMessage.MessageType.SHUTDOWN, "El servidor se está apagando...");
		    broadcast(shutdownMessage);
		    
		    // Cerrar todas las conexiones activas
		    for (Integer id : new ArrayList<>(clients.keySet())) {
		    	
		    	remove(id);
	        }
	        clients.clear(); // Limpiar la lista de clientes
	        
	        // Cerrar el socket del servidor
	        if (serverSocket != null) {
	            serverSocket.close();
	        }
	        
	        System.out.println("Servidor apagado correctamente.");
		    
		} catch (IOException e) {
			
			 System.err.println("Error al apagar el servidor: " + e.getMessage());
		}
		
	}

	@Override
	public void broadcast(ChatMessage message) {
		
		for (ServerThreadForClient client : clients.values()) {
			
			try {
				
				client.outputStream.writeObject(message);
	            client.outputStream.flush();
	            
			} catch (IOException e) {
				
				System.err.println("Error al enviar mensaje a " + client.username);
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
	            System.out.println("Cliente con ID " + id + " eliminado del servidor y conexión cerrada.");
				
			} catch (IOException e) {
				
				 System.err.println("Error al cerrar conexión con el cliente " + id);
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
	            socket = serverSocket.accept();
	            // Asignar un ID al cliente y crear la instancia de ChatClientImpl
	            this.id = ++clientId;
	            
	            // Reutilizar los flujos de ChatClientImpl
	            outputStream = new ObjectOutputStream(socket.getOutputStream());
	            inputStream = new ObjectInputStream(socket.getInputStream());
	            
	            // Enviar el ID al cliente
	            outputStream.writeInt(id);
	            outputStream.flush();
	            
	            // Recibir el primer mensaje con el nombre del usuario
	            ChatMessage firstMessage = (ChatMessage) inputStream.readObject();
	            this.username = firstMessage.getMessage();
	            
	            System.out.println("Cliente " + username + " conectado con ID: " + id);

	            
	            while (true) {
	            	
	            	// Leer el mensaje del cliente
	                ChatMessage message = (ChatMessage) inputStream.readObject();
	                
	                // Si el cliente envía LOGOUT, eliminarlo y cerrar su conexión
	                if (message.getType() == ChatMessage.MessageType.LOGOUT) {
	                	
	                	System.out.println("Cliente " + username + " se ha desconectado.");
	                    remove(id);
	                    break;
	                }
	                
	                // Si es un mensaje normal, enviarlo a todos los clientes
	                System.out.println(username + ": " + message.getMessage());
	                broadcast(message);
	                
	            }
				
			} catch (IOException | ClassNotFoundException e) {
				
				System.err.println("Error en la comunicación con " + username);
				
			} finally {
				
				remove(id);
			}
		}
		
	}
	
	public static void main(String[] args) {
		
		ChatServerImpl server = new ChatServerImpl(1500); // Instanciar el servidor
		server.startup(); // Iniciar el servidor
	}

}
