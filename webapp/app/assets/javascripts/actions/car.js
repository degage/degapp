import fetch from 'isomorphic-fetch'
import qs from 'qs'
import { API_ENDPOINT } from './../redux'
import { checkStatus } from './../util'
import { CarStatuses, CarValidationStatuses } from '../reducers/car'
import FormData from 'form-data'
import { showSnack } from './snackbar'
import moment from 'moment'

export const CREATED_CAR = 'CREATED_CAR'
export const CREATING_CAR = 'CREATING_CAR'
export const CREATING_CAR_FAILED = 'CREATING_CAR_FAILED'
export const UPLOADED_CAR_IMAGE = 'UPLOADED_CAR_IMAGE'
export const UPLOADING_CAR_IMAGE = 'UPLOADING_CAR_IMAGE'
export const UPLOADING_CAR_IMAGE_FAILED = 'UPLOADING_CAR_IMAGE_FAILED'
export const FETCHED_CAR = 'FETCHED_CAR'
export const FETCHING_CAR = 'FETCHING_CAR'
export const FETCHING_CAR_FAILED = 'FETCHING_CAR_FAILED'
export const CHANGE_SHARE_CAR = 'CHANGE_SHARE_CAR'
export const CHANGE_CAR_FIELD = 'CHANGE_CAR_FIELD'
export const CHANGE_ADDRESS_FIELD = 'CHANGE_ADDRESS_FIELD'
export const CHANGE_INSURANCE_FIELD = 'CHANGE_INSURANCE_FIELD'
export const CREATING_USER_ROLE = 'CREATING_USER_ROLE'
export const CREATED_USER_ROLE = 'CREATED_USER_ROLE'
export const CREATING_USER_ROLE_FAILED = 'CREATING_USER_ROLE_FAILED'
export const CAR_IMAGE_FILE_SELECTED = 'CAR_IMAGE_FILE_SELECTED'
export const SET_CAR_VALIDATION = 'SET_CAR_VALIDATION'
export const SET_USER = 'SET_USER'
export const UPDATE_CAR_PROPERTY = 'UPDATE_CAR_PROPERTY'
export const SET_CAR_SUGGESTION_TEXT = 'SET_CAR_SUGGESTION_TEXT'
export const CLEAR_CAR_SUGGESTIONS ='CLEAR_CAR_SUGGESTIONS'
export const FETCHING_CAR_SUGGESTIONS ='FETCHING_CAR_SUGGESTIONS'
export const FETCHING_CAR_SUGGESTIONS_FAILED ='FETCHING_CAR_SUGGESTIONS_FAILED'
export const SET_CAR_SUGGESTION_ID = 'SET_CAR_SUGGESTION_ID'
export const SET_CAR_SUGGESTION ='SET_CAR_SUGGESTION'
export const FETCHED_CAR_SUGGESTIONS ='FETCHED_CAR_SUGGESTIONS'
export const SET_CAR_START_DATEPICKER ='SET_CAR_START_DATEPICKER'
export const SET_CAR_STOP_DATEPICKER ='SET_CAR_STOP_DATEPICKER'
export const SWITCH_EXTRA_INFO = 'SWITCH_EXTRA_INFO'
export const EXTRA_INFO_CHANGE = 'EXTRA_INFO_CHANGE'
export const CREATING_RESERVATION_FAILED ='CREATING_RESERVATION_FAILED'
export const CREATING_RESERVATION = 'CREATING_RESERVATION'
export const CREATED_RESERVATION = 'CREATED_RESERVATION'
export const SWITCH_RESERVE_BTN = 'SWITCH_RESERVE_BTN'
export const END_DATE_BEFORE_START_DATE = 'END_DATE_BEFORE_START_DATE'
export const START_DATE_AFTER_END_DATE = 'START_DATE_AFTER_END_DATE'
export const FETCHING_CAR_INITIAL_STATE_FAILED = 'FETCHING_CAR_INITIAL_STATE_FAILED'
export const FETCHED_INITIAL_STATE_CAR = 'FETCHED_INITIAL_STATE_CAR'
export const CAR_INITIAL_STATE_FILE_SELECTED = 'CAR_INITIAL_STATE_FILE_SELECTED'

const createdCar = (json) => ({
    type: CREATED_CAR,
    car: json,
    receivedAt: Date.now()
})

const creatingCar = () => ({
    type: CREATING_CAR,
    receivedAt: Date.now()
})

