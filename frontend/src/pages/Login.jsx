import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { User, Car } from 'lucide-react';
import logo from '../assets/logo.svg';
import PublicNavbar from '../components/PublicNavbar';
import Footer from '../components/Footer';

const Login = () => {
    const [isDriver, setIsDriver] = useState(false);
    const [mobile, setMobile] = useState('');
    const [password, setPassword] = useState('');
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const role = isDriver ? 'DRIVER' : 'USER';
            const success = await login(mobile, password, role);
            if (success) {
                navigate(isDriver ? '/driver' : '/home');
            }
        } catch (err) {
            alert("Login failed! Please check credentials.");
        }
    };

    return (
        <div className="flex flex-col min-h-screen bg-[#0A0A0A]">
            <PublicNavbar />
            <main className="flex-grow flex flex-col items-center justify-center p-6 relative overflow-hidden mt-16 sm:mt-20">
                {/* Minimalist Background Elements */}
                <div className="absolute top-0 right-0 w-full h-full opacity-5 pointer-events-none" style={{ backgroundImage: 'radial-gradient(#F7D100 0.5px, transparent 0.5px)', backgroundSize: '24px 24px' }}></div>
            <div className="absolute top-[-20%] left-[-10%] w-[600px] h-[600px] bg-[#F7D100]/5 rounded-full blur-[120px] pointer-events-none"></div>

            <div className="w-full max-w-md relative z-10 animate-fade-in">
                {/* Logo Section */}
                <div className="flex flex-col items-center mb-12">
                    <img src={logo} alt="Go-Easy" className="h-36 sm:h-40 w-auto mb-2 object-contain drop-shadow-2xl" />
                    <p className="text-gray-500 text-[10px] font-black uppercase tracking-[0.4em] mt-3">Urban Mobility Partner</p>
                </div>

                <div className="glass-card rounded-[2.5rem] p-8 md:p-10 border-white/5 shadow-2xl">
                    {/* Role Toggle */}
                    <div className="flex bg-black p-1.5 rounded-2xl mb-10 gap-2 border border-white/5">
                        <button
                            onClick={() => setIsDriver(false)}
                            className={`flex-1 py-3.5 rounded-xl font-bold transition-all duration-300 ${!isDriver ? 'bg-[#F7D100] text-black shadow-lg shadow-[#F7D100]/20' : 'text-gray-500 hover:text-white'}`}
                        >
                            Customer
                        </button>
                        <button
                            onClick={() => setIsDriver(true)}
                            className={`flex-1 py-3.5 rounded-xl font-bold transition-all duration-300 ${isDriver ? 'bg-[#F7D100] text-black shadow-lg shadow-[#F7D100]/20' : 'text-gray-500 hover:text-white'}`}
                        >
                            Driver
                        </button>
                    </div>

                    {/* Login Form */}
                    <form onSubmit={handleLogin} className="space-y-6">
                        <div className="space-y-2">
                            <label className="text-xs font-black uppercase tracking-[0.2em] text-gray-400 ml-1">Mobile Number</label>
                            <input
                                type="text"
                                value={mobile}
                                onChange={(e) => setMobile(e.target.value)}
                                className="premium-input"
                                placeholder="Email or Mobile"
                                required
                            />
                        </div>
                        <div className="space-y-2">
                            <label className="text-xs font-black uppercase tracking-[0.2em] text-gray-400 ml-1">Password</label>
                            <input
                                type="password"
                                value={password}
                                onChange={(e) => setPassword(e.target.value)}
                                className="premium-input"
                                placeholder="••••••••"
                                required
                            />
                        </div>

                        <div className="flex justify-end pr-1">
                            <a href="#" className="text-xs text-gray-500 hover:text-[#F7D100] font-bold tracking-tight transition-colors">Forgot Password?</a>
                        </div>

                        <button type="submit" className="premium-button w-full mt-4">
                            LOG IN NOW
                        </button>
                    </form>

                    <div className="mt-10 pt-8 border-t border-white/5 flex flex-col items-center gap-4">
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
