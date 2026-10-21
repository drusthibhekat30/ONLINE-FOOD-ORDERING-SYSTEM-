package com.saffronstone.controller;

import com.saffronstone.dao.FoodDao;
import com.saffronstone.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;

@Controller
public class FoodController {
    private final FoodDao foodDao; private final CartService cart;
    public FoodController(FoodDao foodDao,CartService cart) { this.foodDao=foodDao; this.cart=cart; }
    @GetMapping("/") public String home(@RequestParam(defaultValue="") String q,@RequestParam(defaultValue="") String category,@RequestParam(defaultValue="all") String diet,@RequestParam(required=false) BigDecimal maxPrice,@RequestParam(defaultValue="0") double minRating,HttpSession session,Model model) {
        if(maxPrice!=null && maxPrice.signum()<0) maxPrice=null;
        model.addAttribute("foods",foodDao.search(q.trim(),category,diet,maxPrice,Math.max(0,Math.min(5,minRating)))); model.addAttribute("q",q); model.addAttribute("category",category); model.addAttribute("diet",diet); model.addAttribute("maxPrice",maxPrice); model.addAttribute("minRating",minRating); model.addAttribute("cartLines",cart.lines(session)); model.addAttribute("cartCount",cart.count(session)); model.addAttribute("subtotal",cart.subtotal(session)); model.addAttribute("favorites",session.getAttribute("favorites"));
        return "home";
    }
    @PostMapping("/cart/add") public String add(@RequestParam long itemId,HttpSession session,RedirectAttributes flash) { if(foodDao.byId(itemId).isPresent()) cart.add(session,itemId); flash.addFlashAttribute("notice","Added to your bag."); return "redirect:/"; }
    @PostMapping("/cart/quantity") public String quantity(@RequestParam long itemId,@RequestParam int quantity,HttpSession session) { cart.setQuantity(session,itemId,quantity); return "redirect:/#your-bag"; }
    @PostMapping("/cart/remove") public String remove(@RequestParam long itemId,HttpSession session) { cart.remove(session,itemId); return "redirect:/#your-bag"; }
    @PostMapping("/favorites/toggle") @SuppressWarnings("unchecked") public String favorite(@RequestParam long itemId,HttpSession session) { var favorites=(java.util.Set<Long>)session.getAttribute("favorites"); if(favorites==null) { favorites=new java.util.HashSet<>(); session.setAttribute("favorites",favorites); } if(!favorites.add(itemId)) favorites.remove(itemId); return "redirect:/"; }
}