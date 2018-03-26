import fetch from 'isomorphic-fetch'
import qs from 'qs'
import debounce from 'lodash/debounce'
import { API_ENDPOINT } from './../redux'
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const FETCHED_USER_STATISTICS = 'FETCHED_USER_STATISTICS'
export const FETCHING_USER_STATISTICS = 'FETCHING_USER_STATISTICS'
export const FETCHING_USER_STATISTICS_FAILED = 'FETCHING_USER_STATISTICS_FAILED'
export const SORTING_USER_STATISTICS = 'SORTING_USER_STATISTICS'
export const SORTED_USER_STATISTICS = 'SORTED_USER_STATISTICS'
export const SORTING_USER_STATISTICS_FAILED = 'SORTING_USER_STATISTICS_FAILED'
export const SET_PAGE_USER_STATISTICS = 'SET_PAGE_USER_STATISTICS'
export const SET_SORT_ORDER_USER_STATISTICS = 'SET_SORT_ORDER_USER_STATISTICS'
export const SET_FILTER_USER_STATISTICS = 'SET_FILTER_USER_STATISTICS'
export const SHOW_INVOICE = 'SHOW_INVOICE'
export const HIDE_INVOICE = 'HIDE_INVOICE'
export const SET_PAGESIZE_USER_STATISTICS = 'SET_PAGESIZE_USER_STATISTICS'

const fetchedUserStatistics = (json) => ({
    type: FETCHED_USER_STATISTICS,
    userStatistics: json,
    receivedAt: Date.now()
})

const fetchingUserStatistics = () => ({
    type: FETCHING_USER_STATISTICS,
    receivedAt: Date.now()
})

const fetchingUserStatisticsFailed = (err) => ({
    type: FETCHING_USER_STATISTICS_FAILED,
    err: err,
    receivedAt: Date.now()
})

const setPage = (page) => ({
  type: SET_PAGE_USER_STATISTICS,
  page,
  receivedAt: Date.now()
})

const setSortOrder = (orderBy, asc) => ({
  type: SET_SORT_ORDER_USER_STATISTICS,
  orderBy,
  asc,
  receivedAt: Date.now()
})

const setFilter = (filter) => ({
  type: SET_FILTER_USER_STATISTICS,
  filter,
  receivedAt: Date.now()
})

const sortingUserStatistics = () => ({
    type: SORTING_USER_STATISTICS,
    receivedAt: Date.now()
})

const sortedUserStatistics = (json) => ({
    type: SORTED_USER_STATISTICS,
    userStatistics: json,
    receivedAt: Date.now()
})

const sortingUserStatisticsFailed = (err) => ({
    type: SORTING_USER_STATISTICS_FAILED,
    err: err,
    receivedAt: Date.now()
})


export const sortTable = (orderBy, asc) =>
  dispatch => {
    cookies.set('ustat:orderBy', orderBy, { path: '/' });
    cookies.set('ustat:asc', asc, { path: '/' });
    dispatch(setSortOrder(orderBy, asc))
    dispatch(fetchUserStatistics())
}

export const changePage = (page) =>
  dispatch => {
    dispatch(setPage(page))
    dispatch(fetchUserStatistics())
}

const debounceFetchUserStatistics = (filter) => debounce((dispatch, getState) => {
    if (getState().userStatistics.view.userStatistics.table.filter === filter)
      return dispatch(fetchUserStatistics())
}, 800)

export const changeFilter = (filter) =>
  dispatch => {
    dispatch(setFilter(filter))
    dispatch(debounceFetchUserStatistics(filter))
}

export const fetchUserStatistics = () => {
  return (dispatch, getState) => {
    dispatch(fetchingUserStatistics())
    const state = getState()
    const { page, pageSize, orderBy, asc, filter} = state.userStatistics.view.userStatistics.table
    const queryObject = {
      page,
      pageSize,
      orderBy,
      asc,
      filter
    }
    const queryParams = qs.stringify(queryObject)
    return fetch(`${API_ENDPOINT}/api/payments/userStats?${queryParams}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedUserStatistics(json))
      })
      .catch((err)=>{
        console.error('fetch payment ERROR:',err)
        dispatch(fetchingUserStatisticsFailed(err))
      })
  }
}

const setPagesizeUserStatistics = (size) => ({
    type: SET_PAGESIZE_USER_STATISTICS,
    pageSize: size,
    receivedAt: Date.now()
})

export const changePagesizeUserStatistics = (size) =>
  dispatch => {
    cookies.set('ustat:ps', size, { path: '/' })
    dispatch(setPagesizeUserStatistics(size)),
    dispatch(fetchUserStatistics())
}
