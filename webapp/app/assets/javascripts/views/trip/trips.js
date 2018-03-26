import React, { Component } from 'react';
import { connect } from 'react-redux';
import { TripsStatuses } from './../../reducers/trip'
import UserModal from './../user'
import TripModal from './trip-modal'
import { showUser } from './../../actions/user'
import { showTrip } from './../../actions/trip'
import Table from './../table/table'

const Trips = ({ trips, car, startDate, fetching, fetchingFailed, columns, status, onShowUser, onShowTrip }) => {
  const rendererTableRowMethods = {
    onShowUser,
    onShowTrip
  }

  return (<div>
    <h3>{car.name}: {startDate}</h3>
    <Table title='' rows={trips} fetching={fetching} fetchingFailed={fetchingFailed} columns={columns}
      rendererTableRow={rendererTableRow}
      rendererTableRowMethods={rendererTableRowMethods}/>
      <UserModal />
      <TripModal />
    </div>)
}

const rendererTableRow = ({row, rendererTableRowMethods}) => <TripItem
  key={`trip-${row.id}`}
  trip={row}
  onUserClick={rendererTableRowMethods.onShowUser}
  onTripClick={rendererTableRowMethods.onShowTrip}
/>

const mapStateToProps = (state, ownProps) => {
  return{
    trips: state.trips.trips,
    car: state.trips.car,
    startDate: state.trips.startDate,
    fetching: state.trips.status == TripsStatuses.FETCHING_TRIPS || state.trips.status == TripsStatuses.SEARCHING_TRIPS,
    fetchingFailed: state.trips.status == TripsStatuses.FETCHING_TRIPS_FAILED || state.trips.status == TripsStatuses.SEARCHING_TRIPS_FAILED,
    status: state.trips.status,
    columns: state.trips.view.trips.table.columns
}}

const TripItem = ({trip, onUserClick, onTripClick}) =>
  <tr key={`TripItem-${trip.id}`}>
    <td><button className='btn btn-link' onClick={tripClicked(onTripClick, trip.id)}>{trip.from}</button></td>
    <td>{trip.until}</td>
    <td><button className='btn btn-link' onClick={userClicked(onUserClick, trip.driverId)}>{trip.driverName}</button></td>
    <td>{trip.status}</td>
    <td>{trip.startKm} - {trip.endKm}</td>
  </tr>

const userClicked = (onUserClick, userId) => () => onUserClick(userId)
const tripClicked = (onTripClick, tripId) => () => onTripClick(tripId)

const mapDispatchToProps = {
  onShowUser: showUser,
  onShowTrip: showTrip
}

const TripsContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(Trips)

export default TripsContainer
