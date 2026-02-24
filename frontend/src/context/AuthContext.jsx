import { createContext, useState, useContext, useEffect } from 'react';
import axios from 'axios';

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
            axios.defaults.headers.common['Authorization'] = token;
            localStorage.setItem('token', token);
        } else {
            delete axios.defaults.headers.common['Authorization'];
            localStorage.removeItem('token');
        }
    }, [token]);

    const login = async (mobile, password, userRole) => {
        try {
            const response = await axios.post('http://localhost:8080/auth/login', {
                mobileNo: mobile,
                password: password
            });

            if (response.data.statusCode === 200) {
                const newToken = response.data.data; // "Bearer ..."
                setToken(newToken);
                setRole(userRole);

                // Fetch user details optionally or just store mobile/role
                const userData = { mobile, role: userRole };
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
