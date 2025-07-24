package com.kh.project.domain.product.svc;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kh.project.domain.entity.Product;
import com.kh.project.domain.product.dao.ProductDAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductSVCImpl implements ProductSVC{

  private final ProductDAO productDAO;

  // 상품 저장
  @Override
  public Long saveProduct(Product product, Long sid) {
    return productDAO.saveProduct(product, sid);
  }

  // 상품 조회
  @Override
  public Optional<Product> findById(Long pid) {
    return productDAO.findById(pid);
  }

  // 상품 목록 조회
  @Override
  public List<Product> findByIds(Long sid) {
    return productDAO.findByIds(sid);
  }

  // 상품 수정
  @Override
  public int updateById(Long pid, Product product) {
    log.info("ProductSVCImpl.updateById 호출: pid={}, product={}", pid, product);
    int result = productDAO.updateById(pid, product);
    log.info("ProductSVCImpl.updateById 결과: {}", result);
    return result;
  }

  // 상품 삭제
  @Override
  public int deleteById(Long pid) {
    return productDAO.deleteById(pid);
  }

  // 상품 목록 삭제
  @Override
  public int deleteByIds(List<Long> list) {
    return productDAO.deleteByIds(list);
  }

  // 상품 목록 조회
  @Override
  public List<Product> allProduct() {
    return productDAO.allProduct();
  }
}
