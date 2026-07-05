const CACHE_NAME = 'goeasy-v1';
const STATIC_ASSETS = ['/', '/index.html'];

// Install: pre-cache shell
self.addEventListener('install', (e) => {
    e.waitUntil(
        caches.open(CACHE_NAME).then((c) => c.addAll(STATIC_ASSETS))
    );
    self.skipWaiting();
});

// Activate: clean old caches
self.addEventListener('activate', (e) => {
    e.waitUntil(
        caches.keys().then((keys) =>
            Promise.all(keys.filter((k) => k !== CACHE_NAME).map((k) => caches.delete(k)))
        )
    );
    self.clients.claim();
});

// Fetch: network-first for API, cache-first for assets
self.addEventListener('fetch', (e) => {
    const url = new URL(e.request.url);

    // Let API calls pass through; if they fail, we surface the error gracefully
    if (url.pathname.startsWith('/auth') ||
        url.pathname.startsWith('/booking') ||
        url.pathname.startsWith('/customer') ||
        url.pathname.startsWith('/driver') ||
        url.pathname.startsWith('/availableVehicles')) {
        e.respondWith(
            fetch(e.request).catch(() =>
                new Response(JSON.stringify({ statusCode: 503, message: 'Offline — request queued' }),
                    { headers: { 'Content-Type': 'application/json' } })
            )
        );
        return;
    }

    // Static assets: cache-first
    e.respondWith(
        caches.match(e.request).then((cached) => cached || fetch(e.request).then((res) => {
            if (res.ok) {
                const clone = res.clone();
                caches.open(CACHE_NAME).then((c) => c.put(e.request, clone));
            }
            return res;
        }))
    );
});

// Receive messages from the app (e.g. "cache this booking data")
self.addEventListener('message', (e) => {
    if (e.data?.type === 'CACHE_RIDE') {
        caches.open(CACHE_NAME).then((c) => {
            c.put(
                '/offline-ride',
                new Response(JSON.stringify(e.data.payload), { headers: { 'Content-Type': 'application/json' } })
            );
        });
    }
});
