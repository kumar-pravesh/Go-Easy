import { useState, useEffect } from 'react';
import axios from 'axios';
import API_BASE_URL from '../api';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { Eye, EyeOff, AlertCircle, Wifi, WifiOff } from 'lucide-react';
import logo from '../assets/logo.svg';
import PublicNavbar from '../components/PublicNavbar';
import Footer from '../components/Footer';

const Spinner = () => (
    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
    </svg>
);

const Login = () => {
    const [isDriver, setIsDriver] = useState(false);
    const [mobile, setMobile] = useState('');
    const [password, setPassword] = useState('');
    const [showPwd, setShowPwd] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [serverStatus, setServerStatus] = useState('checking'); // 'checking' | 'online' | 'slow' | 'offline'
    const { login } = useAuth();
    const navigate = useNavigate();

    // Wake up the Render backend as soon as the page loads.
    // By the time the user finishes typing, the server is already warm.
    useEffect(() => {
        const controller = new AbortController();
        const timer = setTimeout(() => setServerStatus('slow'), 5000);

        axios.post(`${API_BASE_URL}/auth/login`, {}, { signal: controller.signal, timeout: 30000 })
            .catch((err) => {
                if (axios.isCancel(err)) return;
                // Any HTTP response (even 4xx) means the server is awake
                setServerStatus(err.response ? 'online' : 'offline');
            })
            .then(() => {
                setServerStatus('online');
                clearTimeout(timer);
            });

        return () => { controller.abort(); clearTimeout(timer); };
    }, []);

    const handleLogin = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const success = await login(mobile, password, isDriver ? 'DRIVER' : 'USER');
            if (success) {
                // If user role is ADMIN, login sets the user. 
                // We'll need a way to know if they are an admin.
                // Currently `login` returns a boolean. Let's redirect based on user role if available, but `user` context might not update synchronously.
                // The `AuthContext`'s `login` function saves role to localStorage. Let's check it.
                const storedRole = localStorage.getItem('role');
                if (storedRole === 'ADMIN') {
                    navigate('/admin');
                } else {
                    navigate(isDriver ? '/driver' : '/home');
                }
            }
        } catch (err) {
            if (!err.response) {
                setError('Cannot reach server. It may still be starting up — please wait a moment and try again.');
            } else {
                const msg = err.response?.data?.message;
                setError(msg && typeof msg === 'string' ? msg : 'Invalid credentials. Please check and try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    const serverBanner = {
        checking: { color: 'bg-yellow-500/10 border-yellow-500/20 text-yellow-400', dot: 'bg-yellow-400 animate-pulse', icon: Wifi, text: 'Connecting to server…' },
        slow:     { color: 'bg-orange-500/10 border-orange-500/20 text-orange-400', dot: 'bg-orange-400 animate-pulse', icon: Wifi, text: 'Server is starting up (Render free-tier). This may take ~30 seconds…' },
        offline:  { color: 'bg-red-500/10 border-red-500/20 text-red-400',          dot: 'bg-red-400',                   icon: WifiOff, text: 'Server offline. Please wait and retry.' },
        online:   null,
    }[serverStatus];

    return (
        <div className="flex flex-col min-h-screen bg-[#0A0A0A]">
            <PublicNavbar />
            <main className="flex-grow flex flex-col items-center justify-center p-6 relative overflow-hidden mt-16 sm:mt-20">
                <div className="absolute top-0 right-0 w-full h-full opacity-5 pointer-events-none" style={{ backgroundImage: 'radial-gradient(#F7D100 0.5px, transparent 0.5px)', backgroundSize: '24px 24px' }} />
                <div className="absolute top-[-20%] left-[-10%] w-[600px] h-[600px] bg-[#F7D100]/5 rounded-full blur-[120px] pointer-events-none" />

                <div className="w-full max-w-md relative z-10 animate-fade-in">
                    {/* Logo */}
                    <div className="flex flex-col items-center mb-10">
                        <img src={logo} alt="Go-Easy" className="h-36 sm:h-40 w-auto mb-2 object-contain drop-shadow-2xl" />
                        <p className="text-gray-500 text-[10px] font-black uppercase tracking-[0.4em] mt-3">Urban Mobility Partner</p>
                    </div>

                    {/* Server status banner */}
                    {serverBanner && (
                        <div className={`flex items-center gap-3 rounded-2xl px-4 py-3 mb-5 border text-xs font-medium ${serverBanner.color}`}>
                            <span className={`w-2 h-2 rounded-full shrink-0 ${serverBanner.dot}`} />
                            {serverBanner.text}
                        </div>
                    )}

                    <div className="glass-card rounded-[2.5rem] p-8 md:p-10 border-white/5 shadow-2xl">
                        {/* Role toggle */}
                        <div className="flex bg-black p-1.5 rounded-2xl mb-8 gap-2 border border-white/5">
                            <button onClick={() => { setIsDriver(false); setError(''); }}
                                className={`flex-1 py-3.5 rounded-xl font-bold transition-all duration-300 ${!isDriver ? 'bg-[#F7D100] text-black shadow-lg shadow-[#F7D100]/20' : 'text-gray-500 hover:text-white'}`}>
                                Customer
                            </button>
                            <button onClick={() => { setIsDriver(true); setError(''); }}
                                className={`flex-1 py-3.5 rounded-xl font-bold transition-all duration-300 ${isDriver ? 'bg-[#F7D100] text-black shadow-lg shadow-[#F7D100]/20' : 'text-gray-500 hover:text-white'}`}>
                                Driver
                            </button>
                        </div>

                        {/* Inline error */}
                        {error && (
                            <div className="flex items-start gap-3 bg-red-500/10 border border-red-500/20 rounded-2xl px-4 py-3 mb-6">
                                <AlertCircle size={14} className="text-red-400 mt-0.5 shrink-0" />
                                <p className="text-red-400 text-xs font-medium leading-relaxed">{error}</p>
                            </div>
                        )}

                        <form onSubmit={handleLogin} className="space-y-5">
                            <div className="space-y-1.5">
                                <label className="text-xs font-black uppercase tracking-[0.2em] text-gray-400 ml-1">Mobile Number or Email</label>
                                <input
                                    type="text"
                                    value={mobile}
                                    onChange={(e) => setMobile(e.target.value)}
                                    className="premium-input"
                                    placeholder="10-digit mobile or email"
                                    required
                                />
                            </div>

                            <div className="space-y-1.5">
                                <label className="text-xs font-black uppercase tracking-[0.2em] text-gray-400 ml-1">Password</label>
                                <div className="relative">
                                    <input
                                        type={showPwd ? 'text' : 'password'}
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        className="premium-input pr-11"
                                        placeholder="••••••••"
                                        required
                                    />
                                    <button type="button" onClick={() => setShowPwd(!showPwd)}
                                        className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-500 hover:text-white transition-colors">
                                        {showPwd ? <EyeOff size={16} /> : <Eye size={16} />}
                                    </button>
                                </div>
                            </div>

                            <div className="flex justify-end pr-1">
                                <a href="#" className="text-xs text-gray-500 hover:text-[#F7D100] font-bold tracking-tight transition-colors">Forgot Password?</a>
                            </div>

                            <button type="submit" disabled={loading} className="premium-button w-full mt-2">
                                {loading ? (
                                    <>
                                        <Spinner />
                                        {serverStatus !== 'online' ? 'Server starting up…' : 'Logging in…'}
                                    </>
                                ) : 'Log In Now'}
                            </button>
                        </form>

                        <div className="mt-8 pt-6 border-t border-white/5 flex flex-col items-center gap-3">
                            <p className="text-gray-500 text-sm font-medium">Don't have an account?</p>
                            <Link to="/register" className="text-[#F7D100] font-black uppercase tracking-[0.1em] text-xs hover:text-white transition-all border-b border-[#F7D100]/30 pb-0.5">
                                Sign Up Now
                            </Link>
                        </div>
                    </div>
                </div>
            </main>
            <Footer />
        </div>
    );
};

export default Login;
