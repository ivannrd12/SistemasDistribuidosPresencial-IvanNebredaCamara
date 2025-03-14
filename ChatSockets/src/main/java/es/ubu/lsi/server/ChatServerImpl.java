package es.ubu.lsi.server;

import java.text.SimpleDateFormat;

import es.ubu.lsi.common.ChatMessage;

public class ChatServerImpl implements ChatServer {

	private static final int DEFAULT_PORT = 1500;
	private static int clientId;
	private static SimpleDateFormat sdf;
	private int port;
	private boolean alive;
	
	
	@Override
	public void startup() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void broadcast(ChatMessage message) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove(int id) {
		// TODO Auto-generated method stub
		
	}

}
