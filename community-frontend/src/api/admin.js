import request from './request'

export const setPostStatusApi = (postId, status) =>
  request.post(`/api/admin/posts/${postId}?status=${status}`)
