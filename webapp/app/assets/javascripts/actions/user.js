import fetch from 'isomorphic-fetch'
import qs from 'qs'
import { API_ENDPOINT } from './../redux'
import debounce from 'lodash/debounce'
import { showSnack } from './snackbar'
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const FETCHED_USER = 'FETCHED_USER'
export const FETCHING_USER = 'FETCHING_USER'
export const FETCHING_USER_FAILED = 'FETCHING_USER_FAILED'
export const FETCHED_USERS = 'FETCHED_USERS'
export const FETCHING_USERS = 'FETCHING_USERS'
export const FETCHING_USERS_FAILED = 'FETCHING_USERS_FAILED'
export const FETCHED_USER_SUGGESTIONS = 'FETCHED_USER_SUGGESTIONS'
export const FETCHING_USER_SUGGESTIONS = 'FETCHING_USER_SUGGESTIONS'
export const FETCHING_USER_SUGGESTIONS_FAILED = 'FETCHING_USER_SUGGESTIONS_FAILED'
export const SHOW_USER = 'SHOW_USER'
export const HIDE_USER = 'HIDE_USER'
export const SET_USER_SUGGESTION = 'SET_USER_SUGGESTION'
export const SET_USER_SUGGESTION_ID = 'SET_USER_SUGGESTION_ID'
export const SET_USER_SUGGESTION_TEXT = 'SET_USER_SUGGESTION_TEXT'
export const CLEAR_USER_SUGGESTIONS = 'CLEAR_USER_SUGGESTIONS'
export const SHOW_USER_PICKER = 'SHOW_USER_PICKER'
export const HIDE_USER_PICKER = 'HIDE_USER_PICKER'
export const SET_FILTER_USERS = 'SET_FILTER_USERS'
export const SET_SORT_ORDER_USERS ='SET_SORT_ORDER_USERS'
export const SET_PAGE_USERS = 'SET_PAGE_USERS'
export const USERS_CLOSE_MODAL = 'USERS_CLOSE_MODAL'
export const USERS_OPEN_MODAL = 'USERS_OPEN_MODAL'
export const SET_PAGESIZE_USERS = 'SET_PAGESIZE_USERS'
export const FETCHED_USER_STATS ='FETCHED_USER_STATS'
export const FETCHING_USER_STATS = 'FETCHING_USER_STATS'
export const CREATED_MEMBERSHIP_INVOICE = 'CREATED_MEMBERSHIP_INVOICE'
export const CREATING_MEMBERSHIP_INVOICE = 'CREATING_MEMBERSHIP_INVOICE'
export const CREATING_MEMBERSHIP_INVOICE_FAILED = 'CREATING_MEMBERSHIP_INVOICE_FAILED'

const fetchedUser = (json) => ({
    type: FETCHED_USER,
    user: json,
    receivedAt: Date.now()
})

const fetchedUserStats = (json) => ({
    type: FETCHED_USER_STATS,
    userStats: json,
    receivedAt: Date.now()
})

const fetchingUser = () => ({
    type: FETCHING_USER,
    receivedAt: Date.now()
})

const fetchingUserStats = () => ({
    type: FETCHING_USER_STATS,
    receivedAt: Date.now()
})

const fetchingUserFailed = (err) => ({
    type: FETCHING_USER_FAILED,
    err: err,
    receivedAt: Date.now()
})

const createdMembershipInvoice = () => ({
  type: CREATED_MEMBERSHIP_INVOICE,
  receivedAt: Date.now()
})

const creatingMembershipInvoice = () => ({
  type: CREATING_MEMBERSHIP_INVOICE,
  receivedAt: Date.now()
})

const creatingMembershipInvoiceFailed = (err) => ({
  type: CREATING_MEMBERSHIP_INVOICE_FAILED,
  err: err,
  receivedAt: Date.now()
})

const showingUser = (userId) => ({
    type: SHOW_USER,
    userId,
    receivedAt: Date.now()
})

const closingUserModal = () => ({
    type: HIDE_USER,
    receivedAt: Date.now()
})

const checkStatus = (response) => {
  if (response.status >= 200 && response.status < 300) {
    return response
  } else {
    var error = new Error(response.statusText)
    error.response = response
    throw error
  }
}

export const fetchUser = (userId) => {
  return dispatch => {
    dispatch(fetchingUser())
    dispatch(fetchUserStats(userId))
    return fetch(`${API_ENDPOINT}/api/users/${userId}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return (
          dispatch(fetchedUser(json))

        )
      })
      .catch((err)=>{
        console.error('fetch payment ERROR:',err)
        dispatch(fetchingUserFailed(err))
      })
  }
}

export const fetchUserStats = (userId) => {
  return dispatch => {
    dispatch(fetchingUserStats())
    return fetch(`${API_ENDPOINT}/api/payments/userStats/${userId}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedUserStats(json))
      })
      .catch((err)=>{
        console.error('fetch payment ERROR:',err)
        dispatch(fetchingUserFailed(err))
      })
  }
}

export const showUser = (userId) =>
  dispatch => {
    dispatch(showingUser(userId))
    dispatch(fetchUser(userId))
}

export const closeUserModal = () =>
  dispatch =>
    dispatch(closingUserModal())

// -----------------------------------------------------------------------------

export const selectSuggestionId = (userId) => dispatch => {
  return dispatch({
    type: SET_USER_SUGGESTION_ID,
    userId,
    receivedAt: Date.now()
  })
}

export const setSuggestion = (user) => dispatch => {
  return dispatch({
    type: SET_USER_SUGGESTION,
    user,
    receivedAt: Date.now()
  })
}

