const KEY_ACTIVE_RIDE  = 'goeasy_active_ride';
const KEY_START_OTP    = 'goeasy_start_otp';
const KEY_END_OTP      = 'goeasy_end_otp';
const KEY_PENDING_SYNC = 'goeasy_pending_sync';

export const cacheActiveRide = (booking) => {
    try {
        localStorage.setItem(KEY_ACTIVE_RIDE, JSON.stringify(booking));
        if (booking?.startOtp) localStorage.setItem(KEY_START_OTP, booking.startOtp);
        if (booking?.endOtp)   localStorage.setItem(KEY_END_OTP,   booking.endOtp);

        // Also push to service worker cache
        if ('serviceWorker' in navigator && navigator.serviceWorker.controller) {
            navigator.serviceWorker.controller.postMessage({ type: 'CACHE_RIDE', payload: booking });
        }
    } catch { /* storage quota exceeded — ignore */ }
};

export const getCachedRide  = () => { try { return JSON.parse(localStorage.getItem(KEY_ACTIVE_RIDE)); } catch { return null; } };
export const getCachedStartOtp = () => localStorage.getItem(KEY_START_OTP);
export const getCachedEndOtp   = () => localStorage.getItem(KEY_END_OTP);
export const clearCachedRide   = () => {
    localStorage.removeItem(KEY_ACTIVE_RIDE);
    localStorage.removeItem(KEY_START_OTP);
    localStorage.removeItem(KEY_END_OTP);
};

// Queue an action to replay when back online
export const queuePendingAction = (action) => {
    try {
        const queue = JSON.parse(localStorage.getItem(KEY_PENDING_SYNC) || '[]');
        queue.push({ ...action, queuedAt: Date.now() });
        localStorage.setItem(KEY_PENDING_SYNC, JSON.stringify(queue));
    } catch { /* ignore */ }
};

export const getPendingActions = () => {
    try { return JSON.parse(localStorage.getItem(KEY_PENDING_SYNC) || '[]'); }
    catch { return []; }
};

export const clearPendingActions = () => localStorage.removeItem(KEY_PENDING_SYNC);
