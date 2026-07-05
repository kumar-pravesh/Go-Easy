import { useState } from 'react';
import { ShieldCheck, X } from 'lucide-react';

const TIERS = {
    GOLD:   { color: 'text-yellow-400', bg: 'bg-yellow-400/10', border: 'border-yellow-400/30', label: 'Gold', emoji: '🥇' },
    SILVER: { color: 'text-gray-300',   bg: 'bg-gray-400/10',   border: 'border-gray-400/30',   label: 'Silver', emoji: '🥈' },
    BRONZE: { color: 'text-orange-400', bg: 'bg-orange-400/10', border: 'border-orange-400/30', label: 'Bronze', emoji: '🥉' },
};

const TIER_INFO = [
    {
        tier: 'BRONZE',
        title: 'Bronze — Mobile Verified',
        steps: ['Mobile number verified at registration'],
        note: 'All registered drivers start here.',
        color: 'text-orange-400',
        border: 'border-orange-400/20',
    },
    {
        tier: 'SILVER',
        title: 'Silver — Documents Verified',
        steps: ['Aadhaar card verified', 'Driving license verified'],
        note: 'Recommended for daily commutes.',
        color: 'text-gray-300',
        border: 'border-gray-400/20',
    },
    {
        tier: 'GOLD',
        title: 'Gold — Background Cleared',
        steps: ['Aadhaar + License verified', 'Background check passed', '6+ months clean record'],
        note: 'Ideal for children, solo women, late-night rides.',
        color: 'text-yellow-400',
        border: 'border-yellow-400/20',
    },
];

export const TierBadge = ({ tier, size = 'sm' }) => {
    const t = TIERS[tier] || TIERS.BRONZE;
    const textSize = size === 'xs' ? 'text-[8px]' : 'text-[9px]';
    return (
        <span className={`inline-flex items-center gap-1 ${t.bg} border ${t.border} ${t.color} ${textSize} font-black uppercase px-2 py-0.5 rounded-full`}>
            <ShieldCheck size={size === 'xs' ? 8 : 10} /> {t.label}
        </span>
    );
};

export const VerificationInfoModal = ({ onClose }) => (
    <div className="fixed inset-0 bg-black/90 backdrop-blur-md z-50 flex items-center justify-center p-4 animate-fade-in">
        <div className="glass-card rounded-[3rem] w-full max-w-lg overflow-hidden shadow-[0_0_100px_rgba(247,209,0,0.08)]">
            <div className="bg-[#F7D100] p-8 flex justify-between items-center">
                <div>
                    <h3 className="text-black font-black text-2xl italic tracking-tighter">Driver Verification Tiers</h3>
                    <p className="text-black/60 text-[10px] font-black uppercase tracking-widest mt-1">Safety levels explained</p>
                </div>
                <button onClick={onClose} className="bg-black/10 p-3 rounded-2xl hover:rotate-90 transition-all text-black"><X size={20} /></button>
            </div>
            <div className="p-8 space-y-5">
                {TIER_INFO.map((t) => (
                    <div key={t.tier} className={`bg-white/5 border ${t.border} rounded-[1.5rem] p-6`}>
                        <p className={`text-sm font-black mb-3 ${t.color}`}>{t.title}</p>
                        <ul className="space-y-1.5 mb-3">
                            {t.steps.map((s) => (
                                <li key={s} className="flex items-center gap-2 text-[11px] text-gray-300 font-bold">
                                    <div className={`w-1.5 h-1.5 rounded-full ${t.color.replace('text-', 'bg-')}`} />
                                    {s}
                                </li>
                            ))}
                        </ul>
                        <p className="text-[10px] text-gray-600 italic">{t.note}</p>
                    </div>
                ))}
                <p className="text-[10px] text-gray-600 text-center pt-2">Badges are verified by the Go-Easy safety team before being granted.</p>
            </div>
        </div>
    </div>
);
