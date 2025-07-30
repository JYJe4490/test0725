package com.kh.project.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 찜 목록 엔티티
 * 구매자가 상품을 찜 목록에 추가/제거하는 기능을 위한 테이블
 */
@Entity
@Table(name = "WISHLIST")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wishlist {
    
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "wishlist_seq")
    @SequenceGenerator(name = "wishlist_seq", sequenceName = "wishlist_wishlist_id_seq", allocationSize = 1)
    @Column(name = "wishlist_id")
    private Long wishlistId;  // 찜 고유 ID
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private Buyer buyer;      // 구매자
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;  // 상품
    
    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt;  // 찜 추가 시간
    
    // 생성자 (찜 추가 시 사용)
    public Wishlist(Buyer buyer, Product product) {
        this.buyer = buyer;
        this.product = product;
        this.addedAt = LocalDateTime.now();
    }
} 