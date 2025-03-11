package es.ubu.lsi.client;

import java.io.*;
import java.net.*;

import es.ubu.lsi.common.ChatMessage;

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
			
			socket = new Socket(server, port);
			outputStream = new ObjectOutputStream(socket.getOutputStream());
			
			// 3. Iniciar el hilo de escucha de mensajes
	        new Thread(new ChatClientListener()).start();
	        
	        System.out.println("Conectado al servidor como " + username);
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
			
			if (socket != null) {
				
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
			
			
		}
	}
	
	/**
     * Método principal para ejecutar el cliente desde la consola.
     */
	public static void main(String[] args) {
		
		
	}
		
		
}
		
    
