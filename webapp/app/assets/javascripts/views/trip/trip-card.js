import React, { Component } from 'react'
import { connect } from 'react-redux'
import { TripStatuses } from './../../reducers/trip'
import { cancelReservation } from './../../actions/trip'
import Spinner from './../table/table-spinner'

const TripCard = ({ trip, driver, previousDriver, nextDriver, nextDate, fetching, fetchingFailed, tripViewStatus, onCancelClicked }) =>
    <div className='panel panel-default col-lg-12' style={{border:'none', padding:'0'}}>
      <div className='panel-heading'>
        <b>{trip.id}</b>
        <Spinner style={{marginLeft: '20px'}} fetching={fetching} fetchingFailed={fetchingFailed} />
      </div>
      <div className='panel-body'>
        <div className='container'>
          <div className='row'>
            <div className='col-lg-9'>
              <div className='btn-group' role='group' aria-label='...'>
                <button type='button' className='btn btn-default' onClick={cancelClicked(onCancelClicked, trip.id)}>Annuleren / markeren als niet doorgegaan</button>
                <button type='button' className='btn btn-default'>Middle</button>
                <button type='button' className='btn btn-default'>Right</button>
              </div>
              <Reservation trip={trip} />
              <MoreInfo previousDriver={previousDriver} nextDriver={nextDriver} driver={driver} nextDate={nextDate}/>
            </div>
          </div>
        </div>
      </div>
    </div>

const Reservation = ({trip}) =>
  <div>
    <div className='col-lg-4'>
      <div className='form-group'>
        <label htmlFor='driverName'>Bestuurder</label>
        <div id='driverName'>{trip.driverName}</div>
      </div>
      <div className='form-group'>
        <label htmlFor='status'>Status</label>
        <div id='status'>{trip.status}</div>
      </div>
      <div className='form-group'>
        <label htmlFor='period'>Periode</label>
        <div id='period'>{`${trip.from} - ${trip.until}`}</div>
      </div>
      <div className='form-group'>
        <label htmlFor='car'>Auto</label>
        <div id='car'>{trip.car != null ? trip.car.name : ''}</div>
      </div>
    </div>
    <div className='col-lg-4'>
      <div className='form-group'>
        <label htmlFor='km'>Km standen</label>
        <div id='km'>{trip.startKm}{' - '}{trip.endKm}</div>
      </div>
      <div className='form-group'>
        <label htmlFor='createdAt'>Aangemaakt op</label>
        <div id='createdAt'>{trip.createdAt}</div>
      </div>
      <div className='form-group'>
        <label htmlFor='messages'>Boodschap</label>
        <div id='messages'>{trip.messages != null && trip.messages.length > 0 ? trip.messages[0] : ''}</div>
      </div>
    </div>
  </div>

const MoreInfo = ({previousDriver, nextDriver, driver, nextDate}) =>
  <div>
    <h4>Ontlener</h4>
    {driver != null ? <Driver driver={driver} /> : '-'}
    <h4>Vorige bestuurder</h4>
    {previousDriver != null ? <Driver driver={previousDriver} /> : '-'}
    <h4>Volgende bestuurder</h4>
    {nextDriver != null ? <Driver driver={nextDriver} /> : '-'}
    <h4>Begin volgende rit</h4>
    {nextDate}
  </div>

const Driver = ({driver}) =>
<div>
  <div className='col-lg-4'>
    <b>{driver.firstName}{' '}{driver.lastName}</b>
    {' '}{' '}<a href={`mailto:${driver.email}`}>{driver.email}</a>
    {' '}{' '}({driver.cellPhone}{' '}{driver.phone})
  </div>
</div>

const cancelClicked = (onCancelClicked, tripId) => () => onCancelClicked(tripId)

const mapStateToProps = (state, ownProps) => {
  return{
    trip: state.trips.trip,
    driver: state.trips.driver,
    previousDriver: state.trips.previousDriver,
    nextDriver: state.trips.nextDriver,
    nextDate: state.trips.nextDate,
    isTripModalOpen: state.trips.view.isTripModalOpen,
    tripViewStatus: state.trips.view.trip.status,
    fetching: state.trips.view.trip.status == TripStatuses.FETCHING_TRIP,
    fetchingFailed: state.trips.view.trip.status == TripStatuses.FETCHING_TRIP_FAILED
}}

const mapDispatchToProps = {
  onCancelClicked: cancelReservation
}

const TripCardContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(TripCard)

export default TripCardContainer
