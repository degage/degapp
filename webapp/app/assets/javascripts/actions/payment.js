import fetch from 'isomorphic-fetch'
import qs from 'qs'
import debounce from 'lodash/debounce'
import { API_ENDPOINT } from './../redux'
import { changeUserSuggestionText, setSuggestion } from './user'
import { showSnack } from './snackbar'
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const FETCHED_PAYMENT = 'FETCHED_PAYMENT'
export const FETCHING_PAYMENT = 'FETCHING_PAYMENT'
export const FETCHING_PAYMENT_FAILED = 'FETCHING_PAYMENT_FAILED'
export const FETCHED_PAYMENTS = 'FETCHED_PAYMENTS'
export const FETCHING_PAYMENTS = 'FETCHING_PAYMENTS'
export const FETCHING_PAYMENTS_FAILED = 'FETCHING_PAYMENTS_FAILED'
export const SEARCHING_PAYMENTS = 'SEARCHING_PAYMENTS'
export const SEARCHING_PAYMENTS_FAILED = 'SEARCHING_PAYMENTS_FAILED'
export const SORTING_PAYMENTS = 'SORTING_PAYMENTS'
export const SORTED_PAYMENTS = 'SORTED_PAYMENTS'
export const SORTING_PAYMENTS_FAILED = 'SORTING_PAYMENTS_FAILED'
export const SET_PAGE_PAYMENTS = 'SET_PAGE_PAYMENTS'
export const SET_SORT_ORDER_PAYMENTS = 'SET_SORT_ORDER_PAYMENTS'
export const SET_FILTER_PAYMENTS = 'SET_FILTER_PAYMENTS'
export const SET_PAGESIZE_PAYMENTS = 'SET_PAGESIZE_PAYMENTS'
export const SELECT_USER_FOR_PAYMENT = 'SELECT_USER_FOR_PAYMENT'
export const UPDATING_PAYMENT_USER = 'UPDATING_PAYMENT_USER'
export const UPDATED_PAYMENT_USER = 'UPDATED_PAYMENT_USER'
export const UPDATING_PAYMENT_USER_FAILED = 'UPDATING_PAYMENT_USER_FAILED'
export const UPDATING_PAYMENT_INCLUDE_IN_BALANCE = 'UPDATING_PAYMENT_INCLUDE_IN_BALANCE'
export const UPDATED_PAYMENT_INCLUDE_IN_BALANCE = 'UPDATED_PAYMENT_INCLUDE_IN_BALANCE'
export const UPDATING_PAYMENT_INCLUDE_IN_BALANCE_FAILED = 'UPDATING_PAYMENT_INCLUDE_IN_BALANCE_FAILED'
export const SHOW_USER_PICKER = 'SHOW_USER_PICKER'
export const HIDE_USER_PICKER = 'HIDE_USER_PICKER'
export const SHOW_PAYMENT = 'SHOW_PAYMENT'
export const HIDE_PAYMENT = 'HIDE_PAYMENT'
export const UNLINKING_PAYMENTS_FOR_INVOICE = 'UNLINKING_PAYMENTS_FOR_INVOICE'
export const UNLINKED_PAYMENTS_FOR_INVOICE = 'UNLINKED_PAYMENTS_FOR_INVOICE'
export const UNLINKING_PAYMENTS_FOR_INVOICE_FAILED = 'UNLINKING_PAYMENTS_FOR_INVOICE_FAILED'
export const LINKING_INVOICE_AND_PAYMENT = 'LINKING_INVOICE_AND_PAYMENT'
export const LINKING_INVOICE_AND_PAYMENT_FAILED = 'LINKING_INVOICE_AND_PAYMENT_FAILED'
export const LINKED_INVOICE_AND_PAYMENT = 'LINKED_INVOICE_AND_PAYMENT'
export const FETCHED_PAYMENTS_SELECT_PAYMENT = 'FETCHED_PAYMENTS_SELECT_PAYMENT'
export const SHOW_SELECT_PAYMENT_MODAL = 'SHOW_SELECT_PAYMENT_MODAL'
export const HIDE_SELECT_PAYMENT_MODAL = 'HIDE_SELECT_PAYMENT_MODAL'
export const SELECTING_PAYMENT = 'SELECTING_PAYMENT'
export const SET_FILTER_SELECT_PAYMENT = 'SET_FILTER_SELECT_PAYMENT'
export const EDITING_PAYMENT_STATUS = 'EDITING_PAYMENT_STATUS'
export const CHANGE_PAYMENT_STATUS = 'CHANGE_PAYMENT_STATUS'
export const CANCEL_UPDATE_PAYMENT_STATUS ='CANCEL_UPDATE_PAYMENT_STATUS'
export const UPDATING_PAYMENT_STATUS = 'UPDATING_PAYMENT_STATUS'
export const UPDATING_PAYMENT_STATUS_FAILED = 'UPDATING_PAYMENT_STATUS_FAILED'
export const UPDATED_PAYMENT_STATUS = 'UPDATED_PAYMENT_STATUS'

