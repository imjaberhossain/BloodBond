package com.bloodbond.repository;

import com.bloodbond.model.Post;
import com.bloodbond.model.Reaction;
import com.bloodbond.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Reaction findByPostAndUser(Post post, User user);
    List<Reaction> findByPost(Post post);
}
