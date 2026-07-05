import { useState, useEffect } from 'react';
import axios from 'axios';
import API_BASE_URL from '../api';
import { useNavigate, Link } from 'react-router-dom';
import {
    Building2, ArrowLeft, Eye, EyeOff, Mail, Lock, User,
    CreditCard, CheckCircle, AlertCircle, Briefcase, ChevronRight,
    Shield, Users, BarChart3, Wallet
} from 'lucide-react';
import logo from '../assets/logo.svg';

const FEATURES = [
    {
        icon: Users,
        title: 'Employee Ride Credits',
        desc: 'Assign monthly ride budgets per employee — auto-resets every month.',
    },
    {
        icon: Shield,
        title: 'Controlled Spending',
        desc: 'Corporate wallet ensures spending stays within defined limits.',
    },
    {
        icon: BarChart3,
        title: 'Usage Analytics',
        desc: 'Track ride spend per employee with real-time dashboard insights.',
    },
    {
        icon: Wallet,
        title: 'Instant Top-Up',
        desc: 'Add funds to your company wallet anytime to keep rides flowing.',
    },
];

const Spinner = () => (
    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
    </svg>
);

const InputField = ({ label, icon: Icon, optional, children }) => (
    <div className="space-y-1.5">
        <label className="flex items-center gap-1.5 text-[11px] font-bold text-gray-400 uppercase tracking-wider ml-0.5">
            <Icon size={11} />
            {label}
            {optional && <span className="text-gray-600 normal-case font-normal tracking-normal ml-1">(optional)</span>}
        </label>
        {children}
    </div>
);

