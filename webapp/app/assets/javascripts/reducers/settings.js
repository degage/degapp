import update from 'immutability-helper'
import { findIndex } from 'lodash/findIndex'

export const SettingsStatuses = {
  INIT: 'INIT',
  FETCHING_SIGNATURE: 'FETCHING_SIGNATURE',
  FETCHED_SIGNATURE: 'FETCHED_SIGNATURE',
  FETCHING_SIGNATURE_FAILED: 'FETCHING_SIGNATURE_FAILED',
  SHOW_VALUE: 'SHOW_VALUE',
  EDIT_VALUE: 'EDIT_VALUE',
  CANCEL_SIGNATURE_UPDATE: 'CANCEL_SIGNATURE_UPDATE',
  UPDATING_SIGNATURE: 'UPDATING_SIGNATURE',
  UPDATED_SIGNATURE: 'UPDATED_SIGNATURE',
  EDITING_SIGNATURE: 'EDITING_SIGNATURE',
  FETCHING_SIGNATURE_IMAGE_FAILED: 'FETCHING_SIGNATURE_IMAGE_FAILED',
  FETCHED_SIGNATURE_IMAGE: 'FETCHED_SIGNATURE_IMAGE',
  UPLOADED_SIGNATURE_IMAGE: 'UPLOADED_SIGNATURE_IMAGE'
}

export const settingsReducer = (state = {
    signature: [],
    signatureImageFileName: '',
    signatureImageFile: null,
    status: SettingsStatuses.INIT,
    err: '',
    signatureTemp: {
      value: ''
    }
  }, action) => {
    switch (action.type) {
      case 'FETCHING_SIGNATURE':
        return update(state, {
          status: {$set: SettingsStatuses.FETCHING_SIGNATURE}
        })
      case 'FETCHING_SIGNATURE_FAILED':
        return update(state, {
          status: {$set: SettingsStatuses.FETCHING_SIGNATURE_FAILED}
        })
      case 'FETCHED_SIGNATURE':
        return update(state, {
          signature: {$set: action.signature},
          status: {$set: SettingsStatuses.FETCHED_SIGNATURE}
        })
      case 'FETCHED_SIGNATURE_IMAGE':
        return update(state, {
          image: {$set: action.signatureImage.value},
          status: {$set: SettingsStatuses.FETCHED_SIGNATURE_IMAGE}
        })
      case 'SHOW_VALUE':
        return update(state, {
          status: {$set: SettingsStatuses.EDITING_SIGNATURE}
        })
      case 'CANCEL_SIGNATURE_UPDATE':
        return update(state, {
          status: {$set: SettingsStatuses.CANCEL_SIGNATURE_UPDATE}
        })
      case 'UPDATING_SIGNATURE':
        return update(state, {
          status: {$set: SettingsStatuses.UPDATING_SIGNATURE}
        })
      case 'UPDATED_SIGNATURE':
        return update(state, {
          signature: {$set: action.signature},
          status: {$set: SettingsStatuses.UPDATED_SIGNATURE}
        })
      case 'UPDATING_SIGNATURE_FAILED':
        return update(state, {
          status: {$set: SettingsStatuses.UPDATING_SIGNATURE_FAILED}
        })
      case 'FETCHING_SIGNATURE_IMAGE_FAILED':
        return update(state, {
          status: {$set: SettingsStatuses.FETCHING_SIGNATURE_IMAGE_FAILED}
        })
      case 'EDITING_SIGNATURE':
        return update(state, {
          signatureTemp: {
            value: {$set: action.signature}
          },
          status: {$set: SettingsStatuses.EDITING_SIGNATURE}
        })
      case 'SIGNATURE_IMAGE_FILE_SELECTED':
        return update(state, {
          signatureImageFileName: {$set: action.fileName},
          signatureImageFile: {$set: action.file}
        })
      default:
          return state;
      }
    };
