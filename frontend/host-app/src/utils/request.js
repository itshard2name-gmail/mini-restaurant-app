import axios from 'axios';

const service = axios.create({
    baseURL: import.meta.env.VITE_APP_BASE_API || '/api', // Use proxy or env
    timeout: 5000
});

// Request interceptor
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token;
            try {
                // Decode JWT to get User ID (Mock Gateway behavior for Local Dev)
                // The backend requires X-User-Id which is normally injected by the Gateway
                const base64Url = token.split('.')[1];
                const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
                const payload = JSON.parse(window.atob(base64));

                // Assuming 'sub' holds the user ID (phone number)
                if (payload.sub) {
                    config.headers['X-User-Id'] = payload.sub;
                }
            } catch (e) {
                console.warn('Failed to parse token for X-User-Id injection', e);
            }
        }
        return config;
    },
    error => {
        console.log(error);
        return Promise.reject(error);
    }
);

// Response interceptor
service.interceptors.response.use(
    response => {
        return response.data;
    },
    error => {
        console.log('err' + error);
        return Promise.reject(error);
    }
);

export default service;
