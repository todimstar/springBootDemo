import request from './request'

export const getPostsApi = (params) => request.get('/api/posts', { params })

export const getPostApi = (id) => request.get(`/api/posts/${id}`)

export const createPostApi = (data) => request.post('/api/posts', data)

export const updatePostApi = (id, data) => request.patch(`/api/posts/${id}`, data)

export const deletePostApi = (id) => request.delete(`/api/posts/${id}`)

export const getUserPostsApi = (userId, params) =>
  request.get(`/api/posts/user/${userId}`, { params })

export const getFeedApi = (params) => request.get('/api/posts/feed', { params })
