import React, { Component } from 'react';
import { connect } from 'react-redux';
import { editValue, cancelUpdateSignature, submitUpdateSignature, editingSignature, uploadSignatureImage, selectSignatureImageFile } from './../../actions/settings'
import {SettingsStatuses} from './../../reducers/settings'

const signature = ({image, value, id, status, signatureImageFileName, onValueClick, onCancelUpdateSignature,
  onSubmitUpdateSignature, onEditingSignature, onFileNameChanged}) => {
  return (
    <div>
      {
        status !== SettingsStatuses.EDITING_SIGNATURE ?
          value == undefined ? '' :
             <p>Link: <a href={value}>{value}</a>{' '}<button onClick={valueClicked(onValueClick)} key='mail-signature-link'>Edit</button></p>
        :
          value == undefined ? '' :
          <div>
            <input onChange={editSignature(onEditingSignature)} style={{width: '100%'}} defaultValue={value}></input>
            {
              image == undefined ? '' :
              <div>
                <input id='myInput' type='file' style={{ display: 'none' }} />
                <img alt={image} src={`/degapp/api/images/${image}`} />
              </div>
            }
            <input type='file' name='image' className='form-control input-md' onChange={fileNameChanged(onFileNameChanged)} value={signatureImageFileName} style={{marginTop: '5px'}}/>
            <div className='btn-group' role='group' style={{width: '100%', size: '12px', marginTop: '10px' }}>
              <button className='btn btn-default' onClick={signatureSubmitClicked(onSubmitUpdateSignature)} style={{size: '12px', border: 'none'}}><i className='fa fa-check'></i></button>
              <button className='btn btn-default' onClick={signatureCancelClicked(onCancelUpdateSignature)} style={{size: '12px', border: 'none'}}><i className='fa fa-close'></i></button>
            </div>
          </div>
        }{
          status !== SettingsStatuses.EDITING_SIGNATURE ?
            image == undefined ? '' :
            <div>
              <input id='myInput' type='file' style={{ display: 'none' }} />
              <img alt={image} src={`/degapp/api/images/${image}`} width='200' onClick={valueClicked(onValueClick)}></img>
            </div>
            : ''
          }
    </div>
  )
}

const valueClicked = (onValueClick) => () => onValueClick()
const signatureCancelClicked = (onCancelUpdateSignature) => () => onCancelUpdateSignature()
const signatureSubmitClicked = (onSubmitUpdateSignature) => () => onSubmitUpdateSignature()
const editSignature = (onEditingSignature) => (event) => onEditingSignature(event.target.value)
const fileNameChanged = (onFileNameChanged) => (event) => onFileNameChanged(event.target.value, event.target.files[0])

export default signature
