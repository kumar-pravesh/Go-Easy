import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import API_BASE_URL from '../api';
import { CheckCircle, MapPin, Receipt, ArrowLeft, Download } from 'lucide-react';
import logo from '../assets/logo.svg';

const RideReceipt = () => {
    const { bookingId } = useParams();
    const navigate = useNavigate();
    const [receipt, setReceipt] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchReceipt = async () => {
            try {
                const res = await axios.get(`${API_BASE_URL}/booking/receipt?bookingId=${bookingId}`);
                if (res.data.statusCode === 200) {
                    setReceipt(res.data.data);
                }
            } catch (err) {
                console.error("Failed to load receipt", err);
            } finally {
                setLoading(false);
            }
        };
        fetchReceipt();
    }, [bookingId]);

    if (loading) {
        return (
            <div className="min-h-screen bg-[#0A0A0A] flex items-center justify-center">
                <div className="w-8 h-8 border-4 border-[#F7D100] border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    if (!receipt) {
        return (
            <div className="min-h-screen bg-[#0A0A0A] text-white flex flex-col items-center justify-center text-center p-4">
                <Receipt size={64} className="text-gray-700 mb-6" />
                <h2 className="text-3xl font-black italic mb-2 tracking-tighter">Receipt Not Found</h2>
                <p className="text-gray-500 mb-8 max-w-sm">We couldn't find a receipt for this ride. It may not be completed yet or the ID is invalid.</p>
                <button onClick={() => navigate('/userHome')} className="premium-button px-8">Return Home</button>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-[#0A0A0A] text-white selection:bg-[#F7D100] selection:text-black py-12 px-4 flex justify-center">
            <div className="w-full max-w-lg">
                <button onClick={() => navigate('/userHome')} className="flex items-center gap-2 text-gray-400 hover:text-white transition-colors mb-6 text-[10px] font-black uppercase tracking-widest">
                    <ArrowLeft size={16} /> Back to Home
                </button>
                
                <div className="bg-white text-black rounded-[2rem] overflow-hidden shadow-2xl relative">
                    {/* Header */}
                    <div className="bg-[#F7D100] p-8 text-center relative overflow-hidden">
                        <div className="absolute top-0 right-0 w-32 h-32 bg-white/20 rounded-full -mr-16 -mt-16"></div>
                        <img src={logo} alt="Go-Easy" className="h-10 mx-auto mb-6 brightness-0" />
                        <CheckCircle size={48} className="mx-auto mb-4" />
                        <h1 className="text-3xl font-black italic tracking-tighter uppercase mb-1">Ride Completed</h1>
                        <p className="text-black/70 text-[10px] font-black uppercase tracking-widest">{receipt.rideDate} • {receipt.receiptNumber}</p>
                    </div>

                    <div className="p-8">
                        {/* Locations */}
                        <div className="space-y-6 relative mb-8">
                            <div className="absolute left-[11px] top-6 bottom-6 w-[2px] bg-gray-200"></div>
                            <div className="flex items-start relative z-10">
                                <div className="w-6 h-6 bg-[#F7D100] rounded-full flex items-center justify-center mr-4 mt-1 shadow-md">
                                    <div className="w-2 h-2 bg-black rounded-full"></div>
                                </div>
                                <div>
                                    <p className="text-[9px] text-gray-400 font-black uppercase tracking-widest mb-1">Pickup</p>
                                    <p className="text-sm font-bold text-gray-800">{receipt.sourceLocation}</p>
                                </div>
                            </div>
                            <div className="flex items-start relative z-10">
                                <div className="w-6 h-6 bg-black rounded-full flex items-center justify-center mr-4 mt-1 shadow-md">
                                    <MapPin size={12} className="text-[#F7D100]" />
                                </div>
                                <div>
                                    <p className="text-[9px] text-gray-400 font-black uppercase tracking-widest mb-1">Dropoff</p>
                                    <p className="text-sm font-bold text-gray-800">{receipt.destinationLocation}</p>
                                </div>
                            </div>
                        </div>

                        {/* Driver & Vehicle */}
                        <div className="bg-gray-50 rounded-2xl p-5 mb-8 border border-gray-100 flex items-center justify-between">
                            <div>
                                <p className="text-[9px] text-gray-400 font-black uppercase tracking-widest mb-1">Driver</p>
                                <p className="font-black text-gray-800">{receipt.driverName || 'N/A'}</p>
                            </div>
                            <div className="text-right">
                                <p className="text-[9px] text-gray-400 font-black uppercase tracking-widest mb-1">Vehicle</p>
                                <p className="font-black text-gray-800">{receipt.vehicleModel || 'N/A'}</p>
                                <p className="text-[10px] font-bold text-gray-500 uppercase">{receipt.vehicleNumber || 'N/A'}</p>
                            </div>
                        </div>

                        {/* Fare Breakdown */}
                        <div className="space-y-4 mb-8">
                            <h3 className="text-[10px] font-black text-gray-400 uppercase tracking-widest border-b border-gray-100 pb-2">Fare Breakdown</h3>
                            
                            <div className="flex justify-between items-center text-sm">
                                <span className="text-gray-600 font-medium">Base Fare</span>
                                <span className="font-bold">₹{Math.round(receipt.baseFare || 0)}</span>
                            </div>
                            
                            <div className="flex justify-between items-center text-sm">
                                <span className="text-gray-600 font-medium">Distance ({receipt.distance?.toFixed(1) || 0} km x ₹{receipt.pricePerKm?.toFixed(0) || 0}/km)</span>
                                <span className="font-bold">₹{Math.round(receipt.distanceFare || 0)}</span>
                            </div>

                            {receipt.penaltyAmount > 0 && (
                                <div className="flex justify-between items-center text-sm text-red-500">
                                    <span className="font-medium">Previous Cancel Penalty</span>
                                    <span className="font-bold">₹{Math.round(receipt.penaltyAmount)}</span>
                                </div>
                            )}

                            {receipt.waitingCharge > 0 && (
                                <div className="flex justify-between items-center text-sm">
                                    <span className="text-gray-600 font-medium">Wait Time Charge</span>
                                    <span className="font-bold">₹{Math.round(receipt.waitingCharge)}</span>
                                </div>
                            )}

                            {receipt.tax > 0 && (
                                <div className="flex justify-between items-center text-sm">
                                    <span className="text-gray-600 font-medium">Taxes</span>
                                    <span className="font-bold">₹{Math.round(receipt.tax)}</span>
                                </div>
                            )}

                            {receipt.discount > 0 && (
                                <div className="flex justify-between items-center text-sm text-green-600">
                                    <span className="font-medium">Discount Applied</span>
                                    <span className="font-bold">-₹{Math.round(receipt.discount)}</span>
                                </div>
                            )}
                        </div>

                        {/* Total */}
                        <div className="bg-black text-white rounded-2xl p-6 flex items-center justify-between shadow-lg">
                            <div>
                                <p className="text-[10px] text-gray-400 font-black uppercase tracking-widest mb-1">Total Paid</p>
                                <p className="text-3xl font-black italic tracking-tighter">₹{Math.round(receipt.totalFare || 0)}</p>
                            </div>
                            <div className="text-right">
                                <span className="inline-block bg-white/20 px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-widest">
                                    {receipt.paymentMode || 'Cash'}
                                </span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="mt-8 text-center">
                    <button onClick={() => window.print()} className="inline-flex items-center gap-2 text-gray-400 hover:text-white transition-colors text-[10px] font-black uppercase tracking-widest">
                        <Download size={14} /> Download PDF
                    </button>
                </div>
            </div>
        </div>
    );
};

export default RideReceipt;
