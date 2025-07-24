package com.kh.project.web;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.project.domain.entity.Product;
import com.kh.project.domain.entity.Seller;
import com.kh.project.domain.product.svc.ProductSVC;
import com.kh.project.domain.seller.svc.SellerSVC;
import com.kh.project.web.api.ApiResponse;
import com.kh.project.web.api.ApiResponseCode;
import com.kh.project.web.buyer.LoginForm;
import com.kh.project.web.product.DetailForm;
import com.kh.project.web.product.SaveForm;
import com.kh.project.web.product.UpdateForm;
import com.kh.project.web.seller.LoginSeller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/seller")
public class ProductController {
  private final ProductSVC productSVC;
  private final SellerSVC sellerSVC;

  // API 엔드포인트 추가
  @GetMapping("/api/product/{productId}")
  @org.springframework.web.bind.annotation.ResponseBody
  public ApiResponse<Product> getProductApi(@PathVariable Long productId) {
    try {
      Optional<Product> productOpt = productSVC.findById(productId);
      if (productOpt.isPresent()) {
        return ApiResponse.of(ApiResponseCode.SUCCESS, productOpt.get());
      } else {
        return ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, null);
      }
    } catch (Exception e) {
      log.error("상품 조회 오류: {}", e.getMessage());
      return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, null);
    }
  }

  // 상품 상태 업데이트 API
  @PostMapping("/api/product/status")
  @org.springframework.web.bind.annotation.ResponseBody
  public ApiResponse<String> updateProductStatus(@RequestBody Map<String, Object> request, HttpSession session) {
    log.info("상품 상태 업데이트 요청 시작: {}", request);
    
    try {
      // 요청 데이터 검증
      if (request.get("productId") == null || request.get("status") == null) {
        log.error("필수 파라미터 누락: productId={}, status={}", request.get("productId"), request.get("status"));
        return ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "필수 파라미터가 누락되었습니다.");
      }
      
      Long productId;
      try {
        productId = Long.valueOf(request.get("productId").toString());
      } catch (NumberFormatException e) {
        log.error("상품 ID 형식 오류: {}", request.get("productId"));
        return ApiResponse.of(ApiResponseCode.VALIDATION_ERROR, "잘못된 상품 ID 형식입니다.");
      }
      
      String newStatus = request.get("status").toString();
      
      log.info("상품 ID: {}, 새로운 상태: {}", productId, newStatus);
      
      // 세션에서 로그인한 판매자 정보 확인
      LoginSeller loginSeller = (LoginSeller) session.getAttribute("loginSeller");
      if (loginSeller == null) {
        log.warn("로그인하지 않은 사용자가 상품 상태 업데이트 시도");
        return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "로그인이 필요합니다.");
      }
      
      log.info("로그인한 판매자 ID: {}", loginSeller.getSellerId());
      
      // 상품 조회
      Optional<Product> productOpt = productSVC.findById(productId);
      if (productOpt.isEmpty()) {
        log.warn("상품을 찾을 수 없음: {}", productId);
        return ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, "상품을 찾을 수 없습니다.");
      }
      
      Product product = productOpt.get();
      log.info("기존 상품 정보: ID={}, 판매자ID={}, 현재상태={}", 
               product.getProductId(), product.getSellerId(), product.getStatus());
      
      // 판매자 본인의 상품인지 확인
      if (!product.getSellerId().equals(loginSeller.getSellerId())) {
        log.warn("다른 판매자의 상품 수정 시도: 요청판매자={}, 상품판매자={}", 
                 loginSeller.getSellerId(), product.getSellerId());
        return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "본인의 상품만 수정할 수 있습니다.");
      }
      
      // 상태 업데이트
      String oldStatus = product.getStatus();
      product.setStatus(newStatus);
      log.info("상품 상태 업데이트 시도: {} -> {}", oldStatus, newStatus);
      
      int result = productSVC.updateById(productId, product);
      log.info("상품 상태 업데이트 결과: {}", result);
      
      if (result > 0) {
        log.info("상품 상태 업데이트 성공: 상품ID={}, 새상태={}", productId, newStatus);
        return ApiResponse.of(ApiResponseCode.SUCCESS, "상품 상태가 성공적으로 업데이트되었습니다.");
      } else {
        log.warn("상품 상태 업데이트 실패: 상품ID={}, 새상태={}, 결과={}", productId, newStatus, result);
        return ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "상품 상태 업데이트에 실패했습니다.");
      }
      
    } catch (Exception e) {
      log.error("상품 상태 업데이트 중 예외 발생: {}", e.getMessage(), e);
      return ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "상품 상태 업데이트 중 오류가 발생했습니다.");
    }
  }

  // 판매글 목록 페이지
  @GetMapping("/list/{sid}")
  public String sellerPage(@PathVariable("sid") Long sid , Model model, HttpSession session){
    List<Product> ids = productSVC.findByIds(sid);
    model.addAttribute("products",ids);
    model.addAttribute("sid",sid);
    
    // 세션에서 로그인한 판매자 정보 가져오기
    LoginSeller loginSeller = (LoginSeller) session.getAttribute("loginSeller");
    
    if (loginSeller != null) {
      model.addAttribute("name", loginSeller.getName());
    } else {
      // 세션이 없을 때 데이터베이스에서 직접 조회
      try {
        Optional<Seller> sellerOpt = sellerSVC.findBySellerId(sid);
        if (sellerOpt.isPresent()) {
          String sellerName = sellerOpt.get().getName();
          model.addAttribute("name", sellerName);
        } else {
          model.addAttribute("name", "알 수 없음");
        }
      } catch (Exception e) {
        model.addAttribute("name", "오류");
      }
    }
    
    return "seller/product_list";
  }

  // 판매글 등록 페이지
  @GetMapping("/add/{sid}")
  public String savePage(@PathVariable("sid") Long sid , Model model, HttpSession session){
    model.addAttribute("product",new SaveForm());
    model.addAttribute("sid",sid);
    
    // 세션에서 로그인한 판매자 정보 가져오기
    LoginSeller loginSeller = (LoginSeller) session.getAttribute("loginSeller");
    
    if (loginSeller != null) {
      model.addAttribute("name", loginSeller.getName());
    } else {
      // 세션이 없을 때 데이터베이스에서 직접 조회
      try {
        Optional<Seller> sellerOpt = sellerSVC.findBySellerId(sid);
        if (sellerOpt.isPresent()) {
          String sellerName = sellerOpt.get().getName();
          model.addAttribute("name", sellerName);
        } else {
          model.addAttribute("name", "알 수 없음");
        }
      } catch (Exception e) {
        model.addAttribute("name", "오류");
      }
    }
    
    return "seller/product_form";
  }

  // 판매글 조회(개별) 상세 페이지
  @GetMapping("/product/{pid}")
  public String detailPage(@PathVariable("pid") Long pid, Model model, HttpSession session) {
    Optional<Product> optionalProduct = productSVC.findById(pid);
    if (optionalProduct.isPresent()) {
      Product product = optionalProduct.get();
      DetailForm detailForm = new DetailForm();
      BeanUtils.copyProperties(product, detailForm);

      Long sellerId = detailForm.getSellerId();
      Optional<Seller> optionalSeller = sellerSVC.findBySellerId(sellerId);
      if (optionalSeller.isEmpty()) {
        return "error/404"; // ❗ seller 정보 없으면 에러 페이지로 보냄
      }
      Seller seller = optionalSeller.get();
      model.addAttribute("seller",seller);
      model.addAttribute("product", detailForm);


      // 세션에서 로그인한 판매자 정보 확인 (null 가능성 대비)
      LoginSeller loginSeller = (LoginSeller) session.getAttribute("loginSeller");
      if (loginSeller != null) {
        model.addAttribute("sellerId", loginSeller.getSellerId());
        model.addAttribute("name", loginSeller.getName());
      } else {
        model.addAttribute("sellerId", null); // 로그인 안 된 상태
        // 세션이 없을 때 데이터베이스에서 직접 조회
        try {
          Optional<Seller> sellerOpt = sellerSVC.findBySellerId(sellerId);
          if (sellerOpt.isPresent()) {
            String sellerName = sellerOpt.get().getName();
            model.addAttribute("name", sellerName);
          } else {
            model.addAttribute("name", "알 수 없음");
          }
        } catch (Exception e) {
          model.addAttribute("name", "오류");
        }
      }
      LoginForm loginBuyer = (LoginForm) session.getAttribute("loginBuyer");
      if (loginBuyer != null) {
        model.addAttribute("buyerId", loginBuyer.getBuyerId());
      }

      return "common/product_detail";
    } else {
      return "error/404"; // 상품이 없을 때
    }
  }

  // 판매글 등록 처리
  @PostMapping("/add/{sid}")
  public String saveProduct(@PathVariable("sid") Long sid, @ModelAttribute SaveForm saveForm, RedirectAttributes redirectAttributes){
    Product product = new Product();
    BeanUtils.copyProperties(saveForm,product);

    productSVC.saveProduct(product,sid);

    redirectAttributes.addAttribute("sid",sid);
    return "redirect:/seller/main/" + sid;  // 임시 수정 필요 !!
  }

  // 판매글 수정 페이지
  @GetMapping("/product/{pid}/edit")
  public String updatePage(@PathVariable("pid") Long pid ,Model model, HttpSession session ){

    // 🔥 기존 상품 정보 불러오기!
    Optional<Product> optionalProduct = productSVC.findById(pid);

    if(optionalProduct.isPresent()) {
      Product product = optionalProduct.get();
      // 🔥 불러온 상품 정보를 UpdateForm 객체에 담아주기
      UpdateForm updateForm = new UpdateForm();
      BeanUtils.copyProperties(product, updateForm); // Product -> UpdateForm 복사

      model.addAttribute("product", updateForm); // 채워진 폼 객체를 모델에 담기
      model.addAttribute("pid", pid);
      model.addAttribute("sid", product.getSellerId());
      
      // 세션에서 로그인한 판매자 정보 가져오기
      LoginSeller loginSeller = (LoginSeller) session.getAttribute("loginSeller");
      
      if (loginSeller != null) {
        model.addAttribute("name", loginSeller.getName());
      } else {
        // 세션이 없을 때 데이터베이스에서 직접 조회
        try {
          Optional<Seller> sellerOpt = sellerSVC.findBySellerId(product.getSellerId());
          if (sellerOpt.isPresent()) {
            String sellerName = sellerOpt.get().getName();
            model.addAttribute("name", sellerName);
          } else {
            model.addAttribute("name", "알 수 없음");
          }
        } catch (Exception e) {
          model.addAttribute("name", "오류");
        }
      }
      
      return "seller/product_edit";  // 요 뷰 이름으로 페이지를 보여줄 거임!
    } else {
      // 🔥 상품 정보 못 찾으면 에러 페이지나 목록으로 리다이렉트
      return "error/404"; // 임시 에러 페이지 또는 "redirect:/seller/{sid}" 등으로 처리
    }
  }

  //판매글 수정 처리
  @PostMapping("/product/{pid}/edit")
  public String updateProduct(@PathVariable("pid") Long pid , UpdateForm updateForm , RedirectAttributes redirectAttributes){
    Product product = new Product();
    BeanUtils.copyProperties(updateForm,product);
    int i = productSVC.updateById(pid, product);

    redirectAttributes.addAttribute("pid",pid);
    return "redirect:/seller/product/{pid}";
  }

  // 판매글 단건 삭제 처리
  @PostMapping("/product/{pid}/delete") //
  public String deleteProduct(@PathVariable("pid") Long pid,
                              @RequestParam("sid") Long sid,
                              RedirectAttributes redirectAttributes) {
    productSVC.deleteById(pid);
    redirectAttributes.addAttribute("sid", sid); // 리다이렉트 URL에 sid 값을 추가
    return "redirect:/seller/{sid}"; // /seller/{sid} 주소로 이동
  }

 //삭제처리
  @PostMapping("/products/delete")
  public String deleteProducts(@RequestParam("productIds") List<Long> pids,
                               @RequestParam("sid") Long sid,
                               RedirectAttributes redirectAttributes) {
    log.info("Deleting products with pids: {}", pids);
    productSVC.deleteByIds(pids);
    redirectAttributes.addAttribute("sid", sid);
    return "redirect:/seller/list/" + sid; //
  }


}
