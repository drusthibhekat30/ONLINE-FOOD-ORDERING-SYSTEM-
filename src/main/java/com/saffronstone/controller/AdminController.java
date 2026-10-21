package com.saffronstone.controller;

import com.saffronstone.dao.FoodDao;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.List;

@Controller
public class AdminController {
    private static final List<String> STATUSES=List.of("Placed","Preparing","Out for delivery","Delivered");
    private static final List<String> CATEGORIES=List.of("Starters","Main Course","Desserts","Drinks");
    private final FoodDao foods; private final JdbcTemplate jdbc;
    public AdminController(FoodDao foods,JdbcTemplate jdbc) { this.foods=foods; this.jdbc=jdbc; }
    @GetMapping("/admin") public String dashboard(HttpSession session,Model model,RedirectAttributes flash) {
        if(!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) { flash.addFlashAttribute("error","Admin access is required."); return "redirect:/account"; }
        model.addAttribute("foods",foods.all()); model.addAttribute("orders",jdbc.queryForList("SELECT o.*,u.name AS customer FROM orders o JOIN users u ON o.user_id=u.id ORDER BY o.created_at DESC")); model.addAttribute("users",jdbc.queryForList("SELECT id,name,email,created_at FROM users ORDER BY created_at DESC")); model.addAttribute("statuses",STATUSES); model.addAttribute("categories",CATEGORIES); return "admin";
    }
    @PostMapping("/admin/food") public String saveFood(@RequestParam(required=false) Long id,@RequestParam String name,@RequestParam String description,@RequestParam String category,@RequestParam BigDecimal price,@RequestParam String imageUrl,@RequestParam(defaultValue="false") boolean vegetarian,HttpSession session,RedirectAttributes flash) {
        if(!Boolean.TRUE.equals(session.getAttribute("isAdmin"))) return "redirect:/account";
        if(name.trim().isEmpty() || name.length()>160 || description.trim().isEmpty() || description.length()>500 || !CATEGORIES.contains(category) || price.compareTo(BigDecimal.ZERO)<=0 || price.compareTo(BigDecimal.valueOf(9999))>0 || imageUrl.length()>600 || !(imageUrl.startsWith("https://") || imageUrl.startsWith("/"))) { flash.addFlashAttribute("error","Menu item details are invalid."); return "redirect:/admin"; }
        if(id==null) foods.save(name.trim(),description.trim(),category,price,imageUrl,vegetarian); else foods.update(id,name.trim(),description.trim(),category,price,imageUrl,vegetarian); return "redirect:/admin";
    }
    @PostMapping("/admin/food/delete") public String deleteFood(@RequestParam long id,HttpSession session) { if(Boolean.TRUE.equals(session.getAttribute("isAdmin"))) foods.delete(id); return "redirect:/admin"; }
    @PostMapping("/admin/order/status") public String status(@RequestParam long orderId,@RequestParam String status,HttpSession session) { if(Boolean.TRUE.equals(session.getAttribute("isAdmin")) && STATUSES.contains(status)) jdbc.update("UPDATE orders SET status=? WHERE id=?",status,orderId); return "redirect:/admin#orders"; }
}