package com.kh.project.domain.buyer.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.kh.project.domain.entity.Wishlist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 찜 목록 DAO 구현체
 * 구매자의 찜 목록 관리를 위한 데이터 접근 구현체
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class WishlistDAOImpl implements WishlistDAO {
    
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    
    // RowMapper 정의
    private final RowMapper<Wishlist> wishlistRowMapper = BeanPropertyRowMapper.newInstance(Wishlist.class);
    
    @Override
    public Long addWishlist(Wishlist wishlist) {
        String sql = """
            INSERT INTO WISHLIST (buyer_id, product_id, added_at)
            VALUES (:buyer.buyerId, :product.productId, :addedAt)
            """;
        
        SqlParameterSource param = new BeanPropertySqlParameterSource(wishlist);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        namedParameterJdbcTemplate.update(sql, param, keyHolder);
        
        return keyHolder.getKey().longValue();
    }
    
    @Override
    public int deleteWishlist(Long buyerId, Long productId) {
        String sql = """
            DELETE FROM WISHLIST 
            WHERE buyer_id = ? AND product_id = ?
            """;
        
        return jdbcTemplate.update(sql, buyerId, productId);
    }
    
    @Override
    public List<Wishlist> findWishlistByBuyerId(Long buyerId) {
        String sql = """
            SELECT w.wishlist_id, w.buyer_id, w.product_id, w.added_at
            FROM WISHLIST w
            WHERE w.buyer_id = ?
            ORDER BY w.added_at DESC
            """;
        
        return jdbcTemplate.query(sql, wishlistRowMapper, buyerId);
    }
    
    @Override
    public Optional<Wishlist> findWishlistByBuyerAndProduct(Long buyerId, Long productId) {
        String sql = """
            SELECT w.wishlist_id, w.buyer_id, w.product_id, w.added_at
            FROM WISHLIST w
            WHERE w.buyer_id = ? AND w.product_id = ?
            """;
        
        List<Wishlist> result = jdbcTemplate.query(sql, wishlistRowMapper, buyerId, productId);
        
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }
    
    @Override
    public int countWishlistByBuyerId(Long buyerId) {
        String sql = """
            SELECT COUNT(*) 
            FROM WISHLIST 
            WHERE buyer_id = ?
            """;
        
        return jdbcTemplate.queryForObject(sql, Integer.class, buyerId);
    }
    
    @Override
    public int countWishlistByProductId(Long productId) {
        String sql = """
            SELECT COUNT(*) 
            FROM WISHLIST 
            WHERE product_id = ?
            """;
        
        return jdbcTemplate.queryForObject(sql, Integer.class, productId);
    }
    
    @Override
    public int deleteWishlistByBuyerAndProducts(Long buyerId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }
        
        String placeholders = productIds.stream()
                .map(id -> "?")
                .reduce((a, b) -> a + "," + b)
                .orElse("");
        
        String sql = String.format("""
            DELETE FROM WISHLIST 
            WHERE buyer_id = ? AND product_id IN (%s)
            """, placeholders);
        
        Object[] params = new Object[productIds.size() + 1];
        params[0] = buyerId;
        for (int i = 0; i < productIds.size(); i++) {
            params[i + 1] = productIds.get(i);
        }
        
        return jdbcTemplate.update(sql, params);
    }
} 