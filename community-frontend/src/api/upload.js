import request from './request'

// 不要手动设 Content-Type！浏览器会自动带上 multipart boundary
export const uploadFileApi = (formData) =>
  request.post('/upload/upload', formData)

export const getFileUrlApi = (objectName) =>
  request.get('/upload/getUrl', { params: { objectName } })

export const deleteFileApi = (objectName) =>
  request.delete('/upload/delete', { params: { objectName } })
