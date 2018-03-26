import fetch from 'isomorphic-fetch'
import qs from 'qs'
import debounce from 'lodash/debounce'
import { API_ENDPOINT } from './../redux'
import { checkStatus } from './../util'
import { showSnack } from './snackbar'
import Cookies from 'universal-cookie'

export const FETCHED_CAR_APPROVALS = 'FETCHED_CAR_APPROVALS'
export const FETCHING_CAR_APPROVALS = 'FETCHING_CAR_APPROVALS'
export const FETCHING_CAR_APPROVALS_FAILED = 'FETCHING_CAR_APPROVALS_FAILED'
export const FETCHED_CAR_ADMINS = 'FETCHED_CAR_ADMINS'
export const FETCHING_CAR_ADMINS = 'FETCHING_CAR_ADMINS'
export const FETCHING_CAR_ADMINS_FAILED = 'FETCHING_CAR_ADMINS_FAILED'
export const SORTING_CAR_APPROVALS = 'SORTING_CAR_APPROVALS'
export const SORTED_CAR_APPROVALS = 'SORTED_CAR_APPROVALS'
export const SORTING_CAR_APPROVALS_FAILED = 'SORTING_CAR_APPROVALS_FAILED'
export const SET_PAGE_CAR_APPROVALS = 'SET_PAGE_CAR_APPROVALS'
export const SET_SORT_ORDER_CAR_APPROVALS = 'SET_SORT_ORDER_CAR_APPROVALS'
export const SET_FILTER_CAR_APPROVALS = 'SET_FILTER_CAR_APPROVALS'
export const SET_STATUS_CAR_APPROVALS = 'SET_STATUS_CAR_APPROVALS'
export const SHOW_INVOICE = 'SHOW_INVOICE'
export const HIDE_INVOICE = 'HIDE_INVOICE'
export const SET_PAGESIZE_CAR_APPROVALS = 'SET_PAGESIZE_CAR_APPROVALS'
export const CHANGE_MODAL_CAR_FIELD = 'CHANGE_MODAL_CAR_FIELD'
export const UPDATED_CAR = 'UPDATED_CAR'
export const OPEN_APPROVAL_MODAL = 'OPEN_APPROVAL_MODAL'
export const CLOSE_APPROVAL_MODAL = 'CLOSE_APPROVAL_MODAL'
export const CHANGE_APPROVAL_FIELD = 'CHANGE_APPROVAL_FIELD'
export const CHANGE_SELECTED_ADMIN = 'CHANGE_SELECTED_ADMIN'

const cookies = new Cookies()

const fetchedCarApprovals = (json) => ({
    type: FETCHED_CAR_APPROVALS,
    carApprovals: json,
    receivedAt: Date.now()
})

const fetchingCarApprovals = () => ({
    type: FETCHING_CAR_APPROVALS,
    receivedAt: Date.now()
})

const fetchingCarApprovalsFailed = (err) => ({
    type: FETCHING_CAR_APPROVALS_FAILED,
    err: err,
    receivedAt: Date.now()
})

const fetchedCarAdmins = (json) => ({
  type: FETCHED_CAR_ADMINS,
  carAdmins: json,
  receivedAt: Date.now()
})

const fetchingCarAdmins = () => ({
  type: FETCHING_CAR_ADMINS,
  receivedAt: Date.now()
})

const fetchingCarAdminsFailed = (err) => ({
  type: FETCHING_CAR_ADMINS_FAILED,
  err: err,
  receivedAt: Date.now()
})

const setPage = (page) => ({
  type: SET_PAGE_CAR_APPROVALS,
  page,
  receivedAt: Date.now()
})

const setSortOrder = (orderBy, asc) => ({
  type: SET_SORT_ORDER_CAR_APPROVALS,
  orderBy,
  asc,
  receivedAt: Date.now()
})

const setFilter = (filter) => ({
  type: SET_FILTER_CAR_APPROVALS,
  filter,
  receivedAt: Date.now()
})

const setStatus = (status) => ({
  type: SET_STATUS_CAR_APPROVALS,
  status,
  receivedAt: Date.now()
})

const sortingCarApprovals = () => ({
    type: SORTING_CAR_APPROVALS,
    receivedAt: Date.now()
})

const sortedCarApprovals = (json) => ({
    type: SORTED_CAR_APPROVALS,
    carApprovals: json,
    receivedAt: Date.now()
})

const sortingCarApprovalsFailed = (err) => ({
    type: SORTING_CAR_APPROVALS_FAILED,
    err: err,
    receivedAt: Date.now()
})


export const sortTable = (orderBy, asc) =>
  dispatch => {
    cookies.set('carappr:orderBy', orderBy, { path: '/' });
    cookies.set('carappr:asc', asc, { path: '/' });
    dispatch(setSortOrder(orderBy, asc))
    dispatch(fetchCarApprovals())
}

export const changePage = (page) =>
  dispatch => {
    dispatch(setPage(page))
    dispatch(fetchCarApprovals())
}

