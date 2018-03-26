import fetch from 'isomorphic-fetch'
import qs from 'qs'
import debounce from 'lodash/debounce'
import { API_ENDPOINT } from './../redux'
import { showSnack } from './snackbar'
import Cookies from 'universal-cookie'

export const FETCHED_INVOICES = 'FETCHED_INVOICES'
export const FETCHING_INVOICES = 'FETCHING_INVOICES'
export const FETCHING_INVOICES_FAILED = 'FETCHING_INVOICES_FAILED'
export const FETCHED_UNPAID_INVOICES = 'FETCHED_UNPAID_INVOICES'
export const FETCHING_UNPAID_INVOICES = 'FETCHING_UNPAID_INVOICES'
export const FETCHING_UNPAID_INVOICES_FAILED = 'FETCHING_UNPAID_INVOICES_FAILED'
export const FETCHED_INVOICE = 'FETCHED_INVOICE'
export const FETCHING_INVOICE = 'FETCHING_INVOICE'
export const FETCHING_INVOICE_FAILED = 'FETCHING_INVOICE_FAILED'
export const FETCHED_PAYMENT = 'FETCHED_PAYMENT'
export const FETCHING_PAYMENT = 'FETCHING_PAYMENT'
export const FETCHING_PAYMENT_FAILED = 'FETCHING_PAYMENT_FAILED'
export const FETCHED_PAYMENTS = 'FETCHED_PAYMENTS'
export const FETCHING_PAYMENTS = 'FETCHING_PAYMENTS'
export const FETCHING_PAYMENTS_FAILED = 'FETCHING_PAYMENTS_FAILED'
export const LINKING_PAYMENT_AND_INVOICE = 'LINKING_PAYMENT_AND_INVOICE'
export const LINKED_PAYMENT_AND_INVOICE = 'LINKED_PAYMENT_AND_INVOICE'
export const LINKING_PAYMENT_AND_INVOICE_FAILED = 'LINKING_PAYMENT_AND_INVOICE_FAILED'
export const UNLINKING_INVOICES_FOR_PAYMENT = 'UNLINKING_INVOICES_FOR_PAYMENT'
export const UNLINKED_INVOICES_FOR_PAYMENT = 'UNLINKED_INVOICES_FOR_PAYMENT'
export const UNLINKING_INVOICES_FOR_PAYMENT_FAILED = 'UNLINKING_PAYMENT_AND_INVOICE_FAILED'
export const SEARCHING_INVOICES = 'SEARCHING_INVOICES'
export const SEARCHING_INVOICES_FAILED = 'SEARCHING_INVOICES_FAILED'
export const SEARCHING_PAYMENTS = 'SEARCHING_PAYMENTS'
export const SEARCHING_PAYMENTS_FAILED = 'SEARCHING_PAYMENTS_FAILED'
export const INVOICES_OPEN_MODAL = 'INVOICES_OPEN_MODAL'
export const INVOICES_CLOSE_MODAL = 'INVOICES_CLOSE_MODAL'
export const SELECTING_INVOICE = 'SELECTING_INVOICE'
export const SORTING_PAYMENTS = 'SORTING_PAYMENTS'
export const SORTED_PAYMENTS = 'SORTED_PAYMENTS'
export const SORTING_PAYMENTS_FAILED = 'SORTING_PAYMENTS_FAILED'
export const SET_PAGE_INVOICES = 'SET_PAGE_INVOICES'
export const SET_SORT_ORDER_INVOICES = 'SET_SORT_ORDER_INVOICES'
export const SET_FILTER_INVOICES = 'SET_FILTER_INVOICES'
export const SET_PAGE_SELECT_INVOICE = 'SET_PAGE_SELECT_INVOICE'
export const SET_SORT_ORDER_SELECT_INVOICE = 'SET_SORT_ORDER_SELECT_INVOICE'
export const SET_FILTER_SELECT_INVOICE = 'SET_FILTER_SELECT_INVOICE'
export const SHOW_INVOICE = 'SHOW_INVOICE'
export const HIDE_INVOICE = 'HIDE_INVOICE'
export const FETCHED_INVOICES_INVOICES = 'FETCHED_INVOICES_INVOICES'
export const FETCHED_INVOICES_SELECT_INVOICE = 'FETCHED_INVOICES_SELECT_INVOICE'
export const HIDE_SELECT_INVOICE = 'HIDE_SELECT_INVOICE'
export const EDITING_INVOICE_STATUS = 'EDITING_INVOICE_STATUS'
export const CHANGE_INVOICE_STATUS = 'CHANGE_INVOICE_STATUS'
export const UPDATING_INVOICE_STATUS = 'UPDATING_INVOICE_STATUS'
export const UPDATED_INVOICE_STATUS = 'UPDATED_INVOICE_STATUS'
export const UPDATING_INVOICE_STATUS_FAILED = 'UPDATING_INVOICE_STATUS_FAILED'
export const CANCEL_UPDATE_INVOICE_STATUS = 'CANCEL_UPDATE_INVOICE_STATUS'
export const SET_PAGESIZE_INVOICES = 'SET_PAGESIZE_INVOICES'

