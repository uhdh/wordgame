package com.gamehub.wordgame.logic

/** Ported 1:1 from src/js/stages.js — same word lists (기본 100 + 지하철역 100), same order. */

data class RawWord(val word: String, val level: String)

private val RAW_100_WORDS = listOf(
    // Level 1: 3글자 단어 (1 ~ 30) - 쉬움
    RawWord("무지개", "쉬움"), RawWord("도토리", "쉬움"), RawWord("대나무", "쉬움"), RawWord("오두막", "쉬움"),
    RawWord("다람쥐", "쉬움"), RawWord("너구리", "쉬움"), RawWord("민들레", "쉬움"), RawWord("지우개", "쉬움"),
    RawWord("호랑이", "쉬움"), RawWord("도루묵", "쉬움"), RawWord("도마뱀", "쉬움"), RawWord("종이배", "쉬움"),
    RawWord("그림책", "쉬움"), RawWord("신호등", "쉬움"), RawWord("달팽이", "쉬움"), RawWord("눈사람", "쉬움"),
    RawWord("물망초", "쉬움"), RawWord("솜사탕", "쉬움"), RawWord("물안개", "쉬움"), RawWord("유리병", "쉬움"),
    RawWord("휘파람", "쉬움"), RawWord("종이학", "쉬움"), RawWord("목련꽃", "쉬움"), RawWord("비빔밥", "쉬움"),
    RawWord("금메달", "쉬움"), RawWord("손수건", "쉬움"), RawWord("은방울", "쉬움"), RawWord("항아리", "쉬움"),
    RawWord("기러기", "쉬움"), RawWord("두루미", "쉬움"),

    // Level 2: 4글자 단어 (31 ~ 65) - 보통
    RawWord("삯바느질", "보통"), RawWord("바람개비", "보통"), RawWord("산들바람", "보통"), RawWord("청개구리", "보통"),
    RawWord("해바라기", "보통"), RawWord("하모니카", "보통"), RawWord("카네이션", "보통"), RawWord("코스모스", "보통"),
    RawWord("시나브로", "보통"), RawWord("동고동락", "보통"), RawWord("일석이조", "보통"), RawWord("고진감래", "보통"),
    RawWord("유비무환", "보통"), RawWord("전화위복", "보통"), RawWord("사필귀정", "보통"), RawWord("모래시계", "보통"),
    RawWord("겨울바람", "보통"), RawWord("호연지기", "보통"), RawWord("군계일학", "보통"), RawWord("다재다능", "보통"),
    RawWord("대기만성", "보통"), RawWord("부귀영화", "보통"), RawWord("설상가상", "보통"), RawWord("속전속결", "보통"),
    RawWord("심사숙고", "보통"), RawWord("역지사지", "보통"), RawWord("온고지신", "보통"), RawWord("용두사미", "보통"),
    RawWord("우공이산", "보통"), RawWord("유유상종", "보통"), RawWord("이심전심", "보통"), RawWord("일거양득", "보통"),
    RawWord("자수성가", "보통"), RawWord("절치부심", "보통"), RawWord("천고마비", "보통"),

    // Level 3: 5글자 단어 (66 ~ 85) - 어려움
    RawWord("시간외근무", "어려움"), RawWord("우주정거장", "어려움"), RawWord("아름다운길", "어려움"), RawWord("자연생태계", "어려움"),
    RawWord("인공지능망", "어려움"), RawWord("환경보호단", "어려움"), RawWord("역사박물관", "어려움"), RawWord("문화유산길", "어려움"),
    RawWord("달맞이공원", "어려움"), RawWord("무지개동산", "어려움"), RawWord("새벽안개길", "어려움"), RawWord("푸른하늘빛", "어려움"),
    RawWord("은하수여행", "어려움"), RawWord("십전대보탕", "어려움"), RawWord("가을단풍길", "어려움"), RawWord("동화속마을", "어려움"),
    RawWord("정보통신망", "어려움"), RawWord("생명공학단", "어려움"), RawWord("초고속통신", "어려움"), RawWord("우주망원경", "어려움"),

    // Level 4: 6~7글자 단어 (86 ~ 100) - 최고난도
    RawWord("국제관세협정", "최고난도"), RawWord("국민건강보험", "최고난도"), RawWord("지속가능발전", "최고난도"),
    RawWord("정보보호관리", "최고난도"), RawWord("신재생에너지", "최고난도"), RawWord("기후변화대응", "최고난도"),
    RawWord("세계무역기구", "최고난도"), RawWord("대한민국정부", "최고난도"), RawWord("자율주행자동차", "최고난도"),
    RawWord("우주왕복항공", "최고난도"), RawWord("국제연합기구", "최고난도"), RawWord("광개토대왕릉비", "최고난도"),
    RawWord("유네스코세계유산", "최고난도"), RawWord("국제연합안전보장", "최고난도"), RawWord("지구온난화방지책", "최고난도")
)

