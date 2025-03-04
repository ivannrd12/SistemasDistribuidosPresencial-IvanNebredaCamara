package es.ubu.lsi.client;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz que define los métodos principales para un cliente de chat.
 */	
public interface ChatClient {

	/**
     * Conecta el cliente al servidor de chat.
     * @return `true` si la conexión es exitosa, `false` en caso contrario.
     */
    boolean start();
    
    /**
     * Envía un mensaje al servidor para que lo retransmita a los demás clientes.
     * @param msg Contenido del mensaje.
     */
    void sendMessage(ChatMessage msg);
    
    /**
     * Desconecta al usuario del servidor y cierra la conexión.
     */
    void disconnect();
	
}
