import { Component } from 'react';

class ErrorBoundary extends Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true, error };
    }

    componentDidCatch(error, info) {
        console.error('ErrorBoundary caught:', error, info);
    }

    render() {
        if (this.state.hasError) {
            return (
                <div className="min-h-screen bg-[#0A0A0A] text-white flex items-center justify-center p-8">
                    <div className="text-center max-w-md">
                        <div className="w-20 h-20 bg-red-500/10 rounded-3xl flex items-center justify-center mx-auto mb-8 border border-red-500/20">
                            <span className="text-3xl">⚠</span>
                        </div>
                        <h2 className="text-2xl font-black italic tracking-tighter mb-4 text-red-400">Something went wrong</h2>
                        <p className="text-gray-500 text-sm mb-8 font-medium">{this.state.error?.message || 'An unexpected error occurred.'}</p>
                        <button
                            onClick={() => { this.setState({ hasError: false, error: null }); window.location.href = '/'; }}
                            className="bg-[#F7D100] text-black font-black px-8 py-3 rounded-xl text-[11px] uppercase tracking-widest"
                        >
                            Go Home
                        </button>
                    </div>
                </div>
            );
        }
        return this.props.children;
    }
}

export default ErrorBoundary;
