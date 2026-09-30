package com.bloodbond.controller;

import com.bloodbond.dto.PostView;
import com.bloodbond.model.*;
import com.bloodbond.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final ReactionRepository reactionRepository;
    private final EmergencyRequestRepository emergencyRequestRepository;

    private User currentUser(Principal principal) {
        return userRepository.findByEmail(principal.getName()).orElseThrow();
    }

    @GetMapping("/")
    public String landing(Model model) {
        long donations = userRepository.sumDonations();
        model.addAttribute("totalDonors", userRepository.countByVerifiedTrue());
        model.addAttribute("totalDonations", donations);
        model.addAttribute("openEmergencies", emergencyRequestRepository.countByStatus(EmergencyStatus.OPEN));
        model.addAttribute("livesImpacted", donations * 3);
        return "index";
    }

    @GetMapping("/home")
    public String feed(Model model, Principal principal) {
        User me = currentUser(principal);
        List<PostView> posts = postRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(p -> toView(p, me)).toList();
        model.addAttribute("posts", posts);
        model.addAttribute("me", me);
        return "home";
    }

    private PostView toView(Post post, User me) {
        List<Reaction> reactions = reactionRepository.findByPost(post);
        Map<String, Long> counts = reactions.stream()
                .collect(Collectors.groupingBy(r -> r.getType().name(), Collectors.counting()));
        String mine = reactions.stream()
                .filter(r -> r.getUser().getId().equals(me.getId()))
                .map(r -> r.getType().name())
                .findFirst().orElse(null);
        List<Comment> comments = commentRepository.findByPostOrderByCreatedAtAsc(post);
        return new PostView(post, counts, mine, comments);
    }

    @PostMapping(value = "/posts", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String createPost(@RequestParam String content,
                             @RequestParam(required = false) MultipartFile image,
                             Principal principal) throws IOException {
        User me = currentUser(principal);
        Post post = new Post();
        post.setAuthor(me);
        post.setContent(content);
        post.setImagePath(saveImage(image, "posts"));
        postRepository.save(post);
        return "redirect:/home";
    }

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, Principal principal) {
        Post post = postRepository.findById(id).orElseThrow();
        User me = currentUser(principal);
        if (post.getAuthor().getId().equals(me.getId()) || me.getRole() == Role.ADMIN) {
            postRepository.delete(post);
        }
        return "redirect:/home";
    }

    @PostMapping("/posts/{id}/react")
    @ResponseBody
    public Map<String, Object> react(@PathVariable Long id,
                                     @RequestBody Map<String, String> body,
                                     Principal principal) {
        User me = currentUser(principal);
        Post post = postRepository.findById(id).orElseThrow();
        ReactionType type = ReactionType.valueOf(body.get("type"));
        Reaction existing = reactionRepository.findByPostAndUser(post, me);
        if (existing != null && existing.getType() == type) {
            reactionRepository.delete(existing);
        } else if (existing != null) {
            existing.setType(type);
            reactionRepository.save(existing);
        } else {
            Reaction r = new Reaction();
            r.setPost(post);
            r.setUser(me);
            r.setType(type);
            reactionRepository.save(r);
        }
        List<Reaction> all = reactionRepository.findByPost(post);
        Map<String, Long> counts = all.stream()
                .collect(Collectors.groupingBy(r -> r.getType().name(), Collectors.counting()));
        String mine = all.stream()
                .filter(r -> r.getUser().getId().equals(me.getId()))
                .map(r -> r.getType().name()).findFirst().orElse(null);
        Map<String, Object> result = new HashMap<>();
        result.put("counts", counts);
        result.put("mine", mine);
        return result;
    }

    @PostMapping("/posts/{id}/comment")
    @ResponseBody
    public Map<String, Object> comment(@PathVariable Long id,
                                       @RequestBody Map<String, String> body,
                                       Principal principal) {
        User me = currentUser(principal);
        Post post = postRepository.findById(id).orElseThrow();
        Comment c = new Comment();
        c.setPost(post);
        c.setAuthor(me);
        c.setContent(body.get("content"));
        commentRepository.save(c);
        Map<String, Object> result = new HashMap<>();
        result.put("id", c.getId());
        result.put("author", me.getFullName());
        result.put("avatar", me.getAvatarPath() == null ? "" : me.getAvatarPath());
        result.put("content", c.getContent());
        result.put("time", c.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a")));
        return result;
    }

    static String saveImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) return null;
        String original = Objects.requireNonNull(file.getOriginalFilename());
        String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : ".png";
        String dir = "uploads/" + folder;
        Files.createDirectories(Paths.get(dir));
        String filename = UUID.randomUUID() + ext;
        Files.copy(file.getInputStream(), Paths.get(dir, filename), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + folder + "/" + filename;
    }
}