// 지하철역 테마 100 Stages (101 ~ 200) - 실제 수도권 전철역 이름(역 접미사 제외)
private val RAW_SUBWAY_100_WORDS = listOf(
    // === 2~3글자 (쉬움) ===
    RawWord("강남", "쉬움"), RawWord("잠실", "쉬움"), RawWord("시청", "쉬움"), RawWord("종각", "쉬움"),
    RawWord("신촌", "쉬움"), RawWord("이대", "쉬움"), RawWord("합정", "쉬움"), RawWord("망원", "쉬움"),
    RawWord("공덕", "쉬움"), RawWord("명동", "쉬움"), RawWord("회현", "쉬움"), RawWord("안국", "쉬움"),
    RawWord("상수", "쉬움"), RawWord("신림", "쉬움"), RawWord("사당", "쉬움"), RawWord("교대", "쉬움"),
    RawWord("신사", "쉬움"), RawWord("잠원", "쉬움"), RawWord("학동", "쉬움"), RawWord("청담", "쉬움"),
    RawWord("성수", "쉬움"), RawWord("천호", "쉬움"), RawWord("강동", "쉬움"), RawWord("마곡", "쉬움"),
    RawWord("발산", "쉬움"), RawWord("화곡", "쉬움"), RawWord("목동", "쉬움"), RawWord("신길", "쉬움"),
    RawWord("마포", "쉬움"), RawWord("고려대", "쉬움"), RawWord("화랑대", "쉬움"), RawWord("봉화산", "쉬움"),
    RawWord("서울숲", "쉬움"), RawWord("개포동", "쉬움"), RawWord("가천대", "쉬움"),

    // === 4글자 (보통) ===
    RawWord("상왕십리", "보통"), RawWord("건대입구", "보통"), RawWord("잠실나루", "보통"), RawWord("잠실새내", "보통"),
    RawWord("홍대입구", "보통"), RawWord("양천구청", "보통"), RawWord("동대입구", "보통"), RawWord("가락시장", "보통"),
    RawWord("경찰병원", "보통"), RawWord("김포공항", "보통"), RawWord("여의나루", "보통"), RawWord("굽은다리", "보통"),
    RawWord("태릉입구", "보통"), RawWord("강남구청", "보통"), RawWord("장승배기", "보통"), RawWord("부천시청", "보통"),
    RawWord("부평구청", "보통"), RawWord("공항시장", "보통"), RawWord("마곡나루", "보통"), RawWord("양천향교", "보통"),
    RawWord("삼성중앙", "보통"), RawWord("석촌고분", "보통"), RawWord("송파나루", "보통"), RawWord("한성백제", "보통"),
    RawWord("둔촌오륜", "보통"), RawWord("마포구청", "보통"), RawWord("버티고개", "보통"), RawWord("매탄권선", "보통"),
    RawWord("수원시청", "보통"), RawWord("강동구청", "보통"), RawWord("몽촌토성", "보통"), RawWord("수지구청", "보통"),
    RawWord("광교중앙", "보통"),

    // === 5글자 (어려움) ===
    RawWord("을지로입구", "어려움"), RawWord("종합운동장", "어려움"), RawWord("서울대입구", "어려움"), RawWord("영등포구청", "어려움"),
    RawWord("신정네거리", "어려움"), RawWord("고속터미널", "어려움"), RawWord("남부터미널", "어려움"), RawWord("영등포시장", "어려움"),
    RawWord("올림픽공원", "어려움"), RawWord("숭실대입구", "어려움"), RawWord("광명사거리", "어려움"), RawWord("삼산체육관", "어려움"),
    RawWord("국회의사당", "어려움"), RawWord("효창공원앞", "어려움"), RawWord("대모산입구", "어려움"), RawWord("단대오거리", "어려움"),
    RawWord("한국항공대", "어려움"),

    // === 6~9글자 (최고난도) ===
    RawWord("어린이대공원", "최고난도"), RawWord("신대방삼거리", "최고난도"), RawWord("중앙보훈병원", "최고난도"),
    RawWord("월드컵경기장", "최고난도"), RawWord("압구정로데오", "최고난도"), RawWord("청라국제도시", "최고난도"),
    RawWord("공항화물청사", "최고난도"), RawWord("남한산성입구", "최고난도"), RawWord("양재시민의숲", "최고난도"),
    RawWord("구로디지털단지", "최고난도"), RawWord("가산디지털단지", "최고난도"), RawWord("부천종합운동장", "최고난도"),
    RawWord("서울지방병무청", "최고난도"), RawWord("디지털미디어시티", "최고난도"), RawWord("동대문역사문화공원", "최고난도")
)

