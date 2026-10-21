package com.saffronstone.dao;

import com.saffronstone.model.FoodItem;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public class FoodDao {
    private final JdbcTemplate jdbc;
    private final RowMapper<FoodItem> mapper = (rs, row) -> new FoodItem(rs.getLong("id"), rs.getString("name"), rs.getString("description"), rs.getString("category"), rs.getBigDecimal("price"), rs.getString("image_url"), rs.getBoolean("is_vegetarian"), rs.getDouble("rating"));
    public FoodDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    public List<FoodItem> search(String query, String category, String diet, BigDecimal maxPrice, double minRating) {
        return jdbc.query("SELECT * FROM food_items WHERE available=TRUE AND (?='' OR LOWER(name) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?)) AND (?='' OR category=?) AND (?='all' OR (?='veg' AND is_vegetarian=TRUE) OR (?='nonveg' AND is_vegetarian=FALSE)) AND (? IS NULL OR price<=?) AND rating>=? ORDER BY category,name", mapper, query, "%"+query+"%", "%"+query+"%", category, category, diet, diet, diet, maxPrice, maxPrice, minRating);
    }
    public List<FoodItem> all() { return jdbc.query("SELECT * FROM food_items ORDER BY id DESC", mapper); }
    public Optional<FoodItem> byId(long id) { return jdbc.query("SELECT * FROM food_items WHERE id=? AND available=TRUE", mapper, id).stream().findFirst(); }
    public void save(String name,String description,String category,BigDecimal price,String image,boolean veg) { jdbc.update("INSERT INTO food_items(name,description,category,price,image_url,is_vegetarian) VALUES(?,?,?,?,?,?)",name,description,category,price,image,veg); }
    public void update(long id,String name,String description,String category,BigDecimal price,String image,boolean veg) { jdbc.update("UPDATE food_items SET name=?,description=?,category=?,price=?,image_url=?,is_vegetarian=? WHERE id=?",name,description,category,price,image,veg,id); }
    public void delete(long id) { jdbc.update("UPDATE food_items SET available=FALSE WHERE id=?", id); }
}