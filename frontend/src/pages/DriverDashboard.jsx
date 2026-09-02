import { useState, useEffect } from 'react';
import axios from 'axios';
import API_BASE_URL from '../api';
import { useAuth } from '../context/AuthContext';
import { useOnlineStatus } from '../hooks/useOnlineStatus';
import { cacheActiveRide, clearCachedRide, getCachedRide } from '../utils/offlineStorage';
import { Car, MapPin, Navigation, CheckCircle, Smartphone, Banknote, QrCode, XCircle, User, X, LogOut, TrendingUp } from 'lucide-react';
import logo from '../assets/logo.svg';

import { useNavigate } from 'react-router-dom';

const DriverDashboard = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [activeRide, setActiveRide] = useState(null);
    const [otpInput, setOtpInput] = useState('');
    const [paymentStatus, setPaymentStatus] = useState('');
    const [status, setStatus] = useState('ONLINE');
    const [loading, setLoading] = useState(false);
    const [showPayment, setShowPayment] = useState(false);
    const [qrCode, setQrCode] = useState(null);
    const [showProfile, setShowProfile] = useState(false);
    const [bookingHistory, setBookingHistory] = useState([]);
    const [historyLoading, setHistoryLoading] = useState(false);
    const [earningsData, setEarningsData] = useState(null);
    const [totalEarnings, setTotalEarnings] = useState(0);
    const [totalTrips, setTotalTrips] = useState(0);
    const [showCancelModal, setShowCancelModal] = useState(false);
    const [cancelReason, setCancelReason] = useState('');
    const [isBroadcasting, setIsBroadcasting] = useState(false);

    const driverMobile = user?.mobile;
    const isOnline     = useOnlineStatus();

    useEffect(() => {
        // Restore cached ride so OTPs are visible even while offline
        const cached = getCachedRide();
        if (cached) setActiveRide(cached);
    }, []);

    useEffect(() => {
        const fetchActiveBooking = async () => {
            if (!isOnline) return; // skip polling when offline — use cached data
            try {
                const res = await axios.get(`${API_BASE_URL}/driver/activeBooking?mobNo=${driverMobile}`);
                const ride = res.data.data;
                if (res.data.statusCode === 200 && ride && (ride.id || ride.bookingId)) {
                    if (ride.id && !ride.bookingId) ride.bookingId = ride.id;
                    setActiveRide(ride);
                    cacheActiveRide(ride);

                    if (ride.bookingStatus === 'COMPLETED') {
                        const payRes = await axios.get(`${API_BASE_URL}/api/payment/status?bookingId=${ride.bookingId}`);
                        const pStatus = payRes.data.data;
                        setPaymentStatus(pStatus);
                        if (pStatus === 'SUCCESS' || pStatus === 'PAID') {
                            setActiveRide(null);
                            setShowPayment(false);
                            clearCachedRide();
                        } else {
                            setShowPayment(true);
                        }
                    }
                } else {
                    setActiveRide(null);
                    setShowPayment(false);
                    clearCachedRide();
                }
            } catch (error) {
                console.log("No active rides");
            }
        };

        fetchActiveBooking();
        fetchBookingHistory(); // Fetch stats on initial load
        const interval = setInterval(fetchActiveBooking, 3000);
        return () => clearInterval(interval);
    }, [driverMobile, isOnline]);

    // Live Location Broadcast
    useEffect(() => {
        if (status === 'OFFLINE' || !driverMobile) {
            setIsBroadcasting(false);
            return;
        }

        let watchId;
        if ('geolocation' in navigator) {
            setIsBroadcasting(true);
            watchId = navigator.geolocation.watchPosition(
                async (position) => {
                    try {
                        await axios.post(`${API_BASE_URL}/driver/updateLocation?mobNo=${driverMobile}&lat=${position.coords.latitude}&lon=${position.coords.longitude}`);
                    } catch (error) {
                        console.error('Failed to update location', error);
                    }
                },
                (error) => {
                    console.error('GPS error:', error);
                    setIsBroadcasting(false);
                },
                { enableHighAccuracy: true, maximumAge: 5000, timeout: 5000 }
            );
        }

        return () => {
            if (watchId) navigator.geolocation.clearWatch(watchId);
            setIsBroadcasting(false);
        };
    }, [status, driverMobile]);

    const toggleStatus = async () => {
        const newStatus = status === 'ONLINE' ? 'OFFLINE' : 'ONLINE';
        setLoading(true);
        try {
            const res = await axios.put(`${API_BASE_URL}/driver/status?mobNo=${driverMobile}&status=${newStatus}`);
            if (res.data.statusCode === 200) setStatus(newStatus);
        } catch (error) {
            alert("Error updating status: " + (error.response?.data?.message || error.message));
        } finally {
            setLoading(false);
        }
    };

    const fetchBookingHistory = async () => {
        setHistoryLoading(true);
        try {
            const res = await axios.get(`${API_BASE_URL}/driver/seeBookingHistory?mobNo=${driverMobile}`);
            if (res.data.statusCode === 200) {
                const historyData = res.data.data;
                setBookingHistory(historyData?.rlist || []);
            }
            
            const earningsRes = await axios.get(`${API_BASE_URL}/driver/earnings?mobNo=${driverMobile}`);
            if (earningsRes.data.statusCode === 200) {
                setEarningsData(earningsRes.data.data);
                setTotalEarnings(earningsRes.data.data.totalEarnings || 0);
                setTotalTrips(earningsRes.data.data.totalTrips || 0);
            }
        } catch (e) {
            console.log("Failed to fetch history");
        } finally {
            setHistoryLoading(false);
        }
    };

    const handleStartRide = async () => {
        if (!otpInput) return alert("Enter Start OTP");
        setLoading(true);
        try {
            const res = await axios.post(`${API_BASE_URL}/booking/startRide?bookingId=${activeRide.bookingId}&otp=${otpInput}`);
            if (res.data.statusCode === 200) {
                setOtpInput('');
            }
        } catch (e) {
            alert("Error: " + (e.response?.data?.message || "Invalid Start OTP"));
        } finally {
            setLoading(false);
        }
    };

    const handleGenerateEndOtp = async () => {
        setLoading(true);
        try {
            await axios.post(`${API_BASE_URL}/booking/generateEndOtp?bookingId=${activeRide.bookingId}`);
            alert("End OTP sent to customer successfully!");
        } catch (e) {
            alert("Failed to generate OTP");
        } finally {
            setLoading(false);
        }
    };

    const handleCompleteRide = async () => {
        if (!otpInput) return alert("Enter Completion OTP");
        setLoading(true);
        try {
            await axios.post(`${API_BASE_URL}/booking/completeRide?bookingId=${activeRide.bookingId}&otp=${otpInput}`);
            setShowPayment(true);
            setOtpInput('');
        } catch (e) {
            alert("Invalid Completion OTP");
        } finally {
            setLoading(false);
        }
    };

    const handleCashPayment = async () => {
        if (!confirm("Confirm cash received?")) return;
        setLoading(true);
        try {
            const res = await axios.post(`${API_BASE_URL}/driver/payByCash?bookingId=${activeRide.bookingId}&paymentType=CASH`);
            const pStatus = res.data.data.paymentStatus;
            setPaymentStatus(pStatus);
            if (pStatus === 'PAID' || pStatus === 'SUCCESS') {
                setActiveRide(null);
                setShowPayment(false);
                clearCachedRide();
                alert("Payment complete! Ride finalized.");
            } else {
                alert("Cash receipt confirmed. Waiting for passenger to confirm cash payment.");
            }
        } catch (e) {
            alert("Payment confirmation failed");
        } finally {
            setLoading(false);
        }
    };

    const handleUpiPayment = async () => {
        setLoading(true);
        try {
            const res = await axios.get(`${API_BASE_URL}/driver/generateUpiQr?bookingId=${activeRide.bookingId}`);
            if (res.data.data?.qr) setQrCode(res.data.data.qr);
        } catch (e) {
            alert("UPI payment failed");
        } finally {
            setLoading(false);
        }
    };

    const confirmUpiReceived = async () => {
        if (!confirm("Confirm UPI payment received?")) return;
        setLoading(true);
        try {
            await axios.post(`${API_BASE_URL}/driver/confirmUpiPayment?bookingId=${activeRide.bookingId}`);
            setActiveRide(null);
            setShowPayment(false);
            setQrCode(null);
        } catch (e) {
            alert("Failed confirming UPI");
        } finally {
            setLoading(false);
        }
    };

    const handleCancelRide = () => {
        setShowCancelModal(true);
        setCancelReason('');
    };

    const submitCancelRide = async () => {
        if (!cancelReason) return alert('Please provide a reason for cancelling.');
        try {
            await axios.put(`${API_BASE_URL}/driver/cancel/${activeRide.bookingId}?reason=${encodeURIComponent(cancelReason)}`);
            setActiveRide(null);
            setShowCancelModal(false);
        } catch (e) {
            alert("Error cancelling");
        }
    };

    const handleRespondRecording = async (accept) => {
        try {
            await axios.post(`${API_BASE_URL}/driver/respondRecording?bookingId=${activeRide.bookingId}&accept=${accept}`);
        } catch (e) {
            alert("Failed to respond to recording request");
        }
    };

    const groupByMonth = (rides) => {
        const groups = {};
        rides.forEach((r) => {
            const key = r.rideDate ? r.rideDate.substring(0, 7) : 'Earlier';
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
                    <div className="flex items-center gap-2">
                        <img src={logo} alt="Go-Easy" className="h-12 sm:h-14 w-auto object-contain" />
                        <h1 className="text-xl sm:text-2xl font-black italic tracking-tighter">Driver<span className="text-[#F7D100]">Panel</span></h1>
                    </div>
                    <div className="flex items-center gap-3">
                        <button 
                            onClick={toggleStatus}
                            className={`hidden sm:flex items-center gap-2 px-6 py-2 rounded-xl text-[10px] font-black tracking-widest transition-all border ${status === 'ONLINE' ? 'bg-[#F7D100] text-black border-transparent shadow-[0_0_20px_rgba(247,209,0,0.2)]' : 'bg-red-500/10 text-red-500 border-red-500/20'}`}
                        >
                            <div className={`w-1.5 h-1.5 rounded-full ${status === 'ONLINE' ? 'bg-black animate-pulse' : 'bg-red-500'}`}></div>
                            {status}
                        </button>
                        <button 
                            onClick={() => { setShowProfile(true); fetchBookingHistory(); }}
                            className="bg-white/5 hover:bg-white/10 p-2.5 rounded-xl border border-white/5 transition-all text-[#F7D100]"
                        >
                            <Smartphone size={18} />
                        </button>
                        <button 
                            onClick={() => { logout(); navigate('/'); }}
                            className="bg-red-500/10 hover:bg-red-500/20 p-2.5 rounded-xl border border-red-500/10 transition-all text-red-500"
                        >
                            <LogOut size={18} />
                        </button>
                    </div>
                </div>
            </header>

            {/* Offline banner — isOnline from useOnlineStatus, cached data from offlineStorage */}
            {!isOnline && (
                <div className="fixed top-16 sm:top-20 left-0 right-0 z-30 bg-orange-500/90 backdrop-blur text-black text-center text-[10px] font-black uppercase tracking-widest py-2 px-4">
                    📡 Offline — OTPs loaded from cache. Actions will sync when reconnected.
                </div>
            )}

            <main className="pt-24 sm:pt-32 pb-20 px-4 max-w-7xl mx-auto">
                <div className="grid lg:grid-cols-12 gap-8">
                    {/* STATS AREA */}
                    <div className="lg:col-span-4 space-y-6">
                        <div className="glass-card rounded-3xl p-8 relative overflow-hidden group">
                           <div className="relative z-10">
                                <p className="text-[10px] font-black text-gray-600 uppercase tracking-[0.3em] mb-1">Logged in Driver</p>
                                <h2 className="text-3xl font-black italic tracking-tighter mb-8">{user?.name || "Driver"}</h2>
                                <div className="grid grid-cols-2 gap-4">
                                    <div className="bg-black/40 p-4 rounded-2xl border border-white/5">
                                        <p className="text-[9px] font-black text-[#F7D100] uppercase tracking-widest">Earnings</p>
                                        <p className="text-xl font-black">₹{totalEarnings}</p>
                                    </div>
                                    <div className="bg-black/40 p-4 rounded-2xl border border-white/5">
                                        <div className={`w-3 h-3 rounded-full ${status === 'ONLINE' ? 'bg-green-500 animate-pulse' : 'bg-red-500'}`}></div>
                                        <span className="font-bold text-xs">{status}</span>
                                    </div>
                                </div>

                                {isBroadcasting && status === 'ONLINE' && (
                                    <div className="mt-4 flex items-center justify-center gap-2 bg-blue-500/10 border border-blue-500/30 text-blue-400 py-1.5 px-3 rounded-full text-[9px] font-black uppercase tracking-widest animate-pulse w-fit">
                                        <Navigation size={10} /> Broadcasting Live GPS
                                    </div>
                                )}
                           </div>
                           <div className="absolute top-0 right-0 w-32 h-32 bg-[#F7D100]/5 rounded-full -mr-16 -mt-16 group-hover:scale-110 transition-transform"></div>
                        </div>

                        <div className="glass-card rounded-3xl p-8 border-dashed border-white/10 flex flex-col items-center justify-center text-center">
                            <h3 className="text-[10px] font-black uppercase tracking-[0.4em] text-gray-600 mb-6">Look for Passengers</h3>
                            <div className="w-16 h-16 bg-black rounded-2xl flex items-center justify-center mb-6 relative">
                                <div className="absolute inset-0 border border-[#F7D100]/20 rounded-2xl animate-ping"></div>
                                <Navigation className="text-[#F7D100]/40" size={24} />
                            </div>
                            <p className="text-[10px] text-gray-500 font-bold uppercase tracking-widest px-4">Stay in busy areas to get more rides.</p>
                        </div>
                    </div>

                    {/* ACTION AREA */}
                    <div className="lg:col-span-8">
                        {!activeRide ? (
                            <div className="bg-white/5 border border-white/5 rounded-[3rem] p-12 text-center h-full flex flex-col items-center justify-center min-h-[400px] animate-fade-in">
                                <div className="w-24 h-24 bg-black rounded-[2rem] flex items-center justify-center mb-10 relative">
                                    <div className={`absolute inset-0 rounded-[2rem] border-2 border-[#F7D100]/20 ${status === 'ONLINE' ? 'animate-ping' : ''}`}></div>
                                    <Car size={40} className={`text-gray-800 transition-colors ${status === 'ONLINE' ? 'text-[#F7D100]' : ''}`} />
                                </div>
                                <h3 className="text-2xl font-black italic tracking-tighter mb-4 uppercase">{status === 'ONLINE' ? 'Waiting for Rides' : 'Offline'}</h3>
                                <p className="text-gray-500 text-sm max-w-sm font-medium leading-relaxed">
                                    {status === 'ONLINE' ? 'Stay active in busy areas to receive more ride requests.' : 'Turn your status to ONLINE to start working.'}
                                </p>
                            </div>
                        ) : showPayment ? (
                            <div className="glass-card rounded-[3rem] p-10 sm:p-16 animate-fade-in border-[#F7D100]/20">
                                <div className="text-center mb-12">
                                    <p className="text-[10px] font-black text-[#F7D100] uppercase tracking-[0.4em] mb-4">Payment Amount</p>
                                    <h2 className="text-7xl font-black tracking-tighter italic text-white mb-2">₹{Math.round(activeRide.fare)}</h2>
                                    <div className="inline-flex items-center gap-2 px-4 py-1.5 bg-white/5 text-gray-400 rounded-full border border-white/5">
                                        <div className="w-1.5 h-1.5 bg-gray-600 rounded-full"></div>
                                        <span className="text-[10px] font-black uppercase tracking-widest">{activeRide.paymentMode || 'Cash Payment'}</span>
                                    </div>
                                </div>

                                {qrCode ? (
                                    <div className="text-center bg-black/40 p-10 rounded-[2.5rem] border border-white/5 animate-fade-in">
                                        <p className="text-[10px] font-black uppercase tracking-widest text-gray-600 mb-10">Payment QR Code</p>
                                        <div className="bg-white p-6 inline-block rounded-3xl shadow-[0_0_50px_rgba(247,209,0,0.1)] mb-10">
                                            <img src={`data:image/png;base64,${qrCode}`} alt="UPI QR" className="rounded-lg max-w-[200px]" />
                                        </div>
                                        <div className="flex gap-4">
                                            <button onClick={() => setQrCode(null)} className="flex-1 bg-white/5 hover:bg-white/10 py-5 rounded-2xl text-[10px] font-black uppercase tracking-widest transition-all">Back</button>
                                            <button onClick={confirmUpiReceived} className="flex-[2] bg-[#F7D100] text-black py-5 rounded-2xl text-[10px] font-black uppercase tracking-widest shadow-xl shadow-[#F7D100]/20 transition-all active:scale-95 flex items-center justify-center gap-2">
                                                <CheckCircle size={16} /> Confirm
                                            </button>
                                        </div>
                                    </div>
                                ) : paymentStatus === 'PENDING_CUSTOMER_CONFIRMATION' ? (
                                    <div className="bg-[#F7D100]/10 border border-[#F7D100]/20 text-[#F7D100] p-6 rounded-[2rem] text-center animate-pulse">
                                        <p className="text-sm font-black uppercase mb-2">⏳ Confirmation Pending</p>
                                        <p className="text-xs text-gray-400">Waiting for passenger to confirm they paid cash...</p>
                                    </div>
                                ) : paymentStatus === 'DISPUTED' ? (
                                    <div className="bg-red-500/10 border border-red-500/20 text-red-400 p-6 rounded-[2rem] text-center">
                                        <p className="text-sm font-black uppercase mb-2">⚠️ Payment Disputed</p>
                                        <p className="text-xs text-gray-500">A mismatch or confirmation timeout has occurred. Under investigation.</p>
                                    </div>
                                ) : (
                                    <div className="grid sm:grid-cols-2 gap-6">
                                        <button onClick={handleCashPayment} className="group bg-white/5 hover:bg-[#F7D100] p-10 rounded-[2.5rem] text-center transition-all border border-white/5 hover:border-transparent">
                                            <Banknote size={40} className="mx-auto mb-6 text-gray-700 group-hover:text-black transition-colors" />
                                            <h3 className="text-xl font-black uppercase group-hover:text-black transition-colors mb-1 italic">Cash Mode</h3>
                                            <p className="text-[9px] font-black text-gray-600 group-hover:text-black/60 uppercase">Cash Payment</p>
                                        </button>
                                        <button onClick={handleUpiPayment} className="group bg-white/5 hover:bg-[#F7D100] p-10 rounded-[2.5rem] text-center transition-all border border-white/5 hover:border-transparent">
                                            <QrCode size={40} className="mx-auto mb-6 text-gray-700 group-hover:text-black transition-colors" />
                                            <h3 className="text-xl font-black uppercase group-hover:text-black transition-colors mb-1 italic">UPI Mode</h3>
                                            <p className="text-[9px] font-black text-gray-600 group-hover:text-black/60 uppercase">Online Payment</p>
                                        </button>
                                    </div>
                                )}
                            </div>
                        ) : (
                            <div className="glass-card rounded-[3rem] p-8 sm:p-12 animate-fade-in relative overflow-hidden">
                                <div className="flex flex-col sm:flex-row justify-between items-start gap-6 mb-12">
                                    <div className="space-y-4">
                                        <span className={`px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-[0.2em] border ${activeRide.bookingStatus === 'BOOKED' ? 'bg-[#F7D100]/10 text-[#F7D100] border-[#F7D100]/20' : 'bg-green-500/10 text-green-500 border-green-500/20'}`}>
                                            {activeRide.bookingStatus === 'BOOKED' ? 'New Request' : 'Ride Started'}
                                        </span>
                                        <h2 className="text-4xl font-black tracking-tighter italic uppercase">Current Ride</h2>
                                    </div>
                                    <div className="text-left sm:text-right">
                                        <p className="text-[10px] font-black text-gray-600 uppercase mb-1">Estimated Fare</p>
                                        <p className="text-4xl font-black text-[#F7D100] tracking-tighter italic">₹{Math.round(activeRide.fare)}</p>
                                    </div>
                                </div>

                                <div className="grid md:grid-cols-2 gap-10 mb-12">
                                    <div className="space-y-8 relative">
                                        <div className="absolute left-[11px] top-6 bottom-6 w-[1px] bg-gray-800"></div>
                                        <div className="flex items-center relative z-10">
                                            <div className="w-6 h-6 bg-black border-2 border-[#F7D100] rounded-full flex items-center justify-center mr-4">
                                                <div className="w-1.5 h-1.5 bg-[#F7D100] rounded-full"></div>
                                            </div>
                                            <div>
                                                <p className="text-[10px] text-gray-600 font-black uppercase">Origin</p>
                                                <p className="text-sm font-bold">{activeRide.sourceLocation}</p>
                                            </div>
                                        </div>
                                        <div className="flex items-center relative z-10">
                                            <div className="w-6 h-6 bg-black border-2 border-gray-800 rounded-full flex items-center justify-center mr-4">
                                                <MapPin size={10} className="text-gray-600" />
                                            </div>
                                            <div>
                                                <p className="text-[10px] text-gray-600 font-black uppercase">Drop Location</p>
                                                <p className="text-sm font-bold">{activeRide.destinationLocation}</p>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="bg-black/40 p-6 rounded-[2rem] border border-white/5">
                                        <p className="text-[10px] font-black text-gray-600 uppercase mb-4">Customer Details</p>
                                        <div className="flex items-center gap-4">
                                            <div className="w-12 h-12 bg-[#F7D100]/10 rounded-2xl flex items-center justify-center border border-[#F7D100]/20">
                                                <User className="text-[#F7D100]" size={20} />
                                            </div>
                                            <div>
                                                <p className="font-black text-lg italic tracking-tight mb-1">{activeRide.customer?.name || 'Customer'}</p>
                                                <p className="text-[10px] font-mono font-bold text-gray-500 uppercase">{activeRide.customer?.mobno || activeRide.customer?.mobile || '—'}</p>
                                                {activeRide.customer?.gender && (
                                                    <p className="text-[9px] font-black text-gray-600 uppercase mt-0.5">{activeRide.customer.gender}</p>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div className="bg-white/5 p-8 rounded-[2.5rem] border border-white/5">
                                    {activeRide.bookingStatus === 'BOOKED' ? (
                                        <div className="space-y-6">
                                            <label className="text-[10px] font-black text-gray-600 uppercase tracking-widest ml-1">Enter Start OTP</label>
                                            <div className="flex flex-col sm:flex-row gap-4">
                                                <input
                                                    type="text"
                                                    placeholder="OTP"
                                                    value={otpInput}
                                                    onChange={(e) => setOtpInput(e.target.value)}
                                                    className="premium-input flex-1 text-center text-3xl font-black tracking-[0.5em] placeholder:tracking-normal placeholder:text-xs"
                                                />
                                                <button onClick={handleStartRide} disabled={loading} className="premium-button px-12 py-5 sm:py-0">START RIDE</button>
                                            </div>
                                            {/* Recording consent prompt */}
                                            {activeRide.recordingConsent === 'REQUESTED' && (
                                                <div className="bg-blue-500/10 border border-blue-500/20 rounded-2xl p-4">
                                                    <p className="text-[10px] font-black text-blue-400 uppercase mb-3">🎙 Passenger Requests Safety Recording</p>
                                                    <p className="text-[9px] text-gray-500 mb-4">Audio stored 24 hrs then auto-deleted. Both parties must consent.</p>
                                                    <div className="flex gap-3">
                                                        <button onClick={() => handleRespondRecording(false)} className="flex-1 bg-white/5 border border-white/10 py-2.5 rounded-xl text-[9px] font-black uppercase text-red-400 hover:bg-red-500/10 transition-all">Decline</button>
                                                        <button onClick={() => handleRespondRecording(true)} className="flex-[2] bg-blue-500/20 border border-blue-500/30 py-2.5 rounded-xl text-[9px] font-black uppercase text-blue-300 hover:bg-blue-500/30 transition-all">Accept Recording</button>
                                                    </div>
                                                </div>
                                            )}
                                            {activeRide.recordingConsent === 'ACTIVE' && (
                                                <div className="flex items-center justify-center gap-2 text-[9px] font-black text-red-400 uppercase py-2 animate-pulse">
                                                    🔴 Recording Active · Auto-deleted in 24h
                                                </div>
                                            )}
                                            <button onClick={handleCancelRide} className="w-full text-center text-red-500/40 hover:text-red-500 transition-colors text-[9px] font-black uppercase tracking-widest">Cancel Ride</button>
                                        </div>
                                    ) : (
                                        <div className="space-y-6">
                                            <div className="flex justify-between items-center mb-2">
                                                <label className="text-[10px] font-black text-[#F7D100] uppercase tracking-widest ml-1">Finish Ride</label>
                                                <button onClick={handleGenerateEndOtp} className="text-[9px] font-black uppercase text-[#F7D100] hover:text-white">Generate OTP</button>
                                            </div>
                                            <div className="flex flex-col sm:flex-row gap-4">
                                                <input
                                                    type="text"
                                                    placeholder="OTP"
                                                    value={otpInput}
                                                    onChange={(e) => setOtpInput(e.target.value)}
                                                    className="premium-input flex-1 text-center text-3xl font-black tracking-[0.5em] placeholder:tracking-normal placeholder:text-xs"
                                                />
                                                <button onClick={handleCompleteRide} disabled={loading} className="bg-[#F7D100] text-black font-black px-12 py-5 sm:py-0 rounded-2xl shadow-xl shadow-[#F7D100]/20 transition-all active:scale-95">FINISH RIDE</button>
                                            </div>
                                        </div>
                                    )}
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </main>

            {/* Console Modal */}
            {showProfile && (
                <div className="fixed inset-0 bg-black/95 backdrop-blur-xl z-50 flex items-center justify-center p-4 animate-fade-in">
                    <div className="glass-card rounded-[3rem] w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col border-[#F7D100]/10 shadow-[0_0_100px_rgba(247,209,0,0.05)]">
                        <div className="bg-[#F7D100] p-10 flex justify-between items-center shrink-0">
                            <div>
                                <h3 className="text-black font-black text-3xl italic tracking-tighter uppercase leading-none">Driver Profile</h3>
                                <p className="text-black/60 text-[10px] font-black uppercase tracking-widest mt-2">{user?.mobile}</p>
                            </div>
                            <button onClick={() => setShowProfile(false)} className="bg-black/10 p-3 rounded-2xl hover:rotate-90 transition-all text-black">
                                <X size={24} />
                            </button>
                        </div>
                        <div className="p-8 sm:p-10 overflow-y-auto custom-scrollbar flex-1">
                            {/* Rich Earnings Dashboard */}
                            {earningsData && (
                                <div className="mb-10 space-y-4">
                                    <h4 className="text-[10px] font-black uppercase tracking-[0.3em] text-gray-600 mb-4">Earnings Overview</h4>
                                    
                                    <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                                        <div className="bg-[#F7D100]/10 border border-[#F7D100]/20 p-5 rounded-[2rem]">
                                            <p className="text-[9px] font-black text-[#F7D100] uppercase tracking-widest mb-1">Total Earnings</p>
                                            <p className="text-3xl font-black italic tracking-tighter text-[#F7D100]">₹{Math.round(earningsData.totalEarnings || 0)}</p>
                                        </div>
                                        <div className="bg-white/5 border border-white/10 p-5 rounded-[2rem]">
                                            <p className="text-[9px] font-black text-gray-400 uppercase tracking-widest mb-1">Trips Completed</p>
                                            <p className="text-3xl font-black tracking-tighter text-white">{earningsData.completedTrips || 0}</p>
                                        </div>
                                        <div className="bg-white/5 border border-white/10 p-5 rounded-[2rem]">
                                            <p className="text-[9px] font-black text-gray-400 uppercase tracking-widest mb-1">Reliability Score</p>
                                            <div className="flex items-baseline gap-1">
                                                <p className={`text-3xl font-black tracking-tighter ${earningsData.reliabilityScore >= 80 ? 'text-green-400' : 'text-red-400'}`}>
                                                    {Math.round(earningsData.reliabilityScore || 100)}%
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                    
                                    <div className="grid grid-cols-2 gap-4 mt-4">
                                        <div className="bg-black/30 border border-white/5 p-4 rounded-2xl flex justify-between items-center">
                                            <span className="text-[9px] font-black text-gray-500 uppercase">Avg. Fare / Trip</span>
                                            <span className="font-bold">₹{Math.round(earningsData.averageFare || 0)}</span>
                                        </div>
                                        <div className="bg-black/30 border border-white/5 p-4 rounded-2xl flex justify-between items-center">
                                            <span className="text-[9px] font-black text-gray-500 uppercase">Cancelled Trips</span>
                                            <span className="font-bold text-red-400">{earningsData.cancelledTrips || 0}</span>
                                        </div>
                                    </div>
                                </div>
                            )}

                            <div className="flex items-center gap-3 mb-6">
                                <TrendingUp size={16} className="text-[#F7D100]" />
                                <h4 className="font-black uppercase tracking-[0.3em] text-[10px] text-gray-600">Ride History</h4>
                                <span className="ml-auto text-[10px] font-black text-[#F7D100] border border-[#F7D100]/30 px-3 py-1 rounded-full uppercase">{bookingHistory.length} Rides</span>
                            </div>

                            <div className="space-y-6">
                                {historyLoading ? (
                                    <div className="py-20 flex justify-center"><div className="w-8 h-8 border-2 border-[#F7D100] border-t-transparent rounded-full animate-spin"></div></div>
                                ) : bookingHistory.length === 0 ? (
                                    <div className="py-20 text-center opacity-30 flex flex-col items-center">
                                        <XCircle size={40} className="mb-4" />
                                        <p className="text-[10px] font-black uppercase tracking-widest text-gray-700">No history found</p>
                                    </div>
                                ) : (
                                    groupByMonth(bookingHistory).map((group) => (
                                        <div key={group.label}>
                                            <div className="flex items-center justify-between mb-3">
                                                <span className="text-[9px] font-black text-gray-600 uppercase tracking-widest">{group.label}</span>
                                                <div className="flex items-center gap-3">
                                                    <span className="text-[9px] font-black text-gray-600">{group.rides.length} rides</span>
                                                    <span className="text-[9px] font-black text-[#F7D100]">₹{Math.round(group.total)}</span>
                                                </div>
                                            </div>
                                            <div className="space-y-3">
                                                {group.rides.map((ride, idx) => (
                                                    <div key={idx} className="bg-white/5 border border-white/5 p-5 rounded-[1.5rem] hover:border-[#F7D100]/20 transition-all">
                                                        <div className="flex justify-between items-center">
                                                            <div className="space-y-2">
                                                                <div className="flex items-center gap-2"><div className="w-2 h-2 bg-[#F7D100] rounded-full"></div><p className="text-xs font-bold text-gray-500">{ride.sourceLocation}</p></div>
                                                                <div className="flex items-center gap-2"><div className="w-2 h-2 bg-gray-800 rounded-full"></div><p className="text-xs font-bold text-gray-500">{ride.destinationLocation}</p></div>
                                                                <div className="flex gap-3 pt-1">
                                                                    <span className="text-[9px] font-black text-gray-700 uppercase">{ride.distance} km</span>
                                                                    <span className="text-[9px] font-black text-[#F7D100] uppercase">{ride.bookingStatus}</span>
                                                                </div>
                                                            </div>
                                                            <div className="text-right">
                                                                <p className="text-2xl font-black italic tracking-tighter mb-1">₹{Math.round(ride.fare)}</p>
                                                                <p className="text-[9px] font-black text-gray-600 uppercase">Earned</p>
                                                            </div>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Cancel Modal */}
            {showCancelModal && (
                <div className="fixed inset-0 bg-black/90 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="glass-card rounded-[2rem] w-full max-w-sm p-8 border-red-500/20">
                        <h3 className="text-xl font-black italic mb-2 text-red-400 uppercase">Cancel Ride</h3>
                        <p className="text-gray-400 text-[10px] uppercase font-bold tracking-widest mb-6">Select a reason</p>
                        <div className="space-y-3 mb-8">
                            {['Customer not at pickup', 'Wrong address entered', 'Vehicle issue / Emergency', 'Other'].map(r => (
                                <button key={r} onClick={() => setCancelReason(r)} className={`w-full text-left px-4 py-3 rounded-xl border transition-all text-xs font-bold uppercase tracking-wider ${cancelReason === r ? 'bg-red-500/20 border-red-500/50 text-red-400' : 'bg-white/5 border-white/5 hover:bg-white/10'}`}>
                                    {r}
                                </button>
                            ))}
                        </div>
                        <div className="flex gap-3">
                            <button onClick={() => setShowCancelModal(false)} className="flex-1 px-4 py-3 rounded-xl bg-white/5 text-[10px] font-black uppercase tracking-widest hover:bg-white/10 transition-colors">Go Back</button>
                            <button onClick={submitCancelRide} className="flex-1 px-4 py-3 rounded-xl bg-red-500/20 text-red-500 border border-red-500/30 text-[10px] font-black uppercase tracking-widest hover:bg-red-500/40 transition-colors">Confirm Cancel</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default DriverDashboard;
