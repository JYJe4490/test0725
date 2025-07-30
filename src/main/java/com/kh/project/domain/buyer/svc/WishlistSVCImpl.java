package com.kh.project.domain.buyer.svc;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.project.domain.buyer.dao.WishlistDAO;
import com.kh.project.domain.cart.dao.CartDAO;
import com.kh.project.domain.entity.Buyer;
import com.kh.project.domain.entity.Cart;
import com.kh.project.domain.entity.Product;
import com.kh.project.domain.entity.Wishlist;
import com.kh.project.domain.product.dao.ProductDAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 찜 목록 서비스 구현체
 * 구매자의 찜 목록 관리를 위한 비즈니스 로직 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class WishlistSVCImpl implements WishlistSVC {
    
    private final WishlistDAO wishlistDAO;
    private final ProductDAO productDAO;
    private final CartDAO cartDAO;
    
    @Override
    public Long addWishlist(Long buyerId, Long productId) {
        // 상품 존재 여부 확인
        Optional<Product> productOpt = productDAO.findById(productId);
        if (productOpt.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 상품입니다.");
        }
        
        Product product = productOpt.get();
        
        // 이미 찜한 상품인지 확인
        Optional<Wishlist> existingWishlist = wishlistDAO.findWishlistByBuyerAndProduct(buyerId, productId);
        if (existingWishlist.isPresent()) {
            throw new IllegalArgumentException("이미 찜한 상품입니다.");
        }
        
        // 찜 목록 추가
        Wishlist wishlist = new Wishlist();
        wishlist.setBuyer(new Buyer());
        wishlist.getBuyer().setBuyerId(buyerId);
        wishlist.setProduct(product);
        
        return wishlistDAO.addWishlist(wishlist);
    }
    
    @Override
    public boolean deleteWishlist(Long buyerId, Long productId) {
        int deletedCount = wishlistDAO.deleteWishlist(buyerId, productId);
        return deletedCount > 0;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Wishlist> getWishlistByBuyerId(Long buyerId) {
        return wishlistDAO.findWishlistByBuyerId(buyerId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean isWishlisted(Long buyerId, Long productId) {
        Optional<Wishlist> wishlist = wishlistDAO.findWishlistByBuyerAndProduct(buyerId, productId);
        return wishlist.isPresent();
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getWishlistCountByBuyerId(Long buyerId) {
        return wishlistDAO.countWishlistByBuyerId(buyerId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public int getWishlistCountByProductId(Long productId) {
        return wishlistDAO.countWishlistByProductId(productId);
    }
    
    @Override
    public int deleteWishlistByBuyerAndProducts(Long buyerId, List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return 0;
        }
        
        return wishlistDAO.deleteWishlistByBuyerAndProducts(buyerId, productIds);
    }
    
    @Override
    public boolean moveWishlistToCart(Long buyerId, Long productId) {
        // 찜 목록에서 상품 정보 조회
        Optional<Wishlist> wishlistOpt = wishlistDAO.findWishlistByBuyerAndProduct(buyerId, productId);
        if (wishlistOpt.isEmpty()) {
            throw new IllegalArgumentException("찜 목록에 존재하지 않는 상품입니다.");
        }
        
        Wishlist wishlist = wishlistOpt.get();
        Product product = wishlist.getProduct();
        
        // 장바구니에 이미 있는지 확인
        Optional<Cart> existingCart = cartDAO.findByBuyerIdAndProductId(buyerId, productId);
        if (existingCart.isPresent()) {
            // 이미 장바구니에 있으면 수량만 증가
            Cart cart = existingCart.get();
            cart.setQuantity(cart.getQuantity() + 1L);
            cartDAO.quantityChange(cart);
        } else {
            // 장바구니에 없으면 새로 추가
            Cart cart = new Cart();
            cart.setBuyerId(buyerId);
            cart.setProductId(productId);
            cart.setQuantity(1L);
            cartDAO.addOrderInCart(cart);
        }
        
        // 찜 목록에서 삭제
        wishlistDAO.deleteWishlist(buyerId, productId);
        
        return true;
    }
} 