
  

    // ===== 주문 내역 관리자 클래스 =====
    class OrderHistoryManager {
      constructor() {
        this.elements = this.initializeElements();
        this.orderCache = new Map(); // 주문 상품 정보 캐시
        this.productCache = new Map(); // 상품 정보 캐시
        
        this.initialize();
      }

      // ===== 요소 초기화 =====
      initializeElements() {
        return {
          orderList: document.getElementById('orderList')
        };
      }

      // ===== 초기화 =====
      async initialize() {
        console.log('=== 주문 내역 페이지 초기화 ===');
        
        try {
          // 로딩 상태 표시
          this.showLoadingState();
          
          // 주문 내역 로드
          await this.loadOrderHistory();
          
          console.log('주문 내역 페이지 초기화 완료');
        } catch (error) {
          console.error('주문 내역 페이지 초기화 중 오류:', error);
          this.showErrorMessage('주문 내역을 불러오는 중 오류가 발생했습니다.');
        }
      }

      // ===== 주문 내역 로드 =====
      async loadOrderHistory() {
        try {
          const response = await fetch('/api/order/list');
          const data = await response.json();
          
          console.log('주문 내역 응답:', data);
          
          if (data.header && data.header.rtcd === 'S00' && data.body && data.body.list) {
            await this.renderOrderHistory(data.body.list);
          } else {
            this.showEmptyMessage();
          }
        } catch (error) {
          console.error('주문 내역 로드 오류:', error);
          throw new Error('주문 내역을 불러올 수 없습니다.');
        }
      }

      // ===== 주문 내역 렌더링 =====
      async renderOrderHistory(orders) {
        if (!orders || orders.length === 0) {
          this.showEmptyMessage();
          return;
        }

        this.elements.orderList.innerHTML = '';

        // 주문들을 병렬로 처리
        const orderPromises = orders.map(order => this.renderOrderItem(order));
        await Promise.all(orderPromises);
      }

      // ===== 개별 주문 아이템 렌더링 =====
      async renderOrderItem(order) {
        const orderElement = document.createElement('article');
        orderElement.className = 'order-item';
        orderElement.setAttribute('data-order-id', order.orderId);
        
        // 주문 상태 설정
        const { statusClass, statusText } = this.getOrderStatus(order.orderStatus);
        
        try {
          // 주문 상품 정보 로드
          const items = await this.loadOrderItems(order.orderId);
          
          if (items && items.length > 0) {
            // 첫 번째 상품 정보로 메인 표시
            const firstItem = items[0];
            const productInfo = await this.getProductInfo(firstItem.productId);
            
            this.renderOrderMain(orderElement, order, statusClass, statusText, productInfo, items.length);
            this.renderProductList(orderElement, items);
            this.addClickEvent(orderElement);
            
          } else {
            this.renderFallbackOrder(orderElement, order, statusClass, statusText, 0);
          }
          
          this.elements.orderList.appendChild(orderElement);
          
        } catch (error) {
          console.error(`주문 ${order.orderId} 렌더링 오류:`, error);
          this.renderFallbackOrder(orderElement, order, statusClass, statusText, 0);
          this.elements.orderList.appendChild(orderElement);
        }
      }

      // ===== 주문 상태 정보 가져오기 =====
      getOrderStatus(orderStatus) {
        let statusClass = 'status-cancelled';
        let statusText = '주문 취소';
        
        switch (orderStatus) {
          case '결제완료':
            statusClass = 'status-completed';
            statusText = '결제 완료';
            break;
          case '배송중':
            statusClass = 'status-shipping';
            statusText = '배송중';
            break;
          case '배송완료':
            statusClass = 'status-completed';
            statusText = '배송 완료';
            break;
          case '주문대기':
            statusClass = 'status-pending';
            statusText = '주문 대기';
            break;
          default:
            statusClass = 'status-cancelled';
            statusText = '주문 취소';
        }
        
        return { statusClass, statusText };
      }

      // ===== 주문 상품 정보 로드 =====
      async loadOrderItems(orderId) {
        // 캐시 확인
        if (this.orderCache.has(orderId)) {
          return this.orderCache.get(orderId);
        }
        
        try {
          const response = await fetch(`/api/order/detail?orderId=${orderId}`);
          const data = await response.json();
          
          if (data.header && data.header.rtcd === 'S00' && data.body && data.body.items) {
            this.orderCache.set(orderId, data.body.items);
            return data.body.items;
          }
          return [];
        } catch (error) {
          console.error(`주문 ${orderId} 상품 정보 로드 오류:`, error);
          return [];
        }
      }

      // ===== 상품 정보 가져오기 =====
      async getProductInfo(productId) {
        // 캐시 확인
        if (this.productCache.has(productId)) {
          return this.productCache.get(productId);
        }
        
        try {
          const response = await fetch(`/seller/api/product/${productId}`);
          const data = await response.json();
          
          let productInfo = {
            thumbnail: '/images/product-placeholder.jpg',
            title: `상품 ID: ${productId}`
          };
          
          if (data.header && data.header.rtcd === 'S00' && data.body) {
            productInfo = {
              thumbnail: data.body.thumbnail || '/images/product-placeholder.jpg',
              title: data.body.title || `상품 ID: ${productId}`
            };
          }
          
          this.productCache.set(productId, productInfo);
          return productInfo;
        } catch (error) {
          console.error(`상품 ${productId} 정보 로드 오류:`, error);
          return {
            thumbnail: '/images/product-placeholder.jpg',
            title: `상품 ID: ${productId}`
          };
        }
      }

      // ===== 주문 메인 정보 렌더링 =====
      renderOrderMain(orderElement, order, statusClass, statusText, productInfo, itemCount) {
        const mainSection = document.createElement('div');
        mainSection.className = 'order-main';
        
        mainSection.innerHTML = `
          <img class="product-image" 
               src="${productInfo.thumbnail}" 
               alt="${productInfo.title}"
               loading="lazy"
               onerror="this.src='/images/product-placeholder.jpg'">
          <div class="order-details">
            <div class="product-name">${productInfo.title}</div>
            <div class="seller-name">수령자: ${order.name}</div>
            <div class="price-quantity">주문일: ${this.formatDate(order.orderDate)} | ${itemCount}개 상품</div>
            <div class="order-info">주소: ${order.deliveryAddress}</div>
          </div>
          <div class="order-status ${statusClass}" aria-label="주문 상태: ${statusText}">${statusText}</div>
          <div class="expand-icon" aria-label="상품 목록 펼치기">▼</div>
        `;
        
        orderElement.appendChild(mainSection);
      }

      // ===== 상품 목록 렌더링 =====
      async renderProductList(orderElement, items) {
        const productsContainer = document.createElement('div');
        productsContainer.className = 'order-products';
        productsContainer.setAttribute('data-order-id', orderElement.getAttribute('data-order-id'));
        
        // 상품들을 병렬로 처리
        const productPromises = items.map(item => this.renderProductItem(item));
        const productElements = await Promise.all(productPromises);
        
        productElements.forEach(element => {
          productsContainer.appendChild(element);
        });
        
        orderElement.appendChild(productsContainer);
      }

      // ===== 개별 상품 아이템 렌더링 =====
      async renderProductItem(item) {
        const productElement = document.createElement('div');
        productElement.className = 'product-item';
        productElement.setAttribute('data-product-id', item.productId);
        
        try {
          const productInfo = await this.getProductInfo(item.productId);
          
          productElement.innerHTML = `
            <img class="product-item-image" 
                 src="${productInfo.thumbnail}" 
                 alt="${productInfo.title}"
                 loading="lazy"
                 onerror="this.src='/images/product-placeholder.jpg'">
            <div class="product-item-details">
              <div class="product-item-name">${productInfo.title}</div>
              <div class="product-item-info">수량: ${item.quantity}개 | 가격: ${this.formatNumber(item.unitPrice)}원</div>
            </div>
          `;
          
          // 상품 클릭 이벤트
          productElement.addEventListener('click', (e) => {
            e.stopPropagation();
            window.location.href = `/seller/product/${item.productId}`;
          });
          
        } catch (error) {
          console.error(`상품 ${item.productId} 렌더링 오류:`, error);
          productElement.innerHTML = `
            <img class="product-item-image" src="/images/product-placeholder.jpg" alt="상품 이미지">
            <div class="product-item-details">
              <div class="product-item-name">상품 ID: ${item.productId}</div>
              <div class="product-item-info">수량: ${item.quantity}개 | 가격: ${this.formatNumber(item.unitPrice)}원</div>
            </div>
          `;
        }
        
        return productElement;
      }

      // ===== 폴백 주문 렌더링 =====
      renderFallbackOrder(orderElement, order, statusClass, statusText, itemCount) {
        orderElement.innerHTML = `
          <div class="order-main">
            <img class="product-image" src="/images/product-placeholder.jpg" alt="상품 이미지">
            <div class="order-details">
              <div class="product-name">주문번호: ${order.orderNumber || order.orderId}</div>
              <div class="seller-name">수령자: ${order.name}</div>
              <div class="price-quantity">주문일: ${this.formatDate(order.orderDate)}${itemCount > 0 ? ` | ${itemCount}개 상품` : ''}</div>
              <div class="order-info">주소: ${order.deliveryAddress}</div>
            </div>
            <div class="order-status ${statusClass}">${statusText}</div>
            <div class="expand-icon">▼</div>
          </div>
          <div class="order-products" data-order-id="${order.orderId}">
            <!-- 상품 목록이 여기에 동적으로 추가됩니다 -->
          </div>
        `;
      }

      // ===== 클릭 이벤트 추가 =====
      addClickEvent(orderElement) {
        orderElement.addEventListener('click', function() {
          const productsContainer = this.querySelector('.order-products');
          const expandIcon = this.querySelector('.expand-icon');
          
          if (productsContainer && expandIcon) {
            productsContainer.classList.toggle('expanded');
            expandIcon.classList.toggle('expanded');
            
            // 접근성 개선
            const isExpanded = productsContainer.classList.contains('expanded');
            expandIcon.setAttribute('aria-label', isExpanded ? '상품 목록 접기' : '상품 목록 펼치기');
          }
        });
      }

      // ===== 로딩 상태 표시 =====
      showLoadingState() {
        this.elements.orderList.innerHTML = `
          <div class="loading-message">
            <p>주문 내역을 불러오는 중...</p>
          </div>
        `;
      }

      // ===== 빈 상태 메시지 =====
      showEmptyMessage() {
        this.elements.orderList.innerHTML = `
          <div class="empty-message">
            <p>주문 내역이 없습니다.</p>
            <p>첫 번째 주문을 시작해보세요!</p>
          </div>
        `;
      }

      // ===== 에러 메시지 =====
      showErrorMessage(message) {
        this.elements.orderList.innerHTML = `
          <div class="error-message">
            <p>❌ ${message}</p>
            <p>잠시 후 다시 시도해주세요.</p>
          </div>
        `;
      }

      // ===== 날짜 포맷 =====
      formatDate(dateString) {
        if (!dateString) return '날짜 정보 없음';
        
        try {
          const date = new Date(dateString);
          return date.toLocaleDateString('ko-KR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric'
          });
        } catch (e) {
          return dateString;
        }
      }

      // ===== 숫자 포맷 =====
      formatNumber(num) {
        if (typeof num !== 'number') {
          num = parseInt(num) || 0;
        }
        return num.toLocaleString();
      }

      // ===== 캐시 정리 =====
      clearCache() {
        this.orderCache.clear();
        this.productCache.clear();
      }

      // ===== 페이지 통계 수집 =====
      trackPageView() {
        console.log('주문 내역 페이지 조회');
        // Google Analytics나 다른 분석 도구 연동 가능
      }
    }

    // ===== 전역 함수들 =====
    function goToMyPage() {
      window.location.href = '/buyer/info';
    }

    function goToOrderHistory() {
      window.location.href = '/buyer/orders';
    }

    function goToCart() {
      window.location.href = '/buyer/cart';
    }

    // ===== 초기화 =====
    document.addEventListener('DOMContentLoaded', () => {
      const orderHistoryManager = new OrderHistoryManager();
      
      // 페이지 통계 수집
      setTimeout(() => {
        orderHistoryManager.trackPageView();
      }, 1000);
    });
  