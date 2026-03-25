import request from './request'

export const getCommentsApi = (postId, params) =>
  request.get(`/api/comments/${postId}/comments`, { params })

export const createCommentApi = (postId, data) =>
  request.put(`/api/comments/${postId}/comments`, data)

export const deleteCommentApi = (commentId) =>
  request.delete(`/api/comments/${commentId}`)
