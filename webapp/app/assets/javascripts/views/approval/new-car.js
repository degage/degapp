import React, { Component } from 'react'
import { connect } from 'react-redux';
import { createCar, changeCarProperty } from './../../actions/car'
import Spinner from './../table/table-spinner'

const NewCar = ({ car, fetching, fetchingFailed, fetched, successMessage, onCreateCar, onCarPropertyChanged }) =>
  <div>
    <Spinner fetching={fetching} fetchingFailed={fetchingFailed} fetched={fetched} successMessage={successMessage}/>
    <div className='form-group'>
      <label htmlFor='car-name'>Naam</label>
      <input type='text' className='form-control' id='car-name' placeholder='Naam' value={car.name} onChange={carPropChanged(onCarPropertyChanged, 'name')}/>
    </div>
    <div className='form-group'>
      <label htmlFor='car-brand'>Merk</label>
      <input type='text' className='form-control' id='car-brand' placeholder='Renault, Volvo, Volkswagen, ...' value={car.brand} onChange={carPropChanged(onCarPropertyChanged, 'brand')}/>
    </div>
    <div className='form-group'>
      <label htmlFor='car-type'>Type</label>
      <input type='text' className='form-control' id='car-type' placeholder='Scenic, Passat, X90, ...' value={car.type} onChange={carPropChanged(onCarPropertyChanged, 'type')}/>
    </div>
    <div className='form-group'>
      <label htmlFor='car-fuel'>Brandstof</label>
      <select id='car-fuel' value={car.fuel} onChange={carPropChanged(onCarPropertyChanged, 'fuel')}>
        <option value='PETROL'>PETROL</option>
        <option value='DIESEL'>DIESEL</option>
        <option value='BIODIESEL'>BIODIESEL</option>
        <option value='LPG'>LPG</option>
        <option value='CNG'>CNG</option>
        <option value='HYBRID'>HYBRID</option>
        <option value='ELECTRIC'>ELECTRIC</option>
      </select>
    </div>
    <button className='btn btn-primary' onClick={createCarClicked(onCreateCar)} style={{marginTop: '10px'}}>Nieuwe auto aanmaken</button>
  </div>

const createCarClicked = (onCreateCar) => () => onCreateCar()
const carPropChanged = (onCarPropertyChanged, fieldName) => (event) => onCarPropertyChanged(fieldName, event.target.value)

const mapStateToProps = (state, ownProps) => {
  return{
    car: state.cars.car,
    fetching: state.cars.view.car.CREATING_CAR,
    fetchingFailed: state.cars.view.car.CREATING_CAR_FAILED,
    fetched: state.cars.view.car.CREATED_CAR,
    successMessage: 'De nieuwe auto werd succesvol aangemaakt.'
}}

const mapDispatchToProps = {
  onCreateCar: createCar,
  onCarPropertyChanged: changeCarProperty
}

const NewCarContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(NewCar)

export default NewCarContainer
