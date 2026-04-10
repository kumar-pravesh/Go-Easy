import { createContext, useState, useContext, useEffect } from 'react';
import axios from 'axios';
import API_BASE_URL from '../api';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(() => {
        try {
            const savedUser = localStorage.getItem('user');
            return savedUser && savedUser !== "undefined" ? JSON.parse(savedUser) : null;
        } catch (e) {
            console.error("Failed to parse user from local storage", e);
            return null;
        }
    });
    const [role, setRole] = useState(null); // 'USER' or 'DRIVER'
    const [token, setToken] = useState(localStorage.getItem('token'));

    // Configure Axios default header
    useEffect(() => {
        if (token) {
            const authStr = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
            axios.defaults.headers.common['Authorization'] = authStr;
            localStorage.setItem('token', token);
        } else {
            delete axios.defaults.headers.common['Authorization'];
            localStorage.removeItem('token');
        }
    }, [token]);

    const login = async (mobile, password, userRole) => {
        try {
            const response = await axios.post(`${API_BASE_URL}/auth/login`, {
                mobileNo: mobile,
                password: password
            });

            if (response.data.statusCode === 200) {
                const { token: newToken, mobileNo, name, role: backendRole } = response.data.data;
                
                setToken(newToken);
                setRole(backendRole);

                const userData = { 
                    mobile: mobileNo, 
                    name: name,
                    role: backendRole 
                };
                setUser(userData);
                localStorage.setItem('user', JSON.stringify(userData));
                return true;
            }
            return false;
        } catch (error) {
            console.error("Login failed", error);
            throw error;
        }
    };

    const logout = () => {
        setUser(null);
        setRole(null);
        setToken(null);
        localStorage.removeItem('user');
    };

    return (
        <AuthContext.Provider value={{ user, role, token, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
