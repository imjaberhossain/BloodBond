package com.bloodbond.dto;

import com.bloodbond.model.Message;
import com.bloodbond.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ConversationView {
    private User user;
    private Message lastMessage;
}
