import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import { openModal, closeSelectPaymentModal } from './../../actions/payment'
import { PaymentsStatuses } from './../../reducers/payment'
import SelectPaymentTable from './select-payment-table'

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

const SelectPaymentModal = ({ fetchPayments, isSelectPaymentModalOpen, linkPaymentAndPayment, searchPayments,
  onCloseSelectPaymentModal, payments, invoiceAndUser, paymentId, paymentSearch}) => {
  return (
    <Modal
      isOpen={isSelectPaymentModalOpen}
      style={modalStyles}
      contentLabel="Modal"
      shouldCloseOnOverlayClick={true}
      onRequestClose={onCloseSelectPaymentModal}
    >
      <button className='btn btn-primary' onClick={closeButtonClicked(onCloseSelectPaymentModal)}><i className='fa fa-close' style={{marginRight: '10px'}}></i>Sluiten</button>
      <Invoice invoiceAndUser={invoiceAndUser} />
      <SelectPaymentTable />
    </Modal>
  )
}

const Invoice = ({invoiceAndUser}) =>
  <div>
    <h4>Afrekening:</h4>
    <strong>{invoiceAndUser.invoice.number}</strong>{' '}{invoiceAndUser.invoice.date}, te betalen voor {invoiceAndUser.invoice.dueDate}<br/>
    {invoiceAndUser.user.firstName}{' '}{invoiceAndUser.user.lastName}<br/>
    {invoiceAndUser.invoice.accountNumber}{' '}<b>{invoiceAndUser.invoice.amount}{' '}€{' '}</b>{invoiceAndUser.invoice.comment}{invoiceAndUser.invoice.structuredCommunication}
    {' '}<span className="badge">{invoiceAndUser.invoice.debitType}</span>{' '}<span className="badge">{invoiceAndUser.invoice.status}</span>
  </div>

const closeButtonClicked = (onCloseSelectPaymentModal) => () =>  onCloseSelectPaymentModal()

const mapStateToProps = (state, ownProps) => {
  return{
    isSelectPaymentModalOpen: state.payments.view.isSelectPaymentModalOpen,
    invoiceAndUser: state.payments.view.selectPayment.invoiceAndUser
}}

const mapDispatchToProps = {
  onCloseSelectPaymentModal: closeSelectPaymentModal
}

const SelectPaymentModalContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(SelectPaymentModal)

export default SelectPaymentModalContainer
