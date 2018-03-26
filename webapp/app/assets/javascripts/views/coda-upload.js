import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux'
import { uploadCoda, selectCodaFile } from './../actions/coda'
import { CodaStatuses } from './../reducers/coda'
import CodasTable from './codas-table'
import Spinner from './table/table-spinner'
import Navigation from './navigation'

const CodaUpload = ({ route, codaFileName, numberOfPayments, fetched, fetching, fetchingFailed, onUploadCoda, onFileNameChanged }) =>
  <div>
    <Navigation route={route} />
    <div className='panel panel-default col-lg-12' style={{border:'none', padding:'0'}}>
      <div className='panel-body'>
        <div className='form-group col-lg-12'>
          <input type='file' name='coda' className='form-control input-md' onChange={fileNameChanged(onFileNameChanged)} value={codaFileName}/>
          <button className='btn btn-primary' onClick={codaUploadClicked(onUploadCoda)} style={{marginTop: '10px'}}>Coda opladen</button>
          <div style={{margin: '40px'}}><Spinner fetching={fetching} fetchingFailed={fetchingFailed}/></div>
          {fetched ? <div className="alert alert-success" role="alert">Er werden {numberOfPayments} betalingen aangemaakt.</div> : null}
        </div>
      </div>
    </div>
    <CodasTable />
  </div>

const codaUploadClicked = (onUploadCoda) => () =>  onUploadCoda()
const fileNameChanged = (onFileNameChanged) => (event) => onFileNameChanged(event.target.value, event.target.files[0])

const mapStateToProps = (state, ownProps) => {
  return{
    codaFileName: state.codas.fileName,
    numberOfPayments: state.codas.numberOfPayments,
    fetching: state.codas.view.coda.status === CodaStatuses.UPLOADING_CODA,
    fetched: state.codas.view.coda.status === CodaStatuses.UPLOADED_CODA,
    fetchingFailed: state.codas.view.coda.status === CodaStatuses.UPLOADING_CODA_FAILED
}}

const mapDispatchToProps = {
  onUploadCoda: uploadCoda,
  onFileNameChanged: selectCodaFile
}

const CodaUploadContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(CodaUpload)

export default CodaUploadContainer
