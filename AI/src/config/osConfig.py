from dotenv import load_dotenv
import os

def load_config():
    """환경 변수 로드 함수"""
    load_dotenv()  # .env 파일 로드
    # if not os.getenv("OPENAI_API_KEY"):  # 이미 로드된 경우 다시 로드하지 않도록 설정

        # 필요한 환경 변수 설정
    api_key = os.getenv("OPENAI_API_KEY")
    base_url = os.getenv("OPENAI_API_BASE")
    print(api_key+" "+base_url)
    # 환경 변수 설정 (필요한 곳에서 접근할 수 있도록)
    os.environ["OPENAI_API_KEY"] = api_key
    os.environ["OPENAI_API_BASE"] = base_url

    print("환경 변수를     로드했습니다.")  # 로딩 확인 메시지
    # else:
        # print("환경 변수가 이미 로드되었습니다.")  # 이미 로드된 경우
