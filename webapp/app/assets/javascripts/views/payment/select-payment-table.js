import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import { fetchPaymentsSelectPayment, linkInvoiceAndPayment, openModal, closeModal,
  sortTableSelectPayment, changePageSelectPayment, changeFilterSelectPayment } from './../../actions/payment'
import { PaymentsStatuses } from './../../reducers/payment'
import Table from './../table/table'

const PaymentsTable = ({ payments, fetching, fetchingFailed, page, pageSize, fullSize, columns, filter, showFilter, showPagination,
  onChangeFilter, onSort, onChangePage, linkInvoiceAndPayment }) => {
  const rendererTableRowMethods = {
    onLinkClick: linkInvoiceAndPayment
  }
  return (
    <div>
      <Table title='' rows={payments} fetching={fetching} fetchingFailed={fetchingFailed} page={page} pageSize={pageSize}
        fullSize={fullSize} filter={filter} showFilter={showFilter} showPagination={showPagination}
        columns={columns} onSort={onSort} onChangePage={onChangePage}
        onChangeFilter={onChangeFilter}
        rendererTableRow={rendererTableRow}
        rendererTableRowMethods={rendererTableRowMethods} />
    </div>)
}

const rendererTableRow = ({row, rendererTableRowMethods}) => <PaymentItem
  key={`payment-${row.payment.paymentId}`}
  paymentAndUser={row}
  onLinkClick={rendererTableRowMethods.onLinkClick}
/>

const PaymentItem = ({paymentAndUser, onLinkClick}) =>
  <tr key={`PaymentItem-${paymentAndUser.payment.paymentId}`}>
    <td><button className='btn btn-primary btn-xs' onClick={linkClicked(onLinkClick, paymentAndUser.payment.paymentId)}>Link</button></td>
    <td>{paymentAndUser.payment.number}</td>
    <td>{paymentAndUser.user != null ? `${paymentAndUser.user.lastName} ${paymentAndUser.user.firstName}` : null}</td>
    <td>{paymentAndUser.payment.name}<br/>{paymentAndUser.payment.address}</td>
    <td>{paymentAndUser.payment.amount}</td>
    <td>{paymentAndUser.payment.date}</td>
    <td>{paymentAndUser.payment.status}</td>
    <td>{paymentAndUser.payment.comment}{paymentAndUser.payment.structuredCommunication}</td>
  </tr>

const linkClicked = (onLinkClick, paymentId) => () => onLinkClick(paymentId)

const mapStateToProps = (state, ownProps) => {
  return {
    payments: state.payments.payments,
    fetching: state.payments.status == PaymentsStatuses.FETCHING_INVOICES,
    fetchingFailed: state.payments.status == PaymentsStatuses.FETCHING_INVOICES_FAILED,
    status: state.payments.status,
    paymentId: state.payments.paymentId,
    page: state.payments.view.selectPayment.table.page,
    pageSize: state.payments.view.selectPayment.table.pageSize,
    fullSize: state.payments.view.selectPayment.table.fullSize,
    filter: state.payments.view.selectPayment.table.filter,
    showFilter: state.payments.view.selectPayment.table.showFilter,
    columns: state.payments.view.selectPayment.table.columns
}}

const mapDispatchToProps = {
  linkInvoiceAndPayment,
  onSort: sortTableSelectPayment,
  onChangeFilter: changeFilterSelectPayment,
  onChangePage: changePageSelectPayment
}

const PaymentsContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(PaymentsTable)

export default PaymentsContainer