// 브랜드 이름 테마 100 Stages (201 ~ 300) - 국내외 유명 브랜드명
private val RAW_BRAND_100_WORDS = listOf(
    // === 2글자 (쉬움) ===
    RawWord("구글", "쉬움"), RawWord("삼성", "쉬움"), RawWord("쿠팡", "쉬움"),
    RawWord("배민", "쉬움"), RawWord("토스", "쉬움"), RawWord("자주", "쉬움"), RawWord("롯데", "쉬움"),
    RawWord("옥션", "쉬움"), RawWord("농협", "쉬움"), RawWord("기아", "쉬움"),
    RawWord("벤츠", "쉬움"), RawWord("혼다", "쉬움"), RawWord("볼보", "쉬움"), RawWord("샤넬", "쉬움"),
    RawWord("구찌", "쉬움"), RawWord("폴로", "쉬움"), RawWord("휠라", "쉬움"), RawWord("푸마", "쉬움"),
    RawWord("던킨", "쉬움"), RawWord("테라", "쉬움"), RawWord("진로", "쉬움"),
    RawWord("설빙", "쉬움"), RawWord("카스", "쉬움"), RawWord("반스", "쉬움"),
    RawWord("펩시", "쉬움"), RawWord("헤라", "쉬움"), RawWord("애플", "쉬움"),

    // === 3글자 (보통) ===
    RawWord("나이키", "보통"), RawWord("카카오", "보통"), RawWord("네이버", "보통"), RawWord("무신사", "보통"),
    RawWord("다이소", "보통"), RawWord("이케아", "보통"), RawWord("컨버스", "보통"), RawWord("크록스", "보통"),
    RawWord("오뚜기", "보통"), RawWord("오리온", "보통"), RawWord("이마트", "보통"), RawWord("지마켓", "보통"),
    RawWord("위메프", "보통"), RawWord("요기요", "보통"), RawWord("아우디", "보통"), RawWord("도요타", "보통"),
    RawWord("테슬라", "보통"), RawWord("페라리", "보통"), RawWord("포르쉐", "보통"), RawWord("프라다", "보통"),
    RawWord("버버리", "보통"), RawWord("티파니", "보통"), RawWord("롤렉스", "보통"), RawWord("유튜브", "보통"),
    RawWord("디즈니", "보통"), RawWord("아마존", "보통"), 
    RawWord("에뛰드", "보통"), RawWord("로레알", "보통"), RawWord("니베아", "보통"), RawWord("바세린", "보통"),
    RawWord("야놀자", "보통"), RawWord("커피빈", "보통"), RawWord("이디야", "보통"),

    // === 4글자 (어려움) ===
    RawWord("올리브영", "어려움"), RawWord("스타벅스", "어려움"), RawWord("맥도날드", "어려움"), RawWord("롯데리아", "어려움"),
    RawWord("뚜레쥬르", "어려움"), RawWord("유니클로", "어려움"), RawWord("아디다스", "어려움"), RawWord("뉴발란스", "어려움"),
    RawWord("룰루레몬", "어려움"), RawWord("코카콜라", "어려움"), RawWord("네스카페", "어려움"), RawWord("코스트코", "어려움"),
    RawWord("홈플러스", "어려움"), RawWord("롯데마트", "어려움"), RawWord("카카오톡", "어려움"), RawWord("신한은행", "어려움"),
    RawWord("국민은행", "어려움"), RawWord("삼성전자", "어려움"), RawWord("엘지전자", "어려움"), RawWord("폭스바겐", "어려움"),
    RawWord("루이비통", "어려움"), RawWord("에르메스", "어려움"), RawWord("넷플릭스", "어려움"), RawWord("무인양품", "어려움"),
    RawWord("당근마켓", "어려움"),

    // === 5글자 이상 (최고난도) ===
    RawWord("노스페이스", "최고난도"), RawWord("인스타그램", "최고난도"), RawWord("현대자동차", "최고난도"),
    RawWord("파리바게뜨", "최고난도"), RawWord("아모레퍼시픽", "최고난도"),
    RawWord("배스킨라빈스", "최고난도"), RawWord("투썸플레이스", "최고난도"), 
    RawWord("마이크로소프트", "최고난도")
)