const cookies = new Cookies()

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

const fetchedInvoice = (json) => ({
    type: FETCHED_INVOICE,
    invoice: json,
    receivedAt: Date.now()
})

const fetchingInvoice = () => ({
    type: FETCHING_INVOICE,
    receivedAt: Date.now()
})

const fetchingInvoiceFailed = (err) => ({
    type: FETCHING_INVOICE_FAILED,
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

const fetchedInvoicesInvoices = (json) => ({
    type: FETCHED_INVOICES_INVOICES,
    invoices: json,
    receivedAt: Date.now()
})

const fetchedInvoicesSelectInvoice = (json) => ({
    type: FETCHED_INVOICES_SELECT_INVOICE,
    invoices: json,
    receivedAt: Date.now()
})

const fetchingInvoices = () => ({
    type: FETCHING_INVOICES,
    receivedAt: Date.now()
})

const selectingInvoice = (paymentId) => ({
    type: SELECTING_INVOICE,
    paymentId,
    receivedAt: Date.now()
})

const fetchingInvoicesFailed = (err) => ({
    type: FETCHING_INVOICES_FAILED,
    err: err,
    receivedAt: Date.now()
})

const linkingPaymentAndInvoice = () => ({
    type: LINKING_PAYMENT_AND_INVOICE,
    receivedAt: Date.now()
})

const linkedPaymentAndInvoice = (json) =>
dispatch => {
  dispatch(changeFilterSelectInvoice(''))
  dispatch({
      type: LINKED_PAYMENT_AND_INVOICE,
      paymentAndUser: json,
      receivedAt: Date.now()
  })
}

const unlinkingInvoicesForPayment = () => ({
    type: UNLINKING_INVOICES_FOR_PAYMENT,
    receivedAt: Date.now()
})

const unlinkedInvoicesForPayment = (json) => ({
    type: UNLINKED_INVOICES_FOR_PAYMENT,
    paymentAndUser: json,
    receivedAt: Date.now()
})

const unlinkingInvoicesForPaymentFailed = () => ({
    type: UNLINKING_INVOICES_FOR_PAYMENT_FAILED,
    receivedAt: Date.now()
})

const searchingInvoices = (invoiceSearch) => ({
    type: SEARCHING_INVOICES,
    invoiceSearch,
    receivedAt: Date.now()
})

const searchingInvoicesFailed = (err) => ({
    type: SEARCHING_INVOICES_FAILED,
    err: err,
    receivedAt: Date.now()
})

const searchingPayments = (paymentSearch) => ({
    type: SEARCHING_PAYMENTS,
    paymentSearch,
    receivedAt: Date.now()
})

const searchingPaymentsFailed = (err) => ({
    type: SEARCHING_PAYMENTS_FAILED,
    err: err,
    receivedAt: Date.now()
})

const linkingPaymentAndInvoiceFailed = (err) => ({
    type: LINKING_PAYMENT_AND_INVOICE_FAILED,
    err: err,
    receivedAt: Date.now()
})

const setPageSelectInvoice = (page) => ({
  type: SET_PAGE_SELECT_INVOICE,
  page,
  receivedAt: Date.now()
})

const setSortOrderSelectInvoice = (orderBy, asc) => ({
  type: SET_SORT_ORDER_SELECT_INVOICE,
  orderBy,
  asc,
  receivedAt: Date.now()
})

const setFilterSelectInvoice = (filter) => ({
  type: SET_FILTER_SELECT_INVOICE,
  filter,
  receivedAt: Date.now()
})

const setPageInvoices = (page) => ({
  type: SET_PAGE_INVOICES,
  page,
  receivedAt: Date.now()
})

const setSortOrderInvoices = (orderBy, asc) => ({
  type: SET_SORT_ORDER_INVOICES,
  orderBy,
  asc,
  receivedAt: Date.now()
})

const setFilterInvoices = (filter) => ({
  type: SET_FILTER_INVOICES,
  filter,
  receivedAt: Date.now()
})

const showingInvoice = (invoiceId) => ({
    type: SHOW_INVOICE,
    invoiceId,
    receivedAt: Date.now()
})

const closingInvoiceModal = () => ({
    type: HIDE_INVOICE,
    receivedAt: Date.now()
})

const closingSelectInvoiceModal = () => ({
    type: HIDE_SELECT_INVOICE,
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

export const openModal = () => ({
  type: INVOICES_OPEN_MODAL
})

export const closeModal = () => ({
  type: INVOICES_CLOSE_MODAL
})

export const linkPaymentAndInvoice = (invoiceId) => {
  return (dispatch, getState) => {
    dispatch(linkingPaymentAndInvoice())
    return fetch(`${API_ENDPOINT}/api/payments/link`, {
          credentials: 'same-origin',
          method: 'post',
          body: JSON.stringify({
            paymentId: getState().payments.payment.paymentId,
            invoiceId
          }),
          headers: new Headers({
            'content-type': 'application/json; charset=utf-8',
            'Accept': 'application/json, application/xml, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        dispatch(showSnack(0, {
          label: `Betaling ${getState().payments.payment.paymentId} verbonden aan afrekening ${invoiceId}.`,
          timeout: 5000,
          button: { label: 'OK' }
        }))
        return dispatch(linkedPaymentAndInvoice(json))
      })
      .catch((err)=>{
          console.error('linkPaymentAndInvoice ERROR:',err)
          return dispatch(linkingPaymentAndInvoiceFailed(err))
      })
  }
}

export const unlinkInvoicesForPayment = (paymentId) => {
  return (dispatch, getState) => {
    dispatch(unlinkingInvoicesForPayment())
    return fetch(`${API_ENDPOINT}/api/payments/unlink`, {
          method: 'post',
          credentials: 'same-origin',
          body: JSON.stringify({
            paymentId
          }),
          headers: new Headers({
            'content-type': 'application/json; charset=utf-8',
            'Accept': 'application/json, application/xml, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        dispatch(showSnack(0, {
          label: `Alle afrekeningen gelinked aan betaling ${paymentId} verwijderd.`,
          timeout: 5000,
          button: { label: 'OK' }
        }))
        return dispatch(unlinkedInvoicesForPayment(json))
      })
      .catch((err)=>{
          console.error('unlinkInvoicesForPayment ERROR:',err)
          return dispatch(unlinkingInvoicesForPaymentFailed(err))
      })
  }
}

const debounceFetchInvoicesInvoices = (filter) => debounce((dispatch, getState) => {
    if (getState().invoices.view.invoices.table.filter === filter)
      return dispatch(fetchInvoicesInvoices())
}, 800)

const debounceFetchInvoicesSelectInvoice = (filter) => debounce((dispatch, getState) => {
    if (getState().invoices.view.selectInvoice.table.filter === filter)
      return dispatch(fetchInvoicesSelectInvoice())
}, 800)

export const fetchInvoicesInvoices = () => {
  return (dispatch, getState) => {
    dispatch(fetchingInvoices())
    const state = getState()
    const { page, pageSize, orderBy, asc, filter} = state.invoices.view.invoices.table
    const queryObject = {
      page,
      pageSize,
      orderBy,
      asc,
      filter
    }
    const queryParams = qs.stringify(queryObject)
    return fetch(`${API_ENDPOINT}/api/invoices?${queryParams}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedInvoicesInvoices(json))
      })
      .catch((err)=>{
        console.error('fetch invoices ERROR:',err)
        dispatch(fetchingInvoicesFailed(err))
      })
  }
}

export const fetchInvoicesSelectInvoice = () => {
  return (dispatch, getState) => {
    dispatch(fetchingInvoices())
    const state = getState()
    const { page, pageSize, orderBy, asc, filter} = state.invoices.view.selectInvoice.table
    const queryObject = {
      page,
      pageSize,
      orderBy,
      asc,
      filter
    }
    const queryParams = qs.stringify(queryObject)
    return fetch(`${API_ENDPOINT}/api/invoices?${queryParams}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedInvoicesSelectInvoice(json))
      })
      .catch((err)=>{
        console.error('fetch invoices ERROR:',err)
        dispatch(fetchingInvoicesFailed(err))
      })
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

export const searchInvoices = (invoiceSearch) => {
  return dispatch => {
    dispatch(searchingInvoices(invoiceSearch))
    return fetch(`${API_ENDPOINT}/api/invoices/search/${invoiceSearch}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedInvoices(json))
      })
      .catch((err)=>{
        console.error('fetch invoices ERROR:',err)
        dispatch(searchingInvoicesFailed(err))
      })
  }
}

export const searchPayments = (paymentSearch) => {
  return dispatch => {
    dispatch(searchingPayments(paymentSearch))
    return fetch(`${API_ENDPOINT}/api/payments/search/${paymentSearch}`, {credentials: 'same-origin'})
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

export const linkInvoice = (paymentId, name) =>
  dispatch => {
    dispatch(changeFilterSelectInvoice(name))
    dispatch(fetchPayment(paymentId))
    dispatch(selectingInvoice(paymentId))
  }

export const unlinkInvoices = (paymentId) =>
  dispatch => {
    dispatch(unlinkInvoicesForPayment(paymentId))
  }

export const sortTableSelectInvoice = (orderBy, asc) =>
  dispatch => {
    dispatch(setSortOrderSelectInvoice(orderBy, asc))
    dispatch(fetchInvoicesSelectInvoice())
}

export const changePageSelectInvoice = (page) =>
  dispatch => {
    dispatch(setPageSelectInvoice(page))
    dispatch(fetchInvoicesSelectInvoice())
}

export const changeFilterSelectInvoice = (filter) =>
  dispatch => {
    dispatch(setFilterSelectInvoice(filter))
    dispatch(debounceFetchInvoicesSelectInvoice(filter))
}

export const sortTableInvoices = (orderBy, asc) =>
  dispatch => {
    cookies.set('inv:orderBy', orderBy, { path: '/' });
    cookies.set('inv:asc', asc, { path: '/' });
    dispatch(setSortOrderInvoices(orderBy, asc))
    dispatch(fetchInvoicesInvoices())
}

export const changePageInvoices = (page) =>
  dispatch => {
    dispatch(setPageInvoices(page))
    dispatch(fetchInvoicesInvoices())
}

export const changeFilterInvoices = (filter) =>
  dispatch => {
    dispatch(setFilterInvoices(filter))
    dispatch(debounceFetchInvoicesInvoices(filter))
}

export const fetchInvoiceByNumber = (invoiceId) => {
  return dispatch => {
    dispatch(fetchingInvoice())
    return fetch(`${API_ENDPOINT}/api/invoices/number/${invoiceId}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedInvoice(json))
      })
      .catch((err)=>{
        console.error('fetch payment ERROR:',err)
        dispatch(fetchingInvoiceFailed(err))
      })
  }
}

