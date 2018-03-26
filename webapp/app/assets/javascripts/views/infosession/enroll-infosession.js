import React, { Component } from 'react';
import { connect } from 'react-redux'
import { changeShareCar, changeCarField, changeAddressField, changeInsuranceField, goNext, selectCarImageFile } from './../../actions/car'
import { CarStatuses, CarValidationStatuses } from './../../reducers/car'
import Table from '../table/table'
import InfosessionItem from './infosession-item'
import ChosenSession from './chosen-session'
// import DatePicker from 'react-datepicker';
// import moment from 'moment';

const EnrollInfosession = ({ validationStarted, car, carImageFileName, shareCar, updateFailed, onChangeShareCar, onChangeCarField, onChangeAddressField, onChangeInsuranceField, onNext, onFileNameChanged }) => {
  return (
    <div className='container'>
      <div className='row'>
        <button className='btn btn-lg btn-default' onClick={shareCarChanged(onChangeShareCar, false)}>
          {shareCar ? null : <i className='fa fa-check'></i>}{' '}Ik wil enkel auto's gebruiken
        </button>
        {' '}{'of'}{' '}
        <button className='btn btn-lg btn-default' onClick={shareCarChanged(onChangeShareCar, true)}>
          {shareCar ? <i className='fa fa-check'></i> : null}{' '}Ik overweeg om mijn auto te delen
        </button>
      </div>
      <div className='row'>
        {updateFailed? <div className='alert alert-danger' role='alert'>Er ging iets fout :-( Gelieve alle velden in te vullen.</div> : null}
        {shareCar
          ? <ShareCar car={car} validationStarted={validationStarted} onChangeCarField={onChangeCarField} onChangeAddressField={onChangeAddressField} onChangeInsuranceField={onChangeInsuranceField} onNext={onNext} onFileNameChanged={onFileNameChanged}/> 
          : null}
        {updateFailed ? <div className='alert alert-danger' role='alert'>Oh snap :(</div> : null}
      </div>
      <div className='row' style={{marginTop: '16px'}}>
        <button className='btn btn-lg btn-primary' onClick={nextClicked(onNext)}>Volgende</button>
      </div>
  </div>)
}

const ShareCar = ({ car, validationStarted = { validationStarted }, carImageFileName, onChangeCarField, onChangeAddressField, onChangeInsuranceField, onNext, onFileNameChanged }) =>
  <div className='panel panel-default' style={{marginTop: '16px'}}>
    <div className='panel-heading'>
      Gelieve hieronder alle info over je auto in te vullen. Velden met een * zijn verplicht.
    </div>
    <div className='panel-body'>
      <div className='form'>
        <div className='row'>
          <div className={`form-group col-md-4 ${validationStarted ? car.brand == '' ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-brand'>Merk *</label>
            <input id='input-brand' type='text' className='form-control' value={car.brand} onChange={carFieldChanged(onChangeCarField, 'brand')} />
          </div>
          <div className={`form-group col-md-4 ${validationStarted ? car.type == '' ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-type'>Type *</label>
            <input id='input-type' type='text' className='form-control' value={car.type} onChange={carFieldChanged(onChangeCarField, 'type')} />
          </div>
          <div className={`form-group col-md-4 ${validationStarted ? car.fuel == '' ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-fuel'>Brandstof *</label>
            <select id='input-fuel' className='form-control' value={car.fuel} onChange={carFieldChanged(onChangeCarField, 'fuel')}>
              <option value='ELECTRIC'>Electrisch</option>
              <option value='DIESEL'>Diesel</option>
              <option value='PETROL'>Benzine</option>
              <option value='HYBRID'>Hybdride</option>
              <option value='LPG'>LPG</option>
              <option value='CNG'>CNG</option>
            </select>
          </div>
        </div>
        <div className='row'>
          <div className={`form-group col-md-4 ${validationStarted ? car.manual == undefined ? 'has-error' : 'has-success' : ''}`}>
            <div style={{display: 'flex', flexDirection: 'row', justifyContent: 'flexStart', height: 50}}>
              <input id='input-manual' type='checkbox' className='form-control' style={{ alignSelf: 'flex-end', width: 24}} checked={car.manual} onChange={carFieldChanged(onChangeCarField, 'manual')} />
              <label htmlFor='input-seats' style={{alignSelf: 'flex-end', margin: 7}} >Manuele versnellingsbak *</label>
            </div>
          </div>
          <div className={`form-group col-md-2 ${validationStarted ? car.seats < 1 ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-seats'>Plaatsen *</label>
            <input id='input-seats' type='number' min='0' className='form-control' value={car.seats} onChange={carFieldChanged(onChangeCarField, 'seats')} />
          </div>
          <div className={`form-group col-md-2 ${validationStarted ? car.doors < 1 ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-doors'>Deuren *</label>
            <input id='input-doors' type='number' min='0' className='form-control' value={car.doors} onChange={carFieldChanged(onChangeCarField, 'doors')} />
          </div>
          <div className={`form-group col-md-4 ${validationStarted ? car.year < 1 ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-year'>Bouwjaar *</label>
            <input id='input-year' type='number' min='0' max={new Date().getFullYear()} className='form-control' value={car.year} onChange={carFieldChanged(onChangeCarField, 'year')} />
          </div>
        </div>
        <div className='row'>
          <div className={`form-group col-md-4 ${validationStarted ? car.fuelEconomy < 1 ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-seats'>Geschat gemiddeld verbruik per 100km *</label>
            <input id='input-seats' type='number' min='0' className='form-control' value={car.fuelEconomy} onChange={carFieldChanged(onChangeCarField, 'fuelEconomy')} />
          </div>
          <div className={`form-group col-md-4 ${validationStarted ? car.estimatedValue < 1 ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-doors'>Eerste schatting van de waarde *</label>
            <input id='input-doors' type='number' min='0' className='form-control' value={car.estimatedValue} onChange={carFieldChanged(onChangeCarField, 'estimatedValue')} />
          </div>
          <div className={`form-group col-md-4 ${validationStarted ? car.ownerAnnualKm < 1 ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-year'>Schatting eigen gereden km per jaar *</label>
            <input id='input-year' type='number' min='0' className='form-control' value={car.ownerAnnualKm} onChange={carFieldChanged(onChangeCarField, 'ownerAnnualKm')} />
          </div>
        </div>
        <div className='row'>
          <div className='form-group col-md-10'>
            <label htmlFor='input-comments'>Commentaar</label>
            <textarea id='input-comments' rows='3' cols='50' type='text' className='form-control' value={car.comments} onChange={carFieldChanged(onChangeCarField, 'comments')} />
          </div>
        </div>
        <div className='row'>
          <div className={`form-group col-md-6 ${validationStarted ? car.insurance.insuranceNameBefore == '' ? 'has-error' : 'has-success' : ''}`}>
          <label htmlFor='input-insurance'>Naam verzekeringsmaatschappij *</label>
            <input id='input-insurance' type='text' className='form-control' value={car.insurance.insuranceNameBefore} onChange={carFieldChanged(onChangeInsuranceField, 'insuranceNameBefore')} />
        </div>
        {/*
        <div className={`form-group col-md-6 ${validationStarted ? car.insurance.name == '' ? 'has-error' : 'has-success' : ''}`}>
          <label htmlFor='input-insurance-date'>Vervaldag verzekering</label>
            <DatePicker
              dateFormat='DD/MM/YYYY'
              selected={car.insurance.expiration}
              minDate={moment()}
              maxDate={moment().add(10, "years")}
              onChange={carFieldChanged(onChangeInsuranceField, 'expiration', 'datepicker')}
            />
        </div>
        */}
      </div>
        <div className='row'>
          <div className='col-md-6'>
            <h4>Standplaats</h4>
          </div>
        </div>
        <div className='row'>
          <div className={`form-group col-md-6 ${validationStarted ? car.location.street == '' ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-street'>Straat *</label>
            <input id='input-street' type='text' className='form-control' value={car.location.street} onChange={carFieldChanged(onChangeAddressField, 'street')} />
          </div>
          <div className={`form-group col-md-6 ${validationStarted ? car.location.num == '' ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-number'>Huisnummer *</label>
            <input id='input-number' type='text' className='form-control' value={car.location.num} onChange={carFieldChanged(onChangeAddressField, 'num')} />
          </div>
        </div>
        <div className='row'>
          <div className={`form-group col-md-6 ${validationStarted ? car.location.zip == '' ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-zip'>Postcode *</label>
            <input id='input-zip' type='text' className='form-control' value={car.location.zip} onChange={carFieldChanged(onChangeAddressField, 'zip')} />
          </div>
          <div className={`form-group col-md-6 ${validationStarted ? car.location.city == '' ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-city'>Stad *</label>
            <input id='input-city' type='text' min='0' className='form-control' value={car.location.city} onChange={carFieldChanged(onChangeAddressField, 'city')} />
          </div>
        </div>
        <div className='row'>
          <div className='col-md-6'>
            <h4>Foto's</h4>
          </div>
        </div>
        <div className='row'>
          <div className={`form-group col-md-6 ${validationStarted ? car.imagesId == '' ? 'has-error' : 'has-success' : ''}`}>
            <label htmlFor='input-photo'>Algemene foto van de auto *</label>
            <img alt={car.imagesId} src={`/degapp/api/images/${car.imagesId}`} width='200' style={{ display: 'block' }}></img>
            <input id='input-photo' type='file' name='image' className='form-control' onChange={fileNameChanged(onFileNameChanged)} value={carImageFileName} style={{marginTop: '5px'}}/>
          </div>
        </div>
      </div>
    </div>
  </div>

const rendererTableRow = ({ row, rendererTableRowMethods }) => 
  <InfosessionItem
    key={`payment-${row.id}`}
    infosession={row}
  />

const fileNameChanged = (onFileNameChanged) => (event) => onFileNameChanged(event.target.value, event.target.files[0])

const mapStateToProps = (state, ownProps) => {
  return{
    car: state.cars.car,
    validationStarted: state.cars.view.car.validationStatus != CarValidationStatuses.UNKNOWN,
    carImageFileName: state.cars.carImageFileName,
    shareCar: state.cars.shareCar,
    infosessions: state.infosessions.infosessions,
    columns: state.infosessions.view.table.columns,
    page: state.infosessions.view.table.page,
    pageSize: state.infosessions.view.table.pageSize,
    fullSize: state.infosessions.view.table.fullSize,
    filter: state.infosessions.view.table.filter,
    showFilter: state.infosessions.view.table.showFilter,
    showPagination: state.infosessions.view.table.showPagination,
    attending: state.infosessions.attending,
    updateFailed: state.cars.view.car.status == CarStatuses.CREATING_USER_ROLE_FAILED
      || state.cars.view.car.status == CarStatuses.CREATING_CAR_FAILED
      || state.cars.view.car.validationStatus == CarValidationStatuses.INVALID
}}

const mapDispatchToProps = {
  onChangeShareCar: changeShareCar,
  onChangeCarField: changeCarField,
  onChangeAddressField: changeAddressField,
  onChangeInsuranceField: changeInsuranceField,
  onNext: goNext,
  onFileNameChanged: selectCarImageFile
}

const shareCarChanged = (onChangeShareCar, shareCar) => () => onChangeShareCar(shareCar)

const carFieldChanged = (onChangeCarField, fieldName, type) => (event) => {
  if (type !== undefined && type === 'datepicker') {
    onChangeCarField(fieldName, event)
  } else if (event.target.type == 'checkbox') {
    onChangeCarField(fieldName, event.target.checked)
  } else {
    onChangeCarField(fieldName, event.target.value)
  }
}

const nextClicked = (onNext) => () => onNext('enrollWithCar')

const EnrollInfosessionContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(EnrollInfosession)

export default EnrollInfosessionContainer