// 먹을거 테마 100 Stages (301 ~ 400) - 음식, 음료, 디저트 이름
private val RAW_FOOD_100_WORDS = listOf(
    // === 2글자 (쉬움) ===
    RawWord("두부", "쉬움"), RawWord("김밥", "쉬움"), RawWord("라면", "쉬움"), RawWord("국수", "쉬움"),
    RawWord("김치", "쉬움"), RawWord("된장", "쉬움"), RawWord("고추", "쉬움"),
    RawWord("감자", "쉬움"), RawWord("당근", "쉬움"), RawWord("양파", "쉬움"), RawWord("버섯", "쉬움"),
    RawWord("계란", "쉬움"), RawWord("치즈", "쉬움"), RawWord("버터", "쉬움"),
    RawWord("커피", "쉬움"), RawWord("녹차", "쉬움"), RawWord("홍차", "쉬움"), RawWord("우유", "쉬움"),
    RawWord("주스", "쉬움"), RawWord("맥주", "쉬움"), RawWord("소주", "쉬움"), RawWord("치킨", "쉬움"),
    RawWord("식혜", "쉬움"), RawWord("냉면", "쉬움"),

    // === 3글자 (보통) ===
    RawWord("요거트", "보통"), RawWord("고구마", "보통"), RawWord("떡볶이", "보통"), RawWord("삼겹살", "보통"),
    RawWord("갈비탕", "보통"), RawWord("설렁탕", "보통"), RawWord("육개장", "보통"), RawWord("짜장면", "보통"),
    RawWord("순대국", "보통"), RawWord("어묵탕", "보통"), RawWord("감자탕", "보통"), RawWord("매운탕", "보통"),
    RawWord("추어탕", "보통"), RawWord("콩나물", "보통"), RawWord("미역국", "보통"), RawWord("된장국", "보통"),
    RawWord("계란찜", "보통"), RawWord("감자전", "보통"), RawWord("부침개", "보통"),
    RawWord("마카롱", "보통"), RawWord("초콜릿", "보통"), RawWord("딸기잼", "보통"), RawWord("사과잼", "보통"),
    RawWord("수정과", "보통"), RawWord("누룽지", "보통"), RawWord("잡곡밥", "보통"), RawWord("볶음밥", "보통"),
    RawWord("만두국", "보통"), RawWord("핫도그", "보통"),

    // === 4글자 (어려움) ===
    RawWord("김치찌개", "어려움"), RawWord("부대찌개", "어려움"), RawWord("계란말이", "어려움"),
    RawWord("닭볶음탕", "어려움"), RawWord("제육볶음", "어려움"), RawWord("낙지볶음", "어려움"), RawWord("콩나물국", "어려움"),
    RawWord("미역줄기", "어려움"), RawWord("감자조림", "어려움"), 
    RawWord("무말랭이", "어려움"), RawWord("열무김치", "어려움"), RawWord("총각김치", "어려움"), RawWord("해물파전", "어려움"),
    RawWord("만두전골", "어려움"), RawWord("떡만두국", "어려움"),

    // === 4~7글자 (최고난도) ===
    RawWord("아메리카노", "최고난도"), RawWord("카푸치노", "최고난도"),
    RawWord("바닐라라떼", "최고난도"), RawWord("카라멜마키아토", "최고난도"), RawWord("딸기스무디", "최고난도"),
    RawWord("망고스무디", "최고난도"), RawWord("자몽에이드", "최고난도"), RawWord("레몬에이드", "최고난도"),
    RawWord("청포도에이드", "최고난도"), RawWord("복숭아아이스티", "최고난도"), RawWord("초코라떼", "최고난도"),
    RawWord("그린티라떼", "최고난도"), RawWord("요거트스무디", "최고난도"), RawWord("마카롱세트", "최고난도"),
    RawWord("티라미수케이크", "최고난도"), RawWord("크로플세트", "최고난도"), RawWord("크루아상", "최고난도"),
    RawWord("샌드위치", "최고난도"), RawWord("불고기버거", "최고난도"), RawWord("치즈버거", "최고난도"),
    RawWord("새우버거", "최고난도"), RawWord("감자튀김", "최고난도"), RawWord("치킨너겟", "최고난도"),
    RawWord("초코케이크", "최고난도")
)

