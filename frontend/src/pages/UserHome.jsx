import { useState, useEffect, useRef } from 'react';
import axios from 'axios';
import API_BASE_URL from '../api';
import {
    MapPin, Navigation, Clock, XCircle, Car, User, X, LogOut,
    LocateFixed, ShieldCheck, Leaf, Star, BellRing, Volume2, MessageSquare,
    ChevronDown, Info
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/logo.svg';
import { TierBadge, VerificationInfoModal } from '../components/VerificationBadge';
import { useOnlineStatus } from '../hooks/useOnlineStatus';
import { cacheActiveRide, clearCachedRide, getCachedRide } from '../utils/offlineStorage';

const FUEL_COLORS = { ELECTRIC: 'text-green-400', CNG: 'text-emerald-400', PETROL: 'text-gray-500', DIESEL: 'text-gray-500' };
const PREF_ICONS  = { SILENT: <Volume2 size={10} />, FRIENDLY: <MessageSquare size={10} />, ANY: null };

const StarRating = ({ value, onChange }) => (
    <div className="flex gap-1 justify-center">
        {[1, 2, 3, 4, 5].map((s) => (
            <button key={s} type="button" onClick={() => onChange(s)}
                className={`text-2xl transition-colors ${s <= value ? 'text-[#F7D100]' : 'text-gray-700 hover:text-[#F7D100]/50'}`}>
                ★
            </button>
        ))}
    </div>
);

const UserHome = () => {
    const { user, logout } = useAuth();
    const isOnline = useOnlineStatus();
    const navigate = useNavigate();

    const [source, setSource] = useState('');
    const [dest, setDest] = useState('');
    const [vehicles, setVehicles] = useState([]);
    const [loading, setLoading] = useState(false);
    const [activeRide, setActiveRide] = useState(null);
    const [paymentMode] = useState('CASH');
    const [showProfile, setShowProfile] = useState(false);
    const [bookingHistory, setBookingHistory] = useState([]);
    const [historyLoading, setHistoryLoading] = useState(false);
    const [locLoading, setLocLoading] = useState(false);

    // Feature toggles
    const [preferFemaleDriver, setPreferFemaleDriver] = useState(false);
    const [scheduleMode, setScheduleMode] = useState(false);
    const [scheduledTime, setScheduledTime] = useState('');
    const [scheduledRides, setScheduledRides] = useState([]);
    const [corporateWallet, setCorporateWallet] = useState(null);
    const [useCorporateWallet, setUseCorporateWallet] = useState(false);
    const [greenOnly, setGreenOnly] = useState(false);
    const [ridePreference, setRidePreference] = useState('ANY');
    const [showFareBreakdown, setShowFareBreakdown] = useState(null); // vehicle number

    // "I'll Wait" feature
    const [waitMode, setWaitMode] = useState(false);
    const waitIntervalRef = useRef(null);

    // Verification info modal
    const [showVerificationInfo, setShowVerificationInfo] = useState(false);

    // Rating modal
    const [showRating, setShowRating] = useState(false);
    const [ratingValue, setRatingValue] = useState(0);
    const [ratingSubmitted, setRatingSubmitted] = useState(false);
    const ratedRideRef = useRef(null);

    useEffect(() => {
        if (!user?.mobile) return;
        fetchScheduledRides();
        fetchCorporateWallet();
        // Restore cached ride when starting offline
        const cached = getCachedRide();
        if (cached && !activeRide) setActiveRide(cached);
    }, [user]);

    useEffect(() => {
        if (!user?.mobile) return;
        const fetchActiveRide = async () => {
            try {
                const res = await axios.get(`${API_BASE_URL}/customer/activeBooking?mobNo=${user.mobile}`);
                const ride = res.data.data;
                if (res.data.statusCode === 200 && ride && (ride.id || ride.bookingId)) {
                    setActiveRide(ride);
                    cacheActiveRide(ride); // keep local copy for offline
                    if (ride.bookingStatus === 'COMPLETED' && !ratingSubmitted && ratedRideRef.current !== ride.id) {
                        setShowRating(true);
                    }
                } else {
                    setActiveRide(null);
                    clearCachedRide();
                }
            } catch { /* no active ride */ }
        };
        fetchActiveRide();
        const interval = setInterval(fetchActiveRide, 3000);
        return () => clearInterval(interval);
    }, [user, ratingSubmitted]);

    // "I'll Wait" auto-search loop
    useEffect(() => {
        if (waitMode && source && dest) {
            waitIntervalRef.current = setInterval(async () => {
                try {
                    const params = buildSearchParams();
                    const res = await axios.get(`${API_BASE_URL}/availableVehicles?${params}`);
                    if (res.data.statusCode === 200 && (res.data.data?.vehicles?.length || 0) > 0) {
                        setVehicles(res.data.data.vehicles);
                        setWaitMode(false);
                        clearInterval(waitIntervalRef.current);
                        if ('Notification' in window && Notification.permission === 'granted') {
                            new Notification('Go-Easy: Driver Found!', { body: 'A driver is now available for your ride.' });
                        }
                    }
                } catch { /* keep waiting */ }
            }, 15000);
        }
        return () => clearInterval(waitIntervalRef.current);
    }, [waitMode]);

    const buildSearchParams = () => {
        const params = new URLSearchParams({ mobile: user.mobile, destination: dest });
        if (preferFemaleDriver) params.set('preferFemaleDriver', 'true');
        if (greenOnly)          params.set('greenOnly', 'true');
        if (ridePreference !== 'ANY') params.set('ridePreference', ridePreference);
        return params.toString();
    };

    const fetchBookingHistory = async () => {
        setHistoryLoading(true);
        try {
            const res = await axios.get(`${API_BASE_URL}/customer/seeBookingHistory?mobNo=${user.mobile}`);
            if (res.data.statusCode === 200) setBookingHistory(res.data.data?.rlist || []);
        } catch { /* ignore */ }
        finally { setHistoryLoading(false); }
    };

    const handleGetCurrentLocation = () => {
        if (!navigator.geolocation) return alert('Geolocation not supported');
        setLocLoading(true);
        navigator.geolocation.getCurrentPosition(async (pos) => {
            const { latitude, longitude } = pos.coords;
            try {
                const res = await axios.get(`${API_BASE_URL}/customer/getCity?lat=${latitude}&lon=${longitude}`);
                if (res.data.statusCode === 200) setSource(res.data.data);
            } catch { alert('Failed to get current city'); }
            finally { setLocLoading(false); }
        }, () => { setLocLoading(false); alert('Location access denied'); });
    };

    const checkAvailability = async () => {
        if (!source || !dest) return alert('Please enter both locations');
        setLoading(true);
        setVehicles([]);
        setWaitMode(false);
        try {
            const res = await axios.get(`${API_BASE_URL}/availableVehicles?${buildSearchParams()}`);
            if (res.data.statusCode === 200) {
                const list = res.data.data?.vehicles || [];
                setVehicles(list);
                if (list.length === 0) {
                    if (window.confirm('No drivers available right now. Enable "I\'ll Wait" to get notified when one becomes available?')) {
                        activateWaitMode();
                    }
                }
            }
        } catch { alert('Error searching vehicles'); }
        finally { setLoading(false); }
    };

    const activateWaitMode = async () => {
        if ('Notification' in window && Notification.permission === 'default') {
            await Notification.requestPermission();
        }
        setWaitMode(true);
    };

    const fetchScheduledRides = async () => {
        try {
            const res = await axios.get(`${API_BASE_URL}/customer/scheduledRides?mobNo=${user.mobile}`);
            if (res.data.statusCode === 200) setScheduledRides(res.data.data || []);
        } catch { /* ignore */ }
    };

    const fetchCorporateWallet = async () => {
        try {
            const res = await axios.get(`${API_BASE_URL}/corporate/employeeWallet?mobNo=${user.mobile}`);
            if (res.data.statusCode === 200 && res.data.data) setCorporateWallet(res.data.data);
        } catch { /* no corporate plan — fine */ }
    };

    const bookVehicle = async (vehicle) => {
        try {
            const payload = {
                sourceLocation: source,
                destinationLocation: dest,
                distance: vehicle.distance || 5.0,
                fare: vehicle.estimatedFare,
                estimatedTime: Math.round(vehicle.estimatedTime) + ' mins',
                vehicleNumber: vehicle.vehicleNumber,
                paymentMode,
                scheduledTime: scheduleMode && scheduledTime ? scheduledTime : null,
                useCorporateWallet: useCorporateWallet && !!corporateWallet,
            };
            const res = await axios.post(`${API_BASE_URL}/booking/bookvehicle?mobno=${user.mobile}`, payload);
            if (res.data.statusCode === 201 || res.data.statusCode === 200) {
                setActiveRide(res.data.data);
                setVehicles([]);
                setRatingSubmitted(false);
            }
        } catch (error) {
            alert('Booking Failed: ' + (error.response?.data?.message || 'Internal error'));
        }
    };

    const handleRequestRecording = async () => {
        try {
            const bookingId = activeRide?.id || activeRide?.bookingId;
            await axios.post(`${API_BASE_URL}/booking/requestRecording?bookingId=${bookingId}`);
            alert('Recording consent request sent to driver. You\'ll be notified when they respond.');
        } catch (e) {
            alert(e.response?.data?.message || 'Could not request recording.');
        }
    };

    const handleCancelRide = async () => {
        if (!activeRide || !confirm('Cancel this ride?')) return;
        try {
            await axios.post(`${API_BASE_URL}/customer/cancelRide?bookingId=${activeRide.id || activeRide.bookingId}`);
            setActiveRide(null);
        } catch { alert('Cancel failed'); }
    };

    const handleSubmitRating = async () => {
        if (ratingValue === 0) return alert('Please select a star rating');
        try {
            const bookingId = activeRide?.id || activeRide?.bookingId;
            await axios.post(`${API_BASE_URL}/driver/rate?bookingId=${bookingId}&rating=${ratingValue}`);
            ratedRideRef.current = bookingId;
            setRatingSubmitted(true);
            setShowRating(false);
        } catch { alert('Could not submit rating'); }
    };

    const rideStatus     = activeRide?.bookingStatus;
    const isRideBooked   = rideStatus === 'BOOKED';
    const isRideOngoing  = rideStatus === 'ONGOING';
    const isRideCompleted = rideStatus === 'COMPLETED';

    const formatTime = (minutes) => {
        if (!minutes) return '0m';
        const h = Math.floor(minutes / 60);
        const m = Math.round(minutes % 60);
        return h > 0 ? `${h}h ${m}m` : `${m}m`;
    };

    // Group history by week for earnings breakdown
    const groupByWeek = (rides) => {
        const groups = {};
        rides.forEach((r) => {
            const key = r.rideDate ? r.rideDate.substring(0, 7) : 'Unknown';
            if (!groups[key]) groups[key] = { label: key, rides: [], total: 0 };
            groups[key].rides.push(r);
            groups[key].total += r.fare || 0;
        });
        return Object.values(groups).sort((a, b) => b.label.localeCompare(a.label));
    };

    return (
        <div className="min-h-screen bg-[#0A0A0A] text-white selection:bg-[#F7D100] selection:text-black">
            {/* Header */}
            <header className="fixed top-0 left-0 right-0 z-40 bg-black/60 backdrop-blur-xl border-b border-white/5 px-4 h-16 sm:h-20 flex items-center justify-center">
                <div className="w-full max-w-7xl flex items-center justify-between">
                    <img src={logo} alt="Go-Easy" className="h-12 sm:h-14 w-auto object-contain" />
                    <div className="flex items-center gap-4">
                        <button onClick={() => { setShowProfile(true); fetchBookingHistory(); }} className="flex items-center gap-2 bg-white/5 hover:bg-white/10 px-4 py-2 rounded-xl border border-white/5 transition-all text-sm font-bold">
                            <User size={16} className="text-[#F7D100]" /><span className="hidden sm:inline">My History</span>
                        </button>
                        <button onClick={() => { logout(); navigate('/'); }} className="flex items-center gap-2 bg-red-500/10 hover:bg-red-500/20 text-red-500 px-4 py-2 rounded-xl border border-red-500/10 transition-all text-sm font-bold">
                            <LogOut size={16} /><span className="hidden sm:inline">Sign Out</span>
                        </button>
                    </div>
                </div>
            </header>

            {/* Offline banner */}
            {!isOnline && (
                <div className="fixed top-16 sm:top-20 left-0 right-0 z-30 bg-orange-500/90 backdrop-blur text-black text-center text-[10px] font-black uppercase tracking-widest py-2 px-4">
                    📡 You are offline — OTPs and ride info loaded from cache. Actions will sync when reconnected.
                </div>
            )}

            <main className="pt-24 sm:pt-32 pb-20 px-4 max-w-7xl mx-auto">
                <div className="grid lg:grid-cols-12 gap-8 items-start">

                    {/* LEFT: Booking form or active ride */}
                    <div className="lg:col-span-5 xl:col-span-4 space-y-4">
                        {!activeRide ? (
                            <div className="glass-card rounded-3xl p-6 sm:p-8 animate-fade-in">
                                <h2 className="text-xl font-black uppercase tracking-widest mb-6 text-[#F7D100]">Book Your Ride</h2>
                                <div className="space-y-4">
                                    {/* Pickup */}
                                    <div className="space-y-1">
                                        <label className="text-[10px] font-black uppercase tracking-widest text-gray-500 ml-1">Pick-Up</label>
                                        <div className="flex gap-2">
                                            <div className="relative flex-1">
                                                <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 text-[#F7D100]" size={16} />
                                                <input readOnly className="premium-input pl-12 text-sm" value={source || 'Finding your location...'} />
                                            </div>
                                            <button onClick={handleGetCurrentLocation} disabled={locLoading} className="w-14 h-14 bg-white/5 border border-white/5 rounded-2xl flex items-center justify-center hover:bg-[#F7D100]/10 transition-colors">
                                                {locLoading ? <div className="w-4 h-4 border-2 border-[#F7D100] border-t-transparent rounded-full animate-spin" /> : <LocateFixed size={20} className="text-[#F7D100]" />}
                                            </button>
                                        </div>
                                    </div>
                                    {/* Drop */}
                                    <div className="space-y-1">
                                        <label className="text-[10px] font-black uppercase tracking-widest text-gray-500 ml-1">Drop</label>
                                        <div className="relative">
                                            <Navigation className="absolute left-4 top-1/2 -translate-y-1/2 text-[#F7D100]" size={16} />
                                            <input value={dest} onChange={(e) => setDest(e.target.value)} className="premium-input pl-12 text-sm" placeholder="Where do you want to go?" />
                                        </div>
                                    </div>

                                    {/* === FEATURE FILTERS === */}
                                    <div className="space-y-2 pt-2">
                                        {/* Female driver toggle */}
                                        <button type="button" onClick={() => setPreferFemaleDriver(p => !p)}
                                            className={`w-full flex items-center justify-between px-4 py-3.5 rounded-2xl border transition-all ${preferFemaleDriver ? 'bg-pink-500/15 border-pink-500/40 text-pink-300' : 'bg-white/5 border-white/10 text-gray-400 hover:border-white/20'}`}>
                                            <div className="flex items-center gap-2.5">
                                                <ShieldCheck size={16} className={preferFemaleDriver ? 'text-pink-400' : 'text-gray-600'} />
                                                <div className="text-left">
                                                    <p className="text-[10px] font-black uppercase tracking-widest">Female Driver Only</p>
                                                    <p className="text-[9px] text-gray-600">For safety &amp; comfort</p>
                                                </div>
                                            </div>
                                            <div className={`w-10 h-5 rounded-full relative transition-colors ${preferFemaleDriver ? 'bg-pink-500' : 'bg-white/10'}`}>
                                                <div className={`absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-all ${preferFemaleDriver ? 'left-5' : 'left-0.5'}`} />
                                            </div>
                                        </button>

                                        {/* Go Green toggle */}
                                        <button type="button" onClick={() => setGreenOnly(p => !p)}
                                            className={`w-full flex items-center justify-between px-4 py-3.5 rounded-2xl border transition-all ${greenOnly ? 'bg-green-500/15 border-green-500/40 text-green-300' : 'bg-white/5 border-white/10 text-gray-400 hover:border-white/20'}`}>
                                            <div className="flex items-center gap-2.5">
                                                <Leaf size={16} className={greenOnly ? 'text-green-400' : 'text-gray-600'} />
                                                <div className="text-left">
                                                    <p className="text-[10px] font-black uppercase tracking-widest">Go Green (CNG / EV only)</p>
                                                    <p className="text-[9px] text-gray-600">Eco-friendly rides</p>
                                                </div>
                                            </div>
                                            <div className={`w-10 h-5 rounded-full relative transition-colors ${greenOnly ? 'bg-green-500' : 'bg-white/10'}`}>
                                                <div className={`absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-all ${greenOnly ? 'left-5' : 'left-0.5'}`} />
                                            </div>
                                        </button>

                                        {/* Ride Preference */}
                                        <div className="relative">
                                            <div className={`w-full flex items-center justify-between px-4 py-3.5 rounded-2xl border bg-white/5 border-white/10 ${ridePreference !== 'ANY' ? 'border-[#F7D100]/30 bg-[#F7D100]/5' : ''}`}>
                                                <div className="flex items-center gap-2.5">
                                                    {ridePreference === 'SILENT' ? <Volume2 size={16} className="text-[#F7D100]" /> : ridePreference === 'FRIENDLY' ? <MessageSquare size={16} className="text-[#F7D100]" /> : <User size={16} className="text-gray-600" />}
                                                    <div>
                                                        <p className="text-[10px] font-black uppercase tracking-widest text-gray-400">Ride Vibe</p>
                                                        <p className="text-[9px] text-gray-600">{ridePreference === 'SILENT' ? 'Quiet ride, no talking' : ridePreference === 'FRIENDLY' ? 'Open to chat' : 'No preference'}</p>
                                                    </div>
                                                </div>
                                                <select value={ridePreference} onChange={(e) => setRidePreference(e.target.value)} className="absolute inset-0 opacity-0 cursor-pointer w-full">
                                                    <option value="ANY">Any</option>
                                                    <option value="SILENT">Silent</option>
                                                    <option value="FRIENDLY">Friendly</option>
                                                </select>
                                                <ChevronDown size={14} className="text-gray-600" />
                                            </div>
                                        </div>
                                    </div>

                                    {/* Corporate Wallet toggle — only shown if enrolled */}
                                    {corporateWallet && (
                                        <button type="button" onClick={() => setUseCorporateWallet(p => !p)}
                                            className={`w-full flex items-center justify-between px-4 py-3.5 rounded-2xl border transition-all ${useCorporateWallet ? 'bg-purple-500/15 border-purple-500/40 text-purple-300' : 'bg-white/5 border-white/10 text-gray-400 hover:border-white/20'}`}>
                                            <div className="flex items-center gap-2.5">
                                                <span className="text-base">🏢</span>
                                                <div className="text-left">
                                                    <p className="text-[10px] font-black uppercase tracking-widest">Use Corporate Wallet</p>
                                                    <p className="text-[9px] text-gray-600">{corporateWallet.companyName} · ₹{Math.round(corporateWallet.remaining)} remaining</p>
                                                </div>
                                            </div>
                                            <div className={`w-10 h-5 rounded-full relative transition-colors ${useCorporateWallet ? 'bg-purple-500' : 'bg-white/10'}`}>
                                                <div className={`absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-all ${useCorporateWallet ? 'left-5' : 'left-0.5'}`} />
                                            </div>
                                        </button>
                                    )}

                                    {/* Schedule Ride toggle */}
                                    <button type="button" onClick={() => setScheduleMode(p => !p)}
                                        className={`w-full flex items-center justify-between px-4 py-3.5 rounded-2xl border transition-all ${scheduleMode ? 'bg-blue-500/15 border-blue-500/40 text-blue-300' : 'bg-white/5 border-white/10 text-gray-400 hover:border-white/20'}`}>
                                        <div className="flex items-center gap-2.5">
                                            <Clock size={16} className={scheduleMode ? 'text-blue-400' : 'text-gray-600'} />
                                            <div className="text-left">
                                                <p className="text-[10px] font-black uppercase tracking-widest">Schedule Ride</p>
                                                <p className="text-[9px] text-gray-600">Lock fare now, ride later</p>
                                            </div>
                                        </div>
                                        <div className={`w-10 h-5 rounded-full relative transition-colors ${scheduleMode ? 'bg-blue-500' : 'bg-white/10'}`}>
                                            <div className={`absolute top-0.5 w-4 h-4 rounded-full bg-white shadow transition-all ${scheduleMode ? 'left-5' : 'left-0.5'}`} />
                                        </div>
                                    </button>

                                    {scheduleMode && (
                                        <div className="space-y-1">
                                            <label className="text-[10px] font-black text-blue-400 uppercase tracking-widest ml-1">Pickup Date &amp; Time</label>
                                            <input
                                                type="datetime-local"
                                                value={scheduledTime}
                                                onChange={(e) => setScheduledTime(e.target.value)}
                                                min={new Date(Date.now() + 30 * 60000).toISOString().slice(0, 16)}
                                                className="premium-input text-sm w-full"
                                                style={{ colorScheme: 'dark' }}
                                            />
                                            <p className="text-[9px] text-gray-600 ml-1">Fare shown below is locked — no surge at ride time.</p>
                                        </div>
                                    )}

                                    {/* "I'll Wait" banner */}
                                    {waitMode && (
                                        <div className="flex items-center gap-3 bg-[#F7D100]/10 border border-[#F7D100]/30 rounded-2xl px-4 py-3 animate-pulse">
                                            <BellRing size={16} className="text-[#F7D100] shrink-0" />
                                            <div className="flex-1">
                                                <p className="text-[10px] font-black uppercase tracking-widest text-[#F7D100]">Waiting for a driver...</p>
                                                <p className="text-[9px] text-gray-500">You'll be notified when one is available</p>
                                            </div>
                                            <button onClick={() => setWaitMode(false)} className="text-gray-600 hover:text-white"><X size={14} /></button>
                                        </div>
                                    )}

                                    <button onClick={checkAvailability} disabled={loading} className="premium-button w-full mt-2">
                                        {loading ? 'SEARCHING...' : 'FIND AVAILABLE DRIVERS'}
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="glass-card rounded-3xl p-8 relative overflow-hidden animate-fade-in border-[#F7D100]/20">
                                <div className="relative z-10">
                                    <div className="flex justify-between items-center mb-8">
                                        <h3 className="text-xl font-black italic tracking-tighter">Current <span className="text-[#F7D100]">Ride</span></h3>
                                        <div className="text-right">
                                            <p className="text-[10px] font-black text-gray-600 uppercase">Ride ID</p>
                                            <p className="text-xs font-bold text-gray-400">#{activeRide.id || activeRide.bookingId}</p>
                                        </div>
                                    </div>

                                    <div className="space-y-8 relative mb-10">
                                        <div className="absolute left-[11px] top-6 bottom-6 w-[1px] bg-gradient-to-b from-[#F7D100] via-gray-800 to-gray-900"></div>
                                        <div className="flex items-center relative z-10">
                                            <div className="w-6 h-6 bg-black border-2 border-[#F7D100] rounded-full flex items-center justify-center mr-4">
                                                <div className="w-1.5 h-1.5 bg-[#F7D100] rounded-full animate-pulse"></div>
                                            </div>
                                            <div>
                                                <p className="text-[10px] text-gray-600 font-black uppercase">Origin</p>
                                                <p className="text-sm font-bold">{activeRide.sourceLocation}</p>
                                            </div>
                                        </div>
                                        <div className="flex items-center relative z-10">
                                            <div className="w-6 h-6 bg-black border-2 border-gray-700 rounded-full flex items-center justify-center mr-4">
                                                <MapPin size={10} className="text-gray-600" />
                                            </div>
                                            <div>
                                                <p className="text-[10px] text-gray-600 font-black uppercase">Target</p>
                                                <p className="text-sm font-bold">{activeRide.destinationLocation}</p>
                                            </div>
                                        </div>
                                    </div>

                                    <div className={`p-6 rounded-[2rem] text-center mb-8 border transition-all ${isRideBooked ? 'bg-[#F7D100] text-black border-transparent shadow-xl' : 'bg-white/5 border-white/5'}`}>
                                        {isRideBooked && (
                                            <>
                                                <p className="text-[10px] font-black uppercase tracking-widest mb-4 opacity-60">Start Ride OTP</p>
                                                <p className="text-5xl font-black tracking-[0.3em] mb-4">{activeRide.startOtp || '----'}</p>
                                                <p className="text-[10px] font-bold opacity-60 px-4">Give this OTP to your driver to start the ride.</p>
                                            </>
                                        )}
                                        {isRideOngoing && (
                                            <>
                                                <div className="w-12 h-12 bg-[#F7D100]/10 rounded-full flex items-center justify-center mx-auto mb-4">
                                                    <Car className="text-[#F7D100] animate-bounce" size={24} />
                                                </div>
                                                <p className="font-black text-xl italic tracking-tight mb-2">Ride in progress</p>
                                                <p className="text-sm text-gray-400">ETA: {activeRide.estimatedTime || 'Recalculating...'}</p>
                                                {activeRide.endOtp && (
                                                    <div className="mt-6 pt-6 border-t border-white/5">
                                                        <p className="text-[10px] font-black uppercase text-gray-500 mb-1">End Ride OTP</p>
                                                        <p className="text-3xl font-black tracking-widest text-[#F7D100]">{activeRide.endOtp}</p>
                                                    </div>
                                                )}
                                            </>
                                        )}
                                        {isRideCompleted && (
                                            <>
                                                <p className="font-black text-xl italic tracking-tight mb-2">Ride Completed!</p>
                                                <p className="text-sm text-gray-500 mb-4">Payment pending driver confirmation.</p>
                                                <div className="bg-black/20 p-4 rounded-2xl border border-white/5">
                                                    <p className="text-[10px] font-black text-gray-500 uppercase">Total Fare</p>
                                                    <p className="text-4xl font-black text-[#F7D100]">₹{activeRide.fare}</p>
                                                </div>
                                            </>
                                        )}
                                    </div>

                                    {/* Safety recording request (BOOKED or ONGOING) */}
                                    {(isRideBooked || isRideOngoing) && (
                                        <div className="mt-2">
                                            {activeRide.recordingConsent === 'NONE' && (
                                                <button onClick={handleRequestRecording}
                                                    className="w-full flex items-center justify-center gap-2 text-gray-500 hover:text-blue-400 text-[10px] font-black uppercase tracking-widest py-2 transition-colors border border-white/5 rounded-2xl">
                                                    🎙 Request Safety Recording
                                                </button>
                                            )}
                                            {activeRide.recordingConsent === 'REQUESTED' && (
                                                <div className="flex items-center justify-center gap-2 text-[10px] font-black text-yellow-400 uppercase py-2 animate-pulse">
                                                    ⏳ Waiting for driver consent...
                                                </div>
                                            )}
                                            {activeRide.recordingConsent === 'ACTIVE' && (
                                                <div className="flex items-center justify-center gap-2 text-[10px] font-black text-red-400 uppercase py-2">
                                                    🔴 Recording Active · Auto-deleted in 24h
                                                </div>
                                            )}
                                            {activeRide.recordingConsent === 'REJECTED' && (
                                                <div className="text-center text-[10px] text-gray-600 py-2">Driver declined recording</div>
                                            )}
                                        </div>
                                    )}

                                    {isRideBooked && (
                                        <button onClick={handleCancelRide} className="w-full flex items-center justify-center gap-2 text-gray-600 hover:text-red-500 text-[10px] font-black uppercase tracking-widest py-2 transition-colors">
                                            <XCircle size={14} /> Cancel Ride
                                        </button>
                                    )}
                                </div>
                            </div>
                        )}
                    </div>

                    {/* RIGHT: Vehicle list or ride graphic */}
                    <div className="lg:col-span-7 xl:col-span-8">
                        {!activeRide && vehicles.length === 0 && !waitMode && (
                            <div className="space-y-4">
                                <div className="bg-white/5 border border-white/5 rounded-[3rem] p-12 text-center flex flex-col items-center justify-center min-h-[300px]">
                                    <div className="w-20 h-20 bg-black rounded-3xl flex items-center justify-center mb-6 shadow-inner border border-white/5">
                                        <Navigation className="text-gray-800" size={32} />
                                    </div>
                                    <h3 className="text-xl font-black text-gray-400 italic">No Ride Requested</h3>
                                    <p className="text-gray-600 text-sm mt-2 font-medium">Enter your drop location to find available drivers.</p>
                                </div>

                                {/* Upcoming scheduled rides */}
                                {scheduledRides.length > 0 && (
                                    <div className="glass-card rounded-[2rem] p-6">
                                        <div className="flex items-center justify-between mb-4">
                                            <h4 className="text-[10px] font-black uppercase tracking-[0.3em] text-blue-400 flex items-center gap-2">
                                                <Clock size={12} /> Scheduled Rides
                                            </h4>
                                            <span className="text-[9px] font-black text-blue-400 border border-blue-400/30 px-2 py-0.5 rounded-full">{scheduledRides.length}</span>
                                        </div>
                                        <div className="space-y-3">
                                            {scheduledRides.map((r) => (
                                                <div key={r.id} className="bg-white/5 border border-blue-500/10 rounded-2xl p-4">
                                                    <div className="flex justify-between items-start">
                                                        <div>
                                                            <p className="text-xs font-bold text-gray-300">{r.sourceLocation} → {r.destinationLocation}</p>
                                                            <p className="text-[9px] font-black text-blue-400 uppercase mt-1">{r.scheduledTime?.replace('T', ' ') || ''}</p>
                                                        </div>
                                                        <div className="text-right">
                                                            <p className="text-sm font-black text-[#F7D100]">₹{Math.round(r.fare)}</p>
                                                            <p className="text-[8px] text-green-400 font-black uppercase">🔒 Locked</p>
                                                        </div>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    </div>
                                )}
                            </div>
                        )}

                        {!activeRide && vehicles.length === 0 && waitMode && (
                            <div className="bg-white/5 border border-[#F7D100]/20 rounded-[3rem] p-12 text-center h-full flex flex-col items-center justify-center min-h-[400px] animate-fade-in">
                                <div className="w-20 h-20 bg-[#F7D100]/10 rounded-3xl flex items-center justify-center mb-6 border border-[#F7D100]/20 animate-pulse">
                                    <BellRing className="text-[#F7D100]" size={32} />
                                </div>
                                <h3 className="text-xl font-black text-[#F7D100] italic">"I'll Wait" Active</h3>
                                <p className="text-gray-500 text-sm mt-3 font-medium max-w-xs">Checking for drivers every 15 seconds. You'll be notified as soon as one is available.</p>
                                <button onClick={() => setWaitMode(false)} className="mt-8 text-[10px] font-black text-gray-600 hover:text-red-500 uppercase tracking-widest">Cancel Wait</button>
                            </div>
                        )}

                        {!activeRide && vehicles.length > 0 && (
                            <div className="space-y-4 animate-fade-in">
                                <div className="flex items-center justify-between mb-6 pl-2">
                                    <div>
                                        <h3 className="text-[10px] font-black uppercase tracking-[0.4em] text-gray-500">Available Drivers</h3>
                                        <div className="flex flex-wrap gap-2 mt-1.5">
                                            {preferFemaleDriver && <span className="flex items-center gap-1 text-[9px] font-black text-pink-400 uppercase"><ShieldCheck size={9} /> Female Only</span>}
                                            {greenOnly && <span className="flex items-center gap-1 text-[9px] font-black text-green-400 uppercase"><Leaf size={9} /> Green Only</span>}
                                            {ridePreference !== 'ANY' && <span className="text-[9px] font-black text-[#F7D100] uppercase">{ridePreference} Ride</span>}
                                        </div>
                                    </div>
                                    <span className="bg-[#F7D100]/10 text-[#F7D100] px-3 py-1 rounded-full text-[10px] font-black border border-[#F7D100]/20">{vehicles.length} Drivers</span>
                                </div>
                                <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-4">
                                    {vehicles.map((v) => (
                                        <div key={v.vehicleNumber} className="glass-card p-6 rounded-[2rem] hover:border-[#F7D100]/30 transition-all group cursor-pointer flex flex-col" onClick={() => bookVehicle(v)}>
                                            {/* Top row */}
                                            <div className="flex justify-between items-start mb-3">
                                                <div className="w-12 h-12 bg-black border border-white/5 rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform">
                                                    <Car className={`${FUEL_COLORS[v.fuelType] || 'text-[#F7D100]/60'} group-hover:text-[#F7D100]`} size={24} />
                                                </div>
                                                <div className="text-right">
                                                    <p className="text-[10px] font-black text-gray-600 uppercase">Total</p>
                                                    <p className="text-xl font-black tracking-tighter">₹{Math.round(v.totalAmout || v.estimatedFare)}</p>
                                                </div>
                                            </div>

                                            {/* Driver info row */}
                                            <div className="flex items-center gap-2 mb-2 flex-wrap">
                                                {v.driverName && <span className="text-xs font-bold text-gray-300">{v.driverName}</span>}
                                                {v.driverGender?.toLowerCase() === 'female' && (
                                                    <span className="flex items-center gap-1 bg-pink-500/15 border border-pink-500/30 text-pink-400 text-[8px] font-black uppercase px-1.5 py-0.5 rounded-full">
                                                        <ShieldCheck size={8} /> F
                                                    </span>
                                                )}
                                                {v.driverVerificationTier && (
                                                    <button
                                                        type="button"
                                                        onClick={(e) => { e.stopPropagation(); setShowVerificationInfo(true); }}
                                                        title="What does this badge mean?"
                                                    >
                                                        <TierBadge tier={v.driverVerificationTier} size="xs" />
                                                    </button>
                                                )}
                                                {v.driverRidePreference && v.driverRidePreference !== 'ANY' && (
                                                    <span className="flex items-center gap-0.5 text-[8px] font-black text-gray-500 uppercase">
                                                        {PREF_ICONS[v.driverRidePreference]} {v.driverRidePreference}
                                                    </span>
                                                )}
                                            </div>

                                            {/* Model + fuel */}
                                            <div className="flex items-center gap-2 mb-3">
                                                <h4 className="text-sm font-black tracking-tight italic flex-1">{v.model}</h4>
                                                {(v.fuelType === 'CNG' || v.fuelType === 'ELECTRIC') && (
                                                    <span className="flex items-center gap-1 bg-green-500/10 border border-green-500/20 text-green-400 text-[8px] font-black uppercase px-1.5 py-0.5 rounded-full">
                                                        <Leaf size={8} /> {v.fuelType}
                                                    </span>
                                                )}
                                            </div>

                                            {/* Rating */}
                                            {v.driverRating > 0 && (
                                                <div className="flex items-center gap-1 mb-3">
                                                    <Star size={10} className="text-[#F7D100] fill-[#F7D100]" />
                                                    <span className="text-[10px] font-black text-gray-400">{v.driverRating.toFixed(1)}</span>
                                                </div>
                                            )}

                                            {/* Time / distance */}
                                            <div className="flex items-center gap-4 text-gray-500 mb-3">
                                                <div className="flex items-center gap-1.5 text-[10px] font-bold">
                                                    <Clock size={12} className="text-[#F7D100]" /> {formatTime(v.estimatedTime)}
                                                </div>
                                                <div className="flex items-center gap-1.5 text-[10px] font-bold">
                                                    <Navigation size={12} className="text-gray-400" /> {v.distance?.toFixed(1) || '5.0'} km
                                                </div>
                                            </div>

                                            {/* Transparent fare breakdown toggle */}
                                            <button
                                                type="button"
                                                onClick={(e) => { e.stopPropagation(); setShowFareBreakdown(showFareBreakdown === v.vehicleNumber ? null : v.vehicleNumber); }}
                                                className="flex items-center gap-1 text-[9px] font-black text-gray-600 hover:text-[#F7D100] uppercase tracking-widest mb-3 transition-colors"
                                            >
                                                <Info size={10} /> Fare Breakdown
                                            </button>
                                            {showFareBreakdown === v.vehicleNumber && (
                                                <div className="bg-black/40 rounded-xl p-3 mb-3 text-[9px] font-bold space-y-1 border border-white/5" onClick={(e) => e.stopPropagation()}>
                                                    <div className="flex justify-between text-gray-500"><span>Base fare</span><span>₹{Math.round(v.baseFare || 100)}</span></div>
                                                    <div className="flex justify-between text-gray-500"><span>Distance ({v.distance?.toFixed(1)} km × ₹{v.pricePerKm?.toFixed(0)}/km)</span><span>₹{Math.round(v.distanceFare || 0)}</span></div>
                                                    {v.penalty > 0 && <div className="flex justify-between text-red-400"><span>Penalty (prev cancel)</span><span>₹{Math.round(v.penalty)}</span></div>}
                                                    <div className="flex justify-between text-white border-t border-white/10 pt-1 mt-1"><span className="font-black uppercase">Total</span><span className="font-black">₹{Math.round(v.totalAmout || v.estimatedFare)}</span></div>
                                                </div>
                                            )}

                                            <button onClick={(e) => { e.stopPropagation(); bookVehicle(v); }} className="mt-auto w-full bg-white/5 border border-white/10 group-hover:bg-[#F7D100] group-hover:text-black py-2.5 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all">
                                                {scheduleMode ? '🔒 LOCK FARE & SCHEDULE' : 'BOOK RIDE'}
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        {activeRide && (
                            <div className="hidden lg:flex bg-white/5 border border-white/5 rounded-[3rem] h-full items-center justify-center min-h-[500px] text-center p-20 animate-fade-in relative overflow-hidden group">
                                <div className="absolute inset-0 bg-gradient-to-br from-[#F7D100]/5 to-transparent skew-y-12 translate-y-20 transition-transform group-hover:translate-y-10 duration-[2000ms]"></div>
                                <div className="relative z-10">
                                    <div className="w-32 h-32 bg-black rounded-[3rem] shadow-2xl flex items-center justify-center mx-auto mb-10 border border-[#F7D100]/20">
                                        <Navigation size={48} className="text-[#F7D100] animate-pulse" />
                                    </div>
                                    <h2 className="text-3xl font-black italic tracking-tighter mb-4 uppercase">Go-Easy is On It</h2>
                                    <p className="text-gray-500 max-w-sm mx-auto font-medium leading-relaxed">Tracking your ride. Safety protocols engaged.</p>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </main>

            {/* Verification Info Modal */}
            {showVerificationInfo && <VerificationInfoModal onClose={() => setShowVerificationInfo(false)} />}

            {/* Rating Modal */}
            {showRating && activeRide && (
                <div className="fixed inset-0 bg-black/90 backdrop-blur-md z-50 flex items-center justify-center p-4 animate-fade-in">
                    <div className="glass-card rounded-[3rem] w-full max-w-sm p-10 text-center shadow-[0_0_100px_rgba(247,209,0,0.1)]">
                        <div className="w-16 h-16 bg-[#F7D100]/10 rounded-3xl flex items-center justify-center mx-auto mb-6 border border-[#F7D100]/20">
                            <Star size={28} className="text-[#F7D100]" />
                        </div>
                        <h3 className="text-2xl font-black italic tracking-tighter mb-2">Rate Your Driver</h3>
                        <p className="text-gray-500 text-sm mb-8">How was your experience?</p>
                        <StarRating value={ratingValue} onChange={setRatingValue} />
                        <div className="flex gap-3 mt-8">
                            <button onClick={() => { setShowRating(false); ratedRideRef.current = activeRide?.id; }} className="flex-1 bg-white/5 border border-white/10 py-4 rounded-2xl text-[10px] font-black uppercase tracking-widest">Skip</button>
                            <button onClick={handleSubmitRating} className="flex-[2] bg-[#F7D100] text-black py-4 rounded-2xl text-[10px] font-black uppercase tracking-widest shadow-xl shadow-[#F7D100]/20">Submit</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Booking History Modal */}
            {showProfile && (
                <div className="fixed inset-0 bg-black/90 backdrop-blur-md z-50 flex items-center justify-center p-4 animate-fade-in">
                    <div className="glass-card rounded-[3rem] w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col shadow-[0_0_100px_rgba(247,209,0,0.1)]">
                        <div className="bg-[#F7D100] p-8 sm:p-12 flex justify-between items-center shrink-0">
                            <div>
                                <h3 className="text-black font-black text-3xl italic tracking-tighter uppercase">My Rides</h3>
                                <p className="text-black/60 text-xs font-bold font-mono mt-1">{user?.mobile}</p>
                            </div>
                            <button onClick={() => setShowProfile(false)} className="bg-black/10 p-3 rounded-2xl hover:rotate-90 transition-all text-black"><X size={24} /></button>
                        </div>

                        {/* Stats bar */}
                        <div className="flex border-b border-white/5 shrink-0">
                            <div className="flex-1 p-5 text-center border-r border-white/5">
                                <p className="text-[9px] font-black text-gray-600 uppercase">Total Rides</p>
                                <p className="text-2xl font-black">{bookingHistory.length}</p>
                            </div>
                            <div className="flex-1 p-5 text-center">
                                <p className="text-[9px] font-black text-[#F7D100] uppercase">Total Spent</p>
                                <p className="text-2xl font-black text-[#F7D100]">₹{Math.round(bookingHistory.reduce((s, r) => s + (r.fare || 0), 0))}</p>
                            </div>
                        </div>

                        <div className="p-6 sm:p-8 overflow-y-auto custom-scrollbar flex-1">
                            {historyLoading ? (
                                <div className="py-20 flex justify-center"><div className="w-8 h-8 border-2 border-[#F7D100] border-t-transparent rounded-full animate-spin"></div></div>
                            ) : bookingHistory.length === 0 ? (
                                <div className="py-20 text-center opacity-30 flex flex-col items-center">
                                    <XCircle size={40} className="mb-4" />
                                    <p className="text-[10px] font-black uppercase tracking-widest">No rides yet</p>
                                </div>
                            ) : (
                                <div className="space-y-6">
                                    {groupByWeek(bookingHistory).map((group) => (
                                        <div key={group.label}>
                                            <div className="flex items-center justify-between mb-3">
                                                <span className="text-[9px] font-black text-gray-600 uppercase tracking-widest">{group.label}</span>
                                                <span className="text-[9px] font-black text-[#F7D100]">₹{Math.round(group.total)}</span>
                                            </div>
                                            <div className="space-y-3">
                                                {group.rides.map((ride, idx) => (
                                                    <div key={idx} className="bg-white/5 border border-white/5 p-5 rounded-[1.5rem] hover:border-[#F7D100]/20 transition-all">
                                                        <div className="flex justify-between items-center">
                                                            <div className="space-y-2">
                                                                <div className="flex items-center gap-2"><div className="w-2 h-2 bg-[#F7D100] rounded-full"></div><p className="text-xs font-bold text-gray-400">{ride.sourceLocation}</p></div>
                                                                <div className="flex items-center gap-2"><div className="w-2 h-2 bg-gray-700 rounded-full"></div><p className="text-xs font-bold text-gray-400">{ride.destinationLocation}</p></div>
                                                                <div className="flex gap-3 pt-1">
                                                                    <span className="text-[9px] font-black text-gray-600 uppercase">{ride.distance} km</span>
                                                                    <span className="text-[9px] font-black text-[#F7D100] uppercase">{ride.bookingStatus}</span>
                                                                </div>
                                                            </div>
                                                            <p className="text-xl font-black italic text-[#F7D100]">₹{Math.round(ride.fare)}</p>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserHome;