export const showInvoiceByNumber = (invoiceNumber) =>
  dispatch => {
    dispatch(showingInvoice(invoiceNumber))
    dispatch(fetchInvoiceByNumber(invoiceNumber))
}

export const closeInvoiceModal = () => dispatch => dispatch(closingInvoiceModal())

export const closeSelectInvoiceModal = () =>
  dispatch => {
    dispatch(closingSelectInvoiceModal())
    dispatch(changeFilterSelectInvoice(''))
  }

// -----------------------------------------------------------------------------
const editingStatus = (tempStatus) => ({
    type: EDITING_INVOICE_STATUS,
    tempStatus,
    receivedAt: Date.now()
})

export const editStatus = () => (dispatch, getState) => dispatch(editingStatus(getState().invoices.invoice.invoice.status))

// -----------------------------------------------------------------------------

export const changeInvoiceStatus = (tempStatus) => dispatch => dispatch({
  type: CHANGE_INVOICE_STATUS,
  tempStatus,
  receivedAt: Date.now()
})

// -----------------------------------------------------------------------------

const updatedInvoiceStatus = (json) => ({
    type: UPDATED_INVOICE_STATUS,
    invoice: json,
    receivedAt: Date.now()
})

const updatingInvoiceStatus = () => ({
    type: UPDATING_INVOICE_STATUS,
    receivedAt: Date.now()
})