// 어려움 테마 100 Stages (401 ~ 500) - 된소리·겹받침(쌍받침) 등 발음이 어려운 단어
private val RAW_HARD_100_WORDS = listOf(
    // === 2글자 (쉬움) ===
    RawWord("까치", "쉬움"), RawWord("딸기", "쉬움"), RawWord("뿌리", "쉬움"), RawWord("뚜껑", "쉬움"),
    RawWord("빨래", "쉬움"), RawWord("빨대", "쉬움"), RawWord("뽀뽀", "쉬움"), RawWord("싸움", "쉬움"),
    RawWord("짝꿍", "쉬움"), RawWord("팔찌", "쉬움"), RawWord("도끼", "쉬움"), RawWord("토끼", "쉬움"),
    RawWord("딱지", "쉬움"), RawWord("낚시", "쉬움"), RawWord("묶음", "쉬움"), RawWord("꼬리", "쉬움"),
    RawWord("꼬막", "쉬움"), RawWord("쑥갓", "쉬움"), RawWord("찌개", "쉬움"), RawWord("짜증", "쉬움"),
    RawWord("눈썹", "쉬움"), RawWord("깍지", "쉬움"), RawWord("깻잎", "쉬움"), RawWord("통닭", "쉬움"),
    RawWord("닭발", "쉬움"), RawWord("반값", "쉬움"), RawWord("집값", "쉬움"), RawWord("싫증", "쉬움"),
    RawWord("여덟", "쉬움"), RawWord("넓이", "쉬움"),

    // === 3글자 (보통) ===
    RawWord("까마귀", "보통"), RawWord("뻐꾸기", "보통"), RawWord("쓰레기", "보통"), RawWord("싸움꾼", "보통"),
    RawWord("깍쟁이", "보통"), RawWord("뚝배기", "보통"), RawWord("쌍꺼풀", "보통"), RawWord("딸꾹질", "보통"),
    RawWord("코딱지", "보통"), RawWord("발꿈치", "보통"), RawWord("팔꿈치", "보통"), RawWord("흙탕물", "보통"),
    RawWord("값어치", "보통"), RawWord("넋두리", "보통"), RawWord("쌍둥이", "보통"), RawWord("떡꼬치", "보통"),
    RawWord("쭈꾸미", "보통"), RawWord("깍두기", "보통"), RawWord("떡갈비", "보통"), RawWord("까치집", "보통"),
    RawWord("까막눈", "보통"), RawWord("까치발", "보통"), RawWord("쌈짓돈", "보통"), RawWord("빨간불", "보통"),
    RawWord("싸움닭", "보통"), RawWord("흙먼지", "보통"), RawWord("닭꼬치", "보통"), RawWord("닭갈비", "보통"),
    RawWord("닭똥집", "보통"), RawWord("깻잎쌈", "보통"), RawWord("깜짝쇼", "보통"), RawWord("싹쓸이", "보통"),
    RawWord("깨소금", "보통"), RawWord("떡시루", "보통"), RawWord("빨간약", "보통"),

    // === 4글자 (어려움) ===
    RawWord("손톱깎이", "어려움"), RawWord("미꾸라지", "어려움"), RawWord("눈치싸움", "어려움"), RawWord("미끄럼틀", "어려움"),
    RawWord("딱따구리", "어려움"), RawWord("쌍방과실", "어려움"), RawWord("깻잎무침", "어려움"), RawWord("동태찌개", "어려움"),
    RawWord("안동찜닭", "어려움"), RawWord("깜짝선물", "어려움"),
    RawWord("값비싸다", "어려움"), RawWord("쑥부쟁이", "어려움"), RawWord("꼴뚜기젓", "어려움"), RawWord("새침떼기", "어려움"),
    RawWord("빨간딱지", "어려움"), RawWord("깜짝등장", "어려움"), RawWord("닭떡볶이", "어려움"),
    RawWord("깜짝파티", "어려움"), RawWord("잠꾸러기", "어려움"),

    // === 5글자 이상 (최고난도) ===
    RawWord("돼지껍데기", "최고난도"), RawWord("쭈꾸미볶음", "최고난도"), RawWord("깻잎장아찌", "최고난도"),
    RawWord("떡갈비덮밥", "최고난도"), RawWord("말썽꾸러기", "최고난도"), 
    
    RawWord("깻잎된장무침", "최고난도")
)

