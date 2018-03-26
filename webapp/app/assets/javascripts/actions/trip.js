import fetch from 'isomorphic-fetch'
import qs from 'qs'
import debounce from 'lodash/debounce'
import { API_ENDPOINT } from './../redux'

export const FETCHED_TRIPS = 'FETCHED_TRIPS'
export const FETCHING_TRIPS = 'FETCHING_TRIPS'
export const FETCHING_TRIPS_FAILED = 'FETCHING_TRIPS_FAILED'
export const FETCHED_TRIP = 'FETCHED_TRIP'
export const FETCHING_TRIP = 'FETCHING_TRIP'
export const FETCHING_TRIP_FAILED = 'FETCHING_TRIP_FAILED'
export const CANCELING_RESERVATION = 'CANCELING_RESERVATION'
export const CANCELING_RESERVATION_FAILED = 'CANCELING_RESERVATION_FAILED'
export const SHOW_TRIP = 'SHOW_TRIP'
export const HIDE_TRIP = 'HIDE_TRIP'

const fetchedTrips = (json) => ({
    type: FETCHED_TRIPS,
    trips: json.trips,
    car: json.car,
    startDate: json.startDate,
    receivedAt: Date.now()
})

const fetchingTrips = () => ({
    type: FETCHING_TRIPS,
    receivedAt: Date.now()
})

const fetchingTripsFailed = (err) => ({
    type: FETCHING_TRIPS_FAILED,
    err: err,
    receivedAt: Date.now()
})

const fetchedTrip = (json) => ({
    type: FETCHED_TRIP,
    trip: json.trip,
    driver: json.driver,
    previousDriver: json.previousDriver,
    nextDriver: json.nextDriver,
    owner: json.owner,
    nextDate: json.nextDate,
    receivedAt: Date.now()
})

const fetchingTrip = () => ({
    type: FETCHING_TRIP,
    receivedAt: Date.now()
})

const fetchingTripFailed = (err) => ({
    type: FETCHING_TRIP_FAILED,
    err: err,
    receivedAt: Date.now()
})

const debounceFetchTrips = (filter) => debounce((dispatch, getState) => {
    if (getState().trips.view.trips.table.filter === filter)
      return dispatch(fetchTrips())
}, 800)

export const changeFilter = (filter) =>
  dispatch => {
    dispatch(setFilter(filter))
    dispatch(debounceFetchTrips(filter))
}

export const fetchTripsByCar = (carId) => {
  return (dispatch, getState) => {
    dispatch(fetchingTrips())
    return fetch(`${API_ENDPOINT}/api/trips/car/${carId}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedTrips(json))
      })
      .catch((err)=>{
        console.error('fetch trips ERROR:',err)
        dispatch(fetchingTripsFailed(err))
      })
  }
}

export const fetchTrip = (tripId) => {
  return (dispatch, getState) => {
    dispatch(fetchingTrip())
    return fetch(`${API_ENDPOINT}/api/trips/${tripId}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedTrip(json))
      })
      .catch((err)=>{
        console.error('fetch trip ERROR:',err)
        dispatch(fetchingTripFailed(err))
      })
  }
}

// ----------------------------------------------------------------------------
const showingTrip = (trip) => ({
    type: SHOW_TRIP,
    trip,
    receivedAt: Date.now()
})

export const showTrip = (tripId) =>
  dispatch => {
    dispatch(showingTrip(tripId))
    dispatch(fetchTrip(tripId))
}

// ----------------------------------------------------------------------------
const closingTripModal = () => ({
    type: HIDE_TRIP,
    receivedAt: Date.now()
})

export const closeTripModal = () =>
  dispatch =>
    dispatch(closingTripModal())

// ----------------------------------------------------------------------------
const cancelingReservation = (tripId) => ({
    type: CANCELING_RESERVATION,
    tripId,
    receivedAt: Date.now()
})

const cancelingReservationFailed = (err) => ({
    type: CANCELING_RESERVATION_FAILED,
    err: err,
    receivedAt: Date.now()
})

export const cancelReservation = (tripId) => {
  return (dispatch, getState) => {
    dispatch(cancelingReservation())
    return fetch(`${API_ENDPOINT}/api/reservations/cancel`, {
          method: 'post',
          credentials: 'same-origin',
          body: JSON.stringify({ tripId: tripId }),
          headers: new Headers({
            'content-type': 'application/json; charset=utf-8',
            'Accept': 'application/json, application/xml, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => { return dispatch(fetchedTrip(json)) })
      .catch((err)=>{
        console.error('cancel trip ERROR:',err)
        dispatch(cancelingReservationFailed(err))
      })
  }
}