const creatingCarFailed = (err) => ({
    type: CREATING_CAR_FAILED,
    err: err,
    receivedAt: Date.now()
})

const uploadingCarImage = () => ({
    type: UPLOADING_CAR_IMAGE,
    receivedAt: Date.now()
})

const uploadedCarImage = (json) => ({
    type: UPLOADED_CAR_IMAGE,
    carImage: json,
    receivedAt: Date.now()
})

const uploadingCarImageFailed = (err) => ({
    type: UPLOADING_CAR_IMAGE_FAILED,
    err: err,
    receivedAt: Date.now()
})

const creatingUserRole = () => ({
    type: CREATING_USER_ROLE,
    receivedAt: Date.now()
})

const creatingUserRoleFailed = () => ({
    type: CREATING_USER_ROLE_FAILED,
    receivedAt: Date.now()
})

const createdUserRole = () => ({
    type: CREATED_USER_ROLE,
    receivedAt: Date.now()
})

const mapCarStateToPostData = (car) => ({
    id: car.id,
    status: car.status,
    name: car.name,
    brand: car.brand,
    type: car.type,
    fuel: car.fuel,
    seats: car.seats,
    manual: car.manual,
    year: car.year,
    doors: car.doors,
    fuelEconomy: car.fuelEconomy,
    estimatedValue: car.estimatedValue,
    ownerAnnualKm: car.ownerAnnualKm,
    comments: car.comments,
    imagesId: car.imagesId,
    locationId: car.locationId,
    location: car.location,
    insurance: { ...car.insurance, expiration: car.insurance.expiration.format('YYYY-MM-DD') }
})

const mapUserRoleToPostData = (userId) => ({
    userId,
    role: 'CAR_USER'
})

