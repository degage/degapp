import fetch from 'isomorphic-fetch'
import qs from 'qs'
import debounce from 'lodash/debounce'
import { API_ENDPOINT } from './../redux'
import { checkStatus } from './../util'
import { showSnack } from './snackbar'

export const FETCHED_INFOSESSIONS = 'FETCHED_INFOSESSIONS'
export const FETCHING_INFOSESSIONS = 'FETCHING_INFOSESSIONS'
export const FETCHING_INFOSESSIONS_FAILED = 'FETCHING_INFOSESSIONS_FAILED'
export const FETCHED_ATTENDING_INFOSESSIONS = 'FETCHED_ATTENDING_INFOSESSIONS'
export const FETCHING_ATTENDING_INFOSESSIONS = 'FETCHING_ATTENDING_INFOSESSIONS'
export const FETCHING_ATTENDING_INFOSESSIONS_FAILED = 'FETCHING_ATTENDING_INFOSESSIONS_FAILED'

const fetchedInfosessions = (json) => ({
    type: FETCHED_INFOSESSIONS,
    infosessions: json,
    receivedAt: Date.now()
})

const fetchingInfosessions = () => ({
    type: FETCHING_INFOSESSIONS,
    receivedAt: Date.now()
})

const fetchingInfosessionsFailed = (err) => ({
    type: FETCHING_INFOSESSIONS_FAILED,
    err: err,
    receivedAt: Date.now()
})

export const fetchUpcomingInfosessions = () => {
  return (dispatch, getState) => {
    dispatch(fetchingInfosessions())
    return fetch(`${API_ENDPOINT}/api/infosessions/upcoming`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedInfosessions(json))
      })
      .catch((err)=>{
        console.error('fetch infosessions ERROR:',err)
        dispatch(fetchingInfosessionsFailed(err))
      })
  }
}

export const attendingInfosessions = () => {
  return (dispatch, getState) => {
    dispatch(fetchingAttendingInfosessions())
    return fetch(`${API_ENDPOINT}/api/infosessions/attending`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedAttendingInfosessions(json))
      })
      .catch((err)=>{
        console.error('fetch infosessions ERROR:',err)
        dispatch(fetchingAttendingInfosessionsFailed(err))
      })
  }
}

const fetchedAttendingInfosessions = (json) => ({
    type: FETCHED_ATTENDING_INFOSESSIONS,
    attending: json,
    receivedAt: Date.now()
})

const fetchingAttendingInfosessions = () => ({
    type: FETCHING_ATTENDING_INFOSESSIONS,
    receivedAt: Date.now()
})

const fetchingAttendingInfosessionsFailed = (err) => ({
    type: FETCHING_ATTENDING_INFOSESSIONS_FAILED,
    err: err,
    receivedAt: Date.now()
})
