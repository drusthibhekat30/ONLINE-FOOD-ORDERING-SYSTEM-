package com.saffronstone.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.regex.Pattern;

@Controller
public class AccountController {
    private static final Pattern EMAIL=Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private final JdbcTemplate jdbc; private final PasswordEncoder encoder;
    public AccountController(JdbcTemplate jdbc,PasswordEncoder encoder) { this.jdbc=jdbc; this.encoder=encoder; }
    @GetMapping("/account") public String account() { return "account"; }
    @PostMapping("/account/register") public String register(@RequestParam String name,@RequestParam String email,@RequestParam String password,HttpServletRequest request,HttpSession session,RedirectAttributes flash) {
        String normalized=email.trim().toLowerCase();
        if(name.trim().length()<2 || name.length()>120 || !EMAIL.matcher(normalized).matches() || password.length()<8 || password.length()>72 || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) { flash.addFlashAttribute("error","Use a valid name and email. Passwords need 8-72 characters, a letter, and a number."); return "redirect:/account"; }
        if(jdbc.queryForObject("SELECT COUNT(*) FROM users WHERE email=?",Integer.class,normalized)>0) { flash.addFlashAttribute("error","An account already exists for that email."); return "redirect:/account"; }
        jdbc.update("INSERT INTO users(name,email,password_hash) VALUES(?,?,?)",name.trim(),normalized,encoder.encode(password)); var user=jdbc.queryForMap("SELECT id,name FROM users WHERE email=?",normalized); request.changeSessionId();
        session.setAttribute("userId",((Number)user.get("id")).longValue()); session.setAttribute("userName",user.get("name")); session.setAttribute("isAdmin",false); flash.addFlashAttribute("notice","Welcome in. Your account is ready."); return "redirect:/";
    }
    @PostMapping("/account/login") public String login(@RequestParam String email,@RequestParam String password,HttpServletRequest request,HttpSession session,RedirectAttributes flash) {
        var users=jdbc.queryForList("SELECT id,name,password_hash FROM users WHERE email=?",email.trim().toLowerCase());
        if(users.isEmpty() || !encoder.matches(password,(String)users.get(0).get("password_hash"))) { flash.addFlashAttribute("error","Email or password is incorrect."); return "redirect:/account"; }
        var user=users.get(0); Long id=((Number)user.get("id")).longValue(); request.changeSessionId(); session.setAttribute("userId",id); session.setAttribute("userName",user.get("name")); session.setAttribute("isAdmin",jdbc.queryForObject("SELECT COUNT(*) FROM admin WHERE user_id=?",Integer.class,id)>0); return "redirect:/";
    }
    @PostMapping("/account/logout") public String logout(HttpSession session) { session.invalidate(); return "redirect:/"; }
}