const fetchedPayment = (json) => ({
    type: FETCHED_PAYMENT,
    payment: json,
    receivedAt: Date.now()
})

const fetchingPayment = () => ({
    type: FETCHING_PAYMENT,
    receivedAt: Date.now()
})

const fetchingPaymentFailed = (err) => ({
    type: FETCHING_PAYMENT_FAILED,
    err: err,
    receivedAt: Date.now()
})

const fetchedPayments = (json) => ({
    type: FETCHED_PAYMENTS,
    payments: json.base,
    pageSize: json.pageSize,
    fullSize: json.fullSize,
    receivedAt: Date.now()
})

const fetchingPayments = () => ({
    type: FETCHING_PAYMENTS,
    receivedAt: Date.now()
})

const fetchingPaymentsFailed = (err) => ({
    type: FETCHING_PAYMENTS_FAILED,
    err: err,
    receivedAt: Date.now()
})

const setPage = (page) => ({
  type: SET_PAGE_PAYMENTS,
  page,
  receivedAt: Date.now()
})

const setSortOrder = (orderBy, asc) => ({
  type: SET_SORT_ORDER_PAYMENTS,
  orderBy,
  asc,
  receivedAt: Date.now()
})

const setFilter = (filter) => ({
  type: SET_FILTER_PAYMENTS,
  filter,
  receivedAt: Date.now()
})

const sortingPayments = () => ({
    type: SORTING_PAYMENTS,
    receivedAt: Date.now()
})

const sortedPayments = (json) => ({
    type: SORTED_PAYMENTS,
    payments: json,
    receivedAt: Date.now()
})

const sortingPaymentsFailed = (err) => ({
    type: SORTING_PAYMENTS_FAILED,
    err: err,
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

export const fetchPayment = (paymentId) => {
  return dispatch => {
    dispatch(fetchingPayment())
    return fetch(`${API_ENDPOINT}/api/payments/${paymentId}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedPayment(json))
      })
      .catch((err)=>{
        console.error('fetch payment ERROR:',err)
        dispatch(fetchingPaymentFailed(err))
      })
  }
}

export const fetchPayments = () => {
  return (dispatch, getState) => {
    dispatch(fetchingPayments())
    const state = getState()
    const { page, pageSize, orderBy, asc, filter} = state.payments.view.payments.table
    const queryObject = {
      page,
      pageSize,
      orderBy,
      asc,
      filter
    }
    const queryParams = qs.stringify(queryObject)
    return fetch(`${API_ENDPOINT}/api/payments?${queryParams}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedPayments(json))
      })
      .catch((err)=>{
        console.error('fetch payment ERROR:',err)
        dispatch(fetchingPaymentsFailed(err))
      })
  }
}

export const searchPayments = (paymentSearch) => {
  return dispatch => {
    dispatch(searchingPayments(paymentSearch))
    return fetch(`${API_ENDPOINT}/api/payments/search/${paymentSearch}`)
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedPayments(json))
      })
      .catch((err)=>{
        console.error('fetch payments ERROR:',err)
        dispatch(searchingPaymentsFailed(err))
      })
  }
}

export const sortTable = (orderBy, asc) =>
  dispatch => {
    cookies.set('paym:orderBy', orderBy, { path: '/' })
    cookies.set('paym:asc', asc, { path: '/' })
    dispatch(setSortOrder(orderBy, asc))
    dispatch(fetchPayments())
}

export const changePage = (page) => {
  return dispatch => {
    dispatch(setPage(page))
    dispatch(fetchPayments())
  }
}

