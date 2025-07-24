package com.kh.project.domain.seller.svc;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.seller.dao.SellerDAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SellerSVCImpl implements SellerSVC {

  private final SellerDAO sellerDAO;

  // 판매자 저장
  @Override
  public Long save(Seller seller) {
    return sellerDAO.save(seller);
  }

  // 판매자 이메일로 조회
  @Override
  public Optional<Seller> findByEmail(String email) {
    return sellerDAO.findByEmail(email);
  }

  // 판매자 이메일 중복 여부 확인
  @Override
  public boolean isExistEmail(String email) {
    return sellerDAO.isExistEmail(email);
  }

  // 판매자 아이디로 조회
  @Override
  public Optional<Seller> findBySellerId(Long sellerId) {
    return sellerDAO.findBySellerId(sellerId);
  }

  // 판매자 정보 수정
  @Override
  public int update(Long sid, Seller seller) {
    return sellerDAO.update(sid,seller);
  }

  // 판매자 탈퇴
  @Override
  public int delete(Long sid) {
    return sellerDAO.delete(sid);
  }

  // 사업자 번호로 가입된 계정 상태 확인
  @Override
  public Optional<Seller> bizRegNoBySellerId(String bizRegNo) {
    return sellerDAO.bizRegNoBySellerId(bizRegNo);
  }
}
