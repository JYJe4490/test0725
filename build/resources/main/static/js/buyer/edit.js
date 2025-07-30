
    // ===== 구매자 정보 편집 관리자 클래스 =====
    class BuyerEditManager {
      constructor() {
        this.elements = this.initializeElements();
        this.addressManager = new AddressManager();
        this.formValidator = new FormValidator();
        
        this.initialize();
      }

      // ===== 요소 초기화 =====
      initializeElements() {
        return {
          form: document.querySelector('.edit-form'),
          telInput: document.getElementById('tel'),
          nicknameInput: document.getElementById('nickname'),
          genderSelect: document.getElementById('gender'),
          birthInput: document.getElementById('birth'),
          detailAddressInput: document.getElementById('detailAddress'),
          addressInput: document.getElementById('address'),
          postNumberInput: document.getElementById('postNumber'),
          fullAddressInput: document.getElementById('fullAddress'),
          saveBtn: document.querySelector('.save-btn'),
          cancelBtn: document.querySelector('.cancel-btn')
        };
      }

      // ===== 초기화 =====
      initialize() {
        console.log('=== 구매자 정보 편집 페이지 초기화 ===');
        
        this.bindEvents();
        this.setupFormValidation();
        this.setupAccessibility();
        
        console.log('구매자 정보 편집 페이지 초기화 완료');
      }

      // ===== 이벤트 바인딩 =====
      bindEvents() {
        // 폼 제출 이벤트
        this.elements.form.addEventListener('submit', (e) => {
          this.handleFormSubmit(e);
        });

        // 전화번호 자동 하이픈
        this.elements.telInput.addEventListener('input', (e) => {
          this.formatPhoneNumber(e.target);
        });

        // 키보드 네비게이션
        this.setupKeyboardNavigation();

        // 실시간 유효성 검사
        this.setupRealTimeValidation();
      }

      // ===== 폼 제출 처리 =====
      async handleFormSubmit(e) {
        e.preventDefault();
        
        if (!this.formValidator.validateForm()) {
          return;
        }

        try {
          this.setLoadingState(true);
          
          // 폼 데이터 수집
          const formData = this.collectFormData();
          
          // 서버로 제출
          const response = await this.submitForm(formData);
          
          if (response.success) {
            this.showSuccessMessage('정보가 성공적으로 수정되었습니다.');
            setTimeout(() => {
              window.location.href = '/buyer/info';
            }, 1500);
          } else {
            this.showErrorMessage(response.message || '정보 수정에 실패했습니다.');
          }
        } catch (error) {
          console.error('폼 제출 중 오류:', error);
          this.showErrorMessage('서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
        } finally {
          this.setLoadingState(false);
        }
      }

      // ===== 폼 데이터 수집 =====
      collectFormData() {
        const formData = new FormData(this.elements.form);
        
        // 주소 정보 결합
        const baseAddr = this.elements.addressInput.value.trim();
        const detail = this.elements.detailAddressInput.value.trim();
        const fullAddress = baseAddr + ' ' + detail;
        
        formData.set('address', fullAddress);
        
        return formData;
      }

      // ===== 폼 제출 =====
      async submitForm(formData) {
        const response = await fetch('/buyer/edit', {
          method: 'POST',
          body: formData
        });

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        return await response.json();
      }

      // ===== 전화번호 포맷팅 =====
      formatPhoneNumber(input) {
        let value = input.value.replace(/[^\d]/g, '');
        
        if (value.length <= 3) {
          input.value = value;
        } else if (value.length <= 7) {
          input.value = value.slice(0, 3) + '-' + value.slice(3);
        } else {
          input.value = value.slice(0, 3) + '-' + value.slice(3, 7) + '-' + value.slice(7, 11);
        }
      }

      // ===== 키보드 네비게이션 =====
      setupKeyboardNavigation() {
        const inputs = this.elements.form.querySelectorAll('input, select');
        
        inputs.forEach((input, index) => {
          input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
              e.preventDefault();
              
              // 다음 입력 필드로 이동
              const nextInput = inputs[index + 1];
              if (nextInput) {
                nextInput.focus();
              } else {
                // 마지막 필드에서 Enter 시 저장 버튼 클릭
                this.elements.saveBtn.click();
              }
            }
          });
        });
      }

      // ===== 실시간 유효성 검사 =====
      setupRealTimeValidation() {
        // 닉네임 검사
        this.elements.nicknameInput.addEventListener('blur', () => {
          this.formValidator.validateNickname(this.elements.nicknameInput.value);
        });

        // 전화번호 검사
        this.elements.telInput.addEventListener('blur', () => {
          this.formValidator.validatePhone(this.elements.telInput.value);
        });

        // 생년월일 검사
        this.elements.birthInput.addEventListener('blur', () => {
          this.formValidator.validateBirth(this.elements.birthInput.value);
        });
      }

      // ===== 폼 유효성 검사 설정 =====
      setupFormValidation() {
        this.formValidator.setForm(this.elements.form);
      }

      // ===== 접근성 설정 =====
      setupAccessibility() {
        // 필수 필드 표시
        const requiredFields = this.elements.form.querySelectorAll('[required]');
        requiredFields.forEach(field => {
          const label = this.elements.form.querySelector(`label[for="${field.id}"]`);
          if (label) {
            label.classList.add('required');
          }
        });

        // 에러 메시지 접근성
        const errorMessages = this.elements.form.querySelectorAll('.error-message');
        errorMessages.forEach(message => {
          message.setAttribute('role', 'alert');
          message.setAttribute('aria-live', 'polite');
        });
      }

      // ===== 로딩 상태 설정 =====
      setLoadingState(isLoading) {
        const submitBtn = this.elements.saveBtn;
        
        if (isLoading) {
          submitBtn.disabled = true;
          submitBtn.textContent = '저장 중...';
          submitBtn.classList.add('loading');
        } else {
          submitBtn.disabled = false;
          submitBtn.textContent = '저장';
          submitBtn.classList.remove('loading');
        }
      }

      // ===== 성공 메시지 표시 =====
      showSuccessMessage(message) {
        this.showMessage(message, 'success');
      }

      // ===== 에러 메시지 표시 =====
      showErrorMessage(message) {
        this.showMessage(message, 'error');
      }

      // ===== 메시지 표시 =====
      showMessage(message, type) {
        const messageDiv = document.createElement('div');
        messageDiv.className = `${type}-message`;
        messageDiv.textContent = message;
        messageDiv.setAttribute('role', 'alert');
        messageDiv.setAttribute('aria-live', 'polite');
        
        const form = this.elements.form;
        form.insertBefore(messageDiv, form.firstChild);
        
        // 5초 후 자동 제거
        setTimeout(() => {
          messageDiv.remove();
        }, 5000);
      }

      // ===== 페이지 통계 수집 =====
      trackPageView() {
        console.log('구매자 정보 편집 페이지 조회');
        // Google Analytics나 다른 분석 도구 연동 가능
      }
    }

    // ===== 주소 관리자 클래스 =====
    class AddressManager {
      constructor() {
        this.elements = {
          postNumber: document.getElementById('postNumber'),
          address: document.getElementById('address'),
          detailAddress: document.getElementById('detailAddress'),
          fullAddress: document.getElementById('fullAddress'),
          searchBtn: document.getElementById('searchPostcodeBtn')
        };
        
        this.bindEvents();
      }

      // ===== 이벤트 바인딩 =====
      bindEvents() {
        // 주소 입력 변경 시 fullAddress 업데이트
        this.elements.detailAddress.addEventListener('input', () => {
          this.updateFullAddress();
        });
        
        this.elements.address.addEventListener('input', () => {
          this.updateFullAddress();
        });
      }

      // ===== 주소 팝업 열기 =====
      openAddressPopup() {
        new daum.Postcode({
          oncomplete: (data) => {
            this.handleAddressComplete(data);
          },
          onclose: () => {
            console.log('주소 검색 팝업이 닫혔습니다.');
          }
        }).open();
      }

      // ===== 주소 검색 완료 처리 =====
      handleAddressComplete(data) {
        const postNumber = data.zonecode;
        const roadAddr = data.roadAddress;
        const jibunAddr = data.jibunAddress;
        const baseAddr = roadAddr || jibunAddr;

        this.elements.postNumber.value = postNumber;
        this.elements.address.value = baseAddr;

        // 상세주소 필드로 포커스 이동
        this.elements.detailAddress.focus();
        
        // fullAddress 업데이트
        this.updateFullAddress();
        
        console.log('주소 검색 완료:', { postNumber, baseAddr });
      }

      // ===== 전체 주소 업데이트 =====
      updateFullAddress() {
        const baseAddr = this.elements.address.value.trim();
        const detail = this.elements.detailAddress.value.trim();
        const fullAddress = baseAddr + ' ' + detail;
        
        this.elements.fullAddress.value = fullAddress;
      }

      // ===== 주소 유효성 검사 =====
      validateAddress() {
        const postNumber = this.elements.postNumber.value.trim();
        const address = this.elements.address.value.trim();
        const detailAddress = this.elements.detailAddress.value.trim();
        
        if (!postNumber) {
          return { isValid: false, message: '우편번호를 검색해주세요.' };
        }
        
        if (!address) {
          return { isValid: false, message: '주소를 검색해주세요.' };
        }
        
        if (!detailAddress) {
          return { isValid: false, message: '상세주소를 입력해주세요.' };
        }
        
        return { isValid: true };
      }
    }

    // ===== 폼 유효성 검사 클래스 =====
    class FormValidator {
      constructor() {
        this.form = null;
        this.validationRules = {
          nickname: {
            required: true,
            minLength: 2,
            maxLength: 20,
            pattern: /^[가-힣a-zA-Z0-9]+$/
          },
          phone: {
            required: true,
            pattern: /^010-\d{4}-\d{4}$/
          },
          birth: {
            required: false,
            pattern: /^\d{4}-\d{2}-\d{2}$/
          }
        };
      }

      // ===== 폼 설정 =====
      setForm(form) {
        this.form = form;
      }

      // ===== 전체 폼 검증 =====
      validateForm() {
        const nickname = document.getElementById('nickname').value;
        const phone = document.getElementById('tel').value;
        const birth = document.getElementById('birth').value;
        
        const nicknameResult = this.validateNickname(nickname);
        const phoneResult = this.validatePhone(phone);
        const birthResult = this.validateBirth(birth);
        
        return nicknameResult && phoneResult && birthResult;
      }

      // ===== 닉네임 검증 =====
      validateNickname(value) {
        const rules = this.validationRules.nickname;
        
        if (rules.required && !value.trim()) {
          this.showFieldError('nickname', '닉네임을 입력해주세요.');
          return false;
        }
        
        if (value.length < rules.minLength) {
          this.showFieldError('nickname', `닉네임은 최소 ${rules.minLength}자 이상이어야 합니다.`);
          return false;
        }
        
        if (value.length > rules.maxLength) {
          this.showFieldError('nickname', `닉네임은 최대 ${rules.maxLength}자까지 가능합니다.`);
          return false;
        }
        
        if (!rules.pattern.test(value)) {
          this.showFieldError('nickname', '닉네임은 한글, 영문, 숫자만 사용 가능합니다.');
          return false;
        }
        
        this.clearFieldError('nickname');
        return true;
      }

      // ===== 전화번호 검증 =====
      validatePhone(value) {
        const rules = this.validationRules.phone;
        
        if (rules.required && !value.trim()) {
          this.showFieldError('tel', '연락처를 입력해주세요.');
          return false;
        }
        
        if (!rules.pattern.test(value)) {
          this.showFieldError('tel', '연락처는 010-0000-0000 형식으로 입력해주세요.');
          return false;
        }
        
        this.clearFieldError('tel');
        return true;
      }

      // ===== 생년월일 검증 =====
      validateBirth(value) {
        if (!value) return true; // 선택사항
        
        const rules = this.validationRules.birth;
        
        if (!rules.pattern.test(value)) {
          this.showFieldError('birth', '올바른 날짜 형식으로 입력해주세요.');
          return false;
        }
        
        const birthDate = new Date(value);
        const today = new Date();
        
        if (birthDate > today) {
          this.showFieldError('birth', '생년월일은 오늘 날짜보다 이전이어야 합니다.');
          return false;
        }
        
        this.clearFieldError('birth');
        return true;
      }

      // ===== 필드 에러 표시 =====
      showFieldError(fieldId, message) {
        const field = document.getElementById(fieldId);
        const errorDiv = document.getElementById(`${fieldId}-error`);
        
        if (errorDiv) {
          errorDiv.textContent = message;
          errorDiv.style.display = 'block';
        }
        
        field.classList.add('error');
      }

      // ===== 필드 에러 제거 =====
      clearFieldError(fieldId) {
        const field = document.getElementById(fieldId);
        const errorDiv = document.getElementById(`${fieldId}-error`);
        
        if (errorDiv) {
          errorDiv.textContent = '';
          errorDiv.style.display = 'none';
        }
        
        field.classList.remove('error');
      }
    }

    // ===== 전역 함수들 =====
    function goToMyPage() {
      window.location.href = '/buyer/info';
    }

    function goToHome() {
      window.location.href = '/home';
    }

    // ===== 초기화 =====
    document.addEventListener('DOMContentLoaded', () => {
      const buyerEditManager = new BuyerEditManager();
      
      // 페이지 통계 수집
      setTimeout(() => {
        buyerEditManager.trackPageView();
      }, 1000);
    });
 