import { useState, useEffect } from 'react';
import axios from 'axios';
import { MapPin, Navigation, Clock, XCircle, Car, User, CreditCard, Banknote, History, X } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import logo from '../assets/logo.png';

const UserHome = () => {
    const { user } = useAuth();
    const [source, setSource] = useState('');
    const [dest, setDest] = useState('');
    const [vehicles, setVehicles] = useState([]);
    const [loading, setLoading] = useState(false);
    const [activeRide, setActiveRide] = useState(null);
    const [paymentMode, setPaymentMode] = useState('CASH'); // CASH or UPI
    const [showProfile, setShowProfile] = useState(false);
    const [bookingHistory, setBookingHistory] = useState([]);
    const [historyLoading, setHistoryLoading] = useState(false);
    const [locLoading, setLocLoading] = useState(false);

    // Poll for active ride on mount
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

    // Fetch booking history
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
        if (!navigator.geolocation) {
            return alert("Geolocation is not supported by your browser");
        }
        setLocLoading(true);
        navigator.geolocation.getCurrentPosition(async (position) => {
            const { latitude, longitude } = position.coords;
            try {
                const res = await axios.get(`http://localhost:8080/customer/getCity?lat=${latitude}&lon=${longitude}`);
                if (res.data.statusCode === 200) {
                    setSource(res.data.data);
                }
            } catch (error) {
                console.error("Location error:", error);
                alert("Failed to get current city");
            } finally {
                setLocLoading(false);
            }
        }, (error) => {
            setLocLoading(false);
            alert("Location access denied or unavailable");
        });
    };

    // Fetch Vehicles
    const checkAvailability = async () => {
        if (!source || !dest) return alert("Please enter both locations");
        setLoading(true);
        setVehicles([]);
        try {
            const mobile = user?.mobile;
            const res = await axios.get(`http://localhost:8080/availableVehicles?mobile=${mobile}&destination=${dest}`);
            if (res.data.statusCode === 200) {
                const vehicleList = res.data.data?.vehicles || [];
                setVehicles(vehicleList);
                if (vehicleList.length === 0) {
                    alert("No vehicles available in your area. Please try again later.");
                }
            }
        } catch (error) {
            console.error("Vehicle search error:", error);
            const errorMessage = error.response?.data?.message || error.message || "Error fetching vehicles";
            alert(`Failed to Fetch Vehicles: ${errorMessage}`);
        } finally {
            setLoading(false);
        }
    };

    const bookVehicle = async (vehicle) => {
        try {
            const mobile = user?.mobile;
            const payload = {
                sourceLocation: source,
                destinationLocation: dest,
                distance: vehicle.distance || 5.0,
                fare: vehicle.estimatedFare,
                estimatedTime: Math.round(vehicle.estimatedTime) + " mins",
                vehicleNumber: vehicle.vehicleNumber,
                paymentMode: paymentMode // Include payment mode
            };
            const res = await axios.post(`http://localhost:8080/booking/bookvehicle?mobno=${mobile}`, payload);
            if (res.data.statusCode === 201 || res.data.statusCode === 200) {
                setActiveRide(res.data.data);
                setVehicles([]);
                alert("Booking Successful!");
            }
        } catch (error) {
            console.error(error);
            const errorMessage = error.response?.data?.message || "Booking Failed";
            alert(`Booking Failed: ${errorMessage}`);
        }
    };

    // Cancel ride by customer
    const handleCancelRide = async () => {
        if (!activeRide) return;
        if (!confirm("Are you sure you want to cancel this ride?")) return;
        try {
            const bookingId = activeRide.id || activeRide.bookingId;
            await axios.post(`http://localhost:8080/customer/cancellRide?bookingId=${bookingId}`);
            alert("Ride Cancelled Successfully!");
            setActiveRide(null);
        } catch (e) {
            const msg = e.response?.data?.message || e.message || "Cancel failed";
            alert("Error: " + msg);
        }
    };

    // Get ride status
    const rideStatus = activeRide?.bookingStatus;
    const isRideBooked = rideStatus === 'BOOKED';
    const isRideOngoing = rideStatus === 'ONGOING';
    const isRideCompleted = rideStatus === 'COMPLETED';

    return (
        <div className="min-h-screen bg-gray-100 p-4">
            {/* Brand Header */}
            <div className="flex justify-between items-center mb-6">
                <div className="flex items-center gap-1.5">
                    <img src={logo} alt="Go-Easy" className="h-12 w-auto object-contain mix-blend-multiply" />
                    <h1 className="text-3xl font-black italic tracking-tighter flex items-center" style={{ fontFamily: "'Lexend', sans-serif" }}>
                        <span className="text-[#2F3C8F]">Go</span>
                        <span className="text-[#3E6FA6]">Easy</span>
                    </h1>
                </div>
                <button
                    onClick={() => { setShowProfile(true); fetchBookingHistory(); }}
                    className="bg-[#5D5FEF] text-white px-5 py-2.5 rounded-full flex items-center gap-2 hover:bg-[#4B4DDF] transition shadow-lg shadow-indigo-100 font-bold"
                >
                    <User size={18} />
                    Profile
                </button>
            </div>

            <h2 className="text-xl font-bold text-gray-800 mb-4 px-1">
                {activeRide ? (
                    isRideCompleted ? "Ride Completed" :
                        isRideOngoing ? "Ride in Progress" : "Ride Booked"
                ) : "Where to?"}
            </h2>

            {/* Profile Modal */}
            {showProfile && (
                <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
                    <div className="bg-white rounded-2xl w-full max-w-md max-h-[80vh] overflow-hidden">
                        <div className="bg-accent p-4 flex justify-between items-center">
                            <h3 className="text-white font-bold text-lg">My Profile</h3>
                            <button onClick={() => setShowProfile(false)} className="text-white">
                                <X size={24} />
                            </button>
                        </div>
                        <div className="p-4">
                            {/* User Info */}
                            <div className="bg-gray-50 rounded-xl p-4 mb-4">
                                <div className="flex items-center gap-3">
                                    <div className="w-12 h-12 bg-accent rounded-full flex items-center justify-center">
                                        <User className="text-white" size={24} />
                                    </div>
                                    <div>
                                        <p className="font-bold text-lg">{user?.name || "Customer"}</p>
                                        <p className="text-gray-500 text-sm">{user?.mobile}</p>
                                    </div>
                                </div>
                            </div>

                            {/* Booking History */}
                            <div className="flex items-center gap-2 mb-3">
                                <History size={18} className="text-gray-500" />
                                <h4 className="font-bold">Ride History</h4>
                            </div>

                            <div className="space-y-3 max-h-60 overflow-y-auto">
                                {historyLoading ? (
                                    <p className="text-center text-gray-500">Loading...</p>
                                ) : bookingHistory.length === 0 ? (
                                    <p className="text-center text-gray-500">No previous rides</p>
                                ) : (
                                    bookingHistory.map((ride, idx) => (
                                        <div key={idx} className="bg-gray-50 rounded-lg p-3 border">
                                            <div className="flex justify-between items-start">
                                                <div>
                                                    <p className="text-sm font-medium">{ride.sourceLocation} → {ride.destinationLocation}</p>
                                                    <p className="text-xs text-gray-500">{ride.distance} km • {ride.bookingStatus}</p>
                                                </div>
                                                <p className="font-bold text-accent">₹{Math.round(ride.fare)}</p>
                                            </div>
                                        </div>
                                    ))
                                )}
                            </div>
                        </div>
                    </div>
                </div>
            )}

            {/* Show Booking Form ONLY if no active ride */}
            {!activeRide && (
                <>
                    <div className="bg-white p-6 rounded-xl shadow-md mb-6">
                        <div className="relative">

                            {/* Refined Pickup Selection */}
                            <div className="mb-4">
                                <div className="flex items-center gap-2">
                                    <div className="relative flex-1 group">
                                        <MapPin className="absolute left-3 top-4 text-accent" size={20} />
                                        <div
                                            className="w-full pl-10 pr-4 py-4 bg-gray-50/50 rounded-xl border-2 border-dashed border-gray-200 min-h-[56px] flex items-center"
                                        >
                                            <span className={`text-lg font-medium transition-colors ${source ? 'text-gray-900' : 'text-gray-400'}`}>
                                                {source || "Set current location"}
                                            </span>
                                        </div>
                                    </div>
                                    <button
                                        onClick={handleGetCurrentLocation}
                                        disabled={locLoading}
                                        className="h-14 w-14 bg-accent/10 hover:bg-accent/20 text-2xl flex items-center justify-center rounded-xl transition-all border-2 border-accent/20 active:scale-95 shadow-sm"
                                        title="Fetch Current Location"
                                    >
                                        {locLoading ? (
                                            <div className="w-6 h-6 border-3 border-accent border-t-transparent rounded-full animate-spin"></div>
                                        ) : (
                                            "📍"
                                        )}
                                    </button>
                                </div>
                                <p className="text-[10px] text-gray-400 mt-1 ml-1 font-bold uppercase tracking-wider">Pickup Point</p>
                            </div>

                            <div className="relative">
                                <Navigation className="absolute left-3 top-4 text-black" size={20} />
                                <input
                                    className="w-full pl-10 pr-4 py-4 bg-gray-50 rounded-xl border-2 border-gray-100 focus:border-black focus:ring-0 outline-none transition-all text-lg font-medium placeholder:text-gray-300"
                                    placeholder="Where to?"
                                    value={dest}
                                    onChange={(e) => setDest(e.target.value)}
                                />
                                <p className="text-[10px] text-gray-400 mt-1 ml-1 font-bold uppercase tracking-wider">Destination</p>
                            </div>
                        </div>

                        {/* Payment Mode Selection */}
                        <div className="mt-6">
                            <p className="text-xs font-black text-gray-400 uppercase tracking-[0.2em] mb-3 ml-1">Payment Method</p>
                            <div className="flex gap-3">
                                <button
                                    onClick={() => setPaymentMode('CASH')}
                                    className={`flex-1 py-4 rounded-xl border-2 flex items-center justify-center gap-3 transition-all font-bold ${paymentMode === 'CASH'
                                        ? 'border-accent bg-accent/5 text-accent shadow-sm'
                                        : 'border-gray-50 bg-gray-50/50 text-gray-400'
                                        }`}
                                >
                                    <Banknote size={20} />
                                    Cash
                                </button>
                                <button
                                    onClick={() => setPaymentMode('UPI')}
                                    className={`flex-1 py-4 rounded-xl border-2 flex items-center justify-center gap-3 transition-all font-bold ${paymentMode === 'UPI'
                                        ? 'border-accent bg-accent/5 text-accent shadow-sm'
                                        : 'border-gray-50 bg-gray-50/50 text-gray-400'
                                        }`}
                                >
                                    <CreditCard size={20} />
                                    UPI
                                </button>
                            </div>
                        </div>

                        <button
                            onClick={checkAvailability}
                            className="w-full mt-6 bg-black text-white py-4 rounded-xl font-black uppercase tracking-widest hover:bg-gray-900 transition-all active:scale-[0.98] shadow-lg shadow-gray-200"
                        >
                            {loading ? (
                                <div className="flex items-center justify-center gap-2">
                                    <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin"></div>
                                    <span>Searching...</span>
                                </div>
                            ) : 'Find Vehicles'}
                        </button>
                    </div>

                    {/* Vehicle List */}
                    <div className="space-y-4 pb-20">
                        {vehicles.map((v) => (
                            <div key={v.vehicleNumber} className="bg-white p-5 rounded-2xl shadow-sm hover:shadow-md transition-all flex justify-between items-center border border-gray-100 group">
                                <div className="flex items-center">
                                    <div className="w-20 h-16 bg-gray-50 rounded-2xl mr-4 flex items-center justify-center group-hover:bg-accent/5 transition-colors">
                                        <Car size={32} className="text-gray-400 group-hover:text-accent transition-colors" />
                                    </div>
                                    <div>
                                        <h3 className="font-black text-xl tracking-tight text-gray-900">{v.model}</h3>
                                        <div className="flex items-center text-gray-500 text-xs font-bold gap-4 mt-1">
                                            <span className="flex items-center gap-1">
                                                <Clock size={14} /> {Math.round(v.estimatedTime)}m
                                            </span>
                                            <span className="flex items-center gap-1 text-accent">
                                                <Navigation size={14} /> {v.distance?.toFixed(1) || "5.0"} km
                                            </span>
                                        </div>
                                    </div>
                                </div>
                                <div className="text-right">
                                    <div className="font-black text-2xl text-gray-900">₹{Math.round(v.estimatedFare)}</div>
                                    <button
                                        onClick={() => bookVehicle(v)}
                                        className="mt-2 text-xs font-black uppercase tracking-wider bg-accent text-white px-6 py-2 rounded-full hover:bg-emerald-600 transition-colors shadow-lg shadow-emerald-100"
                                    >
                                        Book Now
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                </>
            )}

            {/* Active Ride Panel */}
            {activeRide && (
                <div className="bg-white rounded-3xl shadow-xl p-6 border border-gray-100 animate-in fade-in slide-in-from-bottom-4 duration-500">
                    {/* Ride Status Badge */}
                    <div className="flex justify-between items-center mb-6">
                        <span className={`px-4 py-1.5 rounded-full text-xs font-black uppercase tracking-widest ${isRideCompleted ? 'bg-blue-50 text-blue-600' :
                            isRideOngoing ? 'bg-emerald-50 text-emerald-600' : 'bg-amber-50 text-amber-600'}`}>
                            {isRideCompleted ? '🏁 Trip Finished' : isRideOngoing ? '🚗 In Route' : '⏳ Waiting for Driver'}
                        </span>
                        <span className="text-[10px] font-bold text-gray-300 uppercase tracking-tighter">ID: #{activeRide.id || activeRide.bookingId}</span>
                    </div>

                    {/* Route Info */}
                    <div className="mb-8 space-y-6 relative">
                        <div className="absolute left-[11px] top-6 bottom-6 w-0.5 bg-gray-50"></div>
                        <div className="flex items-center relative z-10">
                            <div className="w-6 h-6 bg-white border-2 border-emerald-500 rounded-full flex items-center justify-center mr-4">
                                <div className="w-2 h-2 bg-emerald-500 rounded-full"></div>
                            </div>
                            <div>
                                <p className="text-[10px] text-gray-400 font-bold uppercase tracking-widest">Pickup</p>
                                <p className="font-black text-lg text-gray-900">{activeRide.sourceLocation}</p>
                            </div>
                        </div>
                        <div className="flex items-center relative z-10">
                            <div className="w-6 h-6 bg-white border-2 border-red-500 rounded-full flex items-center justify-center mr-4">
                                <MapPin size={12} className="text-red-500" />
                            </div>
                            <div>
                                <p className="text-[10px] text-gray-400 font-bold uppercase tracking-widest">Destination</p>
                                <p className="font-black text-lg text-gray-900">{activeRide.destinationLocation}</p>
                            </div>
                        </div>
                    </div>

                    {/* Driver Info */}
                    <div className="bg-gray-50 rounded-3xl p-5 mb-6 border border-gray-100">
                        <div className="flex items-center justify-between">
                            <div className="flex items-center gap-4">
                                <div className="w-14 h-14 bg-white rounded-2xl flex items-center justify-center shadow-sm border border-gray-50">
                                    <Car className="text-gray-300" size={28} />
                                </div>
                                <div>
                                    <p className="font-black text-gray-900">{activeRide.vehicle?.driver?.dname || "Driver Partner"}</p>
                                    <p className="text-xs font-bold text-gray-400 uppercase tracking-tight">{activeRide.vehicle?.vehicleModel || "Vehicle"} • {activeRide.vehicle?.vehicleNumber}</p>
                                </div>
                            </div>
                            <div className="text-right">
                                <p className="text-xs text-gray-400 font-bold uppercase tracking-widest">Fare</p>
                                <p className="text-2xl font-black text-gray-900">₹{Math.round(activeRide.fare || 0)}</p>
                            </div>
                        </div>
                    </div>

                    {/* Start OTP Section - Only show if ride is BOOKED */}
                    {isRideBooked && (
                        <div className="bg-accent text-white rounded-3xl p-6 mb-6 text-center shadow-lg shadow-emerald-100 relative overflow-hidden group">
                            <div className="absolute top-0 right-0 w-32 h-32 bg-white/10 rounded-full -mr-16 -mt-16 group-hover:scale-110 transition-transform"></div>
                            <p className="text-xs font-black uppercase tracking-[0.2em] opacity-80 mb-3">Share OTP with Driver</p>
                            <div className="text-5xl font-black tracking-[0.4em] text-white">
                                {activeRide.startOtp || "----"}
                            </div>
                        </div>
                    )}

                    {/* Ride Started - Show End OTP when available */}
                    {isRideOngoing && (
                        <div className="bg-emerald-500 text-white rounded-3xl p-6 mb-6 text-center shadow-lg shadow-emerald-100">
                            <p className="font-black text-xl uppercase tracking-tighter mb-2">Trip in Progress!</p>
                            {activeRide.endOtp ? (
                                <>
                                    <p className="text-xs font-bold opacity-80 uppercase tracking-widest mt-4">END TRIP OTP</p>
                                    <div className="text-4xl font-black tracking-[0.3em] mt-1">
                                        {activeRide.endOtp}
                                    </div>
                                </>
                            ) : (
                                <p className="text-xs font-bold opacity-80 uppercase tracking-widest mt-2 animate-pulse">Enjoy your ride! Driver will request End OTP at destination.</p>
                            )}
                        </div>
                    )}

                    {/* Ride Completed - Payment Instruction */}
                    {isRideCompleted && (
                        <div className="bg-indigo-600 text-white rounded-3xl p-6 mb-6 text-center shadow-lg shadow-indigo-100">
                            <p className="font-black text-xl uppercase tracking-tighter mb-1">Arrived safely!</p>
                            <p className="text-xs font-bold opacity-80 mb-6 uppercase tracking-widest">Payment Instruction</p>

                            <div className="bg-white/10 rounded-2xl p-4 inline-block mb-4">
                                <p className="text-4xl font-black">₹{Math.round(activeRide.fare)}</p>
                                <p className="text-[10px] font-black uppercase tracking-widest mt-1">Total via {activeRide.paymentMode || 'CASH'}</p>
                            </div>

                            <p className="text-xs font-bold opacity-60 animate-pulse italic tracking-tight">
                                Awaiting driver's payment confirmation...
                            </p>
                        </div>
                    )}

                    {/* Cancel Button - Only show if ride is BOOKED */}
                    {isRideBooked && (
                        <button
                            onClick={handleCancelRide}
                            className="w-full text-red-500 py-4 py-4 rounded-2xl font-black uppercase tracking-widest hover:bg-red-50 transition-colors flex items-center justify-center gap-2"
                        >
                            <XCircle size={20} />
                            Cancel Request
                        </button>
                    )}
                </div>
            )}
        </div>
    );
};

export default UserHome;
