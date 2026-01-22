import { useState, useEffect } from 'react';
import { useParams, Link } from 'react-router';
import { ArrowLeft } from 'lucide-react';
import Header from '../components/Header';
import ArticleCard from '../components/ArticleCard';
import { getArticleById, getRelatedArticles, formatDate, Article } from '../data/mockArticles';

export default function ArticleDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [article, setArticle] = useState<Article | null>(null);
  const [relatedArticles, setRelatedArticles] = useState<Article[]>([]);
  const [showOriginal, setShowOriginal] = useState(false);

  useEffect(() => {
    if (id) {
      const foundArticle = getArticleById(id);
      if (foundArticle) {
        setArticle(foundArticle);
        setRelatedArticles(getRelatedArticles(foundArticle));
        
        // Save to viewed articles
        const viewed = JSON.parse(localStorage.getItem('snwa_viewed_articles') || '[]');
        const updatedViewed = [id, ...viewed.filter((viewedId: string) => viewedId !== id)].slice(0, 10);
        localStorage.setItem('snwa_viewed_articles', JSON.stringify(updatedViewed));
      }
    }
  }, [id]);

  if (!article) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Header />
        <div className="max-w-4xl mx-auto px-4 py-16 text-center">
          <p className="text-gray-500">기사를 찾을 수 없습니다.</p>
          <Link to="/" className="text-gray-900 font-medium hover:underline mt-4 inline-block">
            메인으로 돌아가기
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Header />
      
      <main className="max-w-4xl mx-auto px-4 py-8">
        {/* Back Button */}
        <Link 
          to="/" 
          className="inline-flex items-center gap-2 text-sm text-gray-600 hover:text-gray-900 mb-6"
        >
          <ArrowLeft className="w-4 h-4" />
          목록으로
        </Link>

        {/* Article Header */}
        <article className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
          <img
            src={article.thumbnail}
            alt={article.translatedTitle}
            className="w-full aspect-video object-cover"
          />
          
          <div className="p-6 md:p-8">
            {/* Category */}
            <div className="inline-block px-3 py-1 text-sm font-medium text-gray-700 bg-gray-100 rounded mb-4">
              {article.category}
            </div>

            {/* Title */}
            <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-3">
              {article.translatedTitle}
            </h1>
            
            {/* Original Title */}
            <p className="text-lg text-gray-500 mb-6">
              {article.originalTitle}
            </p>

            {/* Meta Info */}
            <div className="flex items-center gap-3 text-sm text-gray-500 pb-6 border-b border-gray-200">
              <span className="font-medium">{article.source}</span>
              <span>·</span>
              <span>{formatDate(article.publishedAt)}</span>
            </div>

            {/* Toggle Button */}
            <div className="flex justify-end mt-6 mb-6">
              <button
                onClick={() => setShowOriginal(!showOriginal)}
                className="px-4 py-2 text-sm font-medium text-gray-700 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
              >
                {showOriginal ? '번역 보기' : '원문 보기'}
              </button>
            </div>

            {/* Content */}
            <div className="prose prose-gray max-w-none">
              {showOriginal ? (
                <div className="text-gray-700 leading-relaxed whitespace-pre-line">
                  {article.originalContent}
                </div>
              ) : (
                <div className="text-gray-900 leading-relaxed whitespace-pre-line">
                  {article.translatedContent}
                </div>
              )}
            </div>
          </div>
        </article>

        {/* Related Articles */}
        {relatedArticles.length > 0 && (
          <div className="mt-12">
            <h2 className="text-xl font-bold text-gray-900 mb-6">관련 기사</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              {relatedArticles.map(relatedArticle => (
                <ArticleCard 
                  key={relatedArticle.id} 
                  article={relatedArticle}
                />
              ))}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}