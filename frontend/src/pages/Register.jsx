import { useState, useEffect } from 'react';
import axios from 'axios';
import API_BASE_URL from '../api';
import { useNavigate, Link } from 'react-router-dom';
import { User, Car, ArrowLeft, LocateFixed, AlertCircle, CheckCircle } from 'lucide-react';
import logo from '../assets/logo.svg';
import PublicNavbar from '../components/PublicNavbar';
import Footer from '../components/Footer';

const Spinner = () => (
    <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24" fill="none">
        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
    </svg>
);

const Register = () => {
    const [isDriver, setIsDriver] = useState(false);
    const navigate = useNavigate();
    const [locLoading, setLocLoading] = useState(false);
    const [coords, setCoords] = useState({ lat: null, lon: null });
    const [locationLabel, setLocationLabel] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [serverStatus, setServerStatus] = useState('checking');

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
        vehicleName: '',
        fuelType: 'PETROL',
        pricePerKm: '',
        upiId: '',
        ridePreference: 'ANY',
    });

    // Wake up Render backend + auto-detect location on page load
    useEffect(() => {
        detectLocation();
        axios.post(`${API_BASE_URL}/auth/login`, {}, { timeout: 30000 })
            .catch((err) => setServerStatus(err.response ? 'online' : 'slow'));
    }, []);

    const detectLocation = () => {
        if (!navigator.geolocation) return;
        setLocLoading(true);
        navigator.geolocation.getCurrentPosition(
            async (pos) => {
                const { latitude, longitude } = pos.coords;
                setCoords({ lat: latitude, lon: longitude });
                try {
                    const res = await axios.get(
                        `${API_BASE_URL}/customer/getCity?lat=${latitude}&lon=${longitude}`
                    );
                    if (res.data.statusCode === 200) setLocationLabel(res.data.data);
                    else setLocationLabel(`${latitude.toFixed(4)}, ${longitude.toFixed(4)}`);
                } catch {
                    setLocationLabel(`${latitude.toFixed(4)}, ${longitude.toFixed(4)}`);
                } finally {
                    setLocLoading(false);
                }
            },
            () => {
                setLocLoading(false);
            }
        );
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleRegister = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        setLoading(true);

        const lat = coords.lat ?? 12.9716;
        const lon = coords.lon ?? 77.5946;

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
                    upiId: formData.upiId || 'notset@upi',
                    dstatus: 'AVAILABLE',
                    ridePreference: formData.ridePreference,
                    vehicle: {
                        vehicleName: formData.vehicleName || formData.vehicleModel,
                        vehicleModel: formData.vehicleModel,
                        vehicleNumber: formData.vehicleNumber,
                        vehicleType: formData.vehicleType,
                        fuelType: formData.fuelType,
                        pricePerKm: parseFloat(formData.pricePerKm),
                        avgspeed: 45.0,
                        vehicleCapacity: 4,
                        latitude: lat,
                        longitude: lon,
                    },
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
                    lat,
                    lon,
                };
                await axios.post(`${API_BASE_URL}/customer/register/save`, payload);
            }
            setSuccess('Registration successful! Redirecting to login…');
            setTimeout(() => navigate('/login'), 1800);
        } catch (err) {
            if (!err.response) {
                setError('Cannot reach server. It may still be starting up — please wait a moment and try again.');
            } else {
                const msg = err.response?.data?.message;
                setError(msg && typeof msg === 'string' ? msg : 'Registration failed. Please check your details and try again.');
            }
        } finally {
            setLoading(false);
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

                    <div className="flex flex-col items-center mb-10">
                        <img src={logo} alt="Go-Easy" className="h-28 sm:h-32 w-auto mb-4 object-contain drop-shadow-2xl" />
                        <h1 className="text-4xl sm:text-5xl font-black italic tracking-tighter mb-2">CREATE <span className="text-[#F7D100]">ACCOUNT</span></h1>
                        <p className="text-gray-500 font-bold uppercase text-[10px] tracking-[0.3em]">Enter your details below</p>
                    </div>

                    {/* Server status banner */}
                    {serverStatus !== 'online' && (
                        <div className="flex items-center gap-3 rounded-2xl px-4 py-3 mb-5 border bg-yellow-500/10 border-yellow-500/20 text-yellow-400 text-xs font-medium">
                            <span className="w-2 h-2 rounded-full bg-yellow-400 animate-pulse shrink-0" />
                            {serverStatus === 'slow'
                                ? 'Server is starting up (Render free-tier). This may take ~30 seconds…'
                                : 'Connecting to server…'}
                        </div>
                    )}

                    {/* Inline alerts */}
                    {error && (
                        <div className="flex items-start gap-3 bg-red-500/10 border border-red-500/20 rounded-2xl px-4 py-3 mb-5">
                            <AlertCircle size={14} className="text-red-400 mt-0.5 shrink-0" />
                            <p className="text-red-400 text-xs font-medium leading-relaxed">{error}</p>
                        </div>
                    )}
                    {success && (
                        <div className="flex items-start gap-3 bg-green-500/10 border border-green-500/20 rounded-2xl px-4 py-3 mb-5">
                            <CheckCircle size={14} className="text-green-400 mt-0.5 shrink-0" />
                            <p className="text-green-400 text-xs font-medium">{success}</p>
                        </div>
                    )}

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
                                <div className="absolute right-5 top-1/2 transform -translate-y-1/2 pointer-events-none text-gray-500 text-[8px]">▼</div>
                            </div>

                            {/* Location auto-detect */}
                            <div className="md:col-span-2">
                                <label className="text-[10px] font-black text-gray-500 uppercase tracking-widest ml-1 mb-2 block">Your Location</label>
                                <div className="flex gap-2">
                                    <div className="flex-1 premium-input flex items-center text-sm text-gray-400">
                                        {locLoading ? (
                                            <span className="flex items-center gap-2"><div className="w-3 h-3 border border-[#F7D100] border-t-transparent rounded-full animate-spin"></div> Detecting...</span>
                                        ) : locationLabel ? (
                                            <span className="text-white">{locationLabel}</span>
                                        ) : (
                                            <span className="text-gray-600">Location not detected</span>
                                        )}
                                    </div>
                                    <button type="button" onClick={detectLocation} disabled={locLoading} className="w-14 h-14 bg-white/5 border border-white/5 rounded-2xl flex items-center justify-center hover:bg-[#F7D100]/10 transition-colors">
                                        <LocateFixed size={18} className="text-[#F7D100]" />
                                    </button>
                                </div>
                                {!coords.lat && (
                                    <p className="text-[10px] text-gray-600 mt-1 ml-1">Allow location access for accurate city matching</p>
                                )}
                            </div>

                            {isDriver && (
                                <>
                                    <div className="md:col-span-2 flex items-center gap-3 mt-8 mb-2">
                                        <div className="h-[1px] flex-1 bg-white/10"></div>
                                        <span className="text-[10px] font-black text-gray-600 uppercase tracking-[0.4em]">Vehicle Details</span>
                                        <div className="h-[1px] flex-1 bg-white/10"></div>
                                    </div>

                                    <input name="license" placeholder="DRIVING LICENSE" onChange={handleChange} className="premium-input" required />
                                    <input name="vehicleModel" placeholder="VEHICLE MODEL (e.g. Honda Activa)" onChange={handleChange} className="premium-input" required />
                                    <input name="vehicleNumber" placeholder="VEHICLE NUMBER (RC)" onChange={handleChange} className="premium-input" required />
                                    <input name="pricePerKm" type="number" placeholder="PRICE PER KM (₹)" onChange={handleChange} className="premium-input" required />
                                    <input name="upiId" placeholder="UPI ID (e.g. name@upi)" onChange={handleChange} className="premium-input" />

                                    <div className="relative group">
                                        <select name="vehicleType" onChange={handleChange} className="w-full premium-input appearance-none cursor-pointer">
                                            <option value="Car" className="bg-[#0A0A0A]">Car</option>
                                            <option value="Bike" className="bg-[#0A0A0A]">Bike</option>
                                            <option value="Auto" className="bg-[#0A0A0A]">Auto</option>
                                        </select>
                                        <div className="absolute right-5 top-1/2 transform -translate-y-1/2 pointer-events-none text-gray-500 text-[8px]">▼</div>
                                    </div>

                                    <div className="relative group">
                                        <select name="fuelType" onChange={handleChange} className="w-full premium-input appearance-none cursor-pointer">
                                            <option value="PETROL" className="bg-[#0A0A0A]">Petrol</option>
                                            <option value="DIESEL" className="bg-[#0A0A0A]">Diesel</option>
                                            <option value="CNG" className="bg-[#0A0A0A]">CNG (Green)</option>
                                            <option value="ELECTRIC" className="bg-[#0A0A0A]">Electric (Green)</option>
                                        </select>
                                        <div className="absolute right-5 top-1/2 transform -translate-y-1/2 pointer-events-none text-gray-500 text-[8px]">▼</div>
                                    </div>

                                    <div className="md:col-span-2 relative group">
                                        <label className="text-[10px] font-black text-gray-500 uppercase tracking-widest ml-1 mb-2 block">Ride Preference</label>
                                        <select name="ridePreference" onChange={handleChange} className="w-full premium-input appearance-none cursor-pointer">
                                            <option value="ANY" className="bg-[#0A0A0A]">Any (default)</option>
                                            <option value="SILENT" className="bg-[#0A0A0A]">Silent Ride (no talking)</option>
                                            <option value="FRIENDLY" className="bg-[#0A0A0A]">Friendly (open to chat)</option>
                                        </select>
                                        <div className="absolute right-5 bottom-4 pointer-events-none text-gray-500 text-[8px]">▼</div>
                                    </div>
                                </>
                            )}

                            <button
                                type="submit"
                                disabled={loading}
                                className="md:col-span-2 mt-8 bg-[#F7D100] text-black font-black py-5 rounded-2xl transition-all shadow-2xl shadow-[#F7D100]/20 flex items-center justify-center gap-2 text-[12px] uppercase tracking-[0.2em] transform active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed"
                            >
                                {loading ? (
                                    <>
                                        <Spinner />
                                        {serverStatus !== 'online' ? 'Server starting up…' : 'Registering…'}
                                    </>
                                ) : 'Establish Connection'}
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
