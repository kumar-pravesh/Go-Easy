import React from 'react';
import { Link } from 'react-router-dom';
import PublicNavbar from '../components/PublicNavbar';
import Footer from '../components/Footer';
import { ArrowRight, ShieldCheck, Zap, Navigation, Star, Clock } from 'lucide-react';
import logo from '../assets/logo.svg';

const LandingPage = () => {
    return (
        <div className="min-h-screen bg-[#111111] text-white flex flex-col font-sans selection:bg-[#F7D100] selection:text-black">
            <PublicNavbar />

            {/* HERO SECTION */}
            <main className="flex-grow flex flex-col relative overflow-hidden">
                {/* Background ambient light */}
                <div className="absolute top-[-20%] left-[-10%] w-[500px] h-[500px] bg-[#F7D100]/5 rounded-full blur-[120px] pointer-events-none"></div>
                <div className="absolute bottom-[-20%] right-[-10%] w-[600px] h-[600px] bg-[#F7D100]/10 rounded-full blur-[150px] pointer-events-none"></div>

                <div className="flex-grow flex flex-col items-center justify-center px-6 pt-32 pb-24 text-center z-10">
                    <div className="inline-flex items-center gap-2 px-4 py-2 bg-white/5 border border-white/10 rounded-full text-[#F7D100] text-xs font-bold uppercase tracking-widest mb-8">
                        <span className="relative flex h-2 w-2">
                          <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#F7D100] opacity-75"></span>
                          <span className="relative inline-flex rounded-full h-2 w-2 bg-[#F7D100]"></span>
                        </span>
                        GoEasy is Live Globally
                    </div>
                    
                    <h1 className="text-5xl md:text-7xl font-black italic tracking-tighter mb-6 drop-shadow-2xl max-w-4xl leading-tight">
                        THE FUTURE OF <br className="hidden sm:block" />
                        <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#F7D100] to-[#D49A00]">URBAN MOBILITY</span>
                    </h1>
                    
                    <p className="text-[#888888] text-lg md:text-xl max-w-2xl mb-12 leading-relaxed">
                        Say goodbye to surge pricing and unreliable rides. GoEasy delivers instant dispatch, premium vehicles, and transparent pricing in one tap.
                    </p>

                    <div className="flex flex-col sm:flex-row items-center gap-6 w-full justify-center max-w-md mx-auto">
                        <Link to="/register" className="w-full sm:w-auto bg-[#F7D100] text-black px-8 py-4 rounded-xl font-black text-lg uppercase tracking-wider hover:bg-[#FFEA66] transition-all hover:scale-105 hover:shadow-[0_0_30px_rgba(247,209,0,0.4)] flex items-center justify-center gap-2 group">
                            Book a Ride
                            <ArrowRight size={20} className="group-hover:translate-x-1 transition-transform" />
                        </Link>
                        <Link to="/login" className="w-full sm:w-auto bg-white/5 border border-white/10 text-white px-8 py-4 rounded-xl font-bold text-lg hover:bg-white/10 transition-all flex items-center justify-center">
                            Driver Login
                        </Link>
                    </div>

                    {/* Stats */}
                    <div className="mt-20 grid grid-cols-2 md:grid-cols-4 gap-8 md:gap-16 border-t border-white/5 pt-12 w-full max-w-5xl">
                        <div className="flex flex-col items-center">
                            <h3 className="text-3xl font-black text-[#F7D100] italic">5M+</h3>
                            <p className="text-[#666666] text-xs font-bold tracking-widest uppercase mt-1">Rides Completed</p>
                        </div>
                        <div className="flex flex-col items-center">
                            <h3 className="text-3xl font-black text-[#F7D100] italic">4.9/5</h3>
                            <p className="text-[#666666] text-xs font-bold tracking-widest uppercase mt-1">User Rating</p>
                        </div>
                        <div className="flex flex-col items-center">
                            <h3 className="text-3xl font-black text-[#F7D100] italic">&lt; 3m</h3>
                            <p className="text-[#666666] text-xs font-bold tracking-widest uppercase mt-1">Avg Wait Time</p>
                        </div>
                        <div className="flex flex-col items-center">
                            <h3 className="text-3xl font-black text-[#F7D100] italic">100+</h3>
                            <p className="text-[#666666] text-xs font-bold tracking-widest uppercase mt-1">Cities Active</p>
                        </div>
                    </div>
                </div>

                {/* FEATURES GRID */}
                <div className="bg-[#050505] w-full py-24 px-6 border-y border-white/5 relative z-10">
                    <div className="max-w-7xl mx-auto">
                        <div className="text-center mb-16">
                            <h2 className="text-3xl md:text-5xl font-black italic mb-4">WHY CHOOSE <span className="text-[#F7D100]">GOEASY?</span></h2>
                            <p className="text-[#888888] max-w-2xl mx-auto">Built from the ground up to focus on reliability, extreme safety, and better driver earnings.</p>
                        </div>
                        
                        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                            <div className="bg-[#111111] p-8 rounded-3xl border border-white/5 hover:border-[#F7D100]/30 transition-all group">
                                <div className="w-14 h-14 bg-[#F7D100]/10 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform">
                                    <Zap size={28} className="text-[#F7D100]" />
                                </div>
                                <h3 className="text-xl font-bold mb-3">Lightning Fast</h3>
                                <p className="text-[#888888] text-sm leading-relaxed">Our advanced routing connects you to the nearest top-rated driver instantly.</p>
                            </div>
                            
                            <div className="bg-[#111111] p-8 rounded-3xl border border-white/5 hover:border-[#F7D100]/30 transition-all group">
                                <div className="w-14 h-14 bg-[#F7D100]/10 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform">
                                    <ShieldCheck size={28} className="text-[#F7D100]" />
                                </div>
                                <h3 className="text-xl font-bold mb-3">Uncompromised Safety</h3>
                                <p className="text-[#888888] text-sm leading-relaxed">Mandatory OTP verification for every ride start and stop. 24/7 incident response team.</p>
                            </div>
                            
                            <div className="bg-[#111111] p-8 rounded-3xl border border-white/5 hover:border-[#F7D100]/30 transition-all group">
                                <div className="w-14 h-14 bg-[#F7D100]/10 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 transition-transform">
                                    <Navigation size={28} className="text-[#F7D100]" />
                                </div>
                                <h3 className="text-xl font-bold mb-3">Premium Fleet</h3>
                                <p className="text-[#888888] text-sm leading-relaxed">Travel exclusively in top-tier vehicles maintained to the highest cleanliness standards.</p>
                            </div>
                        </div>
                    </div>
                </div>
            </main>

            <Footer />
        </div>
    );
};

export default LandingPage;
