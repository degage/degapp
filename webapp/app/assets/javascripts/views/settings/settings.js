import React, { Component } from 'react'
import { connect } from 'react-redux'
import { editValue, cancelUpdateSignature, submitUpdateSignature, editingSignature, uploadSignatureImage, selectSignatureImageFile } from './../../actions/settings'
import {SettingsStatuses} from './../../reducers/settings'
import Signature from './signature.js'

const settings = ({signatureImage, signatureValue, signatureId, signatureStatus, signatureImageFileName, onValueClick, onCancelUpdateSignature,
  onSubmitUpdateSignature, onEditingSignature, onFileNameChanged}) => {
  return (
    <div>
      <div>
        <h2>Email signature</h2>
        <Signature
          image={signatureImage} value={signatureValue} id={signatureId} status={signatureStatus} signatureImageFileName={signatureImageFileName}
          onValueClick={onValueClick} onCancelUpdateSignature={onCancelUpdateSignature}
          onSubmitUpdateSignature={onSubmitUpdateSignature} onEditingSignature={onEditingSignature}
          onFileNameChanged={onFileNameChanged}/>
      </div>
    </div>
  )
}

const mapStateToProps = (state, ownProps) => {
  return {
    signatureValue: state.settings.signature.value,
    signatureId: state.settings.signature.propertyId,
    signatureStatus: state.settings.status,
    signatureImageFileName: state.settings.signatureImageFileName,
    signatureImage:  state.settings.image
}}

const mapDispatchToProps = {
  onValueClick: editValue,
  onCancelUpdateSignature: cancelUpdateSignature,
  onSubmitUpdateSignature: submitUpdateSignature,
  onFileNameChanged: selectSignatureImageFile,
  onEditingSignature: editingSignature
}

const SettingsContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(settings)

export default SettingsContainer
