import { useState, useEffect } from 'react';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';
import { Car, MapPin, Navigation, CheckCircle, Smartphone, Banknote, QrCode, XCircle, History, User, X } from 'lucide-react';
import logo from '../assets/logo.png';

const DriverDashboard = () => {
    const { user } = useAuth();
    const [activeRide, setActiveRide] = useState(null);
    const [otpInput, setOtpInput] = useState('');
    const [status, setStatus] = useState('ONLINE');
    const [loading, setLoading] = useState(false);
    const [showPayment, setShowPayment] = useState(false);
    const [qrCode, setQrCode] = useState(null);

    // Profile & History states
    const [showProfile, setShowProfile] = useState(false);
    const [bookingHistory, setBookingHistory] = useState([]);
    const [historyLoading, setHistoryLoading] = useState(false);

    const driverMobile = user?.mobile || 9000000001;

    // Polling for active booking
    useEffect(() => {
        const fetchActiveBooking = async () => {
            try {
                const res = await axios.get(`http://localhost:8080/driver/activeBooking?mobNo=${driverMobile}`);
                const ride = res.data.data;
                if (res.data.statusCode === 200 && ride && (ride.id || ride.bookingId)) {
                    if (ride.id && !ride.bookingId) {
                        ride.bookingId = ride.id;
                    }
                    setActiveRide(ride);
                } else {
                    setActiveRide(null);
                    setShowPayment(false);
                }
            } catch (error) {
                console.log("No active rides");
            }
        };

        fetchActiveBooking();
        const interval = setInterval(fetchActiveBooking, 3000);
        return () => clearInterval(interval);
    }, [driverMobile]);

    const toggleStatus = async () => {
        const newStatus = status === 'ONLINE' ? 'OFFLINE' : 'ONLINE';
        setLoading(true);
        try {
            const res = await axios.put(`http://localhost:8080/driver/status?mobNo=${driverMobile}&status=${newStatus}`);
            if (res.data.statusCode === 200) {
                setStatus(newStatus);
            }
        } catch (error) {
            console.error("Failed to update status:", error);
            alert("Error updating status: " + (error.response?.data?.message || error.message));
        } finally {
            setLoading(false);
        }
    };

    const fetchBookingHistory = async () => {
        setHistoryLoading(true);
        try {
            const res = await axios.get(`http://localhost:8080/driver/seeBookingHistory?mobNo=${driverMobile}`);
            if (res.data.statusCode === 200) {
                setBookingHistory(res.data.data?.rlist || []);
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
            const res = await axios.post(`http://localhost:8080/booking/startRide?bookingId=${activeRide.bookingId}&otp=${otpInput}`);
            if (res.data.statusCode === 200) {
                alert("Ride Started!");
                setOtpInput('');
            }
        } catch (e) {
            console.error(e);
            const msg = e.response?.data?.message || "Invalid Start OTP";
            alert("Error: " + msg);
        } finally {
            setLoading(false);
        }
    };

    const handleGenerateEndOtp = async () => {
        setLoading(true);
        try {
            const res = await axios.post(`http://localhost:8080/booking/generateEndOtp?bookingId=${activeRide.bookingId}`);
            alert("End OTP sent to customer successfully!");
        } catch (e) {
            console.error(e);
            alert("Failed to generate OTP");
        } finally {
            setLoading(false);
        }
    };

    const handleCompleteRide = async () => {
        if (!otpInput) return alert("Enter Completion OTP");
        setLoading(true);
        try {
            await axios.post(`http://localhost:8080/booking/completeRide?bookingId=${activeRide.bookingId}&otp=${otpInput}`);
            alert("Ride Completed! Proceed to payment.");
            setShowPayment(true);
            setOtpInput('');
        } catch (e) {
            alert("Invalid Completion OTP");
        } finally {
            setLoading(false);
        }
    };

    const handleCashPayment = async () => {
        if (!confirm("Confirm cash received from customer?")) return;
        setLoading(true);
        try {
            await axios.post(`http://localhost:8080/driver/payByCash?bookingId=${activeRide.bookingId}&paymentType=CASH`);
            alert("Payment Confirmed! Ride Complete.");
            setActiveRide(null);
            setShowPayment(false);
        } catch (e) {
            alert("Payment confirmation failed: " + (e.response?.data?.message || e.message));
        } finally {
            setLoading(false);
        }
    };

    const handleUpiPayment = async () => {
        setLoading(true);
        try {
            const res = await axios.get(`http://localhost:8080/driver/generateUpiQr?bookingId=${activeRide.bookingId}`);
            if (res.data.data?.qr) {
                setQrCode(res.data.data.qr);
            } else {
                alert("QR Code generated. Show to customer.");
            }
        } catch (e) {
            alert("UPI payment failed: " + (e.response?.data?.message || e.message));
        } finally {
            setLoading(false);
        }
    };

    const confirmUpiReceived = async () => {
        if (!confirm("Confirm UPI payment received?")) return;
        setLoading(true);
        try {
            await axios.post(`http://localhost:8080/driver/confirmUpiPayment?bookingId=${activeRide.bookingId}`);
            alert("Payment Confirmed! Ride Complete.");
            setActiveRide(null);
            setShowPayment(false);
            setQrCode(null);
        } catch (e) {
            alert("Failed: " + (e.response?.data?.message || e.message));
        } finally {
            setLoading(false);
        }
    };

    const handleCancelRide = async () => {
        if (!confirm("Are you sure you want to cancel this ride?")) return;
        try {
            await axios.put(`http://localhost:8080/driver/cancel/${activeRide.bookingId}`);
            alert("Ride Cancelled");
            setActiveRide(null);
        } catch (e) {
            const msg = e.response?.data?.message || e.message;
            alert("Error cancelling: " + msg);
        }
    };

    return (
        <div className="min-h-screen bg-gray-900 text-white p-6">
            <div className="flex justify-between items-center mb-8 border-b border-gray-800 pb-4">
                <div className="flex items-center gap-2">
                    <img src={logo} alt="Go-Easy" className="h-14 w-auto object-contain" />
                    <div>
                        <h1 className="text-3xl font-black italic tracking-tighter leading-none" style={{ fontFamily: "'Lexend', sans-serif" }}>
                            <span className="text-[#2F3C8F]">Go</span>
                            <span className="text-[#3E6FA6]">Easy</span>
                        </h1>
                        <p className="text-gray-500 text-[10px] mt-1 font-bold uppercase tracking-[0.2em]">Driver Console</p>
                    </div>
                </div>
                <div className="text-right flex items-center gap-4">
                    <button
                        onClick={() => { setShowProfile(true); fetchBookingHistory(); }}
                        className="bg-gray-800 text-white px-4 py-2 rounded-full flex items-center gap-2 hover:bg-gray-700 transition border border-gray-700 font-bold"
                    >
                        <User size={18} />
                        Profile
                    </button>
                    <button
                        onClick={toggleStatus}
                        disabled={loading}
                        className={`px-6 py-2 rounded-full font-bold transition-all ${status === 'ONLINE' ? 'bg-[#5D5FEF] text-white shadow-[0_0_15px_rgba(93,95,239,0.4)]' : 'bg-red-500 text-white'}`}
                    >
                        {status}
                    </button>
                </div>
            </div>

            {/* Profile/History Modal */}
            {showProfile && (
                <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                    <div className="bg-gray-800 rounded-3xl w-full max-w-lg max-h-[85vh] overflow-hidden border border-gray-700 shadow-2xl">
                        <div className="bg-gradient-to-r from-[#2F3C8F] to-[#3E6FA6] p-6 flex justify-between items-center">
                            <h3 className="text-white font-black text-xl italic tracking-tighter">DRIVER PROFILE</h3>
                            <button onClick={() => setShowProfile(false)} className="text-white hover:rotate-90 transition-transform">
                                <X size={28} />
                            </button>
                        </div>
                        <div className="p-6">
                            {/* Driver Info Card */}
                            <div className="bg-gray-900/50 rounded-2xl p-5 mb-6 border border-gray-700 flex items-center gap-5">
                                <div className="w-16 h-16 bg-[#5D5FEF] rounded-full flex items-center justify-center shadow-lg shadow-indigo-500/20">
                                    <User className="text-white" size={32} />
                                </div>
                                <div className="flex-1">
                                    <p className="font-black text-2xl tracking-tight leading-none mb-1">{user?.name || "Driver"}</p>
                                    <div className="flex items-center gap-3 text-gray-400 text-sm font-bold">
                                        <span>ID: {user?.id || "DRV-" + driverMobile.toString().slice(-4)}</span>
                                        <span>•</span>
                                        <span className="text-emerald-400">{status}</span>
                                    </div>
                                </div>
                            </div>

                            {/* Trip Statistics (Mock if real data not available in user object) */}
                            <div className="grid grid-cols-2 gap-4 mb-6">
                                <div className="bg-gray-900/30 p-4 rounded-xl border border-gray-800">
                                    <p className="text-gray-500 text-[10px] font-black uppercase tracking-widest mb-1">Total Trips</p>
                                    <p className="text-2xl font-black">{bookingHistory.length}</p>
                                </div>
                                <div className="bg-gray-900/30 p-4 rounded-xl border border-gray-800">
                                    <p className="text-gray-500 text-[10px] font-black uppercase tracking-widest mb-1">Status</p>
                                    <p className={`text-xl font-black ${status === 'ONLINE' ? 'text-emerald-400' : 'text-red-400'}`}>{status}</p>
                                </div>
                            </div>

                            {/* Ride History Section */}
                            <div className="flex items-center gap-2 mb-4">
                                <History size={20} className="text-[#5D5FEF]" />
                                <h4 className="font-black text-sm uppercase tracking-wider text-gray-400">Recent Completed Trips</h4>
                            </div>

                            <div className="space-y-3 max-h-[300px] overflow-y-auto pr-2 scrollbar-hide">
                                {historyLoading ? (
                                    <div className="flex flex-col items-center justify-center py-10 opacity-50">
                                        <div className="w-8 h-8 border-4 border-gray-700 border-t-indigo-500 rounded-full animate-spin mb-3"></div>
                                        <p className="text-xs font-bold uppercase tracking-widest">Loading Records...</p>
                                    </div>
                                ) : bookingHistory.length === 0 ? (
                                    <div className="flex flex-col items-center justify-center py-10 text-gray-600 bg-gray-900/20 rounded-2xl border border-dashed border-gray-800">
                                        <Car size={40} className="mb-2 opacity-20" />
                                        <p className="text-sm font-bold uppercase tracking-widest">No previous rides</p>
                                    </div>
                                ) : (
                                    bookingHistory.map((ride, idx) => (
                                        <div key={idx} className="bg-gray-900/40 hover:bg-gray-900/60 transition rounded-2xl p-4 border border-gray-800 relative group overflow-hidden">
                                            <div className="absolute top-0 left-0 w-1 h-full bg-[#5D5FEF] opacity-0 group-hover:opacity-100 transition-opacity"></div>
                                            <div className="flex justify-between items-start">
                                                <div className="space-y-1">
                                                    <div className="flex items-center gap-2">
                                                        <MapPin size={14} className="text-emerald-400" />
                                                        <p className="text-xs font-bold text-gray-300 tracking-tight">{ride.sourceLocation}</p>
                                                    </div>
                                                    <div className="flex items-center gap-2">
                                                        <Navigation size={14} className="text-red-400" />
                                                        <p className="text-xs font-bold text-gray-300 tracking-tight">{ride.destinationLocation}</p>
                                                    </div>
                                                    <p className="text-[10px] text-gray-500 font-black uppercase tracking-widest mt-2">{ride.distance} km • COMPLETED</p>
                                                </div>
                                                <div className="text-right">
                                                    <p className="text-lg font-black text-white">₹{Math.round(ride.fare)}</p>
                                                    <p className="text-[9px] text-[#5D5FEF] font-black uppercase tracking-wide">Paid</p>
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

            {!activeRide ? (
                <div className="flex flex-col items-center justify-center h-96 text-gray-500 bg-gray-800 rounded-2xl border border-gray-700">
                    <div className="relative">
                        <div className="absolute inset-0 bg-accent blur-xl opacity-20 rounded-full animate-pulse"></div>
                        <Car size={64} className="mb-4 text-gray-400 relative z-10" />
                    </div>
                    <p className="text-xl font-medium pt-4">Searching for rides...</p>
                    <div className="mt-2 text-sm animate-pulse text-accent">● Live</div>
                </div>
            ) : showPayment ? (
                /* Payment Screen */
                <div className="bg-gray-800 rounded-2xl p-6 border border-gray-700">
                    <h2 className="text-2xl font-bold mb-4 text-center">💰 Collect Payment</h2>
                    <div className="text-center mb-6">
                        <p className="text-gray-400">Total Fare</p>
                        <p className="text-5xl font-bold text-accent">₹{Math.round(activeRide.fare)}</p>
                        <p className="text-sm text-yellow-500 mt-2">
                            Customer selected: <span className="font-bold uppercase">{activeRide.paymentMode || 'Cash'}</span>
                        </p>
                    </div>

                    {qrCode ? (
                        <div className="text-center">
                            <p className="text-gray-400 mb-4">Show QR to Customer for UPI Payment</p>
                            <img src={`data:image/png;base64,${qrCode}`} alt="UPI QR" className="mx-auto rounded-lg mb-4" style={{ maxWidth: '250px' }} />
                            <div className="flex gap-2">
                                <button
                                    onClick={() => setQrCode(null)}
                                    className="bg-gray-700 text-white px-4 py-3 rounded-lg font-bold"
                                >
                                    Back
                                </button>
                                <button
                                    onClick={confirmUpiReceived}
                                    className="flex-1 bg-accent text-white py-3 rounded-lg font-bold"
                                >
                                    <CheckCircle className="inline mr-2" size={20} />
                                    Payment Received
                                </button>
                            </div>
                        </div>
                    ) : (
                        <div className="space-y-4">
                            <button
                                onClick={handleCashPayment}
                                disabled={loading}
                                className="w-full bg-green-600 text-white py-4 rounded-xl font-bold flex items-center justify-center gap-3 hover:bg-green-700"
                            >
                                <Banknote size={24} />
                                Cash Received
                            </button>
                            <button
                                onClick={handleUpiPayment}
                                disabled={loading}
                                className="w-full bg-purple-600 text-white py-4 rounded-xl font-bold flex items-center justify-center gap-3 hover:bg-purple-700"
                            >
                                <QrCode size={24} />
                                Generate UPI QR
                            </button>
                        </div>
                    )}
                </div>
            ) : (
                /* Active Ride */
                <div className="bg-gray-800 rounded-2xl p-6 border border-gray-700 shadow-2xl">
                    <div className="flex justify-between items-start mb-6">
                        <div>
                            <span className="bg-accent text-black text-xs font-bold px-2 py-1 rounded uppercase tracking-wider">
                                {activeRide.bookingStatus}
                            </span>
                            <h2 className="text-2xl font-bold mt-2">
                                {activeRide.bookingStatus === 'BOOKED' ? 'New Trip Request' : 'Ride in Progress'}
                            </h2>
                        </div>
                        <div className="text-right">
                            <div className="text-3xl font-bold">₹{Math.round(activeRide.fare)}</div>
                            <div className="text-gray-400 text-sm">{activeRide.distance} km</div>
                            <div className="text-xs text-gray-500 mt-1">ID: #{activeRide.bookingId}</div>
                        </div>
                    </div>

                    <div className="space-y-4 mb-8">
                        <div className="flex items-center">
                            <MapPin className="text-accent mr-4" size={24} />
                            <div>
                                <p className="text-xs text-gray-500 uppercase">Pickup</p>
                                <p className="text-lg font-medium">{activeRide.sourceLocation}</p>
                            </div>
                        </div>
                        <div className="w-0.5 h-8 bg-gray-700 ml-[11px]"></div>
                        <div className="flex items-center">
                            <Navigation className="text-blue-400 mr-4" size={24} />
                            <div>
                                <p className="text-xs text-gray-500 uppercase">Drop</p>
                                <p className="text-lg font-medium">{activeRide.destinationLocation}</p>
                            </div>
                        </div>
                    </div>

                    <div className="bg-gray-900 p-6 rounded-xl border border-gray-700 relative">
                        {/* Cancel Button - Only before ride starts */}
                        {activeRide.bookingStatus === 'BOOKED' && (
                            <button
                                onClick={handleCancelRide}
                                className="absolute top-4 right-4 text-xs bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700 transition-colors flex items-center gap-1"
                            >
                                <XCircle size={14} />
                                Cancel Ride
                            </button>
                        )}

                        {activeRide.bookingStatus === 'BOOKED' && (
                            <div>
                                <h3 className="text-sm text-gray-400 mb-3 uppercase font-bold">Start Ride</h3>
                                <div className="flex gap-2">
                                    <input
                                        type="text"
                                        placeholder="Enter Customer OTP"
                                        value={otpInput}
                                        onChange={(e) => setOtpInput(e.target.value)}
                                        className="flex-1 bg-black border border-gray-600 rounded-lg px-4 py-3 text-center text-xl tracking-widest focus:border-accent outline-none"
                                    />
                                    <button
                                        onClick={handleStartRide}
                                        disabled={loading}
                                        className="bg-accent text-white px-6 py-3 rounded-lg font-bold hover:bg-emerald-600 disabled:opacity-50"
                                    >
                                        Start
                                    </button>
                                </div>
                            </div>
                        )}

                        {activeRide.bookingStatus === 'ONGOING' && (
                            <div>
                                <h3 className="text-sm text-gray-400 mb-3 uppercase font-bold">End Ride</h3>

                                <button
                                    onClick={handleGenerateEndOtp}
                                    disabled={loading}
                                    className="w-full bg-blue-600 text-white py-3 rounded-lg font-bold mb-4 hover:bg-blue-700"
                                >
                                    <Smartphone className="inline mr-2" size={18} /> Request End OTP
                                </button>

                                <div className="flex gap-2">
                                    <input
                                        type="text"
                                        placeholder="Enter End OTP"
                                        value={otpInput}
                                        onChange={(e) => setOtpInput(e.target.value)}
                                        className="flex-1 bg-black border border-gray-600 rounded-lg px-4 py-3 text-center text-xl tracking-widest focus:border-accent outline-none"
                                    />
                                    <button
                                        onClick={handleCompleteRide}
                                        disabled={loading}
                                        className="bg-red-500 text-white px-6 py-3 rounded-lg font-bold hover:bg-red-600 disabled:opacity-50"
                                    >
                                        End
                                    </button>
                                </div>
                            </div>
                        )}
                    </div>
                </div>
            )}
        </div>
    );
};

export default DriverDashboard;
