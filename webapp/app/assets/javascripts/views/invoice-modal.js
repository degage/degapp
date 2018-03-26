import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import { closeInvoiceModal, editStatus, changeInvoiceStatus, saveStatus, updateInvoiceStatus, cancelUpdateInvoiceStatus } from './../actions/invoice'
import InvoiceCard from './invoice-card'

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

const InvoiceModal = ({invoice, isInvoiceModalOpen, invoiceViewStatus, tempStatus, closeInvoiceModal, onStatusClick, onChangeStatus, onUpdateInvoiceStatus, onCancelUpdateInvoiceStatus}) =>
  <Modal
    isOpen={isInvoiceModalOpen}
    style={modalStyles}
    contentLabel="Modal"
    shouldCloseOnOverlayClick={true}
    onRequestClose={closeInvoiceModal}
  >
    <button className='btn btn-primary' onClick={closeInvoiceModal}><i className='fa fa-close' style={{marginRight: '10px'}}></i>Sluiten</button>
    <InvoiceCard invoice={invoice} invoiceViewStatus={invoiceViewStatus} onStatusClick={onStatusClick} tempStatus={tempStatus}
      onChangeStatus={onChangeStatus} onUpdateInvoiceStatus={onUpdateInvoiceStatus}
      onCancelUpdateInvoiceStatus={onCancelUpdateInvoiceStatus} />
  </Modal>

const mapStateToProps = (state, ownProps) => {
  return{
    invoice: state.invoices.invoice,
    isInvoiceModalOpen: state.invoices.view.isInvoiceModalOpen,
    invoiceViewStatus: state.invoices.view.invoice.status,
    tempStatus: state.invoices.view.invoice.tempStatus
}}

const mapDispatchToProps = {
  closeInvoiceModal,
  onStatusClick: editStatus,
  onChangeStatus: changeInvoiceStatus,
  onUpdateInvoiceStatus: updateInvoiceStatus,
  onCancelUpdateInvoiceStatus: cancelUpdateInvoiceStatus
}

const InvoiceModalContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(InvoiceModal)

export default InvoiceModalContainer