// 신조어 테마 100 Stages (501 ~ 600) - 요즘 많이 쓰는 신조어·인터넷 유행어
private val RAW_SLANG_100_WORDS = listOf(
    // === 2글자 (쉬움) ===
    RawWord("인싸", "쉬움"), RawWord("뇌절", "쉬움"), RawWord("존버", "쉬움"),
    RawWord("만렙", "쉬움"), RawWord("갓생", "쉬움"), RawWord("최애", "쉬움"), 
    RawWord("존예", "쉬움"), RawWord("심쿵", "쉬움"), RawWord("볼매", "쉬움"),
    RawWord("흑화", "쉬움"), RawWord("입덕", "쉬움"), 
    RawWord("냉무", "쉬움"), RawWord("복붙", "쉬움"),
    
    RawWord("억까", "쉬움"), RawWord("국룰", "쉬움"), 
    RawWord("노잼", "쉬움"), RawWord("팩폭", "쉬움"),
    RawWord("관종", "쉬움"), RawWord("찐텐", "쉬움"), 
    RawWord("국뽕", "쉬움"), RawWord("샤갈", "쉬움"),

    // === 3글자 (보통) ===
    RawWord("갑분싸", "보통"), RawWord("워라밸", "보통"), RawWord("소확행", "보통"),
    RawWord("만찢남", "보통"), 
    
    RawWord("얼죽아", "보통"), RawWord("자만추", "보통"), 
    RawWord("답정너", "보통"), RawWord("무물보", "보통"), RawWord("오운완", "보통"), RawWord("케바케", "보통"),
    
    
    
    RawWord("핵노잼", "보통"), 
    

    // === 4글자 (어려움) ===
    RawWord("어쩔티비", "어려움"), RawWord("복세편살", "어려움"),
    RawWord("할많하않", "어려움"), RawWord("안물안궁", "어려움"), RawWord("자강두천", "어려움"), 
    
    
    
    

    // === 5글자 이상 (최고난도) ===
    
)

// 유명인 테마 100 Stages (601 ~ 700) - 국내외 방송·음악·연기·스포츠·문화 인물
private val RAW_CELEBRITY_100_WORDS = listOf(
    // === 2글자 (쉬움) ===
    RawWord("싸이", "쉬움"), RawWord("제니", "쉬움"), RawWord("수지", "쉬움"), RawWord("태연", "쉬움"),
    RawWord("원빈", "쉬움"), RawWord("공유", "쉬움"), 
    RawWord("지코", "쉬움"), RawWord("로제", "쉬움"), RawWord("리사", "쉬움"), RawWord("윤아", "쉬움"),
    RawWord("혜리", "쉬움"), RawWord("덱스", "쉬움"), RawWord("원이", "쉬움"), 
    RawWord("정국", "쉬움"), RawWord("쯔양", "쉬움"),

    // === 3글자 (보통) ===
    RawWord("유재석", "보통"), RawWord("스윙스", "보통"), RawWord("김원훈", "보통"), RawWord("침착맨", "보통"),
    RawWord("이수지", "보통"), RawWord("강호동", "보통"), RawWord("신동엽", "보통"), RawWord("전현무", "보통"),
    RawWord("박명수", "보통"), RawWord("이효리", "보통"), RawWord("아이유", "보통"), RawWord("임영웅", "보통"),
    RawWord("손흥민", "보통"), RawWord("김연아", "보통"), RawWord("박보검", "보통"), RawWord("송중기", "보통"),
    RawWord("이병헌", "보통"), RawWord("정우성", "보통"), RawWord("황정민", "보통"), RawWord("마동석", "보통"),
    RawWord("김혜수", "보통"), RawWord("전지현", "보통"), RawWord("손예진", "보통"), RawWord("김태리", "보통"),
    RawWord("김고은", "보통"), RawWord("박서준", "보통"), RawWord("차은우", "보통"), RawWord("장원영", "보통"),
    RawWord("안유진", "보통"), RawWord("이영지", "보통"), RawWord("백종원", "보통"), RawWord("봉준호", "보통"),
    RawWord("박찬욱", "보통"), RawWord("나영석", "보통"), RawWord("이찬원", "보통"), RawWord("류현진", "보통"),
    RawWord("김연경", "보통"), RawWord("박지성", "보통"), RawWord("비욘세", "보통"), RawWord("호날두", "보통"),
    RawWord("젠슨황", "보통"), RawWord("한강", "보통"), RawWord("이정재", "보통"), RawWord("정해인", "보통"),
    RawWord("변우석", "보통"), RawWord("고윤정", "보통"), RawWord("장도연", "보통"),

    // === 4~5글자 (어려움) ===
    RawWord("샘알트먼", "어려움"), RawWord("톰크루즈", "어려움"), RawWord("빌게이츠", "어려움"), RawWord("워런버핏", "어려움"),
    RawWord("오프라윈프리", "어려움"), RawWord("키아누리브스", "어려움"), RawWord("엠마스톤", "어려움"), RawWord("레이디가가", "어려움"),
    RawWord("브루노마스", "어려움"), RawWord("리오넬메시", "어려움"), RawWord("스티브잡스", "어려움"), RawWord("일론머스크", "어려움"),
    RawWord("황희찬", "어려움"), RawWord("김구", "어려움"), 
    RawWord("손석구", "어려움"), RawWord("최우식", "어려움"), RawWord("신민아", "어려움"), RawWord("박보영", "어려움"),

    // === 6글자 이상 (최고난도) ===
    RawWord("브래드피트", "최고난도"), RawWord("레오나르도", "최고난도"), RawWord("저스틴비버", "최고난도"), RawWord("빌리아일리시", "최고난도"),
    
    RawWord("테일러스위프트", "최고난도"), RawWord("아리아나그란데", "최고난도"), RawWord("스티븐스필버그", "최고난도"), RawWord("크리스토퍼놀란", "최고난도")
)

