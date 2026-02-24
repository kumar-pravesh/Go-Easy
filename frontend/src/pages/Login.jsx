import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { User, Car } from 'lucide-react';
import logo from '../assets/logo.png';

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
        <div className="min-h-screen bg-white flex flex-col items-center justify-center p-6 font-sans">
            <div className="w-full max-w-sm flex flex-col items-center mb-6">
                {/* Logo Section */}
                <div className="mb-8 transform hover:scale-105 transition-transform duration-300">
                    <img src={logo} alt="Go-Easy" className="h-40 object-contain" />
                </div>

                {/* Toggle Buttons */}
                <div className="w-full space-y-3 mb-8">
                    <button
                        onClick={() => setIsDriver(false)}
                        className={`w-full py-3 rounded-xl font-bold transition-all border-2 flex items-center justify-center ${!isDriver ? 'bg-[#5D5FEF] text-white border-[#5D5FEF] shadow-lg shadow-indigo-200' : 'bg-white text-gray-500 border-gray-200 hover:border-[#5D5FEF] hover:text-[#5D5FEF]'}`}
                    >
                        Login as a Customer
                    </button>
                    <button
                        onClick={() => setIsDriver(true)}
                        className={`w-full py-3 rounded-xl font-bold transition-all border-2 flex items-center justify-center ${isDriver ? 'bg-[#5D5FEF] text-white border-[#5D5FEF] shadow-lg shadow-indigo-200' : 'bg-white text-gray-500 border-gray-200 hover:border-[#5D5FEF] hover:text-[#5D5FEF]'}`}
                    >
                        Login as a Driver
                    </button>
                </div>

                {/* Divider/Connect */}
                <div className="relative w-full flex items-center justify-center mb-8">
                    <div className="absolute left-0 w-full h-px bg-gray-200"></div>
                    <span className="bg-white px-4 text-sm text-gray-400 font-medium relative z-10">Sign In</span>
                </div>

                {/* Login Form */}
                <form onSubmit={handleLogin} className="w-full space-y-5">
                    <div>
                        <input
                            type="text"
                            value={mobile}
                            onChange={(e) => setMobile(e.target.value)}
                            className="w-full bg-white text-gray-800 border-2 border-gray-100 rounded-xl py-3.5 px-4 focus:outline-none focus:border-[#5D5FEF] focus:ring-4 focus:ring-indigo-50 font-medium placeholder-gray-400 transition-all"
                            placeholder="Mobile Number or Email"
                            required
                        />
                    </div>
                    <div>
                        <input
                            type="password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            className="w-full bg-white text-gray-800 border-2 border-gray-100 rounded-xl py-3.5 px-4 focus:outline-none focus:border-[#5D5FEF] focus:ring-4 focus:ring-indigo-50 font-medium placeholder-gray-400 transition-all"
                            placeholder="Password"
                            required
                        />
                    </div>

                    <div className="flex justify-between items-center">
                        <a href="#" className="text-sm text-gray-400 hover:text-[#5D5FEF] font-medium transition-colors">Forgot password?</a>
                    </div>

                    <button type="submit" className="w-full bg-[#5D5FEF] hover:bg-[#4B4DDF] text-white font-bold py-4 rounded-xl transition-all shadow-xl shadow-indigo-200 flex items-center justify-center text-lg mt-4">
                        Submit
                    </button>
                </form>

                <div className="mt-8 flex flex-col items-center space-y-4">
                    <div className="flex items-center space-x-2">
                        <span className="text-gray-500 font-medium">Don't have an account yet?</span>
                    </div>
                    <Link to="/register" className="text-[#5D5FEF] font-bold hover:underline">Sign up</Link>
                </div>
            </div>
        </div>
    );
};

export default Login;
