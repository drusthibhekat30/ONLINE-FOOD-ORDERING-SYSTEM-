package com.saffronstone.controller;

import com.saffronstone.model.CartLine;
import com.saffronstone.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Statement;
import java.util.List;

@Controller
public class OrderController {
    private final JdbcTemplate jdbc; private final CartService cart; private final BigDecimal deliveryFee,taxRate; private final ObjectProvider<JavaMailSender> mailSender;
    public OrderController(JdbcTemplate jdbc,CartService cart,@Value("${app.delivery-fee}") BigDecimal deliveryFee,@Value("${app.tax-rate}") BigDecimal taxRate,ObjectProvider<JavaMailSender> mailSender) { this.jdbc=jdbc; this.cart=cart; this.deliveryFee=deliveryFee; this.taxRate=taxRate; this.mailSender=mailSender; }
    @GetMapping("/checkout") public String checkout(HttpSession session,Model model,RedirectAttributes flash) { if(session.getAttribute("userId")==null) return "redirect:/account"; var lines=cart.lines(session); if(lines.isEmpty()) { flash.addFlashAttribute("error","Your bag is empty."); return "redirect:/#your-bag"; } addTotals(model,lines,0); return "checkout"; }
    @PostMapping("/checkout/place") @Transactional public String place(@RequestParam String address,@RequestParam String paymentMode,@RequestParam(defaultValue="") String coupon,HttpSession session,RedirectAttributes flash) {
        Long userId=(Long)session.getAttribute("userId"); List<CartLine> lines=cart.lines(session);
        if(userId==null) return "redirect:/account";
        if(lines.isEmpty() || address.trim().length()<8 || address.length()>500 || !List.of("COD","UPI").contains(paymentMode)) { flash.addFlashAttribute("error","Check your delivery address and payment choice."); return "redirect:/checkout"; }
        BigDecimal subtotal=cart.subtotal(session); int percent=coupon.isBlank()?0:jdbc.query("SELECT discount_percent FROM coupons WHERE code=? AND active=TRUE",rs->rs.next()?rs.getInt(1):0,coupon.trim().toUpperCase());
        BigDecimal discount=subtotal.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP); BigDecimal tax=subtotal.subtract(discount).multiply(taxRate).setScale(2,RoundingMode.HALF_UP); BigDecimal fee=subtotal.compareTo(BigDecimal.valueOf(35))>=0?BigDecimal.ZERO:deliveryFee; BigDecimal total=subtotal.subtract(discount).add(tax).add(fee);
        KeyHolder key=new GeneratedKeyHolder(); jdbc.update(connection->{ var ps=connection.prepareStatement("INSERT INTO orders(user_id,delivery_address,payment_mode,subtotal,tax,delivery_fee,discount,total,status) VALUES(?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS); ps.setLong(1,userId); ps.setString(2,address.trim()); ps.setString(3,paymentMode); ps.setBigDecimal(4,subtotal); ps.setBigDecimal(5,tax); ps.setBigDecimal(6,fee); ps.setBigDecimal(7,discount); ps.setBigDecimal(8,total); ps.setString(9,"Placed"); return ps; },key);
        Number generatedId=key.getKeys().entrySet().stream().filter(entry->entry.getKey().equalsIgnoreCase("id")).map(java.util.Map.Entry::getValue).filter(Number.class::isInstance).map(Number.class::cast).findFirst().orElseThrow(()->new IllegalStateException("The database did not return an order id."));
        long orderId=generatedId.longValue(); for(CartLine line:lines) jdbc.update("INSERT INTO order_items(order_id,food_item_id,item_name,unit_price,quantity) VALUES(?,?,?,?,?)",orderId,line.item().id(),line.item().name(),line.item().price(),line.quantity());
        sendConfirmation(jdbc.queryForObject("SELECT email FROM users WHERE id=?",String.class,userId),orderId,total); cart.clear(session); flash.addFlashAttribute("notice","Order #"+orderId+" is in the kitchen queue."); return "redirect:/orders";
    }
    @GetMapping("/orders") public String history(HttpSession session,Model model,RedirectAttributes flash) { Long id=(Long)session.getAttribute("userId"); if(id==null) { flash.addFlashAttribute("error","Sign in to see your orders."); return "redirect:/account"; } model.addAttribute("orders",jdbc.queryForList("SELECT * FROM orders WHERE user_id=? ORDER BY created_at DESC",id)); return "orders"; }
    private void addTotals(Model model,List<CartLine> lines,Integer percent) { BigDecimal subtotal=lines.stream().map(CartLine::lineTotal).reduce(BigDecimal.ZERO,BigDecimal::add); BigDecimal discount=subtotal.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100),2,RoundingMode.HALF_UP); BigDecimal tax=subtotal.subtract(discount).multiply(taxRate).setScale(2,RoundingMode.HALF_UP); BigDecimal fee=subtotal.compareTo(BigDecimal.valueOf(35))>=0?BigDecimal.ZERO:deliveryFee; model.addAttribute("lines",lines); model.addAttribute("subtotal",subtotal); model.addAttribute("discount",discount); model.addAttribute("tax",tax); model.addAttribute("deliveryFee",fee); model.addAttribute("total",subtotal.subtract(discount).add(tax).add(fee)); }
    private void sendConfirmation(String email,long id,BigDecimal total) { JavaMailSender sender=mailSender.getIfAvailable(); if(sender==null)return; try { SimpleMailMessage message=new SimpleMailMessage(); message.setTo(email); message.setSubject("Order #"+id+" confirmed"); message.setText("Your order is placed. Total: $"+total+". Follow progress in Order history."); sender.send(message); } catch(RuntimeException ignored) { } }
}