const debounceFetchCarApprovals = (filter) => debounce((dispatch, getState) => {
    if (getState().carApprovals.view.carApprovals.table.filter === filter)
      return dispatch(fetchCarApprovals())
}, 800)

export const changeFilter = (filter) =>
  dispatch => {
    dispatch(setFilter(filter))
    dispatch(debounceFetchCarApprovals(filter))
}

export const fetchCarApprovals = () => {
  return (dispatch, getState) => {
    dispatch(fetchingCarApprovals())
    const state = getState()
    const { page, pageSize, orderBy, asc, filter, status} = state.carApprovals.view.carApprovals.table
    const queryObject = {
      page,
      pageSize,
      orderBy,
      asc,
      filter: status + ' ' + filter
    }
    const queryParams = qs.stringify(queryObject)
    return fetch(`${API_ENDPOINT}/api/approvals/cars?${queryParams}`, {credentials: 'same-origin'})
      .then(response => response.json())
      .then(json => { return dispatch(fetchedCarApprovals(json)) })
      .catch((err)=>{
        console.error('fetch carApprovals ERROR:',err)
        dispatch(fetchingCarApprovalsFailed(err))
      })
  }
}

export const fetchCarApprovalsByStatus = (status) => {
  return (dispatch, getState) => {
    dispatch(setFilter(''))
    dispatch(setStatus(status))
    dispatch(fetchCarApprovals())
  }
}

const setPagesizeCarApprovals = (size) => ({
    type: SET_PAGESIZE_CAR_APPROVALS,
    pageSize: size,
    receivedAt: Date.now()
})

export const changePagesizeCarApprovals = (size) =>
  dispatch => {
    cookies.set('carappr:ps', size, { path: '/' });
    dispatch(setPagesizeCarApprovals(size)),
    dispatch(fetchCarApprovals())
}

export const changeApprovalField = (carApprovalId, fieldName, fieldValue) => dispatch =>
  dispatch({
    type: CHANGE_APPROVAL_FIELD,
    carApprovalId,
    fieldName,
    fieldValue
  })

export const acceptCar = (carApprovalId) => dispatch =>
  dispatch(updateApprovalCar({ carApprovalId, accept: true }))

export const refuseCar = (carApprovalId) => dispatch =>
  dispatch(updateApprovalCar({ carApprovalId, accept: false }))

const updateApprovalCar = ({ carApprovalId, accept, adminId }) => {
  return (dispatch, getState) => {
    return fetch(`${API_ENDPOINT}/api/cars/updateapproval/${carApprovalId}`, {
      credentials: 'same-origin',
      method: 'put',
      body: JSON.stringify(mapApprovalStateToPostData(getState().carApprovals.carApprovals.find((carApproval) => carApproval.carApprovalId === carApprovalId), accept, adminId)),
      headers: new Headers({
        'content-type': 'application/json; charset=utf-8',
        'Accept': 'application/json, text/plain, text/html, *.*'
      }),
    })
    .then(checkStatus)
    .then(response => response.json())
    .then(json => {
      dispatch(closeApprovalModal())
      dispatch(fetchCarApprovals())
    })
    .catch((err) => {
      console.error('createCar ERROR:', err)
      return dispatch(showSnack(0, {
        label: `Er ging iets fout.`,
        timeout: 5000,
        button: { label: 'Sluiten' }
      }))

    })
  }
}

const mapApprovalStateToPostData = (approval, accept, adminId) => ({
  ...approval.adminMessage != null ? { adminMessage: approval.adminMessage } : {},
  ...accept != null ? { status: (accept ? 'ACCEPTED' : 'REFUSED') } : {},
  adminId
})

export const fetchCarAdmins = () => {
  return (dispatch, getState) => {
    dispatch(fetchingCarAdmins())
    return fetch(`${API_ENDPOINT}/api/approvals/caradmins?`, { credentials: 'same-origin' })
      .then(response => response.json())
      .then(json => { return dispatch(fetchedCarAdmins(json)) })
      .catch((err) => {
        console.error('fetch caradmins ERROR:', err)
        dispatch(fetchingCarAdminsFailed(err))
      })
  }
}

export const changeSelectedAdmin = (selectedId) => dispatch =>
  dispatch({
    type: CHANGE_SELECTED_ADMIN,
    selectedId
  })

export const assignAdmin = (carApprovalId) => {
  return (dispatch, getState) => {
    return dispatch(updateApprovalCar({ carApprovalId, adminId: getState().carApprovals.selectedCarAdmin }))
  }
}

export const openApprovalModal = (carApprovalId, actionType) => {
  return dispatch => {
    if (actionType === 'ASSIGN_ADMIN') {
      dispatch(fetchCarAdmins())
    }
    dispatch({
      type: OPEN_APPROVAL_MODAL,
      carApprovalId,
      actionType
    })
  }
}

export const closeApprovalModal = () =>
  dispatch => {
    dispatch({
      type: CLOSE_APPROVAL_MODAL
    })
  }
