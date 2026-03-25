import request from './request'

export const loginApi = (data) => request.post('/api/auth/login', data)

export const registerApi = (data) => request.post('/api/auth/register', data)

export const sendRegisterCodeApi = (email) =>
  request.post(`/api/auth/sendRegisterCode?email=${encodeURIComponent(email)}`)

// 不要手动设 Content-Type！浏览器会自动带上 multipart boundary
export const uploadAvatarApi = (formData) =>
  request.post('/api/auth/upload/userAvatar', formData)

export const getAllUsersApi = () => request.get('/api/auth/all')

export const deleteUserApi = (id) => request.delete(`/api/auth/${id}`)
