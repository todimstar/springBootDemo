import request from './request'

export const getCategoriesApi = () => request.get('/api/categories')

export const getCategoryApi = (id) => request.get(`/api/categories/${id}`)

// Admin
export const getAdminCategoriesApi = (data) => request.get('/api/admin/categories', { data })

export const getAdminCategoryApi = (id) => request.get(`/api/admin/categories/${id}`)

export const createCategoryApi = (data) => request.post('/api/admin/categories', data)

export const updateCategoryApi = (data) => request.put('/api/categories', data)

export const enableCategoryApi = (id) => request.put(`/api/categories/${id}/enable`)

export const disableCategoryApi = (id) => request.put(`/api/categories/${id}/disable`)

export const sortCategoryApi = (id, sortOrder) =>
  request.put(`/api/categories/${id}/sort?sortOrder=${sortOrder}`)

export const deleteCategoryApi = (id) => request.delete(`/api/categories/${id}`)
