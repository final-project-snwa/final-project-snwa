import { useState, useEffect } from 'react';
import Header from '../components/Header';
import ArticleCard from '../components/ArticleCard';
import { getArticlesByCategory, Article } from '../data/mockArticles';

export default function MainPage() {
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [articles, setArticles] = useState<Article[]>([]);
  const [, setViewedArticles] = useState<string[]>([]);

  useEffect(() => {
    const filtered = getArticlesByCategory(selectedCategory === 'All' ? undefined : selectedCategory);
    setArticles(filtered);
  }, [selectedCategory]);

  useEffect(() => {
    // Load viewed articles from localStorage
    const viewed = JSON.parse(localStorage.getItem('snwa_viewed_articles') || '[]');
    setViewedArticles(viewed);
  }, []);

  return (
    <div className="min-h-screen bg-gray-50">
      <Header 
        showCategories 
        selectedCategory={selectedCategory}
        onCategoryChange={setSelectedCategory}
      />
      
      <main className="max-w-6xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {articles.map(article => (
            <ArticleCard 
              key={article.id} 
              article={article}
            />
          ))}
        </div>

        {articles.length === 0 && (
          <div className="text-center py-16">
            <p className="text-gray-500">해당 카테고리에 기사가 없습니다.</p>
          </div>
        )}
      </main>
    </div>
  );
}
