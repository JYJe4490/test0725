
/**
 * 로그아웃 처리 함수
 * @param {string} type - 로그아웃 타입 ('buyer' 또는 'seller')
 */
function logout(type) {
  if (!confirm('로그아웃 하시겠습니까?')) {
    return;
  }

  const form = document.createElement('form');
  form.method = 'POST';
  form.action = `/${type}/logout`;

  // CSRF 토큰 처리
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
  if (csrfToken) {
    const csrfInput = document.createElement('input');
    csrfInput.type = 'hidden';
    csrfInput.name = '_csrf';
    csrfInput.value = csrfToken;
    form.appendChild(csrfInput);
  }

  document.body.appendChild(form);
  form.submit();
}

/**
 * 페이지 로드 완료 후 실행되는 초기화 함수
 */
document.addEventListener('DOMContentLoaded', function() {
  // 메시지 자동 숨김 (5초 후)
  const messageContainer = document.querySelector('.message-container');
  if (messageContainer) {
    setTimeout(() => {
      messageContainer.style.animation = 'slideUp 0.3s ease-out';
      setTimeout(() => {
        messageContainer.remove();
      }, 300);
    }, 5000);
  }

  // 이미지 로드 실패 시 기본 이미지로 대체
  const images = document.querySelectorAll('.product-image');
  images.forEach(img => {
    img.addEventListener('error', function() {
      this.src = '/images/default-product.jpg';
      this.alt = '상품 이미지';
    });
  });

  // 상품 카드 클릭 시 로딩 표시
  const productCards = document.querySelectorAll('.product-card');
  productCards.forEach(card => {
    card.addEventListener('click', function() {
      // 로딩 표시 (선택사항)
      this.style.opacity = '0.7';
    });
  });
});

/**
 * 페이지 가시성 변경 시 처리
 */
document.addEventListener('visibilitychange', function() {
  if (document.visibilityState === 'visible') {
    // 페이지가 다시 보일 때 필요한 처리
    console.log('페이지가 다시 활성화되었습니다.');
  }
});
