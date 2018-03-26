import React, { Component } from 'react';
import { connect } from 'react-redux'
import { goNext, selectCarInitialStateFile, deleteCarInitialStateFile, addCarInitialStateFile } from './../../actions/car'

const CarInitialState = ({ carId, carInitialFileName, carInitialFiles, onNext, onFileNameChanged, onDeleteFile, onAddFile }) =>
  carInitialFiles != null && carId != null
  ? <div>
      <div className='row'>
        <div className='col-xs-12'>
          <div className='panel panel-default'>
            <div className='panel-heading'>
              <i className='fa '></i> Laad hier foto's van de oorspronkelijke staat van de auto op (Klik 'Choose file' en 'Doorsturen')
            </div>
            <div className='panel-body'>
              <input type='file' name='file' className='form-control' onChange={fileNameChanged(onFileNameChanged)} value={carInitialFileName || ''} style={{ marginTop: '5px' }} />
              <br />
              <button className='btn btn-sm btn-success' onClick={addFile(onAddFile, carId)}>Doorsturen</button>
            </div>
          </div>
          <div className='panel panel-default'>
            <div className='panel-heading'>
              <i className='fa fa-list-ul'></i> Reeds geüploade bestanden
            </div>
            <div className='panel-body'>
              <ul>
                {carInitialFiles.map(file =>
                  <li key={file.fileId}>
                    <a href={'/degapp/cars/initialstate/file?carId=' + carId + '&fileId=' + file.fileId} target='_blank'>{file.fileName}</a>
                    <button className='btn btn-sm btn-link' onClick={deleteFile(onDeleteFile, carId, file.fileId)}><span className='glyphicon glyphicon-remove'></span></button>
                  </li>
                )}
              </ul>
            </div>
          </div>
        </div>
      </div>
      <div className='row' style={{ marginTop: '16px' }}>
        <div className='col-xs-12'>
          <button className='btn btn-lg btn-primary' onClick={nextClicked(onNext)}>Volgende</button>
        </div>
      </div>
    </div>
  : null

const fileNameChanged = (onFileNameChanged) => (event) => onFileNameChanged(event.target.value, event.target.files[0])

const deleteFile = (onDeleteFile, carId, fileId) => () => onDeleteFile(carId, fileId)

const addFile = (onAddFile, carId) => () => onAddFile(carId)

const mapStateToProps = (state, ownProps) => {
  return {
    carId: state.cars.car.id,
    carInitialFileName: state.cars.carInitialFileName,
    carInitialFiles: state.cars.carInitialFiles
  }
}

const mapDispatchToProps = {
  onNext: goNext,
  onFileNameChanged: selectCarInitialStateFile,
  onDeleteFile: deleteCarInitialStateFile,
  onAddFile: addCarInitialStateFile
}

const nextClicked = (onNext) => () => onNext('setInitialCarState')

const CarInitialStateContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(CarInitialState)

export default CarInitialStateContainer
