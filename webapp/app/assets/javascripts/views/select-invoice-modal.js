import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import { openModal, closeSelectInvoiceModal } from './../actions/invoice'
import { InvoicesStatuses } from './../reducers/invoice'
import SelectInvoiceTable from './select-invoice-table'

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

const SelectInvoiceModal = ({ fetchInvoices, isSelectInvoiceModalOpen, linkPaymentAndInvoice, searchInvoices, closeSelectInvoiceModal,
  invoices, payment, paymentId, invoiceSearch}) => {
  return (
    <Modal
      isOpen={isSelectInvoiceModalOpen}
      style={modalStyles}
      contentLabel="Modal"
      shouldCloseOnOverlayClick={true}
      onRequestClose={closeSelectInvoiceModal}
    >
      <button className='btn btn-primary' onClick={closeSelectInvoiceModal}><i className='fa fa-close' style={{marginRight: '10px'}}></i>Sluiten</button>
      <Payment payment={payment} />
      <SelectInvoiceTable />
    </Modal>
  )
}

const InvoiceItem = ({invoiceAndUser, paymentId, onLinkClick}) =>
  <tr key={`InvoiceItem-${invoiceAndUser.invoice.invoiceId}`}>
    <td><button className='btn btn-primary btn-xs' onClick={linkClicked(onLinkClick, paymentId, invoiceAndUser.invoice.invoiceId)}>Link</button></td>
    <td>{invoiceAndUser.invoice.number}</td>
    <td>{invoiceAndUser.user.lastName}{' '}{invoiceAndUser.user.firstName}</td>
    <td>{invoiceAndUser.invoice.amount}</td>
    <td>{invoiceAndUser.invoice.date}</td>
    <td>{invoiceAndUser.invoice.status}</td>
    <td>{invoiceAndUser.invoice.comment}{invoiceAndUser.invoice.structuredCommunication}</td>
  </tr>

const Payment = ({payment}) =>
  <div>
    Betaling: Nummer{' '}{payment.number}{' '}{payment.name}{payment.address != null ? `, ${payment.address}` : ''}<br/>
    overgeschreven op{' '}{payment.date}<br/>
    {payment.accountNumber}{' '}<b>{payment.amount}{' '}€{' '}</b>{payment.comment}{payment.structuredCommunication}
    {' '}<span className="badge">{payment.debitType}</span>{' '}<span className="badge">{payment.status}</span>
  </div>

const mapStateToProps = (state, ownProps) => {
  return{
    isSelectInvoiceModalOpen: state.invoices.view.isSelectInvoiceModalOpen,
    payment: state.payments.payment
}}

const mapDispatchToProps = {
  closeSelectInvoiceModal
}

const SelectInvoiceModalContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(SelectInvoiceModal)

export default SelectInvoiceModalContainer
