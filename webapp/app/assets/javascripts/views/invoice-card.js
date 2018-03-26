import React, { Component } from 'react';
import { InvoiceStatuses } from './../reducers/invoice'
import Spinner from './table/table-spinner'

const InvoiceCard = ({ invoice, fetching, fetchingFailed, tempStatus, onStatusClick, invoiceViewStatus, onChangeStatus, onUpdateInvoiceStatus, onCancelUpdateInvoiceStatus }) =>
    <div className='panel panel-default col-lg-12' style={{border:'none', padding:'0'}}>
      <div className='panel-heading'>
        <b>{invoice.invoice.number}</b>
        <Spinner style={{marginLeft: '20px'}} fetching={invoiceViewStatus == InvoiceStatuses.UPDATING_INVOICE_STATUS} fetchingFailed={invoiceViewStatus == InvoiceStatuses.UPDATING_INVOICE_STATUS_FAILED} />
      </div>
      <div className='panel-body'>
        <div className='container'>
          <div className='row'>
            <div className='col-lg-9'>
              <p>{invoice.user.firstName}{' '}{invoice.user.lastName}</p>
              <p>{invoice.invoice.amount} €</p>
              <Status invoice={invoice} tempStatus={tempStatus} onStatusClick={onStatusClick}
                invoiceViewStatus={invoiceViewStatus} onChangeStatus={onChangeStatus}
                onUpdateInvoiceStatus={onUpdateInvoiceStatus} onCancelUpdateInvoiceStatus={onCancelUpdateInvoiceStatus}/>
              <p>{invoice.invoice.structuredCommunication}</p>
              <p>Factuurdatum: {invoice.invoice.date}</p>
              <p>Betalingsdatum: {invoice.invoice.paymentDate}</p>
              <p>Te betalen voor: {invoice.invoice.dueDate}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

const Status = ({invoice, tempStatus, onStatusClick, invoiceViewStatus, onChangeStatus, onUpdateInvoiceStatus, onCancelUpdateInvoiceStatus}) =>
  {return invoiceViewStatus === InvoiceStatuses.READ_ONLY ?
    <p onClick={statusClicked(onStatusClick)} style={{cursor: 'pointer'}}>{invoice.invoice.status}</p> :
    <div>
      <select value={tempStatus} onChange={statusChanged(onChangeStatus)}>
        <option value='PAID'>PAID</option>
        <option value='OPEN'>OPEN</option>
        <option value='OVERDUE'>OVERDUE</option>
      </select>
      <div className='btn-group' role='group' style={{marginLeft: '10px', size: '12px'}}>
        <button className='btn btn-default' onClick={statusSaveClicked(onUpdateInvoiceStatus)} style={{size: '12px', border: 'none'}}><i className='fa fa-check'></i></button>
        <button className='btn btn-default' onClick={statusCancelClicked(onCancelUpdateInvoiceStatus)} style={{size: '12px', border: 'none'}}><i className='fa fa-close'></i></button>
      </div>
    </div>
  }

const statusSaveClicked = (onStatusSaveClick) => () => onStatusSaveClick()
const statusCancelClicked = (onCancelUpdateInvoiceStatus) => () => onCancelUpdateInvoiceStatus()
const statusClicked = (onStatusClick) => () => onStatusClick()
const statusChanged = (onChangeStatus) => (event) => onChangeStatus(event.target.value)

export default InvoiceCard