export const createCar = (onCreated) => {
  return (dispatch, getState) => {
    dispatch(creatingCar())
    return fetch(`${API_ENDPOINT}/api/cars/new`, {
          credentials: 'same-origin',
          method: 'post',
          body: JSON.stringify(mapCarStateToPostData(getState().cars.car)),
          headers: new Headers({
            'content-type': 'application/json; charset=utf-8',
            'Accept': 'application/json, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        if (onCreated != null) {
            onCreated(json.id)
        } else {
            return dispatch(createdCar(json))
        }
      })
      .catch((err, response)=>{
          console.error('createCar ERROR:',response)
          return dispatch(creatingCarFailed(err))
      })
  }
}

export const updateCar = (autoId) => {
    return (dispatch, getState) => {
        dispatch(creatingCar())
        return fetch(`${API_ENDPOINT}/api/cars/update/${autoId}`, {
            credentials: 'same-origin',
            method: 'put',
            body: JSON.stringify(mapCarStateToPostData(getState().cars.car)),
            headers: new Headers({
                'content-type': 'application/json; charset=utf-8',
                'Accept': 'application/json, text/plain, text/html, *.*'
            }),
        })
        .then(checkStatus)
        .then(response => response.json())
        .then(json => {
            return dispatch(createdCar(json))
        })
        .catch((err, response) => {
            console.error('updateCar ERROR:', response)
            return dispatch(creatingCarFailed(err))
        })
    }
}

export const changeCarProperty = (fieldName, value) => dispatch => dispatch({
  type: UPDATE_CAR_PROPERTY,
  fieldName,
  value,
  receivedAt: Date.now()
})

const fetchingCar = (json) => ({
    type: FETCHING_CAR,
    receivedAt: Date.now()
})

const fetchingCarFailed = (err) => ({
    type: FETCHING_CAR_FAILED,
    err: err,
    receivedAt: Date.now()
})

const fetchedCar = (json, userId) => ({
    type: FETCHED_CAR,
    car: { ...json, insurance: { ...json.insurance, expiration: moment(json.insurance.expiration, 'DD-MM-YYYY') } },
    receivedAt: Date.now()
})


const setUser = (userId) => ({
    type: SET_USER,
    userId,
    receivedAt: Date.now()
})

const setCarValidation = (valid) => ({
    type: SET_CAR_VALIDATION,
    valid
})

const isCarValid = (car) => {
    return car.fuel != '' && car.ownerAnnualKm > 0 && car.fuelEconomy > 0 && car.estimatedValue > 0 &&
        car.seats > 0 && car.doors > 0 && car.year > 0 &&
        car.brand != '' && car.type != '' && car.insurance.insuranceNameBefore != '' && car.insurance.insuranceNameBefore != null &&
        car.location.street != '' && car.location.number != '' && car.location.zip != '' && car.location.city != '' &&
        car.imagesId > 0 & car.manual != undefined
}

export const goNext = (current) => {
    return (dispatch, getState) => {
        if (current === 'enrollWithCar') {            
            if (getState().cars.shareCar) {
                if (isCarValid(getState().cars.car)) {
                    dispatch(setCarValidation(true))
                    if (getState().cars.car.id > 0) {
                        dispatch(updateCar(getState().cars.car.id))
                        dispatch(navigateTo('/degapp/infosession/setInitialCarState/' + getState().cars.car.id))
                    } else {
                        const onCreated = (carId) => dispatch(navigateTo('/degapp/infosession/setInitialCarState/' + carId))
                        dispatch(createCar(onCreated))
                    }
                } else {
                    dispatch(setCarValidation(false))
                }
                // const status = getState().cars.view.car.status
                // if (status != CarStatuses.CREATING_CAR_FAILED && status != CarStatuses.CREATING_USER_ROLE_FAILED && getState().cars.view.car.validationStatus == CarValidationStatuses.VALID) {
                //     dispatch(navigateTo('/degapp/infosession/setInitialCarState/' + getState().cars.car.id))
                // }
            } else {
                dispatch(createCarUserRole())
                dispatch(navigateTo('/degapp/infosession'))
            }
        } else if (current === 'setInitialCarState') {
            dispatch(navigateTo('/degapp/infosession'))
        }
    }
}

export const navigateTo = (url) =>
    (dispatch, getState) => {
        // window.location.replace(url)
        window.location.href = url
    }

export const createCarUserRole = () => {
    return (dispatch, getState) => {
        dispatch(creatingUserRole())
        return fetch(`${API_ENDPOINT}/api/users/role`, {
            credentials: 'same-origin',
            method: 'post',
            body: JSON.stringify(mapUserRoleToPostData(getState().cars.userId)),
            headers: new Headers({
                'content-type': 'application/json; charset=utf-8',
                'Accept': 'application/json, text/plain, text/html, *.*'
            }),
        })
            .then(checkStatus)
            .then(response => response.json())
            .then(json => {
                return dispatch(createdUserRole(json))
            })
            .catch((err) => {
                console.error('createCarUserRole ERROR:', err)
                return dispatch(creatingUserRoleFailed(err))
            })
    }
}

export const fetchCarByUserId = (userId) => {
    return (dispatch, getState) => {
        dispatch(fetchingCar())
        dispatch(setUser(userId))
        return fetch(`${API_ENDPOINT}/api/cars/user/${userId}`, { credentials: 'same-origin' })
            .then(response => response.json())
            .then(json => {
                if (json != null) {
                    return dispatch(fetchedCar(json))
                }
            })
            .catch((err) => {
                console.error('fetch carByUser ERROR:', err)
                dispatch(fetchingCarFailed(err))
            })
    }
}

export const fetchCarInitialState = (carId) => {
    return (dispatch, getState) => {
        return fetch(`${API_ENDPOINT}/api/cars/initialstate/${carId}`, { credentials: 'same-origin' })
            .then(response => response.json())
            .then(json => {
                if (json != null) {
                    return dispatch(fetchedCarInitialState(json))
                }
            })
            .catch((err) => {
                console.error('fetch carByUser ERROR:', err)
                dispatch(fetchingCarInitialStateFailed(err))
            })
    }
}

const fetchingCarInitialStateFailed = (err) => ({
    type: FETCHING_CAR_INITIAL_STATE_FAILED,
    err: err,
    receivedAt: Date.now()
})

const fetchedCarInitialState = (json) => ({
    type: FETCHED_INITIAL_STATE_CAR,
    carInitialFiles: json,
    receivedAt: Date.now()
})

export const deleteCarInitialStateFile = (autoId, fileId) => {
    return (dispatch, getState) => {
        return fetch(`${API_ENDPOINT}/api/cars/initialstate/delete`, {
            credentials: 'same-origin',
            method: 'post',
            body: JSON.stringify({ autoId, fileId }),
            headers: new Headers({
                'content-type': 'application/json; charset=utf-8',
                'Accept': 'application/json, text/plain, text/html, *.*'
            }),
        })
            .then(response => response.json())
            .then(json => {
                return dispatch(fetchCarInitialState(autoId))
            })
            .catch((err, response) => {
                console.error('deleteCarInitialStateFile ERROR:', response)
                return dispatch(creatingCarFailed(err))
            })
    }
}

export const addCarInitialStateFile = (autoId) => {
    return (dispatch, getState) => {
        const formData = new FormData()
        formData.append('file', getState().cars.carInitialFile, getState().cars.carInitialFileName)
        return fetch(`${API_ENDPOINT}/api/cars/initialstate/add/${autoId}`, {
            credentials: 'same-origin',
            method: 'put',
            body: formData,
            headers: new Headers({
                'Accept': 'application/json, text/plain, text/html, *.*'
            }),
        })
            .then(response => response.json())
            .then(json => {
                return dispatch(fetchCarInitialState(autoId))
            })
            .catch((err, response) => {
                console.error('deleteCarInitialStateFile ERROR:', response)
                return dispatch(creatingCarFailed(err))
            })
    }
}

export const changeShareCar = (shareCar) => dispatch =>
    dispatch({
        type: CHANGE_SHARE_CAR,
        shareCar
    })

export const changeCarField = (fieldName, fieldValue) => dispatch =>
    dispatch({
        type: CHANGE_CAR_FIELD,
        fieldName,
        fieldValue
    })

export const changeAddressField = (fieldName, fieldValue) => dispatch =>
    dispatch({
        type: CHANGE_ADDRESS_FIELD,
        fieldName,
        fieldValue
    })

export const changeInsuranceField = (fieldName, fieldValue) => dispatch =>
    dispatch({
        type: CHANGE_INSURANCE_FIELD,
        fieldName,
        fieldValue
    })

export const selectCarImageFile = (fileName, file) => {
    return (dispatch, getState) => {
        dispatch({
            type: CAR_IMAGE_FILE_SELECTED,
            fileName,
            file,
            receivedAt: Date.now()
        })
        dispatch(uploadCarImage())
    }
}

export const selectCarInitialStateFile = (fileName, file) => {
    return (dispatch, getState) => {
        dispatch({
            type: CAR_INITIAL_STATE_FILE_SELECTED,
            fileName,
            file,
            receivedAt: Date.now()
        })
    }
}

export const uploadCarImage = () => {
    return (dispatch, getState) => {
        if (getState().cars.carImageFile !== null) {
            dispatch(uploadingCarImage())
            const formData = new FormData()
            formData.append('image', getState().cars.carImageFile, getState().cars.carImageFileName)
            return fetch(`${API_ENDPOINT}/api/cars/image`, {
                credentials: 'same-origin',
                method: 'put',
                body: formData,
                headers: new Headers({
                    'Accept': 'application/json, text/plain, text/html, *.*'
                }),
            })
                .then(checkStatus)
                .then(response => response.json())
                .then(json => {
                    dispatch(showSnack(0, {
                        label: `De image werd upgeload.`,
                        timeout: 5000,
                        button: { label: 'OK' }
                    }))
                    return (dispatch(uploadedCarImage(json))
                    )
                })
                .catch((err) => {
                    console.error('uploadCarImage ERROR:', err)
                    dispatch(showSnack(0, {
                        label: `Er ging iets fout.`,
                        timeout: 5000,
                        button: { label: 'Sluiten' }
                    }))
                    return dispatch(uploadingCarImageFailed(err))
                })
        }
    }
}

// -----------------------------------------------------------------------------

const fetchedCarSuggestions = (json) => ({
    type: FETCHED_CAR_SUGGESTIONS,
    cars: json.base,
    pageSize: json.pageSize,
    fullSize: json.fullSize,
    receivedAt: Date.now()
})

const fetchingCarSuggestions = () => ({
    type: FETCHING_CAR_SUGGESTIONS,
    receivedAt: Date.now()
})

const fetchingCarSuggestionsFailed = (err) => ({
    type: FETCHING_CAR_SUGGESTIONS_FAILED,
    err: err,
    receivedAt: Date.now()
})

export const fetchCarSuggestions = () => {
    return (dispatch, getState) => {
        dispatch(fetchingCarSuggestions())
        const state = getState()
        const { page, pageSize, orderBy, asc, filter} = state.cars.view.cars.table
        const queryObject = {
            page,
            pageSize,
            orderBy,
            asc,
            filter
            //search: state.cars.view.suggestionText,

        }
        const queryParams = qs.stringify(queryObject)
        return fetch(`${API_ENDPOINT}/api/cars?${queryParams}`, {credentials: 'same-origin'})
            .then(response =>
                response.json()
            )
            .then(json => {
                return dispatch(fetchedCarSuggestions(json))
            })
            .catch((err)=>{
                console.error('fetch car suggestions ERROR:',err)
                dispatch(fetchingCarSuggestionsFailed(err))
            })
    }
}

export const selectSuggestionId = (carId) => dispatch => {
    return dispatch({
        type: SET_CAR_SUGGESTION_ID,
        carId,
        receivedAt: Date.now()
    })
}

export const setSuggestion = (car) => dispatch => {
    return dispatch({
        type: SET_CAR_SUGGESTION,
        car,
        receivedAt: Date.now()
    })
}

export const changeCarSuggestionText = (suggestionText) => dispatch => {
    return dispatch({
        type: SET_CAR_SUGGESTION_TEXT,
        suggestionText,
        receivedAt: Date.now()
    })
}

export const clearCarSuggestions = () => dispatch => dispatch({
    type: CLEAR_CAR_SUGGESTIONS,
    receivedAt: Date.now()
})

// -----------------------------------------------------------------------------
export const setCarStartDatepicker = (datepicker, endDate) => dispatch => {
  if (datepicker.isSameOrAfter(endDate)){
    dispatch({
      type: START_DATE_AFTER_END_DATE,
      datepicker,
      err: "Waarschuwing: De ingevoerde eind datum is hetzelfde of bevindt zich voor de ingevoerde start datum. Gelieve dit te wijzigen.",
      receivedAt: Date.now()
    })
  } else {
    dispatch({
      type: SET_CAR_START_DATEPICKER,
      datepicker,
      err:"",
      receivedAt: Date.now()
    })
  }
}


export const setCarStopDatepicker = (datepicker, startDate) => dispatch => {
  if(datepicker.isSameOrBefore(startDate)){
    dispatch({
      type: END_DATE_BEFORE_START_DATE,
      datepicker,
      err: "Waarschuwing: De ingevoerde eind datum is hetzelfde of bevindt zich voor de ingevoerde start datum. Gelieve dit te wijzigen.",
      receivedAt: Date.now()
    })
  } else {
    dispatch({
      type: SET_CAR_STOP_DATEPICKER,
      datepicker,
      err: "",
      receivedAt: Date.now()
    })
  }
}

// -----------------------------------------------------------------------------

export const extraInfoSwitch  = (infoState) => dispatch => dispatch({
    type: SWITCH_EXTRA_INFO,
    newState: !infoState,
    receivedAt: Date.now()
})

export const extraInfoChange  = (leText) => dispatch => dispatch({
    type: EXTRA_INFO_CHANGE,
    leText,
    receivedAt: Date.now()
})

export const createReservation = (currentUserId) => {
    return (dispatch, getState) => {
    dispatch(creatingReservation())
    return fetch(`${API_ENDPOINT}/api/reservations/create/${getState().cars.view.suggestion.auto.id}`, {
          credentials: 'same-origin',
          method: 'post',
          body: JSON.stringify({
            from: getState().cars.reservation.datepicker.startDate.format("YYYY-MM-DD hh:mm"),
            until: getState().cars.reservation.datepicker.endDate.format("YYYY-MM-DD hh:mm"),
            message: getState().cars.reservation.extraInfoText,
            currentUserId: currentUserId
          }),

          headers: new Headers({
            'content-type': 'application/json; charset=utf-8',
            'Accept': 'application/json, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        dispatch(showSnack(0, {
          label: `Reservatie van auto ${getState().cars.view.suggestion.auto.name} is gelukt voor ${getState().cars.reservation.datepicker.startDate.format("YYYY-MM-DD hh:mm")} tot ${getState().cars.reservation.datepicker.startDate.format("YYYY-MM-DD hh:mm")} `,
          timeout: 7000,
          button: { label: 'OK' }
        }))
        window.location.href = 'degapp/trips'
          return dispatch(createdReservation(json))
      })
      .catch((err)=>{
          console.error('createReservation ERROR:', err)
          dispatch(createReservationFailed('Oops! Er liep iets fout, bekijk uw invoer.'))
      })
  }
}

const createReservationFailed = (err) => ({
    type: CREATING_RESERVATION_FAILED,
    err: err,
    receivedAt: Date.now()
})

const creatingReservation = () => ({
    type: CREATING_RESERVATION,
    receivedAt: Date.now()
})

const createdReservation = () => ({
    type: CREATED_RESERVATION,
    receivedAt: Date.now()
})

