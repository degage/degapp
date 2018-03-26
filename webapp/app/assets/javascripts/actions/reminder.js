import fetch from 'isomorphic-fetch'
import qs from 'qs'
import debounce from 'lodash/debounce'
import { showSnack } from './snackbar'
import { API_ENDPOINT } from './../redux'
import Cookies from 'universal-cookie'

const cookies = new Cookies()

export const FETCHED_REMINDERS = 'FETCHED_REMINDERS'
export const FETCHING_REMINDERS = 'FETCHING_REMINDERS'
export const FETCHING_REMINDERS_FAILED = 'FETCHING_REMINDERS_FAILED'
export const CREATED_REMINDERS = 'CREATED_REMINDERS'
export const CREATING_REMINDERS = 'CREATING_REMINDERS'
export const CREATING_REMINDERS_FAILED = 'CREATING_REMINDERS_FAILED'
export const MAILED_REMINDER = 'MAILED_REMINDER'
export const MAILING_REMINDER = 'MAILING_REMINDER'
export const MAILING_REMINDER_FAILED = 'MAILING_REMINDER_FAILED'
export const SET_PAGE_REMINDERS = 'SET_PAGE_REMINDERS'
export const SET_SORT_ORDER_REMINDERS = 'SET_SORT_ORDER_REMINDERS'
export const SET_FILTER_REMINDERS = 'SET_FILTER_REMINDERS'
export const SHOW_INVOICE = 'SHOW_INVOICE'
export const HIDE_INVOICE = 'HIDE_INVOICE'
export const SET_PAGESIZE_REMINDERS = 'SET_PAGESIZE_REMINDERS'

const fetchedReminders = (json) => ({
    type: FETCHED_REMINDERS,
    reminders: json,
    receivedAt: Date.now()
})

const fetchingReminders = () => ({
    type: FETCHING_REMINDERS,
    receivedAt: Date.now()
})

const fetchingRemindersFailed = (err) => ({
    type: FETCHING_REMINDERS_FAILED,
    err: err,
    receivedAt: Date.now()
})

const setPage = (page) => ({
  type: SET_PAGE_REMINDERS,
  page,
  receivedAt: Date.now()
})

const setSortOrder = (orderBy, asc) => ({
  type: SET_SORT_ORDER_REMINDERS,
  orderBy,
  asc,
  receivedAt: Date.now()
})

const setFilter = (filter) => ({
  type: SET_FILTER_REMINDERS,
  filter,
  receivedAt: Date.now()
})

export const sortTable = (orderBy, asc) =>
  dispatch => {
    cookies.set('remind:orderBy', orderBy, { path: '/' })
    cookies.set('remind:asc', asc, { path: '/' })
    dispatch(setSortOrder(orderBy, asc))
    dispatch(fetchReminders())
}

export const changePage = (page) =>
  dispatch => {
    dispatch(setPage(page))
    dispatch(fetchReminders())
}

const debounceFetchReminders = (filter) => debounce((dispatch, getState) => {
    if (getState().reminders.view.reminders.table.filter === filter)
      return dispatch(fetchReminders())
}, 800)

export const changeFilter = (filter) =>
  dispatch => {
    dispatch(setFilter(filter))
    dispatch(debounceFetchReminders(filter))
}

export const fetchReminders = () => {
  return (dispatch, getState) => {
    dispatch(fetchingReminders())
    const state = getState()
    const { page, pageSize, orderBy, asc, filter} = state.reminders.view.reminders.table
    const queryObject = {
      page,
      pageSize,
      orderBy,
      asc,
      filter
    }
    const queryParams = qs.stringify(queryObject)
    return fetch(`${API_ENDPOINT}/api/reminders?${queryParams}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedReminders(json))
      })
      .catch((err)=>{
        console.error('fetch reminders ERROR:',err)
        dispatch(fetchingRemindersFailed(err))
      })
  }
}

const setPagesizeReminders = (size) => ({
    type: SET_PAGESIZE_REMINDERS,
    pageSize: size,
    receivedAt: Date.now()
})

export const changePagesizeReminders = (size) =>
  dispatch => {
    cookies.set('remind:ps', size, { path: '/' })
    dispatch(setPagesizeReminders(size)),
    dispatch(fetchReminders())
}

// -----------------------------------------------------------------------------

const creatingReminders = () => ({
    type: CREATING_REMINDERS,
    receivedAt: Date.now()
})

const creatingRemindersFailed = (err) => ({
    type: CREATING_REMINDERS_FAILED,
    err: err,
    receivedAt: Date.now()
})

const createdReminders = (json) => ({
    type: CREATED_REMINDERS,
    receivedAt: Date.now()
})

export const createReminders = () => {
  return (dispatch, getState) => {
    dispatch(creatingReminders())
    return fetch(`${API_ENDPOINT}/api/reminders/create`, {
      credentials: 'same-origin',
      method: 'post',
      headers: new Headers({
        'Accept': 'application/json, text/plain, text/html, *.*'
      })})
      .then(response =>
        response.json()
      )
      .then(json => {
        dispatch(createdReminders(json))
        return dispatch(fetchReminders())
      })
      .catch((err)=>{
        console.error('create reminders ERROR:',err)
        dispatch(creatingRemindersFailed(err))
      })
  }
}

// -----------------------------------------------------------------------------

const mailingReminder = () => ({
    type: MAILING_REMINDER,
    receivedAt: Date.now()
})


const mailingReminderFailed = (err) => ({
    type: MAILING_REMINDER_FAILED,
    err: err,
    receivedAt: Date.now()
})

const mailedReminder = (json) => ({
    type: MAILED_REMINDER,
    reminder: json,
    receivedAt: Date.now()
})

export const mailReminder = (reminderId) => {
  return (dispatch, getState) => {
    dispatch(mailingReminder())
    return fetch(`${API_ENDPOINT}/api/reminders/mail/${reminderId}`, {
      credentials: 'same-origin',
      method: 'post',
      headers: new Headers({
        'Accept': 'application/json, text/plain, text/html, *.*'
      })})
      .then(response =>
        response.json()
      )
      .then(json => {
        dispatch(showSnack(0, {
          label: `Rappel ${json.reminder.id} werd verstuurd naar ${json.user.firstName} ${json.user.lastName}.`,
          timeout: 5000,
          button: { label: 'OK' }
        }))
        return dispatch(mailedReminder(json))
      })
      .catch((err)=>{
        console.error('mail reminder ERROR:',err)
        dispatch(showSnack(0, {
          label: `Er ging iets fout bij het verzenden van de rappel.`,
          timeout: 5000,
          button: { label: 'Sluiten' }
        }))
        dispatch(mailingReminderFailed(err))
      })
  }
}
