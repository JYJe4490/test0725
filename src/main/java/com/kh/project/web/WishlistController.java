//package com.kh.project.web.buyer;
//
//import java.util.List;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.ResponseBody;
//
//import com.kh.project.domain.wishlist.svc.WishlistSVC;
//import com.kh.project.domain.entity.Wishlist;
//import com.kh.project.web.api.ApiResponse;
//import com.kh.project.web.api.ApiResponseCode;
//
//import jakarta.servlet.http.HttpSession;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
///**
// * 찜 목록 컨트롤러
// * 구매자의 찜 목록 관리를 위한 웹 컨트롤러
// */
//@Slf4j
//@Controller
//@RequestMapping("/buyer/wishlist")
//@RequiredArgsConstructor
//public class WishlistController {
//
//    private final WishlistSVC wishlistSVC;
//
//    /**
//     * 찜 목록 페이지
//     */
//    @GetMapping("/{buyerId}")
//    public String wishlistPage(@PathVariable Long buyerId, Model model, HttpSession session) {
//        // 로그인 확인
//        Object loginBuyer = session.getAttribute("loginBuyer");
//        if (loginBuyer == null) {
//            return "redirect:/common/select_login";
//        }
//
//        // 찜 목록 조회
//        List<Wishlist> wishlist = wishlistSVC.getWishlistByBuyerId(buyerId);
//        int wishlistCount = wishlistSVC.getWishlistCountByBuyerId(buyerId);
//
//        model.addAttribute("wishlist", wishlist);
//        model.addAttribute("wishlistCount", wishlistCount);
//        model.addAttribute("buyerId", buyerId);
//
//        return "buyer/wishlist";
//    }
//
//    /**
//     * 찜 목록 추가 (AJAX)
//     */
//    @PostMapping("/add")
//    @ResponseBody
//    public ResponseEntity<ApiResponse> addWishlist(
//            @RequestParam Long buyerId,
//            @RequestParam Long productId,
//            HttpSession session) {
//
//        try {
//            // 로그인 확인
//            Object loginBuyer = session.getAttribute("loginBuyer");
//            if (loginBuyer == null) {
//                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "로그인이 필요합니다."));
//            }
//
//            // 찜 목록 추가
//            Long wishlistId = wishlistSVC.addWishlist(buyerId, productId);
//
//            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, wishlistId));
//
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, e.getMessage()));
//        } catch (Exception e) {
//            log.error("찜 목록 추가 실패", e);
//            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "찜 목록 추가에 실패했습니다."));
//        }
//    }
//
//    /**
//     * 찜 목록 삭제 (AJAX)
//     */
//    @DeleteMapping("/delete")
//    @ResponseBody
//    public ResponseEntity<ApiResponse> deleteWishlist(
//            @RequestParam Long buyerId,
//            @RequestParam Long productId,
//            HttpSession session) {
//
//        try {
//            // 로그인 확인
//            Object loginBuyer = session.getAttribute("loginBuyer");
//            if (loginBuyer == null) {
//                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "로그인이 필요합니다."));
//            }
//
//            // 찜 목록 삭제
//            boolean deleted = wishlistSVC.deleteWishlist(buyerId, productId);
//
//            if (deleted) {
//                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, "찜 목록에서 삭제되었습니다."));
//            } else {
//                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.ENTITY_NOT_FOUND, "찜 목록에서 삭제할 상품을 찾을 수 없습니다."));
//            }
//
//        } catch (Exception e) {
//            log.error("찜 목록 삭제 실패", e);
//            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "찜 목록 삭제에 실패했습니다."));
//        }
//    }
//
//    /**
//     * 찜 여부 확인 (AJAX)
//     */
//    @GetMapping("/check")
//    @ResponseBody
//    public ResponseEntity<ApiResponse> checkWishlist(
//            @RequestParam Long buyerId,
//            @RequestParam Long productId,
//            HttpSession session) {
//
//        try {
//            // 로그인 확인
//            Object loginBuyer = session.getAttribute("loginBuyer");
//            if (loginBuyer == null) {
//                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "로그인이 필요합니다."));
//            }
//
//            // 찜 여부 확인
//            boolean isWishlisted = wishlistSVC.isWishlisted(buyerId, productId);
//
//            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, isWishlisted));
//
//        } catch (Exception e) {
//            log.error("찜 여부 확인 실패", e);
//            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "찜 여부 확인에 실패했습니다."));
//        }
//    }
//
//    /**
//     * 찜 목록에서 장바구니로 이동 (AJAX)
//     */
//    @PostMapping("/move-to-cart")
//    @ResponseBody
//    public ResponseEntity<ApiResponse> moveWishlistToCart(
//            @RequestParam Long buyerId,
//            @RequestParam Long productId,
//            HttpSession session) {
//
//        try {
//            // 로그인 확인
//            Object loginBuyer = session.getAttribute("loginBuyer");
//            if (loginBuyer == null) {
//                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "로그인이 필요합니다."));
//            }
//
//            // 찜 목록에서 장바구니로 이동
//            boolean moved = wishlistSVC.moveWishlistToCart(buyerId, productId);
//
//            if (moved) {
//                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, "장바구니로 이동되었습니다."));
//            } else {
//                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "장바구니 이동에 실패했습니다."));
//            }
//
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, e.getMessage()));
//        } catch (Exception e) {
//            log.error("장바구니 이동 실패", e);
//            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "장바구니 이동에 실패했습니다."));
//        }
//    }
//
//    /**
//     * 찜 목록 개수 조회 (AJAX)
//     */
//    @GetMapping("/count")
//    @ResponseBody
//    public ResponseEntity<ApiResponse> getWishlistCount(
//            @RequestParam Long buyerId,
//            HttpSession session) {
//
//        try {
//            // 로그인 확인
//            Object loginBuyer = session.getAttribute("loginBuyer");
//            if (loginBuyer == null) {
//                return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.BUSINESS_ERROR, "로그인이 필요합니다."));
//            }
//
//            // 찜 목록 개수 조회
//            int count = wishlistSVC.getWishlistCountByBuyerId(buyerId);
//
//            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.SUCCESS, count));
//
//        } catch (Exception e) {
//            log.error("찜 목록 개수 조회 실패", e);
//            return ResponseEntity.ok(ApiResponse.of(ApiResponseCode.INTERNAL_SERVER_ERROR, "찜 목록 개수 조회에 실패했습니다."));
//        }
//    }
//}