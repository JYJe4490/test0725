import requests
import json
import time

# 공공데이터 API 기본 파라미터
base_url = "http://www.garak.co.kr/homepage/publicdata/dataJsonOpen.do"
base_params = {
    "id": "4315",
    "passwd": "busankh9970!",
    "dataid": "data58",
    "portal.templet": "false",
    "s_date": "20250731",
    "s_deal": "211",
    "p_pos_gubun": "3"
}

def test_api_parameters():
    """API 파라미터를 다양하게 테스트"""
    print("API 파라미터 테스트 시작")
    print("=" * 50)
    
    # 테스트할 파라미터 조합들
    test_cases = [
        {
            "name": "기본 설정 (pagesize=1000, pageidx=1)",
            "params": {"pagesize": "1000", "pageidx": "1"}
        },
        {
            "name": "더 큰 페이지 크기 (pagesize=2000, pageidx=1)",
            "params": {"pagesize": "2000", "pageidx": "1"}
        },
        {
            "name": "최대 페이지 크기 (pagesize=5000, pageidx=1)",
            "params": {"pagesize": "5000", "pageidx": "1"}
        },
        {
            "name": "다른 날짜 (s_date=20250730)",
            "params": {"pagesize": "1000", "pageidx": "1", "s_date": "20250730"}
        },
        {
            "name": "다른 날짜 (s_date=20250729)",
            "params": {"pagesize": "1000", "pageidx": "1", "s_date": "20250729"}
        },
        {
            "name": "다른 거래구분 (s_deal=111)",
            "params": {"pagesize": "1000", "pageidx": "1", "s_deal": "111"}
        },
        {
            "name": "다른 거래구분 (s_deal=311)",
            "params": {"pagesize": "1000", "pageidx": "1", "s_deal": "311"}
        },
        {
            "name": "다른 위치구분 (p_pos_gubun=1)",
            "params": {"pagesize": "1000", "pageidx": "1", "p_pos_gubun": "1"}
        },
        {
            "name": "다른 위치구분 (p_pos_gubun=2)",
            "params": {"pagesize": "1000", "pageidx": "1", "p_pos_gubun": "2"}
        }
    ]
    
    for i, test_case in enumerate(test_cases, 1):
        print(f"\n[{i}] {test_case['name']}")
        print("-" * 40)
        
        # 파라미터 설정
        params = base_params.copy()
        params.update(test_case['params'])
        
        try:
            response = requests.get(base_url, params=params, timeout=30)
            print(f"응답 상태 코드: {response.status_code}")
            
            if response.status_code == 200:
                try:
                    data = response.json()
                    items = data.get("resultData", [])
                    print(f"데이터 개수: {len(items)}개")
                    
                    if items:
                        print(f"첫 번째 항목: {items[0].get('PUM_NAME', 'N/A')}")
                        print(f"마지막 항목: {items[-1].get('PUM_NAME', 'N/A')}")
                        
                        # 품목별 개수 확인
                        product_counts = {}
                        for item in items:
                            product_name = item.get('PUM_NAME', 'Unknown')
                            product_counts[product_name] = product_counts.get(product_name, 0) + 1
                        
                        print(f"품목 종류: {len(product_counts)}개")
                        print("상위 5개 품목:")
                        sorted_products = sorted(product_counts.items(), key=lambda x: x[1], reverse=True)
                        for product, count in sorted_products[:5]:
                            print(f"  - {product}: {count}개")
                    else:
                        print("데이터가 없습니다.")
                        
                except json.JSONDecodeError as e:
                    print(f"JSON 파싱 오류: {e}")
                    print(f"응답 내용: {response.text[:200]}")
            else:
                print(f"API 요청 실패: {response.status_code}")
                print(f"응답 내용: {response.text[:200]}")
                
        except Exception as e:
            print(f"요청 오류: {e}")
        
        # API 호출 간격 조절
        time.sleep(1)
    
    print("\n" + "=" * 50)
    print("API 파라미터 테스트 완료")

def test_multiple_pages():
    """여러 페이지를 테스트"""
    print("\n여러 페이지 테스트")
    print("=" * 50)
    
    page_size = 1000
    max_pages = 10
    
    for page in range(1, max_pages + 1):
        print(f"\n페이지 {page} 테스트")
        print("-" * 30)
        
        params = base_params.copy()
        params.update({
            "pagesize": str(page_size),
            "pageidx": str(page)
        })
        
        try:
            response = requests.get(base_url, params=params, timeout=30)
            print(f"응답 상태 코드: {response.status_code}")
            
            if response.status_code == 200:
                try:
                    data = response.json()
                    items = data.get("resultData", [])
                    print(f"데이터 개수: {len(items)}개")
                    
                    if items:
                        print(f"첫 번째 항목: {items[0].get('PUM_NAME', 'N/A')}")
                        print(f"마지막 항목: {items[-1].get('PUM_NAME', 'N/A')}")
                    else:
                        print("데이터가 없습니다.")
                        break
                        
                except json.JSONDecodeError as e:
                    print(f"JSON 파싱 오류: {e}")
                    print(f"응답 내용: {response.text[:200]}")
                    break
            else:
                print(f"API 요청 실패: {response.status_code}")
                break
                
        except Exception as e:
            print(f"요청 오류: {e}")
            break
        
        # API 호출 간격 조절
        time.sleep(1)

if __name__ == "__main__":
    test_api_parameters()
    test_multiple_pages() 