import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import API_BASE_URL from '../api';
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import 'leaflet/dist/leaflet.css';
import L from 'leaflet';
import icon from 'leaflet/dist/images/marker-icon.png';
import iconShadow from 'leaflet/dist/images/marker-shadow.png';
import { ShieldAlert, Car, MapPin } from 'lucide-react';
import logo from '../assets/logo.svg';

let DefaultIcon = L.icon({
    iconUrl: icon,
    shadowUrl: iconShadow,
    iconSize: [25, 41],
    iconAnchor: [12, 41]
});
L.Marker.prototype.options.icon = DefaultIcon;

const PublicTracking = () => {
    const { bookingId } = useParams();
    const navigate = useNavigate();
    const [trackingData, setTrackingData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchTracking = async () => {
            try {
                const res = await axios.get(`${API_BASE_URL}/booking/public/track?bookingId=${bookingId}`);
                if (res.data.statusCode === 200) {
                    setTrackingData(res.data.data);
                } else {
                    setError('Tracking unavailable');
                }
            } catch (err) {
                setError('Failed to fetch tracking data');
            } finally {
                setLoading(false);
            }
        };

        fetchTracking();
        const interval = setInterval(fetchTracking, 5000);
        return () => clearInterval(interval);
    }, [bookingId]);

    if (loading) {
        return (
            <div className="min-h-screen bg-[#0A0A0A] flex items-center justify-center">
                <div className="w-8 h-8 border-4 border-[#F7D100] border-t-transparent rounded-full animate-spin"></div>
            </div>
        );
    }

    if (error || !trackingData) {
        return (
            <div className="min-h-screen bg-[#0A0A0A] text-white flex flex-col items-center justify-center p-4">
                <ShieldAlert size={64} className="text-gray-700 mb-6" />
                <h2 className="text-2xl font-black italic mb-2 tracking-tighter">Link Expired or Invalid</h2>
                <p className="text-gray-500 mb-8 max-w-sm text-center">This ride may have already completed or the tracking link is incorrect.</p>
                <button onClick={() => navigate('/')} className="premium-button px-8">Return Home</button>
            </div>
        );
    }

    return (
        <div className="min-h-screen bg-[#0A0A0A] text-white flex flex-col">
            <header className="bg-black/60 backdrop-blur-xl border-b border-white/5 px-4 py-4 flex items-center justify-between">
                <img src={logo} alt="Go-Easy" className="h-8 object-contain" />
                <span className="bg-green-500/10 text-green-400 border border-green-500/20 px-3 py-1 rounded-full text-[10px] font-black uppercase tracking-widest flex items-center gap-2">
                    <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div> Live Tracking
                </span>
            </header>

            <div className="flex-1 relative">
                {(trackingData.driverLatitude && trackingData.driverLongitude) ? (
                    <MapContainer center={[trackingData.driverLatitude, trackingData.driverLongitude]} zoom={15} className="w-full h-full" zoomControl={false}>
                        <TileLayer
                            url="https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png"
                            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                        />
                        <Marker position={[trackingData.driverLatitude, trackingData.driverLongitude]}>
                            <Popup>
                                <div className="text-black text-center font-bold">
                                    {trackingData.driverName || 'Driver'}<br/>
                                    {trackingData.vehicleModel}
                                </div>
                            </Popup>
                        </Marker>
                    </MapContainer>
                ) : (
                    <div className="w-full h-full bg-black/40 flex items-center justify-center">
                        <p className="text-[10px] font-black uppercase text-gray-500 tracking-widest">Waiting for driver location...</p>
                    </div>
                )}

                {/* Bottom Panel */}
                <div className="absolute bottom-0 left-0 right-0 p-4 z-[400] bg-gradient-to-t from-black via-black/80 to-transparent pt-20">
                    <div className="max-w-md mx-auto glass-card rounded-[2rem] p-6 border-white/10 shadow-2xl backdrop-blur-2xl">
                        <div className="flex items-center justify-between mb-6">
                            <div>
                                <p className="text-[10px] font-black uppercase text-gray-500 tracking-widest mb-1">Status</p>
                                <p className="text-xl font-black text-[#F7D100] tracking-tighter uppercase">{trackingData.bookingStatus}</p>
                            </div>
                            <div className="text-right">
                                <p className="text-[10px] font-black uppercase text-gray-500 tracking-widest mb-1">Vehicle</p>
                                <div className="flex items-center gap-2">
                                    <Car size={14} className="text-gray-400" />
                                    <p className="font-bold">{trackingData.vehicleNumber}</p>
                                </div>
                            </div>
                        </div>

                        <div className="space-y-4">
                            <div className="flex gap-4 items-start">
                                <div className="mt-1"><div className="w-2 h-2 bg-[#F7D100] rounded-full"></div></div>
                                <div>
                                    <p className="text-[9px] font-black uppercase text-gray-500 tracking-widest mb-0.5">Pickup</p>
                                    <p className="text-sm font-bold">{trackingData.sourceLocation}</p>
                                </div>
                            </div>
                            <div className="flex gap-4 items-start">
                                <div className="mt-1"><MapPin size={10} className="text-white" /></div>
                                <div>
                                    <p className="text-[9px] font-black uppercase text-gray-500 tracking-widest mb-0.5">Dropoff</p>
                                    <p className="text-sm font-bold">{trackingData.destinationLocation}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default PublicTracking;
