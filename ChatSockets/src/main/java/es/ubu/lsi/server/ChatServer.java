package es.ubu.lsi.server;

import es.ubu.lsi.common.ChatMessage;

/**
 * Interfaz que define los métodos principales para un servidor de chat.
 */	
public interface ChatServer {

	void startup();
	
	void shutdown();
	
	void broadcast(ChatMessage message);
	
	void remove(int id);
}
