export interface Article {
  id: string;
  category: 'Soccer' | 'Basketball' | 'Baseball' | 'Esports';
  translatedTitle: string;
  originalTitle: string;
  source: string;
  publishedAt: string;
  thumbnail: string;
  translatedContent: string;
  originalContent: string;
  clickCount?: number;
  summary?: string;
  /** 이미 구매한 번역 언어 코드 (KO, JA, EN, ZH) - 재열람 시 확인창 생략용 */
  purchasedTranslationLanguages?: string[];
  /** 기사에서 추출된 태그 목록 */
  tags?: string[];
}

export const mockArticles: Article[] = [
  {
    id: '1',
    category: 'Soccer',
    translatedTitle: '맨체스터 시티, 챔피언스리그 결승 진출 확정',
    originalTitle: 'Manchester City Secures Champions League Final Spot',
    source: 'ESPN',
    publishedAt: '2026-01-22T10:30:00Z',
    thumbnail: 'https://images.unsplash.com/photo-1574629810360-7efbbe195018?w=800&h=450&fit=crop',
    translatedContent: `맨체스터 시티가 레알 마드리드를 상대로 극적인 역전승을 거두며 챔피언스리그 결승 진출을 확정했다.

홈 경기에서 2-0으로 뒤지던 맨체스터 시티는 후반전에 3골을 몰아치며 3-2 승리를 거뒀다. 

펩 과르디올라 감독은 경기 후 인터뷰에서 "선수들의 멘탈이 대단했다. 절대 포기하지 않는 모습을 보여줬다"고 말했다.

이번 승리로 맨체스터 시티는 3년 연속 챔피언스리그 결승에 진출하게 됐으며, 통산 두 번째 우승을 노린다.

결승전은 다음 달 뮌헨에서 열릴 예정이다.`,
    originalContent: `Manchester City mounted a dramatic comeback against Real Madrid to secure their place in the Champions League final.

Trailing 2-0 at home, City scored three second-half goals to win 3-2.

Manager Pep Guardiola said after the match: "The mentality of the players was incredible. They showed they never give up."

This victory marks City's third consecutive Champions League final appearance as they pursue their second title.

The final will take place in Munich next month.`,
  },
  {
    id: '2',
    category: 'Basketball',
    translatedTitle: 'NBA 올스타전, 사상 최고 득점 기록 경신',
    originalTitle: 'NBA All-Star Game Sets New Scoring Record',
    source: 'The Athletic',
    publishedAt: '2026-01-21T18:15:00Z',
    thumbnail: 'https://images.unsplash.com/photo-1546519638-68e109498ffc?w=800&h=450&fit=crop',
    translatedContent: `2026 NBA 올스타전이 사상 최고 득점 기록을 세우며 팬들에게 화려한 볼거리를 선사했다.

동부팀이 서부팀을 211-208로 꺾으며 승리를 거뒀다. 이는 올스타전 역사상 가장 높은 합산 득점이다.

MVP는 48점을 기록한 루카 돈치치가 수상했다. 돈치치는 3점슛 12개를 성공시키며 올스타 3점슛 기록도 경신했다.

올해 올스타전은 새로운 경기 방식을 도입해 4쿼터 내내 치열한 접전이 펼쳐졌다.

팬들은 SNS를 통해 "역대급 경기였다"며 열광적인 반응을 보였다.`,
    originalContent: `The 2026 NBA All-Star Game set a new scoring record, delivering spectacular entertainment for fans.

The Eastern Conference defeated the Western Conference 211-208, marking the highest combined score in All-Star history.

Luka Doncic won MVP honors with 48 points, also breaking the All-Star three-point record with 12 successful attempts.

This year's game introduced a new format that kept the competition tight throughout all four quarters.

Fans took to social media calling it "the best All-Star game ever."`,
  },
  {
    id: '3',
    category: 'Baseball',
    translatedTitle: '오타니, 메이저리그 역사상 최초 60홈런-60도루 달성',
    originalTitle: 'Ohtani Achieves Historic 60-60 Season',
    source: 'MLB.com',
    publishedAt: '2026-01-20T22:45:00Z',
    thumbnail: 'https://images.unsplash.com/photo-1566577739112-5180d4bf9390?w=800&h=450&fit=crop',
    translatedContent: `쇼헤이 오타니가 메이저리그 역사상 최초로 한 시즌 60홈런-60도루를 달성하는 전무후무한 기록을 세웠다.

LA 다저스 소속 오타니는 샌프란시스코 자이언츠와의 경기에서 시즌 60번째 도루에 성공하며 새 역사를 썼다.

이미 지난달 60홈런을 달성한 오타니는 이번 시즌 파워와 스피드를 동시에 과시하며 MVP 수상을 확정지었다.

다저스 데이브 로버츠 감독은 "우리는 역사적인 순간을 목격하고 있다. 오타니는 야구 그 자체를 재정의하고 있다"고 극찬했다.

오타니는 현재 타율 .328, 61홈런, 62도루를 기록 중이다.`,
    originalContent: `Shohei Ohtani made baseball history by becoming the first player ever to achieve a 60-60 season.

The LA Dodgers star stole his 60th base in a game against the San Francisco Giants, completing the unprecedented feat.

Having already hit 60 home runs last month, Ohtani has demonstrated both power and speed, securing the MVP award.

Dodgers manager Dave Roberts said: "We're witnessing history. Ohtani is redefining the game itself."

Ohtani currently has a .328 batting average with 61 home runs and 62 stolen bases.`,
  },
  {
    id: '4',
    category: 'Esports',
    translatedTitle: 'T1, 월드 챔피언십 5회 우승 달성',
    originalTitle: 'T1 Wins Fifth World Championship Title',
    source: 'Dot Esports',
    publishedAt: '2026-01-22T14:20:00Z',
    thumbnail: 'https://images.unsplash.com/photo-1542751371-adc38448a05e?w=800&h=450&fit=crop',
    translatedContent: `T1이 리그 오브 레전드 월드 챔피언십에서 5번째 우승을 차지하며 역대 최다 우승 기록을 경신했다.

결승전에서 T1은 중국의 강호 JD Gaming을 3-1로 꺾고 정상에 올랐다.

페이커는 결승전 MVP를 수상하며 개인 통산 5번째 월드 챔피언십 우승을 달성했다. 이는 e스포츠 역사상 가장 위대한 업적 중 하나로 평가받는다.

T1의 코치 스태프는 "완벽한 준비와 선수들의 헌신이 만들어낸 결과"라고 소감을 밝혔다.

시상식에서 페이커는 "팬들과 함께 이뤄낸 우승이라 더욱 의미있다"고 말했다.`,
    originalContent: `T1 has won their fifth League of Legends World Championship, setting a new record for most titles.

In the finals, T1 defeated China's JD Gaming 3-1 to claim the throne.

Faker won Finals MVP, achieving his fifth World Championship title - considered one of the greatest accomplishments in esports history.

T1's coaching staff stated: "This is the result of perfect preparation and player dedication."

At the ceremony, Faker said: "This victory is even more meaningful because we achieved it together with our fans."`,
  },
  {
    id: '5',
    category: 'Soccer',
    translatedTitle: '호날두, 사우디 리그에서 통산 500호 골 달성',
    originalTitle: 'Ronaldo Reaches 500 Career Goals in Saudi League',
    source: 'Sky Sports',
    publishedAt: '2026-01-21T16:30:00Z',
    thumbnail: 'https://images.unsplash.com/photo-1579952363873-27f3bade9f55?w=800&h=450&fit=crop',
    translatedContent: `크리스티아누 호날두가 사우디 프로 리그에서 통산 500호 골을 기록하며 새로운 이정표를 세웠다.

알 나스르 소속 호날두는 알 힐랄과의 더비 경기에서 해트트릭을 기록하며 팀의 4-2 승리를 이끌었다.

39세의 호날두는 경기 후 "아직도 경쟁하고 골을 넣는 것을 사랑한다. 나이는 숫자일 뿐"이라고 말했다.

사우디 리그 이적 후 호날두는 50경기에서 62골을 기록하며 리그 최고의 스타로 자리매김했다.

팬들은 경기장을 가득 메우고 호날두의 역사적인 순간을 함께 축하했다.`,
    originalContent: `Cristiano Ronaldo reached his 500th career goal in the Saudi Pro League, marking another milestone.

The Al Nassr star scored a hat-trick in the derby against Al Hilal, leading his team to a 4-2 victory.

The 39-year-old said after the match: "I still love competing and scoring goals. Age is just a number."

Since moving to the Saudi league, Ronaldo has scored 62 goals in 50 matches, establishing himself as the league's biggest star.

Fans filled the stadium to celebrate Ronaldo's historic moment together.`,
  },
  {
    id: '6',
    category: 'Basketball',
    translatedTitle: '커리, 통산 3점슛 4000개 돌파',
    originalTitle: 'Curry Surpasses 4000 Career Three-Pointers',
    source: 'NBA.com',
    publishedAt: '2026-01-20T20:10:00Z',
    thumbnail: 'https://images.unsplash.com/photo-1608245449230-4ac19066d2d0?w=800&h=450&fit=crop',
    translatedContent: `스테판 커리가 NBA 역사상 최초로 통산 3점슛 4000개를 돌파했다.

골든스테이트 워리어스의 커리는 덴버 너게츠와의 경기에서 7개의 3점슛을 성공시키며 이 대기록을 달성했다.

이미 역대 3점슛 1위를 기록하고 있던 커리는 이번 기록으로 자신의 입지를 더욱 공고히 했다.

스티브 커 감독은 "스테판은 농구를 완전히 바꿔놓았다. 그의 기록은 앞으로도 오랫동안 깨지지 않을 것"이라고 평가했다.

커리는 경기 후 "팀 동료들과 코치진 덕분"이라며 겸손한 모습을 보였다.`,
    originalContent: `Stephen Curry became the first player in NBA history to surpass 4,000 career three-pointers.

The Golden State Warriors guard hit seven three-pointers against the Denver Nuggets to reach this milestone.

Already the all-time leader in three-pointers made, Curry further solidified his legacy with this achievement.

Coach Steve Kerr said: "Steph has completely changed basketball. His records will stand for a very long time."

Curry remained humble after the game, crediting his teammates and coaching staff.`,
  },
];

export function getArticlesByCategory(category?: string): Article[] {
  if (!category || category === 'All') {
    return mockArticles;
  }
  return mockArticles.filter(article => article.category === category);
}

export function getArticleById(id: string): Article | undefined {
  return mockArticles.find(article => article.id === id);
}

export function getRelatedArticles(currentArticle: Article): Article[] {
  return mockArticles
    .filter(article =>
      article.category === currentArticle.category &&
      article.id !== currentArticle.id
    )
    .slice(0, 3);
}

export function formatDate(dateString: string): string {
  const date = new Date(dateString);
  const now = new Date();
  const diffInHours = Math.floor((now.getTime() - date.getTime()) / (1000 * 60 * 60));

  if (diffInHours < 1) {
    const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));
    return `${diffInMinutes}분 전`;
  } else if (diffInHours < 24) {
    return `${diffInHours}시간 전`;
  } else {
    const diffInDays = Math.floor(diffInHours / 24);
    return `${diffInDays}일 전`;
  }
}
