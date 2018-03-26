import fetch from 'isomorphic-fetch'
import qs from 'qs'
import { showSnack } from './snackbar'
import { API_ENDPOINT } from './../redux'

export const FETCHING_SIGNATURE = 'FETCHING_SIGNATURE'
export const FETCHED_SIGNATURE = 'FETCHED_SIGNATURE'
export const FETCHING_SIGNATURE_FAILED = 'FETCHING_SIGNATURE_FAILED'
export const SHOW_VALUE = 'SHOW_VALUE'
export const CANCEL_SIGNATURE_UPDATE = 'CANCEL_SIGNATURE_UPDATE'
export const UPDATED_SIGNATURE = 'UPDATED_SIGNATURE'
export const UPDATING_SIGNATURE_FAILED = 'UPDATING_SIGNATURE_FAILED'
export const UPLOADING_SIGNATURE_IMAGE = 'UPLOADING_SIGNATURE_IMAGE'
export const UPLOADED_SIGNATURE_IMAGE = 'UPLOADED_SIGNATURE_IMAGE'
export const UPLOADING_SIGNATURE_IMAGE_FAILED = 'UPLOADING_SIGNATURE_IMAGE_FAILED'
export const UPDATING_SIGNATURE = 'UPDATING_SIGNATURE'
export const EDITING_SIGNATURE = 'EDITING_SIGNATURE'
export const FETCHING_SIGNATURE_IMAGE_FAILED = 'FETCHING_SIGNATURE_IMAGE_FAILED'
export const FETCHED_SIGNATURE_IMAGE= 'FETCHED_SIGNATURE_IMAGE'
export const SIGNATURE_IMAGE_FILE_SELECTED = 'SIGNATURE_IMAGE_FILE_SELECTED'

const fetchingSignature = () => ({
  type: FETCHING_SIGNATURE,
  receivedAt: Date.now()
})

const fetchedSignature = (json) => ({
  type: FETCHED_SIGNATURE,
  signature: json,
  receivedAt: Date.now()
})

const fetchingSignatureFailed = (err) => ({
  type: FETCHING_SIGNATURE_FAILED,
  err: err,
  receivedAt: Date.now()
})

const fetchedSignatureImage = (json) => ({
  type: FETCHED_SIGNATURE_IMAGE,
  signatureImage: json,
  receivedAt: Date.now()
})

const fetchingSignatureImageFailed = (err) => ({
  type: FETCHING_SIGNATURE_IMAGE_FAILED,
  err: err,
  receivedAt: Date.now()
})

const uploadingSignatureImage = () => ({
  type: UPLOADING_SIGNATURE_IMAGE,
  receivedAt: Date.now()
})

const uploadedSignatureImage = (json) => ({
  type: UPLOADED_SIGNATURE_IMAGE,
  signatureImage: json,
  receivedAt: Date.now()
})

const uploadingSignatureImageFailed = (err) => ({
  type: UPLOADING_SIGNATURE_IMAGE_FAILED,
  err: err,
  receivedAt: Date.now()
})

export const fetchSettings = () => {
  return dispatch => {
    dispatch(fetchSignature())
  }
}

export const fetchSignature = () => {
  return (dispatch, getState) => {
    dispatch(fetchingSignature()),
    dispatch(fetchSignatureImage())
    const state = getState()
    return fetch(`${API_ENDPOINT}/api/properties/1`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedSignature(json))
      })
      .catch((err)=>{
        console.error('fetch reminders ERROR:',err)
        dispatch(fetchingSignatureFailed(err))
      })
  }
}

const fetchSignatureImage = () => {
  return (dispatch, getState) => {
    const state = getState()
    return fetch(`${API_ENDPOINT}/api/properties/2`, {credentials: 'same-origin'})
      .then(response =>
        response.json()
      )
      .then(json => {
        return dispatch(fetchedSignatureImage(json))
      })
      .catch((err)=>{
        console.error('fetch reminders ERROR:',err)
        dispatch(fetchingSignatureImageFailed(err))
      })
  }
}

export const editValue = () =>({
  type: SHOW_VALUE,
  receivedAt: Date.now()
})

export const cancelUpdateSignature = () => ({
  type: CANCEL_SIGNATURE_UPDATE,
  receivedAt: Date.now()
})

export const submitUpdateSignature = () => {
  return (dispatch, getState) => {
    dispatch(updatingSignature())
    dispatch(uploadSignatureImage())
    return fetch(`${API_ENDPOINT}/api/properties/1`, {
          method: 'put',
          credentials: 'same-origin',
          body: JSON.stringify({
            value: getState().settings.signatureTemp.value === "" ? getState().settings.signature.value : getState().settings.signatureTemp.value,
            key: getState().settings.signature.key
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
          label: `De email signature is aangepast.`,
          timeout: 5000,
          button: { label: 'OK' }
        }))
        return dispatch(updatedSignature(json))
      })
      .catch((err)=>{
          console.error('updatingSignature ERROR:',err)
          return dispatch(updatedSignatureFailed(err))
      })
  }
}


export const selectSignatureImageFile = (fileName, file) => dispatch => {
  return dispatch({
    type: SIGNATURE_IMAGE_FILE_SELECTED,
    fileName,
    file,
    receivedAt: Date.now()
  })
}

export const uploadSignatureImage = () => {
  return (dispatch, getState) => {
    if (getState().settings.signatureImageFile !== null){
      dispatch(uploadingSignatureImage())
      const formData = new FormData()
      formData.append('image', getState().settings.signatureImageFile, getState().settings.signatureImageFileName)
      return fetch(`${API_ENDPOINT}/api/properties/image/2`, {
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
          return (
            dispatch(uploadedSignatureImage(json)),
            dispatch(fetchSignatureImage())
          )
        })
        .catch((err)=>{
            console.error('uploadSignatureImage ERROR:',err)
            dispatch(showSnack(0, {
              label: `Er ging iets fout.`,
              timeout: 5000,
              button: { label: 'Sluiten' }
            }))
            return dispatch(uploadingSignatureImageFailed(err))
        })
      }
  }
}

export const editingSignature = (string) => ({
  type: EDITING_SIGNATURE,
  signature: string,
  receivedAt: Date.now()
})

export const updatingSignature = () => ({
  type: UPDATING_SIGNATURE,
  receivedAt: Date.now()
})

export const updatedSignature = (json) => ({
  type: UPDATED_SIGNATURE,
  signature: json,
  receivedAt: Date.now()
})

export const updatedSignatureFailed = (json) => ({
  type: UPDATING_SIGNATURE_FAILED,
  err,
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
