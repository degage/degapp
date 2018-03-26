import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import { closeTripModal } from './../../actions/trip'
import TripCard from './trip-card'

const modalStyles = {
  content : {
    top                   : '15%',
    left                  : '10%',
    right                 : '10%',
    bottom                : '15%',
    backgroundColor       : 'rgba(240, 240, 240, 1)',
    padding               : '0'
  }
};

const TripModal = ({isTripModalOpen, closeTripModal}) =>
  <Modal
    isOpen={isTripModalOpen}
    style={modalStyles}
    contentLabel="Modal"
    shouldCloseOnOverlayClick={true}
    onRequestClose={closeTripModal}
  >
    <button className='btn btn-primary' onClick={closeTripModal}><i className='fa fa-close' style={{marginRight: '10px'}}></i>Sluiten</button>
    <TripCard  />
  </Modal>


  const mapStateToProps = (state, ownProps) => {
    return{
      isTripModalOpen: state.trips.view.isTripModalOpen
  }}

  const mapDispatchToProps = {
    closeTripModal
  }

  const TripModalContainer = connect(
    mapStateToProps,
    mapDispatchToProps
  )(TripModal)

  export default TripModalContainer
