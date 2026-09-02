import React, { useState, useEffect } from 'react';
import axios from 'axios';
import API_BASE_URL from '../api';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { Activity, Users, Car, AlertTriangle, IndianRupee, ShieldAlert, LogOut, ChevronRight, CheckCircle, XCircle } from 'lucide-react';
import logo from '../assets/logo.svg';

const AdminDashboard = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    
    const [stats, setStats] = useState(null);
    const [drivers, setDrivers] = useState([]);
    const [bookings, setBookings] = useState([]);
    const [sosEvents, setSosEvents] = useState([]);
    const [activeTab, setActiveTab] = useState('overview');
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!user || user.role !== 'ADMIN') {
            navigate('/');
            return;
        }
        fetchAllData();
    }, [user]);

    const fetchAllData = async () => {
        setLoading(true);
        try {
            const [statsRes, driversRes, bookingsRes, sosRes] = await Promise.all([
                axios.get(`${API_BASE_URL}/admin/stats`),
                axios.get(`${API_BASE_URL}/admin/drivers`),
                axios.get(`${API_BASE_URL}/admin/bookings`),
                axios.get(`${API_BASE_URL}/admin/sos`)
            ]);
            
            if (statsRes.data.statusCode === 200) setStats(statsRes.data.data);
            if (driversRes.data.statusCode === 200) setDrivers(driversRes.data.data);
            if (bookingsRes.data.statusCode === 200) setBookings(bookingsRes.data.data.reverse());
            if (sosRes.data.statusCode === 200) setSosEvents(sosRes.data.data.reverse());
        } catch (error) {
            console.error("Failed to fetch admin data", error);
        } finally {
            setLoading(false);
        }
    };

    const handleBlockDriver = async (driverId, currentStatus) => {
        const isBlocked = currentStatus === 'BLOCKED';
        if (!confirm(`Are you sure you want to ${isBlocked ? 'unblock' : 'block'} this driver?`)) return;
        
        try {
            const endpoint = isBlocked ? '/admin/unblockDriver' : '/admin/blockDriver';
            const res = await axios.post(`${API_BASE_URL}${endpoint}?driverId=${driverId}`);
            if (res.data.statusCode === 200) {
                fetchAllData(); // refresh list
            }
        } catch (error) {
            alert("Action failed");
        }
    };

    if (loading) {
        return (
            <div className="min-h-screen bg-[#0A0A0A] flex items-center justify-center">
                <div className="w-12 h-12 border-4 border-[#F7D100] border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-[#0A0A0A] text-white selection:bg-[#F7D100] selection:text-black flex">
            {/* Sidebar */}
            <aside className="w-64 border-r border-white/5 bg-black/40 backdrop-blur-xl hidden md:flex flex-col">
                <div className="h-20 flex items-center gap-2 px-6 border-b border-white/5">
                    <img src={logo} alt="Go-Easy" className="h-8 w-auto" />
                    <h1 className="text-xl font-black italic tracking-tighter">Admin<span className="text-[#F7D100]">Panel</span></h1>
                </div>
                <div className="flex-1 py-6 px-4 space-y-2">
                    <button onClick={() => setActiveTab('overview')} className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-xs font-bold uppercase tracking-widest ${activeTab === 'overview' ? 'bg-[#F7D100]/10 text-[#F7D100] border border-[#F7D100]/20' : 'text-gray-500 hover:bg-white/5'}`}>
                        <Activity size={16} /> Overview
                    </button>
                    <button onClick={() => setActiveTab('drivers')} className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-xs font-bold uppercase tracking-widest ${activeTab === 'drivers' ? 'bg-[#F7D100]/10 text-[#F7D100] border border-[#F7D100]/20' : 'text-gray-500 hover:bg-white/5'}`}>
                        <Users size={16} /> Drivers
                    </button>
                    <button onClick={() => setActiveTab('bookings')} className={`w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-xs font-bold uppercase tracking-widest ${activeTab === 'bookings' ? 'bg-[#F7D100]/10 text-[#F7D100] border border-[#F7D100]/20' : 'text-gray-500 hover:bg-white/5'}`}>
                        <Car size={16} /> Bookings
                    </button>
                    <button onClick={() => setActiveTab('sos')} className={`w-full flex items-center justify-between px-4 py-3 rounded-xl transition-all text-xs font-bold uppercase tracking-widest ${activeTab === 'sos' ? 'bg-red-500/10 text-red-500 border border-red-500/20' : 'text-gray-500 hover:bg-white/5'}`}>
                        <div className="flex items-center gap-3"><ShieldAlert size={16} /> SOS Events</div>
                        {sosEvents.length > 0 && <span className="bg-red-500 text-white px-2 py-0.5 rounded-full text-[9px]">{sosEvents.length}</span>}
                    </button>
                </div>
                <div className="p-4 border-t border-white/5">
                    <button onClick={() => { logout(); navigate('/'); }} className="w-full flex items-center gap-3 px-4 py-3 rounded-xl transition-all text-xs font-bold uppercase tracking-widest text-red-500 hover:bg-red-500/10">
                        <LogOut size={16} /> Logout
                    </button>
                </div>
            </aside>

            {/* Main Content */}
            <main className="flex-1 flex flex-col max-h-screen overflow-hidden">
                <header className="h-20 border-b border-white/5 bg-black/20 flex items-center px-8 shrink-0">
                    <div>
                        <p className="text-[10px] font-black uppercase tracking-[0.3em] text-gray-500 mb-1">Platform Control</p>
                        <h2 className="text-2xl font-black italic tracking-tighter capitalize">{activeTab}</h2>
                    </div>
                </header>

                <div className="flex-1 overflow-y-auto custom-scrollbar p-8">
                    {activeTab === 'overview' && stats && (
                        <div className="space-y-8">
                            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                                <div className="bg-gradient-to-br from-[#F7D100]/20 to-transparent border border-[#F7D100]/30 p-6 rounded-[2rem]">
                                    <p className="text-[10px] font-black uppercase tracking-widest text-[#F7D100] mb-2">Total Revenue</p>
                                    <p className="text-4xl font-black italic tracking-tighter">₹{Math.round(stats.totalRevenue)}</p>
                                </div>
                                <div className="bg-white/5 border border-white/10 p-6 rounded-[2rem]">
                                    <p className="text-[10px] font-black uppercase tracking-widest text-gray-400 mb-2">Active Rides</p>
                                    <p className="text-4xl font-black italic tracking-tighter text-blue-400">{stats.activeRides}</p>
                                </div>
                                <div className="bg-red-500/10 border border-red-500/20 p-6 rounded-[2rem]">
                                    <p className="text-[10px] font-black uppercase tracking-widest text-red-400 mb-2">SOS Alerts</p>
                                    <p className="text-4xl font-black italic tracking-tighter text-red-500">{stats.sosAlerts}</p>
                                </div>
                                <div className="bg-white/5 border border-white/10 p-6 rounded-[2rem]">
                                    <p className="text-[10px] font-black uppercase tracking-widest text-gray-400 mb-2">Total Drivers</p>
                                    <p className="text-4xl font-black italic tracking-tighter">{stats.totalDrivers}</p>
                                </div>
                                <div className="bg-white/5 border border-white/10 p-6 rounded-[2rem]">
                                    <p className="text-[10px] font-black uppercase tracking-widest text-gray-400 mb-2">Total Customers</p>
                                    <p className="text-4xl font-black italic tracking-tighter">{stats.totalCustomers}</p>
                                </div>
                                <div className="bg-white/5 border border-white/10 p-6 rounded-[2rem]">
                                    <p className="text-[10px] font-black uppercase tracking-widest text-gray-400 mb-2">Total Bookings</p>
                                    <p className="text-4xl font-black italic tracking-tighter">{stats.totalBookings}</p>
                                </div>
                            </div>
                        </div>
                    )}

                    {activeTab === 'drivers' && (
                        <div className="bg-white/5 border border-white/10 rounded-[2rem] overflow-hidden">
                            <table className="w-full text-left">
                                <thead className="bg-black/50 border-b border-white/10">
                                    <tr>
                                        <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-500">Driver</th>
                                        <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-500">Contact</th>
                                        <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-500">Status</th>
                                        <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-500">Score</th>
                                        <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-500 text-right">Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {drivers.map(d => (
                                        <tr key={d.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                            <td className="px-6 py-4">
                                                <p className="font-bold">{d.dname}</p>
                                                <p className="text-[10px] text-gray-500 font-mono">{d.dlNumber}</p>
                                            </td>
                                            <td className="px-6 py-4">
                                                <p className="text-sm">{d.mobNo}</p>
                                                <p className="text-[10px] text-gray-500">{d.mailId}</p>
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className={`px-3 py-1 rounded-full text-[9px] font-black uppercase tracking-widest ${d.userr?.role === 'BLOCKED' ? 'bg-red-500/20 text-red-400 border border-red-500/30' : 'bg-green-500/20 text-green-400 border border-green-500/30'}`}>
                                                    {d.userr?.role === 'BLOCKED' ? 'BLOCKED' : 'ACTIVE'}
                                                </span>
                                            </td>
                                            <td className="px-6 py-4">
                                                <p className={`font-black ${d.reliabilityScore >= 80 ? 'text-green-400' : 'text-red-400'}`}>{d.reliabilityScore}%</p>
                                            </td>
                                            <td className="px-6 py-4 text-right">
                                                <button 
                                                    onClick={() => handleBlockDriver(d.id, d.userr?.role)}
                                                    className={`px-4 py-2 rounded-xl text-[10px] font-black uppercase tracking-widest border transition-all ${d.userr?.role === 'BLOCKED' ? 'bg-white/5 border-white/20 hover:bg-white/10' : 'bg-red-500/10 border-red-500/30 text-red-500 hover:bg-red-500/20'}`}
                                                >
                                                    {d.userr?.role === 'BLOCKED' ? 'Unblock' : 'Block'}
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {activeTab === 'bookings' && (
                        <div className="bg-white/5 border border-white/10 rounded-[2rem] overflow-hidden">
                            <table className="w-full text-left">
                                <thead className="bg-black/50 border-b border-white/10">
                                    <tr>
                                        <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-500">Booking ID</th>
                                        <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-500">Route</th>
                                        <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-500">Status</th>
                                        <th className="px-6 py-4 text-[10px] font-black uppercase tracking-widest text-gray-500 text-right">Fare</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    {bookings.map(b => (
                                        <tr key={b.id} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                            <td className="px-6 py-4 text-sm font-mono text-gray-400">#{b.id}</td>
                                            <td className="px-6 py-4">
                                                <div className="flex items-center gap-2 text-sm"><div className="w-2 h-2 bg-[#F7D100] rounded-full"></div>{b.sourceLocation}</div>
                                                <div className="flex items-center gap-2 text-sm mt-1"><div className="w-2 h-2 bg-gray-600 rounded-full"></div>{b.destinationLocation}</div>
                                            </td>
                                            <td className="px-6 py-4">
                                                <span className="text-[10px] font-black uppercase tracking-widest bg-white/10 px-3 py-1 rounded-full">{b.bookingStatus}</span>
                                            </td>
                                            <td className="px-6 py-4 text-right font-black">
                                                {b.fare ? `₹${Math.round(b.fare)}` : '—'}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {activeTab === 'sos' && (
                        <div className="space-y-4">
                            {sosEvents.map(sos => (
                                <div key={sos.id} className="bg-red-500/5 border border-red-500/20 p-6 rounded-[2rem] flex items-start gap-4">
                                    <div className="w-12 h-12 bg-red-500/20 text-red-500 rounded-2xl flex items-center justify-center shrink-0">
                                        <AlertTriangle size={24} />
                                    </div>
                                    <div className="flex-1">
                                        <div className="flex justify-between items-start">
                                            <div>
                                                <h4 className="font-black italic tracking-tighter text-red-400 text-xl">SOS Alert</h4>
                                                <p className="text-[10px] font-black uppercase tracking-widest text-gray-500 mb-2">Booking #{sos.booking?.id || 'Unknown'}</p>
                                            </div>
                                            <span className="text-[10px] font-mono text-gray-400 bg-black/50 px-3 py-1 rounded-full">{new Date(sos.triggeredAt).toLocaleString()}</span>
                                        </div>
                                        <div className="grid grid-cols-2 gap-4 mt-4">
                                            <div className="bg-black/30 p-4 rounded-xl border border-white/5">
                                                <p className="text-[9px] font-black uppercase tracking-widest text-gray-500 mb-1">Triggered By</p>
                                                <p className="font-bold">{sos.triggeredBy}</p>
                                            </div>
                                            <div className="bg-black/30 p-4 rounded-xl border border-white/5">
                                                <p className="text-[9px] font-black uppercase tracking-widest text-gray-500 mb-1">Status</p>
                                                <p className="font-black text-red-400">{sos.status}</p>
                                            </div>
                                        </div>
                                        {sos.locationLat && sos.locationLon && (
                                            <div className="mt-4 text-[10px] font-mono text-gray-500 flex gap-4">
                                                <span>Lat: {sos.locationLat}</span>
                                                <span>Lon: {sos.locationLon}</span>
                                            </div>
                                        )}
                                    </div>
                                </div>
                            ))}
                            {sosEvents.length === 0 && (
                                <div className="text-center py-20 text-gray-500">
                                    <ShieldAlert size={48} className="mx-auto mb-4 opacity-20" />
                                    <p className="font-black uppercase tracking-widest text-sm">No SOS Events Recorded</p>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            </main>
        </div>
    );
};

export default AdminDashboard;
