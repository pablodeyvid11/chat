package br.ufrn.imd.ppaiva.chat.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.ufrn.imd.ppaiva.chat.models.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
	List<Message> findByRecipientIdAndDeliveredFalse(String recipientId);
}
