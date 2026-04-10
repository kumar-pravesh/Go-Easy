import { useState, useEffect } from 'react';
import axios from 'axios';
import API_BASE_URL from '../api';
import { MapPin, Navigation, Clock, XCircle, Car, User, CreditCard, Banknote, History, X, LogOut, LocateFixed } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import logo from '../assets/logo.svg';

const UserHome = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [source, setSource] = useState('');
    const [dest, setDest] = useState('');
    const [vehicles, setVehicles] = useState([]);
    const [loading, setLoading] = useState(false);
    const [activeRide, setActiveRide] = useState(null);
    const [paymentMode, setPaymentMode] = useState('CASH');
    const [showProfile, setShowProfile] = useState(false);
    const [bookingHistory, setBookingHistory] = useState([]);
    const [historyLoading, setHistoryLoading] = useState(false);
    const [locLoading, setLocLoading] = useState(false);

    useEffect(() => {
        if (!user?.mobile) return;
        const fetchActiveRide = async () => {
            try {
                const res = await axios.get(`http://localhost:8080/customer/activeBooking?mobNo=${user.mobile}`);
                const ride = res.data.data;
                if (res.data.statusCode === 200 && ride && (ride.id || ride.bookingId)) {
                    setActiveRide(ride);
                } else if (res.data.statusCode === 200 && !ride) {
                    setActiveRide(null);
                }
            } catch (e) {
                console.log("No active ride found");
            }
        };
        fetchActiveRide();
        const interval = setInterval(fetchActiveRide, 3000);
        return () => clearInterval(interval);
    }, [user]);

    const fetchBookingHistory = async () => {
        setHistoryLoading(true);
        try {
            const res = await axios.get(`http://localhost:8080/customer/seeBookingHistory?mobNo=${user.mobile}`);
            if (res.data.statusCode === 200) {
                setBookingHistory(res.data.data?.rlist || []);
            }
        } catch (e) {
            console.log("Failed to fetch history");
        } finally {
            setHistoryLoading(false);
        }
    };

    const handleGetCurrentLocation = () => {
        if (!navigator.geolocation) return alert("Geolocation not supported");
        setLocLoading(true);
        navigator.geolocation.getCurrentPosition(async (pos) => {
            const { latitude, longitude } = pos.coords;
            try {
                const res = await axios.get(`http://localhost:8080/customer/getCity?lat=${latitude}&lon=${longitude}`);
                if (res.data.statusCode === 200) setSource(res.data.data);
            } catch (error) {
                alert("Failed to get current city");
            } finally {
                setLocLoading(false);
            }
        }, () => {
            setLocLoading(false);
            alert("Location access denied");
        });
    };

    const checkAvailability = async () => {
        if (!source || !dest) return alert("Please enter both locations");
        setLoading(true);
        setVehicles([]);
        try {
            const mobile = user?.mobile;
            const res = await axios.get(`${API_BASE_URL}/availableVehicles?mobile=${mobile}&destination=${dest}`);
            if (res.data.statusCode === 200) {
                const vehicleList = res.data.data?.vehicles || [];
                setVehicles(vehicleList);
                if (vehicleList.length === 0) alert("No vehicles available");
            }
        } catch (error) {
            alert("Error searching vehicles");
        } finally {
            setLoading(false);
        }
    };

    const bookVehicle = async (vehicle) => {
        try {
            const payload = {
                sourceLocation: source,
                destinationLocation: dest,
                distance: vehicle.distance || 5.0,
                fare: vehicle.estimatedFare,
                estimatedTime: Math.round(vehicle.estimatedTime) + " mins",
                vehicleNumber: vehicle.vehicleNumber,
                paymentMode: paymentMode 
            };
            const res = await axios.post(`${API_BASE_URL}/booking/bookvehicle?mobno=${user.mobile}`, payload);
            if (res.data.statusCode === 201 || res.data.statusCode === 200) {
                setActiveRide(res.data.data);
                setVehicles([]);
            }
        } catch (error) {
            alert("Booking Failed: " + (error.response?.data?.message || "Internal error"));
        }
    };

    const handleCancelRide = async () => {
        if (!activeRide || !confirm("Cancel this ride?")) return;
        try {
            const bookingId = activeRide.id || activeRide.bookingId;
            await axios.post(`${API_BASE_URL}/customer/cancelRide?bookingId=${bookingId}`);
            setActiveRide(null);
        } catch (e) {
            alert("Cancel failed");
        }
    };

    const rideStatus = activeRide?.bookingStatus;
    const isRideBooked = rideStatus === 'BOOKED';
    const isRideOngoing = rideStatus === 'ONGOING';
    const isRideCompleted = rideStatus === 'COMPLETED';

    const formatTime = (minutes) => {
        if (!minutes) return "0m";
        const h = Math.floor(minutes / 60);
        const m = Math.round(minutes % 60);
        return h > 0 ? `${h}h ${m}m` : `${m}m`;
    };

    return (
        <div className="min-h-screen bg-[#0A0A0A] text-white selection:bg-[#F7D100] selection:text-black">
            {/* Header */}
            <header className="fixed top-0 left-0 right-0 z-40 bg-black/60 backdrop-blur-xl border-b border-white/5 px-4 h-16 sm:h-20 flex items-center justify-center">
                <div className="w-full max-w-7xl flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <img src={logo} alt="Go-Easy" className="h-12 sm:h-14 w-auto object-contain" />
                    </div>
                    <div className="flex items-center gap-4">
                        <button 
                            onClick={() => { setShowProfile(true); fetchBookingHistory(); }}
                            className="flex items-center gap-2 bg-white/5 hover:bg-white/10 px-4 py-2 rounded-xl border border-white/5 transition-all text-sm font-bold"
                        >
                            <User size={16} className="text-[#F7D100]" />
                            <span className="hidden sm:inline">My History</span>
                        </button>
                        <button 
                            onClick={() => { logout(); navigate('/'); }}
                            className="flex items-center gap-2 bg-red-500/10 hover:bg-red-500/20 text-red-500 px-4 py-2 rounded-xl border border-red-500/10 transition-all text-sm font-bold"
                        >
                            <LogOut size={16} />
                            <span className="hidden sm:inline">Sign Out</span>
                        </button>
                    </div>
                </div>
            </header>

            <main className="pt-24 sm:pt-32 pb-20 px-4 max-w-7xl mx-auto">
                <div className="grid lg:grid-cols-12 gap-8 items-start">
                    
                    {/* LEFT AREA: Search & Progress */}
                    <div className="lg:col-span-5 xl:col-span-4 space-y-6">
                        {!activeRide ? (
                            <div className="glass-card rounded-3xl p-6 sm:p-8 animate-fade-in">
                                <h2 className="text-xl font-black uppercase tracking-widest mb-8 text-[#F7D100]">Book Your Ride</h2>
                                <div className="space-y-6">
                                    <div className="space-y-4">
                                        <div className="space-y-2">
                                            <label className="text-[10px] font-black uppercase tracking-widest text-gray-500 ml-1">Pick-Up Location</label>
                                            <div className="flex gap-2">
                                                <div className="relative flex-1">
                                                    <MapPin className="absolute left-4 top-1/2 -translate-y-1/2 text-[#F7D100]" size={16} />
                                                    <input readOnly className="premium-input pl-12 text-sm" value={source || "Finding your location..."} />
                                                </div>
                                                <button onClick={handleGetCurrentLocation} disabled={locLoading} className="w-14 h-14 bg-white/5 border border-white/5 rounded-2xl flex items-center justify-center hover:bg-[#F7D100]/10 transition-colors">
                                                    {locLoading ? <div className="w-4 h-4 border-2 border-[#F7D100] border-t-transparent rounded-full animate-spin"></div> : <LocateFixed size={20} className="text-[#F7D100]" />}
                                                </button>
                                            </div>
                                        </div>
                                        <div className="space-y-2">
                                            <label className="text-[10px] font-black uppercase tracking-widest text-gray-500 ml-1">Drop Location</label>
                                            <div className="relative">
                                                <Navigation className="absolute left-4 top-1/2 -translate-y-1/2 text-[#F7D100]" size={16} />
                                                <input value={dest} onChange={(e) => setDest(e.target.value)} className="premium-input pl-12 text-sm" placeholder="Where do you want to go?" />
                                            </div>
                                        </div>
                                    </div>



                                    <button onClick={checkAvailability} disabled={loading} className="premium-button w-full">
                                        {loading ? "SEARCHING..." : "FIND AVAILABLE DRIVERS"}
                                    </button>
                                </div>
                            </div>
                        ) : (
                            <div className="glass-card rounded-3xl p-8 relative overflow-hidden animate-fade-in border-[#F7D100]/20 shadow-[0_0_50px_rgba(247,209,0,0.05)]">
                                <div className="relative z-10">
                                    <div className="flex justify-between items-center mb-8">
                                        <h3 className="text-xl font-black italic tracking-tighter">Current <span className="text-[#F7D100]">Ride</span></h3>
                                        <div className="text-right">
                                            <p className="text-[10px] font-black text-gray-600 uppercase">Ride ID</p>
                                            <p className="text-xs font-bold text-gray-400">#{activeRide.id || activeRide.bookingId}</p>
                                        </div>
                                    </div>

                                    <div className="space-y-8 relative mb-10">
                                        <div className="absolute left-[11px] top-6 bottom-6 w-[1px] bg-gradient-to-b from-[#F7D100] via-gray-800 to-gray-900 border-none"></div>
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
                                                <p className="text-5xl font-black tracking-[0.3em] mb-4">{activeRide.startOtp || "----"}</p>
                                                <p className="text-[10px] font-bold opacity-60 px-4">Give this OTP to your driver to start the ride.</p>
                                            </>
                                        )}
                                        {isRideOngoing && (
                                            <>
                                                <div className="w-12 h-12 bg-[#F7D100]/10 rounded-full flex items-center justify-center mx-auto mb-4">
                                                    <Car className="text-[#F7D100] animate-bounce" size={24} />
                                                </div>
                                                <p className="font-black text-xl italic tracking-tight mb-2">Driver is on the way</p>
                                                <p className="text-sm text-gray-400">ETA: {activeRide.estimatedTime || "Recalculating..."}</p>
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
                                                <p className="font-black text-xl italic tracking-tight mb-2">Arrival Success</p>
                                                <p className="text-sm text-gray-500 mb-6">Movement sequence completed.</p>
                                                <div className="bg-black/20 p-4 rounded-2xl border border-white/5">
                                                    <p className="text-[10px] font-black text-gray-500 uppercase">Total Fare Paid</p>
                                                    <p className="text-4xl font-black text-[#F7D100]">₹{activeRide.fare}</p>
                                                </div>
                                            </>
                                        )}
                                    </div>

                                    {isRideBooked && (
                                        <button onClick={handleCancelRide} className="w-full flex items-center justify-center gap-2 text-gray-600 hover:text-red-500 text-[10px] font-black uppercase tracking-widest py-2 transition-colors">
                                            <XCircle size={14} /> Cancel Ride
                                        </button>
                                    )}
                                </div>
                            </div>
                        )}
                    </div>

                    {/* RIGHT AREA: Find Drivers & Discovery */}
                    <div className="lg:col-span-7 xl:col-span-8">
                        {!activeRide && vehicles.length === 0 && (
                            <div className="bg-white/5 border border-white/5 rounded-[3rem] p-12 text-center h-full flex flex-col items-center justify-center min-h-[400px]">
                                <div className="w-20 h-20 bg-black rounded-3xl flex items-center justify-center mb-6 shadow-inner border border-white/5">
                                    <Navigation className="text-gray-800" size={32} />
                                </div>
                                <h3 className="text-xl font-black text-gray-400 italic">No Ride Requested</h3>
                                <p className="text-gray-600 text-sm mt-2 font-medium">Enter your drop location to find available drivers.</p>
                            </div>
                        )}

                        {!activeRide && vehicles.length > 0 && (
                            <div className="space-y-4 animate-fade-in">
                                <div className="flex items-center justify-between mb-8 pl-2">
                                    <h3 className="text-[10px] font-black uppercase tracking-[0.4em] text-gray-500">Available Drivers</h3>
                                    <span className="bg-[#F7D100]/10 text-[#F7D100] px-3 py-1 rounded-full text-[10px] font-black border border-[#F7D100]/20">{vehicles.length} Drivers</span>
                                </div>
                                <div className="grid sm:grid-cols-2 xl:grid-cols-3 gap-4">
                                    {vehicles.map((v) => (
                                        <div key={v.vehicleNumber} className="glass-card p-6 rounded-[2rem] hover:border-[#F7D100]/30 transition-all group cursor-pointer" onClick={() => bookVehicle(v)}>
                                            <div className="flex justify-between items-start mb-6">
                                                <div className="w-14 h-14 bg-black border border-white/5 rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform">
                                                    <Car className="text-[#F7D100]/60 group-hover:text-[#F7D100]" size={28} />
                                                </div>
                                                <div className="text-right">
                                                    <p className="text-[10px] font-black text-gray-600 uppercase">Fare</p>
                                                    <p className="text-2xl font-black tracking-tighter">₹{Math.round(v.estimatedFare)}</p>
                                                </div>
                                            </div>
                                            <h4 className="text-lg font-black tracking-tight mb-2 italic">{v.model}</h4>
                                            <div className="flex items-center gap-4 text-gray-500">
                                                <div className="flex items-center gap-1.5 text-[10px] font-bold">
                                                    <Clock size={12} className="text-[#F7D100]" /> {formatTime(v.estimatedTime)}
                                                </div>
                                                <div className="flex items-center gap-1.5 text-[10px] font-bold">
                                                    <Navigation size={12} className="text-gray-400" /> {v.distance?.toFixed(1) || "5.0"} km
                                                </div>
                                            </div>
                                            <button onClick={() => bookVehicle(v)} className="w-full mt-6 bg-white/5 border border-white/10 group-hover:bg-[#F7D100] group-hover:text-black py-2.5 rounded-xl text-[10px] font-black uppercase tracking-widest transition-all">
                                                BOOK RIDE
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
                                        <div className="relative">
                                            <Navigation size={48} className="text-[#F7D100] animate-pulse" />
                                            <div className="absolute -top-1 -right-1 w-3 h-3 bg-[#F7D100] rounded-full"></div>
                                        </div>
                                    </div>
                                    <h2 className="text-3xl font-black italic tracking-tighter mb-4 uppercase">Sector Intelligence Active</h2>
                                    <p className="text-gray-500 max-w-sm mx-auto font-medium leading-relaxed">The Go-Easy grid is tracking your unit's movement across the sector in real-time. Safety protocols fully engaged.</p>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            </main>

            {/* Profile Modal */}
            {showProfile && (
                <div className="fixed inset-0 bg-black/90 backdrop-blur-md z-50 flex items-center justify-center p-4 animate-fade-in">
                    <div className="glass-card rounded-[3rem] w-full max-w-2xl max-h-[90vh] overflow-hidden flex flex-col shadow-[0_0_100px_rgba(247,209,0,0.1)]">
                        <div className="bg-[#F7D100] p-8 sm:p-12 flex justify-between items-center shrink-0">
                            <div>
                                <h3 className="text-black font-black text-3xl italic tracking-tighter uppercase">Passenger Terminal</h3>
                                <div className="flex items-center gap-2 mt-2">
                                    <span className="text-black font-black text-[10px] uppercase tracking-widest bg-black/10 px-2 py-0.5 rounded">Verified ID</span>
                                    <p className="text-black/60 text-xs font-bold font-mono tracking-tight">{user?.mobile}</p>
                                </div>
                            </div>
                            <button onClick={() => setShowProfile(false)} className="bg-black/10 p-3 rounded-2xl hover:rotate-90 transition-all text-black">
                                <X size={24} />
                            </button>
                        </div>
                        <div className="p-8 sm:p-12 overflow-y-auto custom-scrollbar flex-1">
                            <div className="flex items-center justify-between mb-8">
                                <div className="flex items-center gap-3">
                                    <History size={18} className="text-[#F7D100]" />
                                    <h4 className="font-black uppercase tracking-[0.3em] text-[10px] text-gray-500">Service Logs</h4>
                                </div>
                                <span className="text-[10px] font-black text-[#F7D100] border border-[#F7D100]/30 px-3 py-1 rounded-full uppercase">{bookingHistory.length} Movement Actions</span>
                            </div>

                            <div className="space-y-4">
                                {historyLoading ? (
                                    <div className="py-20 flex justify-center"><div className="w-8 h-8 border-2 border-[#F7D100] border-t-transparent rounded-full animate-spin"></div></div>
                                ) : bookingHistory.length === 0 ? (
                                    <div className="py-20 text-center opacity-30 flex flex-col items-center">
                                        <XCircle size={40} className="mb-4" />
                                        <p className="text-[10px] font-black uppercase tracking-widest">No sector movement recorded</p>
                                    </div>
                                ) : (
                                    bookingHistory.map((ride, idx) => (
                                        <div key={idx} className="bg-white/5 border border-white/5 p-6 rounded-[2rem] hover:border-[#F7D100]/20 transition-all group">
                                            <div className="flex justify-between items-center">
                                                <div className="space-y-3">
                                                    <div className="flex items-center gap-3">
                                                        <div className="w-2 h-2 bg-[#F7D100] rounded-full"></div>
                                                        <p className="text-xs font-bold text-gray-400">{ride.sourceLocation}</p>
                                                    </div>
                                                    <div className="flex items-center gap-3">
                                                        <div className="w-2 h-2 bg-gray-700 rounded-full"></div>
                                                        <p className="text-xs font-bold text-gray-400">{ride.destinationLocation}</p>
                                                    </div>
                                                    <div className="pt-2 flex gap-4">
                                                        <span className="text-[9px] font-black text-gray-600 uppercase tracking-widest">{ride.distance} KM Vector</span>
                                                        <span className="text-[9px] font-black text-[#F7D100] uppercase tracking-widest">{ride.bookingStatus}</span>
                                                    </div>
                                                </div>
                                                <div className="text-right">
                                                    <p className="text-2xl font-black italic tracking-tighter text-[#F7D100]">₹{Math.round(ride.fare)}</p>
                                                    <p className="text-[9px] font-black text-gray-600 uppercase">Credits Transfered</p>
                                                </div>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserHome;
