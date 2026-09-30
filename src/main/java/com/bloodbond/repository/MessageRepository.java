package com.bloodbond.repository;

import com.bloodbond.model.Message;
import com.bloodbond.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findBySenderOrReceiverOrderByTimestampDesc(User sender, User receiver);

    @Query("select m from Message m where (m.sender = :a and m.receiver = :b) " +
            "or (m.sender = :b and m.receiver = :a) order by m.timestamp asc")
    List<Message> findConversation(@Param("a") User a, @Param("b") User b);

    @Modifying
    @Transactional
    @Query("update Message m set m.read = true where m.sender = :from and m.receiver = :to and m.read = false")
    int markRead(@Param("to") User to, @Param("from") User from);
}
