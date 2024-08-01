package br.ufrn.imd.ppaiva.chat.handlers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufrn.imd.ppaiva.chat.models.Message;
import br.ufrn.imd.ppaiva.chat.repositories.MessageRepository;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

	private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
	private final MessageRepository messageRepository;
	private final ObjectMapper objectMapper = new ObjectMapper();

	public ChatWebSocketHandler(MessageRepository messageRepository) {
		this.messageRepository = messageRepository;
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String userId = getUserIdFromSession(session);
		sessions.put(userId, session);

		// Enviar mensagens não entregues
		List<Message> undeliveredMessages = messageRepository.findByRecipientIdAndDeliveredFalse(userId);
		for (Message msg : undeliveredMessages) {
			String payload = msg.getSenderName() + ": " + msg.getContent();
			session.sendMessage(new TextMessage(payload));
			msg.setDelivered(true);
			messageRepository.save(msg);
		}
	}

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		MessagePayload messagePayload = objectMapper.readValue(message.getPayload(), MessagePayload.class);
		String targetUserId = messagePayload.getRecipientId();
		String senderName = messagePayload.getSenderName();
		String msg = messagePayload.getContent();

		WebSocketSession targetSession = sessions.get(targetUserId);
		if (targetSession != null && targetSession.isOpen()) {
			targetSession.sendMessage(new TextMessage(senderName + ": " + msg));
		} else {
			// Armazenar mensagem não entregue
			Message undeliveredMessage = new Message();
			undeliveredMessage.setSenderId(getUserIdFromSession(session));
			undeliveredMessage.setSenderName(senderName);
			undeliveredMessage.setRecipientId(targetUserId);
			undeliveredMessage.setContent(msg);
			undeliveredMessage.setDelivered(false);
			messageRepository.save(undeliveredMessage);
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
		String userId = getUserIdFromSession(session);
		sessions.remove(userId);
	}

	private String getUserIdFromSession(WebSocketSession session) {
		return session.getUri().getQuery().split("=")[1];
	}

	private static class MessagePayload {
		private String recipientId;
		private String senderName;
		private String content;

		public MessagePayload() {
		}

		public MessagePayload(String recipientId, String senderName, String content) {
			this.recipientId = recipientId;
			this.senderName = senderName;
			this.content = content;
		}

		public String getRecipientId() {
			return recipientId;
		}

		public void setRecipientId(String recipientId) {
			this.recipientId = recipientId;
		}

		public String getSenderName() {
			return senderName;
		}

		public void setSenderName(String senderName) {
			this.senderName = senderName;
		}

		public String getContent() {
			return content;
		}

		public void setContent(String content) {
			this.content = content;
		}

		// Getters e setters
	}
}
