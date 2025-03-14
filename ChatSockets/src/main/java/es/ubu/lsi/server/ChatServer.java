package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz que define los métodos principales para un servidor de chat.
 */	
public interface ChatServer {
	
	/**
     * Inicia el servidor y comienza a aceptar conexiones entrantes.
     */
	void startup();
	
	/**
     * Apaga el servidor, cerrando todos los sockets abiertos y liberando recursos.
     */
	void shutdown();
	
	/**
     * Difunde un mensaje de chat a todos los clientes conectados.
     * @param message Objeto ChatMessage que contiene los datos del mensaje a enviar.
     */
	void broadcast(ChatMessage message);
	
	/**
     * Elimina (desconecta) al cliente con el identificador especificado.
     * @param id Identificador único del cliente que se va a remover.
     */
	void remove(int id);
}
