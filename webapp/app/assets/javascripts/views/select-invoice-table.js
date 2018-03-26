import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import { fetchInvoicesSelectInvoice, linkPaymentAndInvoice, searchInvoices, openModal, closeModal,
  sortTableSelectInvoice, changePageSelectInvoice, changeFilterSelectInvoice } from './../actions/invoice'
import { InvoicesStatuses } from './../reducers/invoice'
import Table from './table/table'

const InvoicesTable = ({ invoices, fetching, fetchingFailed, page, pageSize, fullSize, columns, filter, showFilter, showPagination,
  onChangeFilter, onSort, onChangePage, linkPaymentAndInvoice }) => {
  const rendererTableRowMethods = {
    onLinkClick: linkPaymentAndInvoice
  }
  return (
    <div>
      <Table title='' rows={invoices} fetching={fetching} fetchingFailed={fetchingFailed} page={page} pageSize={pageSize}
        fullSize={fullSize} filter={filter} showFilter={showFilter} showPagination={showPagination}
        columns={columns} onSort={onSort} onChangePage={onChangePage}
        onChangeFilter={onChangeFilter}
        rendererTableRow={rendererTableRow}
        rendererTableRowMethods={rendererTableRowMethods} />
    </div>)
}

const rendererTableRow = ({row, rendererTableRowMethods}) => <InvoiceItem
  key={`invoice-${row.invoice.invoiceId}`}
  invoiceAndUser={row}
  onLinkClick={rendererTableRowMethods.onLinkClick}
/>

const InvoiceItem = ({invoiceAndUser, onLinkClick}) =>
  <tr key={`InvoiceItem-${invoiceAndUser.invoice.invoiceId}`}>
    <td><button className='btn btn-primary btn-xs' onClick={linkClicked(onLinkClick, invoiceAndUser.invoice.invoiceId)}>Link</button></td>
    <td>{invoiceAndUser.invoice.number}</td>
    <td>{invoiceAndUser.user.lastName}{' '}{invoiceAndUser.user.firstName}</td>
    <td>{invoiceAndUser.invoice.amount}</td>
    <td>{invoiceAndUser.invoice.date}</td>
    <td>{invoiceAndUser.invoice.status}</td>
    <td>{invoiceAndUser.invoice.comment}{invoiceAndUser.invoice.structuredCommunication}</td>
  </tr>

const linkClicked = (onLinkClick, invoiceId) => () => onLinkClick(invoiceId)

const mapStateToProps = (state, ownProps) => {
  return {
    invoices: state.invoices.invoices,
    fetching: state.invoices.status == InvoicesStatuses.FETCHING_INVOICES || state.invoices.status == InvoicesStatuses.SEARCHING_INVOICES,
    fetchingFailed: state.invoices.status == InvoicesStatuses.FETCHING_INVOICES_FAILED || state.invoices.status == InvoicesStatuses.SEARCHING_INVOICES_FAILED,
    status: state.invoices.status,
    paymentId: state.invoices.paymentId,
    page: state.invoices.view.selectInvoice.table.page,
    pageSize: state.invoices.view.selectInvoice.table.pageSize,
    fullSize: state.invoices.view.selectInvoice.table.fullSize,
    filter: state.invoices.view.selectInvoice.table.filter,
    showFilter: state.invoices.view.selectInvoice.table.showFilter,
    columns: state.invoices.view.selectInvoice.table.columns
}}

const mapDispatchToProps = {
  linkPaymentAndInvoice,
  onSort: sortTableSelectInvoice,
  onChangeFilter: changeFilterSelectInvoice,
  onChangePage: changePageSelectInvoice
}

const InvoicesContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(InvoicesTable)

export default InvoicesContainer
