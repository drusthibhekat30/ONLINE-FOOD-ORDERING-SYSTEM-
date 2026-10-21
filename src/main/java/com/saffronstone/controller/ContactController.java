package com.saffronstone.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.regex.Pattern;

@Controller
public class ContactController {
    private final JdbcTemplate jdbc;
    public ContactController(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    @GetMapping("/contact") public String contact() { return "contact"; }
    @PostMapping("/contact") public String send(@RequestParam String name,@RequestParam String email,@RequestParam String message,RedirectAttributes flash) {
        if(name.trim().isEmpty() || name.length()>120 || !Pattern.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$",email) || message.trim().isEmpty() || message.length()>1200) { flash.addFlashAttribute("error","Please check the form fields and try again."); return "redirect:/contact"; }
        jdbc.update("INSERT INTO contacts(name,email,message) VALUES(?,?,?)",name.trim(),email.trim(),message.trim()); flash.addFlashAttribute("notice","Message received. We'll be in touch."); return "redirect:/contact";
    }
}