const CorporateLogin = () => {
    const navigate = useNavigate();
    const [tab, setTab] = useState('login');
    const [showPwd, setShowPwd] = useState(false);
    const [showRegPwd, setShowRegPwd] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [serverStatus, setServerStatus] = useState('checking'); // 'checking' | 'online' | 'offline'

    useEffect(() => {
        axios.get(`${API_BASE_URL}/auth/ping`, { timeout: 8000 })
            .then(() => setServerStatus('online'))
            .catch(() => {
                // Try a simpler endpoint as fallback
                axios.get(`${API_BASE_URL}/corporate/dashboard?companyId=0`, { timeout: 8000 })
                    .then(() => setServerStatus('online'))
                    .catch((e) => setServerStatus(e.response ? 'online' : 'offline'));
            });
    }, []);

    const [loginForm, setLoginForm] = useState({ email: '', password: '' });
    const [regForm, setRegForm] = useState({
        companyName: '',
        companyEmail: '',
        contactPerson: '',
        password: '',
        confirmPassword: '',
        gstNumber: '',
        monthlyBudgetPerEmployee: 1000,
    });

    const switchTab = (t) => { setTab(t); setError(''); setSuccess(''); };

    const parseError = (err) => {
        if (!err.response) {
            return 'Cannot reach the server. It may be starting up — please wait 20 seconds and try again.';
        }
        const msg = err.response?.data?.message;
        if (!msg || typeof msg !== 'string') {
            return `Server error (${err.response.status}). Please try again.`;
        }
        return msg;
    };

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const res = await axios.post(`${API_BASE_URL}/corporate/login`, {
                email: loginForm.email,
                password: loginForm.password,
            });
            if (res.data.statusCode === 200) {
                localStorage.setItem('corp_company', JSON.stringify(res.data.data));
                navigate('/corporate/dashboard');
            }
        } catch (err) {
            setError(parseError(err));
        } finally {
            setLoading(false);
        }
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setError('');
        if (regForm.password !== regForm.confirmPassword) {
            setError('Passwords do not match.');
            return;
        }
        if (regForm.password.length < 6) {
            setError('Password must be at least 6 characters.');
            return;
        }
        setLoading(true);
        try {
            const { confirmPassword, ...payload } = regForm;
            const res = await axios.post(`${API_BASE_URL}/corporate/register`, payload);
            if (res.data.statusCode === 201) {
                localStorage.setItem('corp_company', JSON.stringify(res.data.data));
                setSuccess('Company registered successfully! Redirecting to your dashboard…');
                setTimeout(() => navigate('/corporate/dashboard'), 1800);
            }
        } catch (err) {
            const msg = parseError(err);
            if (msg.toLowerCase().includes('already exists') || msg.toLowerCase().includes('already registered')) {
                setError(msg + ' Try signing in instead.');
                setTimeout(() => switchTab('login'), 3000);
            } else {
                setError(msg);
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-[#0A0A0A] text-white flex">

            {/* ── Left branding panel (desktop only) ── */}
            <div className="hidden lg:flex lg:w-[45%] xl:w-[42%] bg-[#0D0D0D] border-r border-white/5 flex-col justify-between p-10 xl:p-14 relative overflow-hidden">
                <div className="absolute -top-32 -right-32 w-96 h-96 bg-[#F7D100]/6 rounded-full blur-3xl pointer-events-none" />
                <div className="absolute -bottom-24 -left-16 w-80 h-80 bg-[#F7D100]/4 rounded-full blur-3xl pointer-events-none" />

                <div className="relative z-10">
                    <Link to="/" className="inline-flex items-center gap-2 text-gray-500 hover:text-[#F7D100] text-[11px] font-bold uppercase tracking-widest transition-colors">
                        <ArrowLeft size={13} /> Back to Home
                    </Link>
                </div>

                <div className="relative z-10 space-y-10">
                    <div className="space-y-4">
                        <img src={logo} alt="Go-Easy" className="h-12 w-auto object-contain" />
                        <div>
                            <h1 className="text-4xl xl:text-5xl font-black tracking-tight leading-[1.1]">
                                Corporate<br /><span className="text-[#F7D100]">Portal</span>
                            </h1>
                            <p className="text-gray-400 text-sm leading-relaxed mt-3 max-w-xs">
                                Streamline employee commutes with managed ride credits, budget controls, and live analytics.
                            </p>
                        </div>
                    </div>

                    <div className="space-y-3">
                        {FEATURES.map(({ icon: Icon, title, desc }) => (
                            <div key={title} className="flex items-start gap-4 p-4 rounded-2xl bg-white/[0.03] border border-white/5 hover:border-white/10 transition-colors">
                                <div className="w-9 h-9 rounded-xl bg-[#F7D100]/10 flex items-center justify-center shrink-0 mt-0.5">
                                    <Icon size={16} className="text-[#F7D100]" />
                                </div>
                                <div>
                                    <p className="text-white font-semibold text-sm leading-snug">{title}</p>
                                    <p className="text-gray-500 text-xs mt-1 leading-relaxed">{desc}</p>
                                </div>
                            </div>
                        ))}
                    </div>
                </div>

                <p className="relative z-10 text-gray-700 text-[11px]">© 2025 Go-Easy. All rights reserved.</p>
            </div>

            {/* ── Right form panel ── */}
            <div className="flex-1 flex flex-col items-center justify-center p-6 sm:p-8 lg:p-12 overflow-y-auto">

                {/* Mobile: back + logo */}
                <div className="lg:hidden w-full max-w-sm mb-8">
                    <Link to="/" className="inline-flex items-center gap-2 text-gray-500 hover:text-[#F7D100] text-[11px] font-bold uppercase tracking-widest transition-colors mb-6">
                        <ArrowLeft size={13} /> Back
                    </Link>
                    <div className="flex flex-col items-center text-center">
                        <img src={logo} alt="Go-Easy" className="h-10 w-auto object-contain mb-3" />
                        <h1 className="text-2xl font-black tracking-tight">Corporate <span className="text-[#F7D100]">Portal</span></h1>
                        <p className="text-gray-500 text-[10px] font-bold uppercase tracking-widest mt-1">Manage employee ride credits</p>
                    </div>
                </div>

                <div className="w-full max-w-sm">

                    {/* Desktop heading */}
                    <div className="hidden lg:block mb-7">
                        <h2 className="text-2xl font-black tracking-tight">
                            {tab === 'login' ? 'Welcome back' : 'Create account'}
                        </h2>
                        <p className="text-gray-500 text-sm mt-1">
                            {tab === 'login'
                                ? 'Sign in to your company account'
                                : 'Register your company on Go-Easy Corporate'}
                        </p>
                    </div>

                    {/* Tab switcher */}
                    <div className="flex bg-white/5 rounded-xl p-1 border border-white/5 mb-6">
                        {['login', 'register'].map((t) => (
                            <button
                                key={t}
                                onClick={() => switchTab(t)}
                                className={`flex-1 py-2.5 rounded-lg text-[11px] font-bold uppercase tracking-widest transition-all ${
                                    tab === t
                                        ? 'bg-[#F7D100] text-black shadow-md'
                                        : 'text-gray-500 hover:text-white'
                                }`}
                            >
                                {t === 'login' ? 'Sign In' : 'Register'}
                            </button>
                        ))}
                    </div>

                    {/* Server status */}
                    {serverStatus !== 'online' && (
                        <div className={`flex items-center gap-2 rounded-xl px-4 py-2.5 mb-4 text-xs font-medium border ${
                            serverStatus === 'checking'
                                ? 'bg-yellow-500/10 border-yellow-500/20 text-yellow-400'
                                : 'bg-red-500/10 border-red-500/20 text-red-400'
                        }`}>
                            <span className={`w-2 h-2 rounded-full shrink-0 ${serverStatus === 'checking' ? 'bg-yellow-400 animate-pulse' : 'bg-red-400'}`} />
                            {serverStatus === 'checking'
                                ? 'Connecting to server…'
                                : 'Server is offline or starting up. Please wait 20s and retry.'}
                        </div>
                    )}

                    {/* Alerts */}
                    {error && (
                        <div className="flex items-start gap-3 bg-red-500/10 border border-red-500/20 rounded-xl px-4 py-3 mb-5">
                            <AlertCircle size={14} className="text-red-400 mt-0.5 shrink-0" />
                            <p className="text-red-400 text-xs font-medium leading-relaxed">{error}</p>
                        </div>
                    )}
                    {success && (
                        <div className="flex items-start gap-3 bg-green-500/10 border border-green-500/20 rounded-xl px-4 py-3 mb-5">
                            <CheckCircle size={14} className="text-green-400 mt-0.5 shrink-0" />
                            <p className="text-green-400 text-xs font-medium">{success}</p>
                        </div>
                    )}

                    {/* ── LOGIN FORM ── */}
                    {tab === 'login' ? (
                        <form onSubmit={handleLogin} className="space-y-4">
                            <InputField label="Company Email" icon={Mail}>
                                <div className="relative">
                                    <input
                                        value={loginForm.email}
                                        onChange={(e) => setLoginForm({ ...loginForm, email: e.target.value })}
                                        type="email"
                                        placeholder="admin@company.com"
                                        className="premium-input pl-5"
                                        required
                                    />
                                </div>
                            </InputField>

                            <InputField label="Password" icon={Lock}>
                                <div className="relative">
                                    <input
                                        value={loginForm.password}
                                        onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })}
                                        type={showPwd ? 'text' : 'password'}
                                        placeholder="Enter your password"
                                        className="premium-input pr-11"
                                        required
                                    />
                                    <button
                                        type="button"
                                        onClick={() => setShowPwd(!showPwd)}
                                        className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-500 hover:text-white transition-colors"
                                    >
                                        {showPwd ? <EyeOff size={15} /> : <Eye size={15} />}
                                    </button>
                                </div>
                            </InputField>

                            <button type="submit" disabled={loading} className="premium-button w-full mt-1">
                                {loading ? <><Spinner /> Signing in…</> : <>Sign In <ChevronRight size={14} /></>}
                            </button>

                            <p className="text-center text-xs text-gray-600 pt-1">
                                New to Go-Easy Corporate?{' '}
                                <button type="button" onClick={() => switchTab('register')} className="text-[#F7D100] font-semibold hover:underline">
                                    Register your company
                                </button>
                            </p>
                        </form>

                    ) : (
                    /* ── REGISTER FORM ── */
                        <form onSubmit={handleRegister} className="space-y-4">
                            <InputField label="Company Name" icon={Briefcase}>
                                <input
                                    value={regForm.companyName}
                                    onChange={(e) => setRegForm({ ...regForm, companyName: e.target.value })}
                                    placeholder="Acme Technologies Pvt. Ltd."
                                    className="premium-input"
                                    required
                                />
                            </InputField>

                            <InputField label="Company Email" icon={Mail}>
                                <input
                                    value={regForm.companyEmail}
                                    onChange={(e) => setRegForm({ ...regForm, companyEmail: e.target.value })}
                                    type="email"
                                    placeholder="admin@company.com"
                                    className="premium-input"
                                    required
                                />
                            </InputField>

                            <InputField label="Contact Person" icon={User}>
                                <input
                                    value={regForm.contactPerson}
                                    onChange={(e) => setRegForm({ ...regForm, contactPerson: e.target.value })}
                                    placeholder="Full name of admin"
                                    className="premium-input"
                                    required
                                />
                            </InputField>

                            <div className="grid grid-cols-2 gap-3">
                                <InputField label="Password" icon={Lock}>
                                    <div className="relative">
                                        <input
                                            value={regForm.password}
                                            onChange={(e) => setRegForm({ ...regForm, password: e.target.value })}
                                            type={showRegPwd ? 'text' : 'password'}
                                            placeholder="Min. 6 chars"
                                            className="premium-input pr-10"
                                            required
                                        />
                                        <button type="button" onClick={() => setShowRegPwd(!showRegPwd)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500 hover:text-white transition-colors">
                                            {showRegPwd ? <EyeOff size={13} /> : <Eye size={13} />}
                                        </button>
                                    </div>
                                </InputField>

                                <InputField label="Confirm" icon={Lock}>
                                    <input
                                        value={regForm.confirmPassword}
                                        onChange={(e) => setRegForm({ ...regForm, confirmPassword: e.target.value })}
                                        type={showRegPwd ? 'text' : 'password'}
                                        placeholder="Repeat password"
                                        className="premium-input"
                                        required
                                    />
                                </InputField>
                            </div>

                            <InputField label="GST Number" icon={CreditCard} optional>
                                <input
                                    value={regForm.gstNumber}
                                    onChange={(e) => setRegForm({ ...regForm, gstNumber: e.target.value })}
                                    placeholder="22AAAAA0000A1Z5"
                                    className="premium-input"
                                />
                            </InputField>

                            {/* Budget slider */}
                            <div className="space-y-2 bg-white/[0.03] border border-white/5 rounded-2xl p-4">
                                <div className="flex items-center justify-between">
                                    <span className="text-[11px] font-bold text-gray-400 uppercase tracking-wider">Monthly Budget / Employee</span>
                                    <span className="text-[#F7D100] font-black text-base">₹{regForm.monthlyBudgetPerEmployee.toLocaleString('en-IN')}</span>
                                </div>
                                <input
                                    type="range"
                                    min="100"
                                    max="5000"
                                    step="100"
                                    value={regForm.monthlyBudgetPerEmployee}
                                    onChange={(e) => setRegForm({ ...regForm, monthlyBudgetPerEmployee: parseFloat(e.target.value) })}
                                    className="w-full h-1.5 rounded-full appearance-none cursor-pointer accent-[#F7D100] bg-white/10"
                                />
                                <div className="flex justify-between text-[10px] text-gray-600">
                                    <span>₹100</span>
                                    <span>₹5,000</span>
                                </div>
                            </div>

                            <button type="submit" disabled={loading} className="premium-button w-full">
                                {loading
                                    ? <><Spinner /> Registering…</>
                                    : <><Building2 size={14} /> Register Company <ChevronRight size={14} /></>}
                            </button>

                            <p className="text-center text-xs text-gray-600 pt-1">
                                Already registered?{' '}
                                <button type="button" onClick={() => switchTab('login')} className="text-[#F7D100] font-semibold hover:underline">
                                    Sign in here
                                </button>
                            </p>
                        </form>
                    )}
                </div>
            </div>
        </div>
    );
};

export default CorporateLogin;
