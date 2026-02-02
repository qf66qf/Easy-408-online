const CACHE_NAME = 'easy408-v1';

// 需要缓存的核心资源
// 注意：由于你使用了 esm.sh，我们也需要缓存这些外部 CDN 资源
const URLS_TO_CACHE = [
  '/',
  '/index.html',
  '/manifest.json',
  '/logo192.png', // 假设你有图标，如果没有可以忽略
  '/logo512.png'
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME).then((cache) => {
      console.log('Opened cache');
      return cache.addAll(URLS_TO_CACHE);
    })
  );
});

self.addEventListener('fetch', (event) => {
  const url = new URL(event.request.url);

  // 1. 忽略 API 请求 (API 请求应该走网络，失败则由前端 api.ts 处理为离线模式)
  if (url.pathname.startsWith('/api/')) {
    return;
  }

  // 2. 静态资源策略：优先查缓存，缓存没有则请求网络并写入缓存 (Stale-while-revalidate 变体)
  event.respondWith(
    caches.match(event.request).then((response) => {
      // 命中缓存直接返回
      if (response) {
        return response;
      }

      // 没命中，去网络请求
      return fetch(event.request).then((response) => {
        // 检查是否是有效的响应
        if (!response || response.status !== 200 || response.type !== 'basic' && response.type !== 'cors') {
          return response;
        }

        // 克隆响应（因为流只能消费一次）
        const responseToCache = response.clone();

        caches.open(CACHE_NAME).then((cache) => {
            // 缓存 esm.sh 的资源以及本地构建的 assets
            if (url.host === self.location.host || url.host === 'esm.sh' || url.host === 'cdn.tailwindcss.com') {
                cache.put(event.request, responseToCache);
            }
        });

        return response;
      });
    })
  );
});

// 激活时清理旧缓存
self.addEventListener('activate', (event) => {
  const cacheWhitelist = [CACHE_NAME];
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheWhitelist.indexOf(cacheName) === -1) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});