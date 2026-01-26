import { Link } from 'react-router';
import { Article, formatDate } from '../data/mockArticles';

interface ArticleCardProps {
  article: Article;
  compact?: boolean;
}

export default function ArticleCard({ article, compact = false }: ArticleCardProps) {
  if (compact) {
    return (
      <Link
        to={`/articles/${article.id}`}
        className="group block"
      >
        <div className="flex gap-3">
          <img
            src={article.thumbnail}
            alt={article.translatedTitle}
            className="w-24 h-16 object-cover rounded flex-shrink-0"
          />
          <div className="flex-1 min-w-0">
            <h3 className="text-sm font-medium text-gray-900 line-clamp-2 group-hover:text-gray-600 transition-colors">
              {article.translatedTitle}
            </h3>
            <div className="flex items-center gap-2 mt-1 text-xs text-gray-500">
              <span>{article.source}</span>
              <span>·</span>
              <span>{formatDate(article.publishedAt)}</span>
            </div>
          </div>
        </div>
      </Link>
    );
  }

  return (
    <Link
      to={`/articles/${article.id}`}
      className="group block bg-white rounded-lg overflow-hidden hover:shadow-lg transition-shadow border border-gray-100"
    >
      <div className="aspect-video overflow-hidden">
        <img
          src={article.thumbnail}
          alt={article.translatedTitle}
          className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
        />
      </div>
      <div className="p-4">
        <div className="inline-block px-2 py-1 text-xs font-medium text-gray-700 bg-gray-100 rounded mb-2">
          {article.category}
        </div>
        <h2 className="text-lg font-bold text-gray-900 mb-1 line-clamp-2 group-hover:text-gray-600 transition-colors">
          {article.translatedTitle}
        </h2>
        <p className="text-sm text-gray-500 mb-3 line-clamp-1">
          {article.originalTitle}
        </p>
        <div className="flex items-center gap-2 text-sm text-gray-500">
          <span>{article.source}</span>
          <span>·</span>
          <span>{formatDate(article.publishedAt)}</span>
        </div>
      </div>
    </Link>
  );
}