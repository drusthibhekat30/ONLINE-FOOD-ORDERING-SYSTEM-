package com.saffronstone.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {
    private final JdbcTemplate jdbc;
    public ReviewController(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @PostMapping("/reviews") public String review(@RequestParam long itemId,@RequestParam int rating,@RequestParam String comment,HttpSession session,RedirectAttributes flash) {
        Long userId=(Long)session.getAttribute("userId");
        if(userId==null) { flash.addFlashAttribute("error","Sign in to leave a review."); return "redirect:/account"; }
        if(rating<1 || rating>5 || comment.trim().isEmpty() || comment.length()>600 || jdbc.queryForObject("SELECT COUNT(*) FROM food_items WHERE id=?",Integer.class,itemId)==0) { flash.addFlashAttribute("error","Please provide a rating and a short review."); return "redirect:/"; }
        jdbc.update("INSERT INTO reviews(user_id,food_item_id,rating,comment) VALUES(?,?,?,?)",userId,itemId,rating,comment.trim());
        jdbc.update("UPDATE food_items SET rating=(SELECT AVG(CAST(rating AS DECIMAL(3,1))) FROM reviews WHERE food_item_id=?) WHERE id=?",itemId,itemId);
        flash.addFlashAttribute("notice","Thanks for sharing your review."); return "redirect:/";
    }
}