export const changeUserSuggestionText = (suggestionText) => dispatch => {
  return dispatch({
    type: SET_USER_SUGGESTION_TEXT,
    suggestionText,
    receivedAt: Date.now()
  })
}

// -----------------------------------------------------------------------------

export const clearUserSuggestions = () => dispatch => dispatch({
  type: CLEAR_USER_SUGGESTIONS,
  receivedAt: Date.now()
})

// -----------------------------------------------------------------------------

const fetchedUserSuggestions = (json) => ({
    type: FETCHED_USER_SUGGESTIONS,
    users: json,
    pageSize: json.pageSize,
    fullSize: json.fullSize,
    receivedAt: Date.now()
})

const fetchingUserSuggestions = () => ({
    type: FETCHING_USER_SUGGESTIONS,
    receivedAt: Date.now()
})

const fetchingUserSuggestionsFailed = (err) => ({
    type: FETCHING_USER_SUGGESTIONS_FAILED,
    err: err,
    receivedAt: Date.now()
})

export const fetchUserSuggestions = () => {
  return (dispatch, getState) => {
    dispatch(fetchingUserSuggestions())
    const state = getState()
    const queryObject = {
      search: state.users.view.suggestionText,
      status: 'FULL'
    }
    const queryParams = qs.stringify(queryObject)
    return fetch(`${API_ENDPOINT}/api/users/filter?${queryParams}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedUserSuggestions(json))
      })
      .catch((err)=>{
        console.error('fetch user suggestions ERROR:',err)
        dispatch(fetchingUserSuggestionsFailed(err))
      })
  }
}

// -----------------------------------------------------------------------------

export const showUserPicker = () => dispatch => dispatch({
  type: SHOW_USER_PICKER,
  receivedAt: Date.now()
})

export const changeFilterUsers = (filter) =>
  dispatch => {
    dispatch(setFilterUsers(filter))
    dispatch(debounceFetchUsers(filter))
}

const setFilterUsers = (filter) => ({
  type: SET_FILTER_USERS,
  filter,
  receivedAt: Date.now()
})

const debounceFetchUsers = (filter) => debounce((dispatch, getState) => {
    if (getState().users.view.users.table.filter === filter)
      return dispatch(fetchUsers())
}, 800)

export const fetchUsers = () => {
  return (dispatch, getState) => {
    dispatch(fetchingUsers())
    const state = getState()
    const { page, pageSize, orderBy, asc, filter} = state.users.view.users.table
    const queryObject = {
      page,
      pageSize,
      orderBy,
      asc,
      filter
    }
    const queryParams = qs.stringify(queryObject)
    return fetch(`${API_ENDPOINT}/api/users?${queryParams}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedUsers(json))
      })
      .catch((err)=>{
        console.error('fetch invoices ERROR:',err)
        dispatch(fetchingUsersFailed(err))
      })
  }
}

const fetchingUsers = () => ({
  type: FETCHING_USERS,
  receivedAt: Date.now()
})

const fetchedUsers = (json) => ({
  type: FETCHED_USERS,
  fullSize: json.fullSize,
  users: json.base,
  pageSize: json.pageSize,
  receivedAt: Date.now()
})

const fetchingUsersFailed = (err) => ({
  type: FETCHING_USERS_FAILED,
  err: err,
  receivedAt: Date.now()
})

export const sortTableUsers = (orderBy, asc) =>
  dispatch => {
    cookies.set('u:orderBy', orderBy, { path: '/' })
    cookies.set('u:asc', asc, { path: '/' })
    dispatch(setSortOrderUsers(orderBy, asc))
    dispatch(fetchUsers())
}

const setSortOrderUsers = (orderBy, asc) => ({
  type: SET_SORT_ORDER_USERS,
  orderBy,
  asc,
  receivedAt: Date.now()
})

export const changePageUsers = (page) =>
  dispatch => {
    dispatch(setPageUsers(page))
    dispatch(fetchUsers())
}

const setPageUsers = (page) => ({
  type: SET_PAGE_USERS,
  page,
  receivedAt: Date.now()
})

export const openModal = () => ({
  type: USERS_OPEN_MODAL
})

export const closeModal = () => ({
  type: USERS_CLOSE_MODAL
})

const setPagesizeUsers = (size) => ({
    type: SET_PAGESIZE_USERS,
    pageSize: size,
    receivedAt: Date.now()
})

export const changePagesizeUsers = (size) =>
  dispatch => {
    cookies.set('u:ps', size, { path: '/' })
    dispatch(setPagesizeUsers(size)),
    dispatch(fetchUsers())
}

export const createMembershipInvoice = (userId) => {
  return (dispatch, getState) => {
    dispatch(creatingMembershipInvoice())
    return fetch(`${API_ENDPOINT}/api/invoices/membership/new/${userId}`, {
          credentials: 'same-origin',
          method: 'post',
          body: JSON.stringify({}),
          headers: new Headers({
            'content-type': 'application/json; charset=utf-8',
            'Accept': 'application/json, application/xml, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        dispatch(showSnack(0, {
          label: `Factuur ${json.invoice.number} werd aangemaakt voor gebruiker ${json.user.firstName} ${json.user.lastName}.`,
          timeout: 5000,
          button: { label: 'OK' }
        }))
        return dispatch(createdMembershipInvoice(json))
      })
      .catch((err)=>{
          console.error('creatingMembershipInvoiceFailed ERROR:',err)
          dispatch(showSnack(0, {
            label: `Er ging iets fout. Het kan zijn dat de factuur reeds aangemaakt is.`,
            timeout: 5000,
            button: { label: 'Sluiten' }
          }))
          return dispatch(creatingMembershipInvoiceFailed(err))
      })
  }
}