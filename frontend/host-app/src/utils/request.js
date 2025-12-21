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
