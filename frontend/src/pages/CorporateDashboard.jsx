import { useState, useEffect } from 'react';
import axios from 'axios';
import API_BASE_URL from '../api';
import { useNavigate } from 'react-router-dom';
import { Building2, Users, Wallet, TrendingUp, Plus, LogOut, X, CheckCircle } from 'lucide-react';
import logo from '../assets/logo.svg';

const CorporateDashboard = () => {
    const navigate = useNavigate();
    const [company, setCompany]         = useState(() => { try { return JSON.parse(localStorage.getItem('corp_company')); } catch { return null; } });
    const [dashboard, setDashboard]     = useState(null);
    const [loading, setLoading]         = useState(false);
    const [showAddEmployee, setShowAddEmployee] = useState(false);
    const [showTopUp, setShowTopUp]     = useState(false);
    const [empMobile, setEmpMobile]     = useState('');
    const [topUpAmount, setTopUpAmount] = useState('');
    const [msg, setMsg]                 = useState('');

    useEffect(() => {
        if (!company) { navigate('/corporate/login'); return; }
        loadDashboard();
    }, [company]);

    const loadDashboard = async () => {
        setLoading(true);
        try {
            const res = await axios.get(`${API_BASE_URL}/corporate/dashboard?companyId=${company.id}`);
            if (res.data.statusCode === 200) setDashboard(res.data.data);
        } catch { setMsg('Failed to load dashboard'); }
        finally { setLoading(false); }
    };

    const handleAddEmployee = async () => {
        if (!empMobile) return;
        try {
            await axios.post(`${API_BASE_URL}/corporate/addEmployee?companyId=${company.id}&customerMobno=${empMobile}`);
            setMsg('Employee added successfully!');
            setShowAddEmployee(false);
            setEmpMobile('');
            loadDashboard();
        } catch (e) { setMsg(e.response?.data?.message || 'Failed to add employee'); }
    };

    const handleTopUp = async () => {
        if (!topUpAmount || parseFloat(topUpAmount) <= 0) return;
        try {
            const res = await axios.post(`${API_BASE_URL}/corporate/topUp?companyId=${company.id}&amount=${topUpAmount}`);
            setMsg(`Wallet topped up! New balance: ₹${res.data.data?.walletBalance}`);
            setShowTopUp(false);
            setTopUpAmount('');
            loadDashboard();
        } catch (e) { setMsg(e.response?.data?.message || 'Top-up failed'); }
    };

    const logout = () => {
        localStorage.removeItem('corp_company');
        navigate('/corporate/login');
    };

    if (!company) return null;

    return (
        <div className="min-h-screen bg-[#0A0A0A] text-white selection:bg-[#F7D100] selection:text-black">
            {/* Header */}
            <header className="fixed top-0 left-0 right-0 z-40 bg-black/60 backdrop-blur-xl border-b border-white/5 px-4 h-16 sm:h-20 flex items-center justify-center">
                <div className="w-full max-w-7xl flex items-center justify-between">
                    <div className="flex items-center gap-3">
                        <img src={logo} alt="Go-Easy" className="h-10 w-auto object-contain" />
                        <div>
                            <p className="text-[9px] font-black text-gray-600 uppercase tracking-widest">Corporate</p>
                            <h1 className="text-sm font-black italic tracking-tight leading-none">{company.companyName}</h1>
                        </div>
                    </div>
                    <button onClick={logout} className="flex items-center gap-2 bg-red-500/10 hover:bg-red-500/20 text-red-500 px-4 py-2 rounded-xl border border-red-500/10 transition-all text-[10px] font-black uppercase">
                        <LogOut size={14} /> Sign Out
                    </button>
                </div>
            </header>

            <main className="pt-24 sm:pt-32 pb-20 px-4 max-w-7xl mx-auto">
                {msg && (
                    <div className="mb-6 bg-[#F7D100]/10 border border-[#F7D100]/30 text-[#F7D100] rounded-2xl px-5 py-3 text-[10px] font-black uppercase flex items-center justify-between">
                        <span>{msg}</span>
                        <button onClick={() => setMsg('')}><X size={14} /></button>
                    </div>
                )}

                {/* Stats cards */}
                <div className="grid grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
                    {[
                        { label: 'Wallet Balance',       value: `₹${Math.round(dashboard?.walletBalance ?? 0)}`,        icon: <Wallet size={18} />,    color: 'text-[#F7D100]' },
                        { label: 'Employees',            value: dashboard?.totalEmployees ?? 0,                          icon: <Users size={18} />,     color: 'text-blue-400' },
                        { label: 'Spent This Month',     value: `₹${Math.round(dashboard?.totalSpentThisMonth ?? 0)}`,  icon: <TrendingUp size={18} />, color: 'text-green-400' },
                        { label: 'Budget/Employee',      value: `₹${Math.round(company.monthlyBudgetPerEmployee ?? 500)}`, icon: <Building2 size={18} />, color: 'text-purple-400' },
                    ].map((s) => (
                        <div key={s.label} className="glass-card rounded-3xl p-6">
                            <div className={`${s.color} mb-3`}>{s.icon}</div>
                            <p className="text-[9px] font-black text-gray-600 uppercase tracking-widest mb-1">{s.label}</p>
                            <p className={`text-2xl font-black ${s.color}`}>{loading ? '—' : s.value}</p>
                        </div>
                    ))}
                </div>

                {/* Action buttons */}
                <div className="flex flex-wrap gap-3 mb-8">
                    <button onClick={() => setShowAddEmployee(true)} className="flex items-center gap-2 bg-blue-500/10 border border-blue-500/20 text-blue-400 hover:bg-blue-500/20 px-5 py-3 rounded-2xl text-[10px] font-black uppercase tracking-widest transition-all">
                        <Plus size={14} /> Add Employee
                    </button>
                    <button onClick={() => setShowTopUp(true)} className="flex items-center gap-2 bg-[#F7D100]/10 border border-[#F7D100]/20 text-[#F7D100] hover:bg-[#F7D100]/20 px-5 py-3 rounded-2xl text-[10px] font-black uppercase tracking-widest transition-all">
                        <Wallet size={14} /> Top Up Wallet
                    </button>
                    <button onClick={loadDashboard} className="flex items-center gap-2 bg-white/5 border border-white/10 text-gray-400 hover:bg-white/10 px-5 py-3 rounded-2xl text-[10px] font-black uppercase tracking-widest transition-all">
                        Refresh
                    </button>
                </div>

                {/* Employee table */}
                <div className="glass-card rounded-3xl overflow-hidden">
                    <div className="p-6 border-b border-white/5 flex items-center justify-between">
                        <h3 className="text-[10px] font-black uppercase tracking-[0.3em] text-gray-500 flex items-center gap-2">
                            <Users size={14} className="text-blue-400" /> Employee Ride Usage — This Month
                        </h3>
                        <span className="text-[9px] font-black text-blue-400 border border-blue-400/20 px-2 py-0.5 rounded-full">
                            {dashboard?.employees?.length ?? 0} employees
                        </span>
                    </div>
                    <div className="overflow-x-auto">
                        {loading ? (
                            <div className="py-20 flex justify-center"><div className="w-8 h-8 border-2 border-[#F7D100] border-t-transparent rounded-full animate-spin" /></div>
                        ) : !dashboard?.employees?.length ? (
                            <div className="py-20 text-center text-gray-600">
                                <Users size={40} className="mx-auto mb-4 opacity-20" />
                                <p className="text-[10px] font-black uppercase tracking-widest">No employees yet. Add one to get started.</p>
                            </div>
                        ) : (
                            <table className="w-full">
                                <thead>
                                    <tr className="border-b border-white/5">
                                        {['Employee', 'Mobile', 'Budget', 'Used', 'Remaining', 'Status'].map((h) => (
                                            <th key={h} className="px-6 py-4 text-left text-[9px] font-black text-gray-600 uppercase tracking-widest">{h}</th>
                                        ))}
                                    </tr>
                                </thead>
                                <tbody>
                                    {dashboard.employees.map((emp, i) => {
                                        const pct = Math.round((emp.used / emp.budget) * 100);
                                        return (
                                            <tr key={i} className="border-b border-white/5 hover:bg-white/5 transition-colors">
                                                <td className="px-6 py-4 font-bold text-sm">{emp.name}</td>
                                                <td className="px-6 py-4 text-[10px] font-mono text-gray-500">{emp.mobile}</td>
                                                <td className="px-6 py-4 text-[11px] font-black">₹{emp.budget}</td>
                                                <td className="px-6 py-4 text-[11px] font-black text-orange-400">₹{Math.round(emp.used)}</td>
                                                <td className="px-6 py-4 text-[11px] font-black text-green-400">₹{Math.round(emp.remaining)}</td>
                                                <td className="px-6 py-4">
                                                    <div className="flex items-center gap-2">
                                                        <div className="w-16 h-1.5 bg-white/10 rounded-full overflow-hidden">
                                                            <div className={`h-full rounded-full ${pct > 80 ? 'bg-red-400' : 'bg-green-400'}`} style={{ width: `${Math.min(pct, 100)}%` }} />
                                                        </div>
                                                        <span className="text-[9px] font-black text-gray-600">{pct}%</span>
                                                    </div>
                                                </td>
                                            </tr>
                                        );
                                    })}
                                </tbody>
                            </table>
                        )}
                    </div>
                </div>
            </main>

            {/* Add Employee Modal */}
            {showAddEmployee && (
                <div className="fixed inset-0 bg-black/90 backdrop-blur-md z-50 flex items-center justify-center p-4">
                    <div className="glass-card rounded-[3rem] w-full max-w-sm p-10">
                        <div className="flex justify-between items-center mb-8">
                            <h3 className="text-xl font-black italic tracking-tight">Add Employee</h3>
                            <button onClick={() => setShowAddEmployee(false)} className="text-gray-600 hover:text-white"><X size={20} /></button>
                        </div>
                        <label className="text-[10px] font-black text-gray-500 uppercase tracking-widest mb-2 block">Employee Mobile Number</label>
                        <input
                            type="number"
                            value={empMobile}
                            onChange={(e) => setEmpMobile(e.target.value)}
                            placeholder="10-digit mobile"
                            className="premium-input w-full mb-6"
                        />
                        <button onClick={handleAddEmployee} className="premium-button w-full">
                            <CheckCircle size={14} className="inline mr-2" /> Add to Plan
                        </button>
                    </div>
                </div>
            )}

            {/* Top-up Modal */}
            {showTopUp && (
                <div className="fixed inset-0 bg-black/90 backdrop-blur-md z-50 flex items-center justify-center p-4">
                    <div className="glass-card rounded-[3rem] w-full max-w-sm p-10">
                        <div className="flex justify-between items-center mb-8">
                            <h3 className="text-xl font-black italic tracking-tight">Top Up Wallet</h3>
                            <button onClick={() => setShowTopUp(false)} className="text-gray-600 hover:text-white"><X size={20} /></button>
                        </div>
                        <p className="text-[10px] text-gray-500 font-black uppercase mb-4">Current Balance: ₹{Math.round(dashboard?.walletBalance ?? 0)}</p>
                        <input
                            type="number"
                            value={topUpAmount}
                            onChange={(e) => setTopUpAmount(e.target.value)}
                            placeholder="Amount in ₹"
                            className="premium-input w-full mb-6"
                        />
                        <button onClick={handleTopUp} className="premium-button w-full">
                            <Wallet size={14} className="inline mr-2" /> Add Funds
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};

export default CorporateDashboard;