const debounceFetchPayments = (filter) => debounce((dispatch, getState) => {
    if (getState().payments.view.payments.table.filter === filter)
      return dispatch(fetchPayments())
}, 800)

export const changeFilter = (filter) =>
  dispatch => {
    dispatch(setFilter(filter))
    dispatch(debounceFetchPayments(filter))
}

const setPagesizePayments = (size) => ({
    type: SET_PAGESIZE_PAYMENTS,
    pageSize: size,
    receivedAt: Date.now()
})

export const changePagesizePayments = (size) =>
  dispatch => {
    cookies.set('paym:orderBy', size, { path: '/' })
    dispatch(setPagesizePayments(size)),
    dispatch(fetchPayments())
}

// -----------------------------------------------------------------------------

const updatedPaymentUser = (json) => ({
    type: UPDATED_PAYMENT_USER,
    paymentAndUser: json,
    receivedAt: Date.now()
})

const updatingPaymentUser = () => ({
    type: UPDATING_PAYMENT_USER,
    receivedAt: Date.now()
})

const updatingPaymentUserFailed = (err) => ({
    type: UPDATING_PAYMENT_USER_FAILED,
    err: err,
    receivedAt: Date.now()
})

export const selectUserForPayment = () => {
  return (dispatch, getState) => {
    dispatch(updatingPaymentUser())
    return fetch(`${API_ENDPOINT}/api/payments/user`, {
          method: 'post',
          credentials: 'same-origin',
          body: JSON.stringify({
            userId: getState().users.view.suggestion.id,
            paymentId: getState().payments.view.userPicker.paymentId
          }),
          headers: new Headers({
            'content-type': 'application/json charset=utf-8',
            'Accept': 'application/json, application/xml, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        dispatch(showSnack(0, {
          label: `Gebruiker ${json.user.firstName} ${json.user.lastName} verbonden aan betaling ${getState().payments.view.userPicker.paymentId}.`,
          timeout: 5000,
          button: { label: 'OK' }
        }))
        return dispatch(updatedPaymentUser(json))
      })
      .catch((err)=>{
          console.error('updatingPaymentUser ERROR:',err)
          return dispatch(updatingPaymentUserFailed(err))
      })
  }
}

// -----------------------------------------------------------------------------

export const showUserPickerModal = (paymentId, user) => dispatch => {
  dispatch(changeUserSuggestionText(user == null ? '' : `${user.lastName} ${user.firstName}`))
  dispatch(setSuggestion(user))
  dispatch({
    type: SHOW_USER_PICKER,
    paymentId
  })
}

export const hideUserPickerModal = () => dispatch => dispatch({
    type: HIDE_USER_PICKER
})

// -----------------------------------------------------------------------------

const unlinkingPaymentsForInvoice = () => ({
    type: UNLINKING_PAYMENTS_FOR_INVOICE,
    receivedAt: Date.now()
})

const unlinkedPaymentsForInvoice = (json) => ({
    type: UNLINKED_PAYMENTS_FOR_INVOICE,
    invoiceAndUser: json,
    receivedAt: Date.now()
})

const unlinkingPaymentsForInvoiceFailed = () => ({
    type: UNLINKING_PAYMENTS_FOR_INVOICE_FAILED,
    receivedAt: Date.now()
})

export const unlinkPaymentsForInvoice = (invoiceId) => {
  return (dispatch, getState) => {
    dispatch(unlinkingPaymentsForInvoice())
    return fetch(`${API_ENDPOINT}/api/invoices/unlink`, {
          method: 'post',
          credentials: 'same-origin',
          body: JSON.stringify({
            invoiceId
          }),
          headers: new Headers({
            'content-type': 'application/json charset=utf-8',
            'Accept': 'application/json, application/xml, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        dispatch(showSnack(0, {
          label: `Alle betalingen gelinked aan afrekening ${json.invoice.number} verwijderd.`,
          timeout: 5000,
          button: { label: 'OK' }
        }))
        return dispatch(unlinkedPaymentsForInvoice(json))
      })
      .catch((err)=>{
          console.error('unlinkPaymentsForInvoice ERROR:',err)
          return dispatch(unlinkingPaymentsForInvoiceFailed(err))
      })
  }
}

// -----------------------------------------------------------------------------

const selectingPayment = (invoiceAndUser) => ({
    type: SELECTING_PAYMENT,
    invoiceAndUser,
    receivedAt: Date.now()
})

