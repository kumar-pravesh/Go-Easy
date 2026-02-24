import { useState } from 'react';
import axios from 'axios';
import { useNavigate, Link } from 'react-router-dom';
import { User, Car, ArrowLeft } from 'lucide-react';
import logo from '../assets/logo.png';

const Register = () => {
    const [isDriver, setIsDriver] = useState(false);
    const navigate = useNavigate();

    // Common Fields
    const [formData, setFormData] = useState({
        name: '',
        email: '',
        phone: '',
        password: '',
        gender: 'Male',
        age: '',
        // Driver Only
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
                        avgspeed: formData.avgSpeed ? parseFloat(formData.avgSpeed) : 45.0, // Default if empty
                        vehicleCapacity: 4, // Default capacity
                        city: "Bangalore", // Default city
                        latitude: 12.9716,
                        longitude: 77.5946
                    }
                };
                await axios.post('http://localhost:8080/driver/save', payload);
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
                await axios.post('http://localhost:8080/customer/register/save', payload);
            }
            alert("Registration Successful! Please Login.");
            navigate('/');
        } catch (error) {
            console.error(error);
            const errorMessage = error.response?.data?.message || error.message || "Unknown Error";
            alert(`Registration Failed: ${errorMessage}`);
        }
    };

    return (
        <div className="min-h-screen bg-white flex flex-col items-center p-6 font-sans">

            <div className="w-full max-w-2xl">
                <Link to="/" className="text-gray-500 hover:text-[#5D5FEF] flex items-center mb-8 font-medium transition-colors w-fit">
                    <ArrowLeft size={20} className="mr-2" /> Back to Login
                </Link>

                <div className="text-center mb-10">
                    <h1 className="text-3xl font-bold text-gray-900 mb-2">Create an Account</h1>
                    <p className="text-gray-500">Get started with Go-Easy today.</p>
                </div>

                <div className="bg-gray-50 p-1.5 rounded-xl mb-8 flex border border-gray-200 shadow-sm">
                    <button
                        onClick={() => setIsDriver(false)}
                        className={`flex-1 py-3 rounded-lg font-bold transition-all flex items-center justify-center ${!isDriver ? 'bg-white text-[#5D5FEF] shadow-md ring-1 ring-gray-100' : 'text-gray-500 hover:text-gray-800'}`}
                    >
                        <User size={18} className="mr-2" /> Customer
                    </button>
                    <button
                        onClick={() => setIsDriver(true)}
                        className={`flex-1 py-3 rounded-lg font-bold transition-all flex items-center justify-center ${isDriver ? 'bg-white text-[#5D5FEF] shadow-md ring-1 ring-gray-100' : 'text-gray-500 hover:text-gray-800'}`}
                    >
                        <Car size={18} className="mr-2" /> Driver
                    </button>
                </div>

                <form onSubmit={handleRegister} className="grid grid-cols-1 md:grid-cols-2 gap-5">

                    <div className="md:col-span-2 text-xs font-bold text-gray-400 uppercase tracking-widest pl-1">Personal Details</div>

                    <input name="name" placeholder="Full Name" onChange={handleChange} className="bg-white text-gray-900 border-2 border-gray-100 rounded-xl p-4 focus:outline-none focus:border-[#5D5FEF] placeholder-gray-400 font-medium transition-all focus:ring-4 focus:ring-indigo-50" required />
                    <input name="email" type="email" placeholder="Email Address" onChange={handleChange} className="bg-white text-gray-900 border-2 border-gray-100 rounded-xl p-4 focus:outline-none focus:border-[#5D5FEF] placeholder-gray-400 font-medium transition-all focus:ring-4 focus:ring-indigo-50" required />
                    <input name="phone" type="number" placeholder="Mobile Number" onChange={handleChange} className="bg-white text-gray-900 border-2 border-gray-100 rounded-xl p-4 focus:outline-none focus:border-[#5D5FEF] placeholder-gray-400 font-medium transition-all focus:ring-4 focus:ring-indigo-50" required />
                    <input name="password" type="password" placeholder="Create Password" onChange={handleChange} className="bg-white text-gray-900 border-2 border-gray-100 rounded-xl p-4 focus:outline-none focus:border-[#5D5FEF] placeholder-gray-400 font-medium transition-all focus:ring-4 focus:ring-indigo-50" required />
                    <input name="age" type="number" placeholder="Age" onChange={handleChange} className="bg-white text-gray-900 border-2 border-gray-100 rounded-xl p-4 focus:outline-none focus:border-[#5D5FEF] placeholder-gray-400 font-medium transition-all focus:ring-4 focus:ring-indigo-50" required />
                    <div className="relative">
                        <select name="gender" onChange={handleChange} className="w-full bg-white text-gray-900 border-2 border-gray-100 rounded-xl p-4 focus:outline-none focus:border-[#5D5FEF] appearance-none font-medium text-gray-500 focus:ring-4 focus:ring-indigo-50 transition-all">
                            <option value="Male">Male</option>
                            <option value="Female">Female</option>
                        </select>
                        <div className="absolute right-4 top-1/2 transform -translate-y-1/2 pointer-events-none text-gray-400">▼</div>
                    </div>

                    {/* Driver Extra Fields */}
                    {isDriver && (
                        <>
                            <div className="md:col-span-2 text-xs font-bold text-gray-400 uppercase tracking-widest pl-1 mt-6 border-t border-gray-100 pt-6">Vehicle Information</div>
                            <input name="license" placeholder="License Number" onChange={handleChange} className="bg-white text-gray-900 border-2 border-gray-100 rounded-xl p-4 focus:outline-none focus:border-[#5D5FEF] placeholder-gray-400 font-medium transition-all focus:ring-4 focus:ring-indigo-50" required />
                            <input name="vehicleModel" placeholder="Vehicle Model (e.g. Swift)" onChange={handleChange} className="bg-white text-gray-900 border-2 border-gray-100 rounded-xl p-4 focus:outline-none focus:border-[#5D5FEF] placeholder-gray-400 font-medium transition-all focus:ring-4 focus:ring-indigo-50" required />
                            <input name="vehicleNumber" placeholder="Vehicle Number" onChange={handleChange} className="bg-white text-gray-900 border-2 border-gray-100 rounded-xl p-4 focus:outline-none focus:border-[#5D5FEF] placeholder-gray-400 font-medium transition-all focus:ring-4 focus:ring-indigo-50" required />
                            <input name="pricePerKm" type="number" placeholder="Price per KM" onChange={handleChange} className="bg-white text-gray-900 border-2 border-gray-100 rounded-xl p-4 focus:outline-none focus:border-[#5D5FEF] placeholder-gray-400 font-medium transition-all focus:ring-4 focus:ring-indigo-50" required />
                            <input name="avgSpeed" type="number" placeholder="Average Speed (km/h)" onChange={handleChange} className="bg-white text-gray-900 border-2 border-gray-100 rounded-xl p-4 focus:outline-none focus:border-[#5D5FEF] placeholder-gray-400 font-medium transition-all focus:ring-4 focus:ring-indigo-50" />
                        </>
                    )}

                    <button type="submit" className="md:col-span-2 mt-8 bg-[#5D5FEF] hover:bg-[#4B4DDF] text-white font-bold py-4 rounded-xl transition-all shadow-xl shadow-indigo-200 flex items-center justify-center text-lg transform hover:-translate-y-1">
                        Create Account
                    </button>
                </form>
            </div>
        </div>
    );
};
export default Register;
