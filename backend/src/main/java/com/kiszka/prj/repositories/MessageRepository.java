package com.kiszka.prj.repositories;

import com.kiszka.prj.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findBySenderIdAndSenderType(int senderId, String senderType);
}