// OTT 테마 100 Stages (701 ~ 800) - 국내외 스트리밍 영화·드라마·예능
private val RAW_OTT_100_WORDS = listOf(
    // === 2~3글자 (쉬움) ===
    RawWord("무빙", "쉬움"), RawWord("지옥", "쉬움"), RawWord("킹덤", "쉬움"), RawWord("디피", "쉬움"),
    RawWord("카지노", "쉬움"), 
    RawWord("수리남", "쉬움"),
    RawWord("안나", "쉬움"),
    RawWord("파친코", "쉬움"), RawWord("사일로", "쉬움"),
    RawWord("로키", "쉬움"), 
    RawWord("웬즈데이", "쉬움"),

    // === 4글자 (보통) ===
    RawWord("피의게임", "보통"), RawWord("직장인들", "보통"), RawWord("더글로리", "보통"), RawWord("마스크걸", "보통"),
    RawWord("스위트홈", "보통"), RawWord("소년심판", "보통"), RawWord("솔로지옥", "보통"),
    RawWord("약한영웅", "보통"), 
    RawWord("조명가게", "보통"), 
    RawWord("환승연애", "보통"), 
    RawWord("남의연애", "보통"), 
    RawWord("세브란스", "보통"), RawWord("완다비전", "보통"), RawWord("종이의집", "보통"), RawWord("블랙미러", "보통"),
    RawWord("체르노빌", "보통"), RawWord("브리저튼", "보통"), RawWord("유포리아", "보통"), RawWord("오징어게임", "보통"),

    // === 5~6글자 (어려움) ===
    RawWord("흑백요리사", "어려움"), RawWord("피지컬아시아", "어려움"), RawWord("데블스플랜", "어려움"), 
    RawWord("크라임씬제로", "어려움"), 
    
    
    RawWord("왕좌의게임", "어려움"), RawWord("기묘한이야기", "어려움"),
    RawWord("폭싹속았수다", "어려움"), RawWord("중증외상센터", "어려움"),
    

    // === 7글자 이상 (최고난도) ===
    
    
    RawWord("라스트오브어스", "최고난도"), RawWord("지금불륜이문제가아닙니다", "최고난도"), RawWord("모태솔로지만연애는하고싶어", "최고난도")
)

// 프랜차이즈 테마 (801~) - 카페·버거·치킨·피자·분식·한식·베이커리·편의점 체인점 이름
private val RAW_FRANCHISE_100_WORDS = listOf(
    // === 2글자 (쉬움) ===
    RawWord("굽네", "쉬움"), RawWord("씨유", "쉬움"), RawWord("공차", "쉬움"), RawWord("설빙", "쉬움"),
    RawWord("깐부", "쉬움"), RawWord("던킨", "쉬움"), RawWord("교촌", "쉬움"), RawWord("본죽", "쉬움"),

    // === 3글자 (보통) ===
    RawWord("버거킹", "보통"), RawWord("이디야", "보통"), RawWord("다이소", "보통"), RawWord("폴바셋", "보통"),
    RawWord("김가네", "보통"), RawWord("비비큐", "보통"), RawWord("빽다방", "보통"), RawWord("이차돌", "보통"),
    RawWord("커피빈", "보통"), RawWord("피자헛", "보통"), RawWord("컴포즈", "보통"), RawWord("푸라닭", "보통"),
    RawWord("할리스", "보통"), RawWord("더벤티", "보통"),

    // === 4글자 (어려움) ===
    RawWord("맥도날드", "어려움"), RawWord("올리브영", "어려움"), RawWord("파스쿠찌", "어려움"), RawWord("또래오래", "어려움"),
    RawWord("역전우동", "어려움"), RawWord("스타벅스", "어려움"), RawWord("삼첩분식", "어려움"), RawWord("페리카나", "어려움"),
    RawWord("홍콩반점", "어려움"), RawWord("탐앤탐스", "어려움"), RawWord("파파존스", "어려움"), RawWord("네네치킨", "어려움"),
    RawWord("뚜레쥬르", "어려움"), RawWord("써브웨이", "어려움"), RawWord("본도시락", "어려움"), RawWord("와플대학", "어려움"),
    RawWord("메가커피", "어려움"), RawWord("노랑통닭", "어려움"), RawWord("피자스쿨", "어려움"), RawWord("롯데리아", "어려움"),
    RawWord("맘스터치", "어려움"), RawWord("에그드랍", "어려움"),

    // === 5글자 이상 (최고난도) ===
    RawWord("세븐일레븐", "최고난도"), RawWord("파리바게뜨", "최고난도"), RawWord("신전떡볶이", "최고난도"), RawWord("도미노피자", "최고난도"),
    RawWord("처갓집양념치킨", "최고난도"), RawWord("하남돼지집", "최고난도"), RawWord("지에스이십오", "최고난도"), RawWord("미스터피자", "최고난도"),
    RawWord("큰맘할매순대국", "최고난도"), RawWord("노브랜드버거", "최고난도"), RawWord("배스킨라빈스", "최고난도"), RawWord("놀부부대찌개", "최고난도"),
    RawWord("엔제리너스", "최고난도"), RawWord("죠스떡볶이", "최고난도"), RawWord("새마을식당", "최고난도"), RawWord("한솥도시락", "최고난도"),
    RawWord("명륜진사갈비", "최고난도"), RawWord("크리스피크림도넛", "최고난도"), RawWord("이삭토스트", "최고난도"), RawWord("원할머니보쌈", "최고난도"),
    RawWord("프랭크버거", "최고난도"), RawWord("동대문엽기떡볶이", "최고난도"), RawWord("케이에프씨", "최고난도"), RawWord("바르다김선생", "최고난도"),
    RawWord("비에이치씨", "최고난도"), RawWord("역전할머니맥주", "최고난도"), RawWord("피자알볼로", "최고난도"), RawWord("호식이두마리치킨", "최고난도"),
    RawWord("투썸플레이스", "최고난도")
)

