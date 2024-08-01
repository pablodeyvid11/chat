package br.ufrn.imd.ppaiva.chat.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import br.ufrn.imd.ppaiva.chat.handlers.ChatWebSocketHandler;
import br.ufrn.imd.ppaiva.chat.repositories.MessageRepository;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
	@Autowired
	private MessageRepository messageRepository;

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(new ChatWebSocketHandler(messageRepository), "/chat").setAllowedOrigins("*");
	}
}