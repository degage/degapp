import fetch from 'isomorphic-fetch'
import qs from 'qs'
import debounce from 'lodash/debounce'
import { API_ENDPOINT } from './../redux'
import { checkStatus } from './../util'
import { showSnack } from './snackbar'

export const FETCHED_CODAS = 'FETCHED_CODAS'
export const FETCHING_CODAS = 'FETCHING_CODAS'
export const FETCHING_CODAS_FAILED = 'FETCHING_CODAS_FAILED'
export const UPLOADED_CODA = 'UPLOADED_CODA'
export const UPLOADING_CODA = 'UPLOADING_CODA'
export const UPLOADING_CODA_FAILED = 'UPLOADING_CODA_FAILED'
export const SET_PAGE_CODAS = 'SET_PAGE_CODAS'
export const SET_SORT_ORDER_CODAS = 'SET_SORT_ORDER_CODAS'
export const SET_FILTER_CODAS = 'SET_FILTER_CODAS'
export const CODA_FILE_SELECTED = 'CODA_FILE_SELECTED'
export const SET_PAGESIZE_CODAS = 'SET_PAGESIZE_CODAS'

const fetchedCodas = (json) => ({
    type: FETCHED_CODAS,
    codas: json,
    receivedAt: Date.now()
})

const fetchingCodas = () => ({
    type: FETCHING_CODAS,
    receivedAt: Date.now()
})

const fetchingCodasFailed = (err) => ({
    type: FETCHING_CODAS_FAILED,
    err: err,
    receivedAt: Date.now()
})

const uploadedCoda = (json) => ({
    type: UPLOADED_CODA,
    numberOfPayments: json.numberOfPayments,
    receivedAt: Date.now()
})

const uploadingCoda = () => ({
    type: UPLOADING_CODA,
    receivedAt: Date.now()
})

const uploadingCodaFailed = (err) => ({
    type: UPLOADING_CODA_FAILED,
    err: err,
    receivedAt: Date.now()
})

const setPage = (page) => ({
  type: SET_PAGE_CODAS,
  page,
  receivedAt: Date.now()
})

const setSortOrder = (orderBy, asc) => ({
  type: SET_SORT_ORDER_CODAS,
  orderBy,
  asc,
  receivedAt: Date.now()
})

const setFilter = (filter) => ({
  type: SET_FILTER_CODAS,
  filter,
  receivedAt: Date.now()
})

export const sortTable = (orderBy, asc) =>
  dispatch => {
    dispatch(setSortOrder(orderBy, asc))
    dispatch(fetchCodas())
}

export const changePage = (page) =>
  dispatch => {
    dispatch(setPage(page))
    dispatch(fetchCodas())
}

const debounceFetchCodas = (filter) => debounce((dispatch, getState) => {
    if (getState().codas.view.codas.table.filter === filter)
      return dispatch(fetchCodas())
}, 800)

export const changeFilterCodas = (filter) =>
  dispatch => {
    dispatch(setFilter(filter))
    dispatch(debounceFetchCodas(filter))
}

export const fetchCodas = () => {
  return (dispatch, getState) => {
    dispatch(fetchingCodas())
    const state = getState()
    const { page, pageSize, orderBy, asc, filter} = state.codas.view.codas.table
    const queryObject = {
      page,
      pageSize,
      orderBy,
      asc,
      filter
    }
    const queryParams = qs.stringify(queryObject)
    return fetch(`${API_ENDPOINT}/api/codas?${queryParams}`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedCodas(json))
      })
      .catch((err)=>{
        console.error('fetch codas ERROR:',err)
        dispatch(fetchingCodasFailed(err))
      })
  }
}

export const uploadCoda = (invoiceId) => {
  return (dispatch, getState) => {
    dispatch(uploadingCoda())
    const formData = new FormData()
    formData.append('coda', getState().codas.file, getState().codas.fileName)
    return fetch(`${API_ENDPOINT}/api/upload-coda`, {
          credentials: 'same-origin',
          method: 'post',
          body: formData,
          headers: new Headers({
            'Accept': 'application/json, text/plain, text/html, *.*'
          }),
        })
      .then(checkStatus)
      .then(response => response.json())
      .then(json => {
        dispatch(showSnack(0, {
          label: `De coda's werden verwerkt. Er werden ${json.numberOfPayments} betalingen aangemaakt.`,
          timeout: 5000,
          button: { label: 'OK' }
        }))
        return dispatch(uploadedCoda(json))
      })
      .catch((err)=>{
          console.error('uploadCoda ERROR:',err)
          dispatch(showSnack(0, {
            label: `Er ging iets fout.`,
            timeout: 5000,
            button: { label: 'Sluiten' }
          }))
          return dispatch(uploadingCodaFailed(err))
      })
  }
}

export const selectCodaFile = (fileName, file) => dispatch => {
  return dispatch({
    type: CODA_FILE_SELECTED,
    fileName,
    file,
    receivedAt: Date.now()
  })
}

const setPagesizeCodas = (size) => ({
    type: SET_PAGESIZE_CODAS,
    pageSize: size,
    receivedAt: Date.now()
})

export const changePagesizeCodas = (size) =>
  dispatch => {
    dispatch(setPagesizeCodas(size)),
    dispatch(fetchCodas())
}
