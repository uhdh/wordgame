/**
 * <언어의 조각> 100 Stage Puzzle Database (Sorted by Difficulty / Length)
 */
import { getWordBaseTiles, decomposeWordToTargetTiles, getWordChosungHint } from './hangulEngine.js';

// 100 Curated Standard Korean Words (표준국어대사전 등재 단어)
const RAW_100_WORDS = [
  // === Level 1: 3글자 단어 (1 ~ 30) - 쉬움 ===
  { word: '금메달', level: '쉬움' },
  { word: '무지개', level: '쉬움' },
  { word: '도토리', level: '쉬움' },
  { word: '도루묵', level: '쉬움' },
  { word: '대나무', level: '쉬움' },
  { word: '도마뱀', level: '쉬움' },
  { word: '달무리', level: '쉬움' },
  { word: '그림책', level: '쉬움' },
  { word: '오두막', level: '쉬움' },
  { word: '신호등', level: '쉬움' },
  { word: '미나리', level: '쉬움' },
  { word: '너구리', level: '쉬움' },
  { word: '달팽이', level: '쉬움' },
  { word: '눈사람', level: '쉬움' },
  { word: '개나리', level: '쉬움' },
  { word: '물망초', level: '쉬움' },
  { word: '솜사탕', level: '쉬움' },
  { word: '안개꽃', level: '쉬움' },
  { word: '유리병', level: '쉬움' },
  { word: '지우개', level: '쉬움' },
  { word: '호랑이', level: '쉬움' },
  { word: '휘파람', level: '쉬움' },
  { word: '종이학', level: '쉬움' },
  { word: '목련꽃', level: '쉬움' },
  { word: '비빔밥', level: '쉬움' },
  { word: '손수건', level: '쉬움' },
  { word: '은방울', level: '쉬움' },
  { word: '항아리', level: '쉬움' },
  { word: '기러기', level: '쉬움' },
  { word: '두루미', level: '쉬움' },

  // === Level 2: 4글자 단어 (31 ~ 65) - 보통 ===
  { word: '삯바느질', level: '보통' },
  { word: '바람개비', level: '보통' },
  { word: '산들바람', level: '보통' },
  { word: '청개구리', level: '보통' },
  { word: '해바라기', level: '보통' },
  { word: '하모니카', level: '보통' },
  { word: '카네이션', level: '보통' },
  { word: '코스모스', level: '보통' },
  { word: '시나브로', level: '보통' },
  { word: '동고동락', level: '보통' },
  { word: '일석이조', level: '보통' },
  { word: '고진감래', level: '보통' },
  { word: '유비무환', level: '보통' },
  { word: '전화위복', level: '보통' },
  { word: '사필귀정', level: '보통' },
  { word: '모래시계', level: '보통' },
  { word: '겨울바람', level: '보통' },
  { word: '호연지기', level: '보통' },
  { word: '군계일학', level: '보통' },
  { word: '다재다능', level: '보통' },
  { word: '대기만성', level: '보통' },
  { word: '부귀영화', level: '보통' },
  { word: '설상가상', level: '보통' },
  { word: '속전속결', level: '보통' },
  { word: '심사숙고', level: '보통' },
  { word: '역지사지', level: '보통' },
  { word: '온고지신', level: '보통' },
  { word: '용두사미', level: '보통' },
  { word: '우공이산', level: '보통' },
  { word: '유유상종', level: '보통' },
  { word: '이심전심', level: '보통' },
  { word: '일거양득', level: '보통' },
  { word: '자수성가', level: '보통' },
  { word: '절치부심', level: '보통' },
  { word: '천고마비', level: '보통' },

  // === Level 3: 5글자 단어 (66 ~ 85) - 어려움 ===
  { word: '시간외근무', level: '어려움' },
  { word: '우주정거장', level: '어려움' },
  { word: '아름다운길', level: '어려움' },
  { word: '자연생태계', level: '어려움' },
  { word: '인공지능망', level: '어려움' },
  { word: '환경보호단', level: '어려움' },
  { word: '역사박물관', level: '어려움' },
  { word: '문화유산길', level: '어려움' },
  { word: '달맞이꽃밭', level: '어려움' },
  { word: '무지개동산', level: '어려움' },
  { word: '새벽안개길', level: '어려움' },
  { word: '푸른하늘빛', level: '어려움' },
  { word: '은하수여행', level: '어려움' },
  { word: '십전대보탕', level: '어려움' },
  { word: '가을단풍길', level: '어려움' },
  { word: '동화속마을', level: '어려움' },
  { word: '정보통신망', level: '어려움' },
  { word: '생명공학단', level: '어려움' },
  { word: '초고속통신', level: '어려움' },
  { word: '우주망원경', level: '어려움' },

  // === Level 4: 6~7글자 단어 (86 ~ 100) - 최고난도 ===
  { word: '국제관세협정', level: '최고난도' },
  { word: '국민건강보험', level: '최고난도' },
  { word: '지속가능발전', level: '최고난도' },
  { word: '정보보호관리', level: '최고난도' },
  { word: '신재생에너지', level: '최고난도' },
  { word: '기후변화대응', level: '최고난도' },
  { word: '세계무역기구', level: '최고난도' },
  { word: '대한민국정부', level: '최고난도' },
  { word: '자율주행자동차', level: '최고난도' },
  { word: '우주왕복항공', level: '최고난도' },
  { word: '국제연합기구', level: '최고난도' },
  { word: '광개토대왕릉비', level: '최고난도' },
  { word: '유네스코세계유산', level: '최고난도' },
  { word: '국제연합안전보장', level: '최고난도' },
  { word: '지구온난화방지책', level: '최고난도' }
];

/**
 * Generate full puzzle objects for all 100 stages
 */
export const STAGES_100 = RAW_100_WORDS.map((item, index) => {
  const { word, level } = item;
  const { consonants, vowels } = getWordBaseTiles(word);
  
  // Base tiles pool (consonants + vowels)
  const tiles = [...consonants, ...vowels];
  const targetTiles = decomposeWordToTargetTiles(word);
  const chosungHint = getWordChosungHint(word);

  return {
    stage: index + 1,
    title: `${index + 1}단계 (${word.length}글자 단어)`,
    level,
    length: word.length,
    points: word.length,
    tiles,
    answer: word,
    targetTiles,
    chosungHint,
    description: `타일을 회전하고 배치하여 ${word.length}글자 정답 타일 배열을 찾아보세요.`
  };
});
