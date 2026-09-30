package com.bloodbond.controller;

import com.bloodbond.dto.ConversationView;
import com.bloodbond.model.Message;
import com.bloodbond.model.User;
import com.bloodbond.repository.MessageRepository;
import com.bloodbond.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messaging;

    public static String conversationKey(Long a, Long b) {
        return a < b ? a + "-" + b : b + "-" + a;
    }

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @GetMapping("/chat")
    public String conversations(Model model, Principal principal) {
        User me = currentUser(principal);
        List<Message> all = messageRepository.findBySenderOrReceiverOrderByTimestampDesc(me, me);
        Map<Long, Message> lastByPartner = new LinkedHashMap<>();
        for (Message m : all) {
            Long partnerId = m.getSender().getId().equals(me.getId())
                    ? m.getReceiver().getId() : m.getSender().getId();
            lastByPartner.putIfAbsent(partnerId, m);
        }
        List<ConversationView> conversations = lastByPartner.entrySet().stream()
                .map(e -> new ConversationView(
                        userRepository.findById(e.getKey()).orElseThrow(), e.getValue()))
                .toList();
        model.addAttribute("conversations", conversations);
        model.addAttribute("me", me);
        return "chat";
    }

    @GetMapping("/chat/{userId}")
    public String room(@PathVariable Long userId, Model model, Principal principal) {
        User me = currentUser(principal);
        User other = userRepository.findById(userId).orElseThrow();
        messageRepository.markRead(me, other);
        model.addAttribute("me", me);
        model.addAttribute("other", other);
        model.addAttribute("conversationKey", conversationKey(me.getId(), other.getId()));
        return "chat-room";
    }

    @GetMapping("/api/chat/{userId}/history")
    @ResponseBody
    public List<Message> history(@PathVariable Long userId, Principal principal) {
        User me = currentUser(principal);
        User other = userRepository.findById(userId).orElseThrow();
        return messageRepository.findConversation(me, other);
    }

    @PostMapping("/api/chat/{userId}/send")
    @ResponseBody
    public Message send(@PathVariable Long userId,
                        @RequestBody Map<String, String> body,
                        Principal principal) {
        User me = currentUser(principal);
        User other = userRepository.findById(userId).orElseThrow();
        Message m = new Message();
        m.setSender(me);
        m.setReceiver(other);
        m.setContent(body.get("content"));
        m.setTimestamp(LocalDateTime.now());
        messageRepository.save(m);
        messaging.convertAndSend("/topic/chat/" + conversationKey(me.getId(), other.getId()), m);
        return m;
    }
}