const updatingInvoiceStatusFailed = (err) => ({
    type: UPDATING_INVOICE_STATUS_FAILED,
    err: err,
    receivedAt: Date.now()
})

export const updateInvoiceStatus = () => {
  return (dispatch, getState) => {
    dispatch(updatingInvoiceStatus())
    return fetch(`${API_ENDPOINT}/api/invoices`, {
          method: 'post',
          credentials: 'same-origin',
          body: JSON.stringify({
            invoiceId: getState().invoices.invoice.invoice.invoiceId,
            status: getState().invoices.view.invoice.tempStatus
          }),
          headers: new Headers({
            'content-type': 'application/json; charset=utf-8',
            'Accept': 'application/json, application/xml, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        return dispatch(updatedInvoiceStatus(json))
      })
      .catch((err)=>{
          console.error('updatingInvoiceStatus ERROR:',err)
          return dispatch(updatingInvoiceStatusFailed(err))
      })
  }
}

// -----------------------------------------------------------------------------

export const cancelUpdateInvoiceStatus = () => dispatch => dispatch({
  type: CANCEL_UPDATE_INVOICE_STATUS,
  receivedAt: Date.now()
})

const setPagesizeInvoices = (size) => ({
    type: SET_PAGESIZE_INVOICES,
    pageSize: size,
    receivedAt: Date.now()
})

export const changePagesizeInvoices = (size) =>
  dispatch => {
    cookies.set('inv:ps', size, { path: '/' });
    dispatch(setPagesizeInvoices(size)),
    dispatch(fetchInvoicesInvoices())
}

// -----------------------------------------------------------------------------

const fetchingUnpaidInvoices = (json) => ({
    type: FETCHING_UNPAID_INVOICES,
    receivedAt: Date.now()
})

const fetchingUnpaidInvoicesFailed = (err) => ({
    type: FETCHING_UNPAID_INVOICES_FAILED,
    err: err,
    receivedAt: Date.now()
})

const fetchedUnpaidInvoices = (json) => ({
    type: FETCHED_UNPAID_INVOICES,
    unpaidInvoices: json,
    receivedAt: Date.now()
})

export const fetchUnpaidInvoices = () => {
  return (dispatch, getState) => {
    dispatch(fetchingUnpaidInvoices())
    return fetch(`${API_ENDPOINT}/api/invoices/unpaid`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedUnpaidInvoices(json))
      })
      .catch((err)=>{
        console.error('fetch unpaid invoices ERROR:',err)
        dispatch(fetchingUnpaidInvoicesFailed(err))
      })
  }
}
