package es.ubu.lsi.client;

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
		// TODO Auto-generated method stub
		return false;
	}

	public void sendMessage(ChatMessage msg) {
		// TODO Auto-generated method stub
		
	}

	public void disconnect() {
		// TODO Auto-generated method stub
		
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
		
    
