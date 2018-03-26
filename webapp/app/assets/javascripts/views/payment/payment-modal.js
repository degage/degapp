import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import PaymentCard from './../payment/payment-card'

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

const PaymentModal = ({paymentAndUser, isPaymentModalOpen, onClosePaymentModal}) =>
  <Modal
    isOpen={isPaymentModalOpen}
    style={modalStyles}
    contentLabel='Modal'
    shouldCloseOnOverlayClick={true}
  >
    <button className='btn btn-primary' onClick={closePaymentModalClicked(onClosePaymentModal)}><i className='fa fa-close' style={{marginRight: '10px'}}></i>Sluiten</button>
    <PaymentCard />
  </Modal>

const closePaymentModalClicked = (onClosePaymentModal) => () => onClosePaymentModal()

export default PaymentModal
