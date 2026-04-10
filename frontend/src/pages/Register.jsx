import { useState } from 'react';
import axios from 'axios';
import API_BASE_URL from '../api';
import { useNavigate, Link } from 'react-router-dom';
import { User, Car, ArrowLeft } from 'lucide-react';
import logo from '../assets/logo.svg';
import PublicNavbar from '../components/PublicNavbar';
import Footer from '../components/Footer';

const Register = () => {
    const [isDriver, setIsDriver] = useState(false);
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        name: '',
        email: '',
        phone: '',
        password: '',
        gender: 'Male',
        age: '',
        license: '',
        vehicleModel: '',
        vehicleNumber: '',
        vehicleType: 'Car',
        pricePerKm: ''
    });

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            if (isDriver) {
                const payload = {
                    dname: formData.name,
                    mobNo: formData.phone,
                    password: formData.password,
                    mailId: formData.email,
                    gender: formData.gender,
                    age: formData.age,
                    licNo: formData.license,
                    upiId: 'demo@upi',
                    dstatus: 'AVAILABLE',
                    vehicle: {
                        vehicleModel: formData.vehicleModel,
                        vehicleNumber: formData.vehicleNumber,
                        vehicleType: formData.vehicleType,
                        pricePerKm: parseFloat(formData.pricePerKm),
                        avgspeed: 45.0,
                        vehicleCapacity: 4,
                        city: "Bangalore",
                        latitude: 12.9716,
                        longitude: 77.5946
                    }
                };
                await axios.post(`${API_BASE_URL}/driver/save`, payload);
            } else {
                const payload = {
                    name: formData.name,
                    mobno: formData.phone,
                    email: formData.email,
                    password: formData.password,
                    gender: formData.gender,
                    age: formData.age,
                    lat: 12.9716,
                    lon: 77.5946
                };
                await axios.post(`${API_BASE_URL}/customer/register/save`, payload);
            }
            alert("Registration Successful! Please Login.");
            navigate('/');
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.message || "Unknown Error";
            alert(`Registration Failed: ${errorMessage}`);
        }
    };

    return (
        <div className="flex flex-col min-h-screen bg-[#0A0A0A] text-white selection:bg-[#F7D100] selection:text-black">
            <PublicNavbar />
            <main className="flex-grow flex flex-col items-center p-4 sm:p-6 overflow-x-hidden mt-16 sm:mt-20">
                <div className="w-full max-w-2xl py-8 sm:py-12 animate-fade-in text-center sm:text-left">
                    <Link to="/login" className="inline-flex items-center gap-2 text-gray-500 hover:text-[#F7D100] font-black text-[10px] uppercase tracking-widest transition-all mb-8 group">
                    <ArrowLeft size={16} className="group-hover:-translate-x-1 transition-transform" /> Back to Login
                </Link>

                <div className="flex flex-col items-center mb-12">
                    <img src={logo} alt="Go-Easy" className="h-28 sm:h-32 w-auto mb-4 object-contain drop-shadow-2xl" />
                    <h1 className="text-4xl sm:text-5xl font-black italic tracking-tighter mb-2">CREATE <span className="text-[#F7D100]">ACCOUNT</span></h1>
                    <p className="text-gray-500 font-bold uppercase text-[10px] tracking-[0.3em]">Enter your details below</p>
                </div>

                <div className="glass-card rounded-[2.5rem] p-1 shadow-[0_0_50px_rgba(0,0,0,0.5)] mb-8 max-w-sm mx-auto sm:mx-0">
                    <div className="flex bg-black/40 rounded-[2.2rem] p-1.5 border border-white/5">
                        <button
                            onClick={() => setIsDriver(false)}
                            className={`flex-1 py-4 rounded-[2rem] font-black uppercase text-[10px] tracking-widest transition-all flex items-center justify-center gap-2 ${!isDriver ? 'bg-[#F7D100] text-black shadow-xl ring-1 ring-white/10' : 'text-gray-500 hover:text-white'}`}
                        >
                            <User size={16} /> Customer
                        </button>
                        <button
                            onClick={() => setIsDriver(true)}
                            className={`flex-1 py-4 rounded-[2rem] font-black uppercase text-[10px] tracking-widest transition-all flex items-center justify-center gap-2 ${isDriver ? 'bg-[#F7D100] text-black shadow-xl ring-1 ring-white/10' : 'text-gray-500 hover:text-white'}`}
                        >
                            <Car size={16} /> Driver
                        </button>
                    </div>
                </div>

                <form onSubmit={handleRegister} className="glass-card rounded-[3rem] p-6 sm:p-12 border-white/5 relative overflow-hidden">
                    <div className="absolute top-0 right-0 w-64 h-64 bg-[#F7D100]/5 rounded-full -mr-32 -mt-32 blur-3xl"></div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4 sm:gap-6 relative z-10">
                        <div className="md:col-span-2 flex items-center gap-3 mb-2">
                            <div className="h-[1px] flex-1 bg-white/10"></div>
                            <span className="text-[10px] font-black text-gray-600 uppercase tracking-[0.4em]">Personal Details</span>
                            <div className="h-[1px] flex-1 bg-white/10"></div>
                        </div>

                        <input name="name" placeholder="FULL NAME" onChange={handleChange} className="premium-input" required />
                        <input name="email" type="email" placeholder="EMAIL ADDRESS" onChange={handleChange} className="premium-input" required />
                        <input name="phone" type="number" placeholder="MOBILE NUMBER" onChange={handleChange} className="premium-input" required />
                        <input name="password" type="password" placeholder="PASSWORD" onChange={handleChange} className="premium-input" required />
                        <input name="age" type="number" placeholder="AGE" onChange={handleChange} className="premium-input" required />
                        
                        <div className="relative group">
                            <select name="gender" onChange={handleChange} className="w-full premium-input appearance-none cursor-pointer">
                                <option value="Male" className="bg-[#0A0A0A]">Male</option>
                                <option value="Female" className="bg-[#0A0A0A]">Female</option>
                            </select>
                            <div className="absolute right-5 top-1/2 transform -translate-y-1/2 pointer-events-none text-gray-500 text-[8px] group-hover:text-[#F7D100] transition-colors">▼</div>
                        </div>

                        {isDriver && (
                            <>
                                <div className="md:col-span-2 flex items-center gap-3 mt-8 mb-2">
                                    <div className="h-[1px] flex-1 bg-white/10"></div>
                                    <span className="text-[10px] font-black text-gray-600 uppercase tracking-[0.4em]">Vehicle Details</span>
                                    <div className="h-[1px] flex-1 bg-white/10"></div>
                                </div>
                                <input name="license" placeholder="DRIVING LICENSE" onChange={handleChange} className="premium-input" required />
                                <input name="vehicleModel" placeholder="VEHICLE MODEL" onChange={handleChange} className="premium-input" required />
                                <input name="vehicleNumber" placeholder="VEHICLE NUMBER (RC)" onChange={handleChange} className="premium-input" required />
                                <input name="pricePerKm" type="number" placeholder="PRICE PER KM (₹)" onChange={handleChange} className="premium-input" required />
                            </>
                        )}

                        <button 
                            type="submit" 
                            className="md:col-span-2 mt-8 bg-[#F7D100] text-black font-black py-5 rounded-2xl transition-all shadow-2xl shadow-[#F7D100]/20 flex items-center justify-center text-[12px] uppercase tracking-[0.2em] transform active:scale-[0.98]"
                        >
                            Establish Connection
                        </button>
                    </div>
                </form>

                <p className="mt-12 text-center text-gray-600 font-bold text-[10px] uppercase tracking-widest">
                    Already have an account? <Link to="/login" className="text-[#F7D100] hover:underline ml-2">Login Here</Link>
                </p>
            </div>
            </main>
            <Footer />
        </div>
    );
};
export default Register;
