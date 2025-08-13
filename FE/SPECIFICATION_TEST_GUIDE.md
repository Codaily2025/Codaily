# 요구사항 명세서 기능 테스트 가이드

## 개요
이 가이드는 요구사항 명세서 기능의 테스트 방법을 설명합니다.

## 주요 기능

### 1. 명세서 데이터 처리
- API 응답에 따라 자동으로 명세서 데이터를 처리
- 프로젝트 요약 정보, 기능 그룹, 상세 기능 등을 자동으로 추가/업데이트

### 2. 명세서 초기화
- 페이지 이동 시 자동으로 명세서 초기화
- 수동으로 초기화 버튼 클릭 가능

### 3. 디버깅 기능
- 현재 명세서 상태를 콘솔에 출력
- Raw 데이터 확인 가능

## 테스트 방법

### 브라우저 콘솔에서 테스트

#### 1. 프로젝트 요약 정보 테스트
```javascript
window.testProjectSummary()
```

#### 2. 기능 그룹 추가 테스트
```javascript
window.testSpecData()
```

#### 3. 상세 기능 추가 테스트
```javascript
window.testSubFeature()
```

#### 4. 명세서 초기화 테스트
```javascript
window.resetSpec()
```

#### 5. 현재 상태 출력 테스트
```javascript
window.printSpec()
```

### UI 버튼으로 테스트

#### 1. 디버그 버튼
- 요구사항 명세서 우측 상단의 파란색 "디버그" 버튼 클릭
- 브라우저 콘솔에서 현재 명세서 상태 확인

#### 2. 초기화 버튼
- 요구사항 명세서 우측 상단의 빨간색 "초기화" 버튼 클릭
- 명세서 데이터가 모두 초기화됨

## API 응답 형식

### 1. 프로젝트 요약 정보 (project:summarization)
```json
{
  "type": "project:summarization",
  "content": {
    "projectTitle": "온라인 쇼핑몰 플랫폼 개발",
    "specTitle": "온라인 쇼핑몰 플랫폼 명세서",
    "projectDescription": "사용자들이 온라인으로 상품을 구매할 수 있는 쇼핑몰 웹사이트를 개발하는 프로젝트입니다.",
    "projectId": 1,
    "specId": 1
  }
}
```

### 2. 기능 그룹 추가 (spec, spec:regenerate, spec:add:field)
```json
{
  "type": "spec",
  "content": {
    "projectId": 3,
    "specId": 1,
    "field": "배송",
    "mainFeature": {
      "id": 2088,
      "title": "배송 조회",
      "description": "사용자와 관리자 모두 배송 상태와 위치를 확인할 수 있음",
      "estimatedTime": 3,
      "priorityLevel": null
    },
    "subFeature": [
      {
        "id": 2089,
        "title": "배송 상태 조회",
        "description": "사용자와 관리자가 배송의 현재 상태를 확인하는 기능을 수행함",
        "estimatedTime": 1,
        "priorityLevel": 8
      }
    ]
  }
}
```

### 3. 상세 기능 추가 (spec:add:feature:sub)
```json
{
  "type": "spec:add:feature:sub",
  "content": {
    "projectId": 3,
    "specId": 1,
    "parentFeatureId": 2078,
    "featureSaveItem": {
      "id": 2084,
      "title": "포인트 사용 선택 인터페이스 표시",
      "description": "사용자가 결제 시 포인트를 사용할 수 있도록 선택할 수 있는 옵션을 화면에 표시",
      "estimatedTime": 2,
      "priorityLevel": 7
    }
  }
}
```

## 데이터 구조

### 프론트엔드 명세서 구조
```javascript
{
  projectOverview: {
    projectName: string,
    projectDescription: string,
    projectPurpose: string
  },
  mainFeatures: [
    {
      id: number,
      name: string,
      description: string,
      hours: number,
      priority: 'High' | 'Normal' | 'Low',
      checked: boolean,
      isOpen: boolean,
      subTasks: [
        {
          id: number,
          name: string,
          description: string,
          hours: number,
          priority: 'High' | 'Normal' | 'Low',
          checked: boolean,
          isOpen: boolean,
          subTasks: [...]
        }
      ]
    }
  ],
  projectId: number,
  specId: number,
  showSidebar: boolean,
  rawData: object
}
```

## 주의사항

1. **페이지 이동 시 자동 초기화**: 명세서가 필요하지 않은 페이지로 이동하면 자동으로 초기화됩니다.
2. **중복 필드 처리**: 같은 field명의 기능이 추가되면 기존 기능을 업데이트합니다.
3. **우선순위 변환**: 서버의 priorityLevel(1-10)을 프론트엔드의 priority(High/Normal/Low)로 자동 변환합니다.
4. **디버깅**: 문제 발생 시 브라우저 콘솔에서 `window.printSpec()`을 실행하여 현재 상태를 확인하세요.

## 문제 해결

### 명세서가 업데이트되지 않는 경우
1. 브라우저 콘솔에서 `window.printSpec()` 실행
2. Raw Data 섹션에서 API 응답 데이터 확인
3. 네트워크 탭에서 API 요청/응답 확인

### 초기화가 되지 않는 경우
1. 브라우저 콘솔에서 `window.resetSpec()` 실행
2. 페이지 새로고침 후 다시 시도
3. 다른 페이지로 이동 후 다시 돌아오기
