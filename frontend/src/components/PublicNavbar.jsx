import React from 'react';
import { Link } from 'react-router-dom';
import logo from '../assets/logo.svg';
import { LogIn, UserPlus } from 'lucide-react';

const PublicNavbar = () => {
    return (
        <nav className="absolute top-0 w-full z-50 px-6 py-4 flex items-center justify-between">
            <div className="flex items-center">
                <Link to="/">
                    <img src={logo} alt="Go-Easy" className="h-10 sm:h-12 w-auto object-contain cursor-pointer drop-shadow-lg transition-transform hover:scale-105" />
                </Link>
            </div>
            
            <div className="flex items-center gap-4">
                <Link 
                    to="/login"
                    className="hidden sm:flex items-center gap-2 text-white font-bold text-sm tracking-wide hover:text-[#F7D100] transition-colors"
                >
                    <LogIn size={18} />
                    Sign In
                </Link>
                <Link 
                    to="/register"
                    className="flex items-center gap-2 bg-[#F7D100] text-black px-6 py-2.5 rounded-full font-black text-sm uppercase tracking-widest hover:bg-[#FFEA66] transition-all hover:scale-105 hover:shadow-[0_0_20px_rgba(247,209,0,0.4)]"
                >
                    <UserPlus size={18} />
                    Get Started
                </Link>
            </div>
        </nav>
    );
};

export default PublicNavbar;
