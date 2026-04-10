import React from 'react';
import { Twitter, Instagram, Linkedin, Facebook, MapPin, Mail, Phone } from 'lucide-react';
import logo from '../assets/logo.svg';

const Footer = () => {
    return (
        <footer className="w-full bg-[#050505] border-t border-white/5 pt-16 pb-8 px-6 text-white relative z-10">
            <div className="max-w-7xl mx-auto">
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-12 mb-16">
                    {/* Brand Column */}
                    <div className="flex flex-col gap-6">
                        <img src={logo} alt="Go-Easy" className="h-10 w-auto object-contain self-start" />
                        <p className="text-[#888888] text-sm leading-relaxed">
                            Pioneering the future of urban mobility. Reliable, premium, and seamless transportation solutions at your fingertips.
                        </p>
                        <div className="flex items-center gap-4 text-[#F7D100]">
                            <a href="#" className="hover:text-white transition-colors p-2 bg-white/5 rounded-full hover:bg-white/10"><Twitter size={18} /></a>
                            <a href="#" className="hover:text-white transition-colors p-2 bg-white/5 rounded-full hover:bg-white/10"><Instagram size={18} /></a>
                            <a href="#" className="hover:text-white transition-colors p-2 bg-white/5 rounded-full hover:bg-white/10"><Linkedin size={18} /></a>
                            <a href="#" className="hover:text-white transition-colors p-2 bg-white/5 rounded-full hover:bg-white/10"><Facebook size={18} /></a>
                        </div>
                    </div>

                    {/* Quick Links */}
                    <div className="flex flex-col gap-4">
                        <h4 className="text-lg font-black uppercase italic tracking-widest mb-2">Company</h4>
                        <a href="#" className="text-[#888888] text-sm hover:text-[#F7D100] transition-colors w-fit">About Us</a>
                        <a href="#" className="text-[#888888] text-sm hover:text-[#F7D100] transition-colors w-fit">Careers</a>
                        <a href="#" className="text-[#888888] text-sm hover:text-[#F7D100] transition-colors w-fit">Investor Relations</a>
                        <a href="#" className="text-[#888888] text-sm hover:text-[#F7D100] transition-colors w-fit">Press & Media</a>
                    </div>

                    {/* Resources */}
                    <div className="flex flex-col gap-4">
                        <h4 className="text-lg font-black uppercase italic tracking-widest mb-2">Resources</h4>
                        <a href="#" className="text-[#888888] text-sm hover:text-[#F7D100] transition-colors w-fit">Help Center</a>
                        <a href="#" className="text-[#888888] text-sm hover:text-[#F7D100] transition-colors w-fit">Privacy Policy</a>
                        <a href="#" className="text-[#888888] text-sm hover:text-[#F7D100] transition-colors w-fit">Terms of Service</a>
                        <a href="#" className="text-[#888888] text-sm hover:text-[#F7D100] transition-colors w-fit">Safety Guidelines</a>
                    </div>

                    {/* Contact */}
                    <div className="flex flex-col gap-4">
                        <h4 className="text-lg font-black uppercase italic tracking-widest mb-2">Contact</h4>
                        <div className="flex items-start gap-3 text-[#888888] text-sm">
                            <MapPin size={18} className="text-[#F7D100] shrink-0 mt-0.5" />
                            <span>124 GoEasy Tower, Tech District, Metro City, 452001</span>
                        </div>
                        <div className="flex items-center gap-3 text-[#888888] text-sm">
                            <Phone size={18} className="text-[#F7D100] shrink-0" />
                            <span>+91 98000 12345</span>
                        </div>
                        <div className="flex items-center gap-3 text-[#888888] text-sm">
                            <Mail size={18} className="text-[#F7D100] shrink-0" />
                            <span>support@goeasy.com</span>
                        </div>
                    </div>
                </div>

                {/* Bottom Bar */}
                <div className="pt-8 border-t border-white/10 flex flex-col sm:flex-row items-center justify-between gap-4">
                    <p className="text-[#888888] text-sm font-medium tracking-wide">
                        &copy; {new Date().getFullYear()} GoEasy. All Rights Reserved.
                    </p>
                    <p className="text-xs text-[#666666] tracking-[0.2em] uppercase font-bold">
                        Made by Pravesh
                    </p>
                </div>
            </div>
        </footer>
    );
};

export default Footer;
