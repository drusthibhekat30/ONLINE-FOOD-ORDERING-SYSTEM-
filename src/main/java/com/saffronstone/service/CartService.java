package com.saffronstone.service;

import com.saffronstone.dao.FoodDao;
import com.saffronstone.model.CartLine;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartService {
    private static final String CART = "cart";
    private final FoodDao foodDao;
    public CartService(FoodDao foodDao) { this.foodDao = foodDao; }
    @SuppressWarnings("unchecked") private Map<Long,Integer> contents(HttpSession session) { Map<Long,Integer> cart=(Map<Long,Integer>)session.getAttribute(CART); if(cart==null) { cart=new LinkedHashMap<>(); session.setAttribute(CART,cart); } return cart; }
    public void add(HttpSession session,long id) { contents(session).merge(id,1,(current,added)->Math.min(current+added,25)); }
    public void setQuantity(HttpSession session,long id,int quantity) { if(quantity<1) contents(session).remove(id); else contents(session).put(id,Math.min(quantity,25)); }
    public void remove(HttpSession session,long id) { contents(session).remove(id); }
    public void clear(HttpSession session) { contents(session).clear(); }
    public List<CartLine> lines(HttpSession session) { List<CartLine> lines=new ArrayList<>(); contents(session).forEach((id,q)->foodDao.byId(id).ifPresent(item->lines.add(new CartLine(item,q)))); return lines; }
    public BigDecimal subtotal(HttpSession session) { return lines(session).stream().map(CartLine::lineTotal).reduce(BigDecimal.ZERO,BigDecimal::add); }
    public int count(HttpSession session) { return contents(session).values().stream().mapToInt(Integer::intValue).sum(); }
}