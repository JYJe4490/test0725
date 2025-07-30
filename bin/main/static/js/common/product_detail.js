// 상품 정보 변수
let productData = {
  status: '',
  productId: 0,
  title: '',
  price: 0,
  thumbnail: '',
  deliveryFee: 0
};

// DOM 요소들
let quantityInput, minusBtn, plusBtn, totalPriceEl;

// 초기화 함수
function initializeElements() {
  quantityInput = document.getElementById('quantity');
  minusBtn = document.querySelector('.minus-btn');
  plusBtn = document.getElementById('plusButton');
  totalPriceEl = document.getElementById('totalPrice');

  if (!quantityInput || !minusBtn || !plusBtn || !totalPriceEl) {
    console.error('필수 DOM 요소를 찾을 수 없습니다.');
    return false;
  }
  return true;
}

// 가격 계산 함수
function calculateTotal() {
  const quantity = parseInt(quantityInput.value) || 1;
  const pricePerUnit = Number(quantityInput.dataset.price) || 0;
  const total = quantity * pricePerUnit;
  totalPriceEl.textContent = total.toLocaleString();
  return { quantity, total };
}

// 이벤트 리스너 설정
function setupEventListeners() {
  minusBtn.addEventListener('click', () => {
    let quantity = parseInt(quantityInput.value) || 1;
    if (quantity > 1) {
      quantityInput.value = --quantity;
      calculateTotal();
    }
  });

  plusBtn.addEventListener('click', () => {
    let quantity = parseInt(quantityInput.value) || 1;
    quantityInput.value = ++quantity;
    calculateTotal();
  });

  quantityInput.addEventListener('input', () => {
    let quantity = parseInt(quantityInput.value);
    if (isNaN(quantity) || quantity < 1) {
      quantityInput.value = 1;
    }
    calculateTotal();
  });
}

// 장바구니 추가 함수
async function addToCart() {
  if (productData.status === '재고소진') {
    alert('매진된 상품입니다.');
    return;
  }
  
  if (productData.status === '비활성화') {
    alert('비활성화된 상품입니다.');
    return;
  }

  const { quantity } = calculateTotal();
  const buyerId = document.getElementById('buyerId')?.value;

  if (!buyerId) {
    alert('로그인 정보가 없습니다.');
    return;
  }

  const data = {
    buyerId: parseInt(buyerId),
    productId: parseInt(productData.productId),
    quantity: quantity
  };

  try {
    const response = await fetch('/api/cart', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify(data)
    });

    const result = await response.json();
    
    if (result.header?.rtcd === 'S00') {
      showModal('cartModal');
    } else if (result.header?.rtcd === 'E02') {
      alert(result.body || '이미 장바구니에 있는 상품입니다.');
    } else {
      const msg = result.header?.rtmsg || '장바구니 추가 실패';
      alert('장바구니 추가 실패: ' + msg);
    }
  } catch (error) {
    console.error('Error:', error);
    alert('장바구니 추가 중 오류 발생');
  }
}

// 바로구매 함수
function buyNow() {
  if (productData.status === '재고소진') {
    alert('매진된 상품입니다.');
    return;
  }
  
  if (productData.status === '비활성화') {
    alert('비활성화된 상품입니다.');
    return;
  }

  const { quantity, total } = calculateTotal();
  const buyerId = document.getElementById('buyerId')?.value;

  if (!buyerId) {
    alert('로그인 정보가 없습니다.');
    return;
  }

  const orderForm = {
    totalPrice: total,
    totalDeliveryFee: productData.deliveryFee,
    items: [{
      productId: productData.productId,
      productTitle: productData.title,
      productThumbnail: productData.thumbnail,
      quantity: quantity,
      totalPrice: total
    }]
  };

  sessionStorage.setItem('orderForm', JSON.stringify(orderForm));
  sessionStorage.setItem('buyerId', buyerId);
  window.location.href = '/buyer/payment';
}

// 로그인 리다이렉트 함수
function redirectToLogin() {
  if (productData.status === '재고소진') {
    alert('매진된 상품입니다.');
    return;
  }
  
  if (productData.status === '비활성화') {
    alert('비활성화된 상품입니다.');
    return;
  }
  showModal('loginModal');
}

// 모달 표시 함수
function showModal(modalId) {
  document.getElementById(modalId).classList.add('active');
}

// 모달 닫기 함수
function closeModal() {
  document.getElementById('cartModal').classList.remove('active');
}

function closeLoginModal() {
  document.getElementById('loginModal').classList.remove('active');
}

// 장바구니로 이동
function goToCart() {
  const buyerId = document.getElementById('buyerId')?.value;
  if (buyerId) {
    window.location.href = `/buyer/cart/${buyerId}`;
  } else {
    alert('로그인 정보가 없습니다.');
  }
}

// 로그인 페이지로 이동
function goToLogin() {
  window.location.href = '/buyer/login';
}

// Thymeleaf에서 전달받은 상품 데이터 설정
function setProductData(data) {
  productData = data;
}

// 페이지 로드 시 초기화
document.addEventListener('DOMContentLoaded', function() {
  if (initializeElements()) {
    setupEventListeners();
    calculateTotal();
  }
});