export const linkPayment = (invoiceAndUser) =>
  dispatch => {
    dispatch(selectingPayment(invoiceAndUser))
    dispatch(changeFilterSelectPayment((invoiceAndUser.user.firstName + ' ' + invoiceAndUser.user.lastName)))
  }

export const unlinkPayments = (invoiceId) =>
  dispatch => {
    dispatch(unlinkPaymentsForInvoice(invoiceId))
  }

// -----------------------------------------------------------------------------

const showingPayment = (paymentId) => ({
    type: SHOW_PAYMENT,
    paymentId,
    receivedAt: Date.now()
})

export const showPaymentById = (paymentId) =>
  dispatch => {
    dispatch(showingPayment(paymentId))
    dispatch(fetchPayment(paymentId))
}

// -----------------------------------------------------------------------------

export const closePaymentModal = () =>
  dispatch => dispatch({
      type: HIDE_PAYMENT,
      receivedAt: Date.now()
  })

// -----------------------------------------------------------------------------

const fetchedPaymentsSelectPayment = (json) => ({
    type: FETCHED_PAYMENTS_SELECT_PAYMENT,
    payments: json.base,
    fullSize: json.fullSize,
    receivedAt: Date.now()
})

export const fetchPaymentsSelectPayment = () => {
  return (dispatch, getState) => {
    dispatch(fetchingPayments())
    const state = getState()
    const { page, pageSize, orderBy, asc, filter} = state.payments.view.selectPayment.table
    const queryObject = {
      page,
      pageSize,
      orderBy,
      asc,
      filter
    }
    const queryParams = qs.stringify(queryObject)
    return fetch(`${API_ENDPOINT}/api/payments?${queryParams}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedPaymentsSelectPayment(json))
      })
      .catch((err)=>{
        console.error('fetch payments ERROR:',err)
        dispatch(fetchingPaymentsFailed(err))
      })
  }
}

const debounceFetchPaymentsSelectPayment = (filter) => debounce((dispatch, getState) => {
    if (getState().payments.view.selectPayment.table.filter === filter)
      return dispatch(fetchPaymentsSelectPayment())
}, 800)

const setFilterSelectPayment = (filter) => ({
  type: SET_FILTER_SELECT_PAYMENT,
  filter,
  receivedAt: Date.now()
})

export const changeFilterSelectPayment = (filter) =>
  dispatch => {
    dispatch(setFilterSelectPayment(filter))
    dispatch(debounceFetchPaymentsSelectPayment(filter))
}

// -----------------------------------------------------------------------------

export const closeSelectPaymentModal = () =>
  dispatch =>{
    dispatch(changeFilterSelectPayment(''))
    dispatch({
        type: HIDE_SELECT_PAYMENT_MODAL,
        receivedAt: Date.now()
    })
  }


// -----------------------------------------------------------------------------

const linkingInvoiceAndPayment = (err) => ({
    type: LINKING_INVOICE_AND_PAYMENT,
    receivedAt: Date.now()
})

const linkingInvoiceAndPaymentFailed = (err) => ({
    type: LINKING_INVOICE_AND_PAYMENT_FAILED,
    err: err,
    receivedAt: Date.now()
})

const linkedInvoiceAndPayment = (json) =>
  dispatch => {
    dispatch(changeFilterSelectPayment(''))
    dispatch({
    type: LINKED_INVOICE_AND_PAYMENT,
    invoiceAndUser: json,
    receivedAt: Date.now()
  })
}

export const linkInvoiceAndPayment = (paymentId) => {
  return (dispatch, getState) => {
    dispatch(linkingInvoiceAndPayment())
    return fetch(`${API_ENDPOINT}/api/invoices/link`, {
          credentials: 'same-origin',
          method: 'post',
          body: JSON.stringify({
            invoiceId: getState().payments.view.selectPayment.invoiceAndUser.invoice.invoiceId,
            paymentId
          }),
          headers: new Headers({
            'content-type': 'application/json charset=utf-8',
            'Accept': 'application/json, application/xml, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        dispatch(showSnack(0, {
          label: `Afrekening ${getState().payments.view.selectPayment.invoiceAndUser.invoice.number} verbonden aan betaling ${paymentId}.`,
          timeout: 5000,
          button: { label: 'OK' }
        }))
        return dispatch(linkedInvoiceAndPayment(json))
      })
      .catch((err)=>{
          console.error('linkInvoiceAndPayment ERROR:',err)
          return dispatch(linkingInvoiceAndPaymentFailed(err))
      })
  }
}

// ------------------------------------------------------------------------------


const updatedPaymentIncludedInBalance = (json) => ({
    type: UPDATED_PAYMENT_INCLUDE_IN_BALANCE,
    paymentAndUser: json,
    receivedAt: Date.now()
})

const updatingPaymentIncludedInBalance = () => ({
    type: UPDATING_PAYMENT_INCLUDE_IN_BALANCE,
    receivedAt: Date.now()
})

const updatingPaymentIncludedInBalanceFailed = (err) => ({
    type: UPDATING_PAYMENT_INCLUDE_IN_BALANCE_FAILED,
    err: err,
    receivedAt: Date.now()
})

export const changeIncludeInBalance = (paymentId, includeInBalance) => {
  return (dispatch, getState) => {
    dispatch(updatingPaymentIncludedInBalance())
    return fetch(`${API_ENDPOINT}/api/payments/includeInBalance`, {
          method: 'post',
          credentials: 'same-origin',
          body: JSON.stringify({
            includeInBalance,
            paymentId
          }),
          headers: new Headers({
            'content-type': 'application/json charset=utf-8',
            'Accept': 'application/json, application/xml, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        dispatch(showSnack(0, {
          label: `Betaling ${paymentId} ${includeInBalance ? 'meegenomen' : 'uitgesloten'} bij berekening balans.`,
          timeout: 5000,
          button: { label: 'OK' }
        }))
        return dispatch(updatedPaymentIncludedInBalance(json))
      })
      .catch((err)=>{
        dispatch(showSnack(0, {
          label: `Er ging iets fout bij het updaten van de betaling.`,
          timeout: 5000,
          button: { label: 'Sluiten' }
        }))
          return dispatch(updatingPaymentIncludedInBalanceFailed(err))
      })
  }
}

// -----------------------------------------------------------------------------

const editingStatus = (tempStatus, id) => ({
    type: EDITING_PAYMENT_STATUS,
    tempStatus,
    id,
    receivedAt: Date.now()
})

export const editStatus = (id) => (dispatch, getState) => dispatch(editingStatus(getState().payments.view.payment.status, id))

export const changePaymentStatus = (tempStatus) => dispatch => dispatch({
  type: CHANGE_PAYMENT_STATUS,
  tempStatus,
  receivedAt: Date.now()
})

export const updatePaymentStatus = () => {
  return (dispatch, getState) => {
    dispatch(updatingPaymentStatus())
    return fetch(`${API_ENDPOINT}/api/payments/status`, {
          method: 'post',
          credentials: 'same-origin',
          body: JSON.stringify({
            paymentId: getState().payments.view.payment.paymentId,
            status: getState().payments.view.payment.tempStatus
          }),
          headers: new Headers({
            'content-type': 'application/json charset=utf-8',
            'Accept': 'application/json, application/xml, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        dispatch(showSnack(0, {
          label: `De status van betaling ${json.payment.paymentId} werd aangepast naar ${json.payment.status}.`,
          timeout: 5000,
          button: { label: 'OK' }
        }))
        return dispatch(updatedPaymentStatus(json))
      })
      .catch((err)=>{
        dispatch(showSnack(0, {
          label: `Er ging iets fout bij het updaten van de status van de betaling.`,
          timeout: 5000,
          button: { label: 'Sluiten' }
        }))
        console.error('updatingPaymentStatus ERROR:',err)
        return dispatch(updatingPaymentStatusFailed(err))
      })
  }
}

const updatingPaymentStatus = () => ({
    type: UPDATING_PAYMENT_STATUS,
    receivedAt: Date.now()
})

const updatingPaymentStatusFailed = (err) => ({
    type: UPDATING_PAYMENT_STATUS_FAILED,
    err: err,
    receivedAt: Date.now()
})

export const cancelUpdatePaymentStatus = () => dispatch => dispatch({
  type: CANCEL_UPDATE_PAYMENT_STATUS,
  receivedAt: Date.now()
})

const updatedPaymentStatus = (json) => {
  return {
    type: UPDATED_PAYMENT_STATUS,
    paymentAndUser: json,
    receivedAt: Date.now()
  }
}
