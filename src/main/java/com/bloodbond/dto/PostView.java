package com.bloodbond.dto;

import com.bloodbond.model.Comment;
import com.bloodbond.model.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
public class PostView {
    private Post post;
    private Map<String, Long> reactionCounts;
    private String myReaction;
    private List<Comment> comments;
}
