import axios from 'axios'
import { message } from 'ant-design-vue'
//区分开发和生产环境
const DEV_BASE_URL = 'http://localhost:8123';
const PROD_BASE_URL = 'YourIP';
const myAxios = axios.create({
  baseURL: PROD_BASE_URL,
  timeout: 10000,
  withCredentials: true,
});

// 添加请求拦截器
myAxios.interceptors.request.use(function (config) {
  // 在发送请求之前做些什么
  return config;
}, function (error) {
  // 对请求错误做些什么
  return Promise.reject(error);
});

// 添加响应拦截器
myAxios.interceptors.response.use(function (response) {
  //解构response的data属性
  const { data } = response;
  if (data.code === 40100) {
    if (!response.request.responseURL.includes('user/get/login') &&
        !window.location.pathname.includes('user/login')
    ) {
      message.warning('请先登录');
      window.location.href = `user/login?redirect=${window.location.href}`;
    }
  }
  // 2xx 范围内的状态码都会触发该函数。
  // 对响应数据做点什么
  return response;
}, function (error) {
  // 超出 2xx 范围的状态码都会触发该函数。
  // 对响应错误做点什么
  return Promise.reject(error);
});

export default myAxios;