private val RAW_ALL_WORDS = RAW_100_WORDS + RAW_SUBWAY_100_WORDS + RAW_BRAND_100_WORDS + RAW_FOOD_100_WORDS +
    RAW_HARD_100_WORDS + RAW_SLANG_100_WORDS + RAW_CELEBRITY_100_WORDS + RAW_OTT_100_WORDS + RAW_FRANCHISE_100_WORDS

/** 각 테마의 현재 단어 수에 맞춰 등록 순서대로 전역 단계 번호를 계산한다. */
const val STAGE_SET_SIZE = 100

data class StageSet(val key: String, val label: String, val start: Int, val size: Int)

val STAGE_SETS = buildList {
    fun addSet(key: String, label: String, size: Int) =
        add(StageSet(key, label, this@buildList.sumOf(StageSet::size), size))
    addSet("basic", "기본", RAW_100_WORDS.size)
    addSet("subway", "지하철역", RAW_SUBWAY_100_WORDS.size)
    addSet("brand", "브랜드", RAW_BRAND_100_WORDS.size)
    addSet("food", "먹을거", RAW_FOOD_100_WORDS.size)
    addSet("hard", "어려움", RAW_HARD_100_WORDS.size)
    addSet("slang", "신조어", RAW_SLANG_100_WORDS.size)
    addSet("celebrity", "유명인", RAW_CELEBRITY_100_WORDS.size)
    addSet("ott", "OTT", RAW_OTT_100_WORDS.size)
    addSet("franchise", "프랜차이즈", RAW_FRANCHISE_100_WORDS.size)
}

fun getStageSetForIndex(index: Int): StageSet =
    STAGE_SETS.find { index >= it.start && index < it.start + it.size } ?: STAGE_SETS[0]

data class Puzzle(
    val stage: Int,
    val setKey: String,
    val setLabel: String,
    val localStage: Int,
    val title: String,
    val level: String,
    val length: Int,
    val points: Int,
    val tiles: List<String>,
    val answer: String,
    val targetTiles: List<String>,
    val chosungHint: String,
    val description: String
)

val STAGES_100: List<Puzzle> = RAW_ALL_WORDS.mapIndexed { index, item ->
    val (consonants, vowels) = getWordBaseTiles(item.word)
    val tiles = consonants + vowels
    val targetTiles = decomposeWordToTargetTiles(item.word)
    val chosungHint = getWordChosungHint(item.word)
    val set = getStageSetForIndex(index)
    Puzzle(
        stage = index + 1,
        setKey = set.key,
        setLabel = set.label,
        localStage = index - set.start + 1,
        title = "${index + 1}단계 (${item.word.length}글자 단어)",
        level = item.level,
        length = item.word.length,
        points = item.word.length,
        tiles = tiles,
        answer = item.word,
        targetTiles = targetTiles,
        chosungHint = chosungHint,
        description = "타일을 회전하고 배치하여 ${item.word.length}글자 정답 타일 배열을 찾아보세요."
    )
}
