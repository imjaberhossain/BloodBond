package com.bloodbond.repository;

import com.bloodbond.model.Post;
import com.bloodbond.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findTop50ByOrderByCreatedAtDesc();
    List<Post> findByAuthorOrderByCreatedAtDesc(User author);
}
