import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import { changePageInvoices, changeFilterInvoices, sortTableInvoices } from './../../actions/invoice'
import { InvoicesStatuses } from './../../reducers/invoice'
import Table from './../table/table'

const UnpaidInvoices = ({ invoices, fetching, fetchingFailed, showFilter, showPagination, fullSize, columns }) => {
  if (invoices.length < 1) return null
  const rendererTableRowMethods = {
  }
  return (
    <div className='panel panel-danger' style={{margin: '5px 15px'}}>
      <div className='panel-heading'>Openstaande afrekeningen:</div>
      <div className='panel-body' style={{padding: '0px'}}>
        <Table title='' rows={invoices} fetching={fetching} fetchingFailed={fetchingFailed}
          fullSize={fullSize} columns={columns} showFilter={showFilter} showPagination={showPagination}
          rendererTableRow={rendererTableRow}
          rendererTableRowMethods={rendererTableRowMethods} />
      </div>
    </div>)
}

const rendererTableRow = ({row, rendererTableRowMethods}) => <InvoiceItem
  key={`invoice-${row.invoice.invoiceId}`}
  invoice={row.invoice}
/>

const InvoiceItem = ({invoice, onUserClick}) =>
  <tr key={`InvoiceItem-${invoice.invoiceId}`}>
    <td>
      <DownloadButton invoice={invoice}/>
    </td>
    <td>€ {invoice.amount.toFixed(2)}{" "}
      {invoice.paidAmount > 0 ? `(tekort ${(invoice.amount - invoice.paidAmount).toFixed(2)})` : ''}</td>
    <td>{invoice.date}</td>
    <td>{invoice.comment}{invoice.structuredCommunication}</td>
  </tr>

const DownloadButton = ({invoice}) => {
  if (invoice.carId == 0) {
    return (<a href={`/degapp/billing/user?id=${invoice.billingId}&uid=${invoice.userId}`} className='btn btn-link'>
      <i className='fa fa-file-pdf-o' style={{fontSize:'18px'}}></i>{' '}{invoice.number}
    </a>)
  } else {
    return (<a href={`/degapp/billing/car?id=${invoice.billingId}&cid=${invoice.carId}`} className='btn btn-link'>
      <i className='fa fa-file-pdf-o' style={{fontSize:'18px'}}></i>{' '}{invoice.number}
    </a>)
  }
}



// AppContainer.js
const mapStateToProps = (state, ownProps) => {
  return{
  invoices: state.invoices.unpaidInvoices,
  fetching: state.invoices.view.unpaidInvoices.status == InvoicesStatuses.FETCHING_INVOICES,
  fetchingFailed: state.invoices.view.unpaidInvoices.status == InvoicesStatuses.FETCHING_INVOICES_FAILED,
  status: state.invoices.view.unpaidInvoices.status,
  fullSize: state.invoices.view.unpaidInvoices.table.fullSize,
  showFilter: state.invoices.view.unpaidInvoices.table.showFilter,
  showPagination: state.invoices.view.unpaidInvoices.table.showPagination,
  columns: state.invoices.view.unpaidInvoices.table.columns
}}

const mapDispatchToProps = {
}

const UnpaidInvoicesTableContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(UnpaidInvoices)

export default UnpaidInvoicesTableContainer
