import fetch from 'isomorphic-fetch'
import qs from 'qs'
import debounce from 'lodash/debounce'
import { API_ENDPOINT } from './../redux'
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const FETCHED_QUARTER_STATISTICS = 'FETCHED_QUARTER_STATISTICS'
export const FETCHING_QUARTER_STATISTICS = 'FETCHING_QUARTER_STATISTICS'
export const FETCHING_QUARTER_STATISTICS_FAILED = 'FETCHING_QUARTER_STATISTICS_FAILED'
export const SORTING_QUARTER_STATISTICS = 'SORTING_QUARTER_STATISTICS'
export const SORTED_QUARTER_STATISTICS = 'SORTED_QUARTER_STATISTICS'
export const SORTING_QUARTER_STATISTICS_FAILED = 'SORTING_QUARTER_STATISTICS_FAILED'
export const SET_PAGE = 'SET_PAGE'
export const SET_SORT_ORDER = 'SET_SORT_ORDER'
export const SET_FILTER = 'SET_FILTER'
export const SET_PAGESIZE_QUARTER_STATISTICS = 'SET_PAGESIZE_QUARTER_STATISTICS'

const fetchedQuarterStatistics = (json) => ({
    type: FETCHED_QUARTER_STATISTICS,
    quarterStatistics: json,
    receivedAt: Date.now()
})

const fetchingQuarterStatistics = () => ({
    type: FETCHING_QUARTER_STATISTICS,
    receivedAt: Date.now()
})

const fetchingQuarterStatisticsFailed = (err) => ({
    type: FETCHING_QUARTER_STATISTICS_FAILED,
    err: err,
    receivedAt: Date.now()
})

const setPage = (page) => ({
  type: SET_PAGE,
  page,
  receivedAt: Date.now()
})

const setSortOrder = (orderBy, asc) => ({
  type: SET_SORT_ORDER,
  orderBy,
  asc,
  receivedAt: Date.now()
})

const setFilter = (filter) => ({
  type: SET_FILTER,
  filter,
  receivedAt: Date.now()
})

const sortingQuarterStatistics = () => ({
    type: SORTING_QUARTER_STATISTICS,
    receivedAt: Date.now()
})

const sortedQuarterStatistics = (json) => ({
    type: SORTED_QUARTER_STATISTICS,
    quarterStatistics: json,
    receivedAt: Date.now()
})

const sortingQuarterStatisticsFailed = (err) => ({
    type: SORTING_QUARTER_STATISTICS_FAILED,
    err: err,
    receivedAt: Date.now()
})


export const sortTable = (orderBy, asc) =>
  dispatch => {
    cookies.set('qstat:orderBy', orderBy, { path: '/' })
    cookies.set('qstat:asc', asc, { path: '/' })
    dispatch(setSortOrder(orderBy, asc))
    dispatch(fetchQuarterStatistics())
}

export const changePage = (page) =>
  dispatch => {
    dispatch(setPage(page))
    dispatch(fetchQuarterStatistics())
}

const debounceFetchQuarterStatistics = (filter) => debounce((dispatch, getState) => {
    if (getState().quarterStatistics.view.quarterStatistics.table.filter === filter)
      return dispatch(fetchQuarterStatistics())
}, 800)

export const changeFilter = (filter) =>
  dispatch => {
    dispatch(setFilter(filter))
    dispatch(debounceFetchQuarterStatistics(filter))
}

export const fetchQuarterStatistics = () => {
  return (dispatch, getState) => {
    dispatch(fetchingQuarterStatistics())
    return fetch(`${API_ENDPOINT}/api/payments/quarterStats`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedQuarterStatistics(json))
      })
      .catch((err)=>{
        console.error('fetch quarter statistics ERROR:',err)
        dispatch(fetchingQuarterStatisticsFailed(err))
      })
  }
}
const setPagesizeQuarterStatistics = (size) => ({
    type: SET_PAGESIZE_QUARTER_STATISTICS,
    pageSize: size,
    receivedAt: Date.now()
})

export const changePagesizeQuarterStatistics = (size) =>
  dispatch => {
    dispatch(setPagesizeQuarterStatistics(size)),
    dispatch(fetchQuarterStatistics())
}
