import React, { Component } from 'react';
import { connect } from 'react-redux';
import { linkInvoice, unlinkInvoices, showInvoiceByNumber } from './../actions/invoice'
import { closePaymentModal, showPaymentById, changePaymentStatus, updatePaymentStatus, cancelUpdatePaymentStatus, hangePaymentStatus, editStatus, changePagesizePayments, fetchPayments, sortTable, changePage, changeFilter, selectUserForPayment, showUserPickerModal, hideUserPickerModal, changeIncludeInBalance } from './../actions/payment'
import { showUser, showPayment } from './../actions/user'
import { PaymentsStatuses, PaymentStatuses } from './../reducers/payment'
import UserModal from './user'
import UserPickerModal from './user/user-picker-modal'
import InvoiceModal from './invoice-modal'
import SelectInvoiceModal from './select-invoice-modal'
import Table from './table/table'
import Navigation from './navigation'
import PaymentModal from './payment/payment-modal'

const Payments = ({ asc, orderBy, onClosePaymentModal, isPaymentModalOpen, onShowPayment, onPaymentIdClick, currentPaymentId, onCancelUpdatePaymentStatus, onStatusSaveClick, onChangeStatus, paymentViewStatus, onStatusClick, onChangePagesize, route, payments, isUserPickerModalOpen, isSaveUserEnabled, fetching, fetchingFailed, page, pageSize, fullSize,
  columns, filter, showFilter, paymentId, status, onChangeFilter, onLinkInvoice, onUnlinkInvoices, onShowUser,
  onShowInvoice, onSort, onChangePage, showPagination,
  onPaymentClick, onUserSelected, onUserPickerClick, onCloseUserPickerModal, onChangeIncludeInBalance }) => {
  const rendererTableRowMethods = {
    onLinkInvoice,
    onUnlinkInvoices,
    onShowUser,
    onShowInvoice,
    onUserPickerClick,
    onChangeIncludeInBalance,
    onChangePagesize,
    orderBy,
    onStatusClick,
    onChangeStatus,
    paymentViewStatus,
    onStatusSaveClick,
    onCancelUpdatePaymentStatus,
    currentPaymentId,
    onShowPayment
  }

  return (
    <div>
      <Navigation route={route} />
      <Table title='' rows={payments} fetching={fetching} fetchingFailed={fetchingFailed} page={page} pageSize={pageSize}
        fullSize={fullSize} filter={filter} showPagination={showPagination} showFilter={showFilter} columns={columns}
        onSort={onSort} onChangePage={onChangePage} orderBy={orderBy} asc={asc}
        onChangeFilter={onChangeFilter}
        rendererTableRow={rendererTableRow}
        rendererTableRowMethods={rendererTableRowMethods}/>
      <SelectInvoiceModal paymentId={paymentId}/>
      <InvoiceModal />
      <UserModal />
      <PaymentModal isPaymentModalOpen={isPaymentModalOpen} onClosePaymentModal={onClosePaymentModal}/>
      <UserPickerModal isSaveUserEnabled={isSaveUserEnabled} isUserPickerModalOpen={isUserPickerModalOpen} onCloseUserPickerModal={onCloseUserPickerModal} onUserSelected={onUserSelected}/>
    </div>)
}

const rendererTableRow = ({ row, rendererTableRowMethods}) => <PaymentItem
    key={`payment-${row.payment.paymentId}`}
    paymentAndUser={row}
    onLinkInvoiceClick={rendererTableRowMethods.onLinkInvoice}
    onUnlinkInvoicesClick={rendererTableRowMethods.onUnlinkInvoices}
    onUserClick={rendererTableRowMethods.onShowUser}
    onInvoiceClick={rendererTableRowMethods.onShowInvoice}
    selectUserForPayment={rendererTableRowMethods.selectUserForPayment}
    onUserPickerClick={rendererTableRowMethods.onUserPickerClick}
    onChangeIncludeInBalance={rendererTableRowMethods.onChangeIncludeInBalance}
    onStatusClick={rendererTableRowMethods.onStatusClick}
    paymentViewStatus={rendererTableRowMethods.paymentViewStatus}
    onStatusSaveClick={rendererTableRowMethods.onStatusSaveClick}
    onCancelUpdatePaymentStatus={rendererTableRowMethods.onCancelUpdatePaymentStatus}
    onChangeStatus={rendererTableRowMethods.onChangeStatus}
    currentPaymentId={rendererTableRowMethods.currentPaymentId}
    onPaymentIdClick={rendererTableRowMethods.onShowPayment}
  />

const statusStyle = {
  display: 'inline',
  padding: '.2em .6em .3em',
  fontSize: '75%',
  fontWeight: '700',
  //lineHeight: '1',
  color: '#fff',
  textAlign: 'center',
  whiteSpace: 'nowrap',
  verticalAlign: 'baseline',
  borderRadius: '.25em',
}

const amountStyle = (type) =>
  type === 'CREDIT' ? {backgroundColor: '#5cb85c'} : {backgroundColor: '#f0ad4e'}
const statusColor = (status) =>
  status === 'OK' ? {backgroundColor: '#5cb85c'} : status === 'UNASSIGNED' ? {backgroundColor: '#d9534f'} : {backgroundColor: '#f0ad4e'}

const PaymentItem = ({ onPaymentIdClick, currentPaymentId, onCancelUpdatePaymentStatus, onStatusSaveClick, onChangeStatus, paymentViewStatus, onStatusClick, tempStatus, paymentAndUser, onLinkInvoiceClick, onUnlinkInvoicesClick, onUserClick, onUserPickerClick, onInvoiceClick, onChangeIncludeInBalance}) =>
  <tr>
    <td>
      <button key={'payment-nr-'+paymentAndUser.payment.paymentId} onClick={paymentIdClicked(onPaymentIdClick, paymentAndUser.payment.paymentId)} className='btn btn-link btn-xs'>
        {paymentAndUser.payment.paymentId}
      </button>
    </td>
    <td>{paymentAndUser.user == null
      ? '-'
      : <button className='btn btn-link' onClick={userClicked(onUserClick, paymentAndUser.user == null ? null : paymentAndUser.user.id)}>{paymentAndUser.user.lastName + ' ' + paymentAndUser.user.firstName}</button>}
      <button className='btn btn-link' onClick={userPickerClicked(onUserPickerClick, paymentAndUser.payment.paymentId, paymentAndUser.user)}><i className='fa fa-edit'></i></button>
    </td>
    <td>{paymentAndUser.payment.name}<br/>{paymentAndUser.payment.address}</td>
    <td><p className="pull-left" style={{...statusStyle, ...amountStyle(paymentAndUser.payment.debitType)}}> {paymentAndUser.payment.debitType === 'CREDIT' ? 'C' : 'D'}</p><p className="pull-right">€ {paymentAndUser.payment.amount.toFixed(2)}</p></td>
    <td>{paymentAndUser.payment.date}</td>
    <td>
      <Status payment={paymentAndUser.payment} tempStatus={tempStatus} onStatusClick={onStatusClick}
        paymentViewStatus={paymentViewStatus} onChangeStatus={onChangeStatus}
        onStatusSaveClick={onStatusSaveClick} onCancelUpdatePaymentStatus={onCancelUpdatePaymentStatus} currentPaymentId={currentPaymentId}/>
      <p>
        <IncludeInBalanceCheck payment={paymentAndUser.payment} onChangeIncludeInBalance={onChangeIncludeInBalance} />
      </p>
    </td>
    <td><InvoiceCell payment={paymentAndUser.payment} onLinkInvoiceClick={onLinkInvoiceClick} onUnlinkInvoicesClick={onUnlinkInvoicesClick} onInvoiceClick={onInvoiceClick}/></td>
    <td>{paymentAndUser.payment.comment}{paymentAndUser.payment.structuredCommunication}</td>
  </tr>

const InvoiceCell = ({payment, onLinkInvoiceClick, onUnlinkInvoicesClick, onInvoiceClick}) => (
  <span>
    {payment.invoiceNumbers.map(invoiceNumber =>
      <button key={'inv-nr-'+invoiceNumber} onClick={invoiceClicked(onInvoiceClick, invoiceNumber)} className='btn btn-link btn-xs'>
        {invoiceNumber}
      </button>)}
    <button className='btn btn-primary btn-link' onClick={linkInvoiceClicked(onLinkInvoiceClick,  payment.paymentId, payment.name.trim().toLowerCase())}
      data-toggle="tooltip" data-placement="left" title="Link afrekening aan betaling">
      <i className='fa fa-link'></i>
    </button>
    {payment.invoiceNumbers.length > 0 ?
      (<button className='btn btn-warning btn-xs btn-link' onClick={unlinkInvoicesClicked(onUnlinkInvoicesClick,  payment.paymentId)}
        data-toggle="tooltip" data-placement="left" title="Verwijder link met afrekening">
        <i className='fa fa-chain-broken' style={{color: 'grey'}}></i>
      </button>) : null
    }
  </span>
)

const Status = ({currentPaymentId, payment, tempStatus, onStatusClick, paymentViewStatus, onChangeStatus, onStatusSaveClick, onCancelUpdatePaymentStatus}) =>
  {return paymentViewStatus == PaymentStatuses.EDITING_STATUS && payment.paymentId == currentPaymentId ?
    <div>
      <select value={tempStatus} onChange={statusChanged(onChangeStatus)}>
        <option value='0'>Maak een keuze</option>
        <option value='OK'>OK</option>
        <option value='UNASSIGNED'>UNASSIGNED</option>
      </select>
      <div className='btn-group' role='group' style={{marginLeft: '10px', size: '12px'}}>
        <button className='btn btn-default' onClick={statusSaveClicked(onStatusSaveClick)} style={{size: '12px', border: 'none'}}><i className='fa fa-check'></i></button>
        <button className='btn btn-default' onClick={statusCancelClicked(onCancelUpdatePaymentStatus)} style={{size: '12px', border: 'none'}}><i className='fa fa-close'></i></button>
      </div>
    </div>:
    <p onClick={statusClicked(onStatusClick, payment.paymentId)} style={{...statusStyle, ...statusColor(payment.status), ...{cursor: 'pointer'}}}>{payment.status}</p>
  }

const IncludeInBalanceCheck = ({payment, onChangeIncludeInBalance}) =>
  <label style={{fontWeight: 'normal'}}>
    in balans?{' '}
    <input type='checkbox' checked={payment.includeInBalance} value='in balans?'
      onChange={includeInBalanceChanged(onChangeIncludeInBalance, payment.paymentId, !payment.includeInBalance)} />
  </label>

const linkInvoiceClicked = (onLinkInvoiceClick, paymentId, name) => () =>  onLinkInvoiceClick(paymentId, name)

const unlinkInvoicesClicked = (onUnlinkInvoicesClick, paymentId) => () => onUnlinkInvoicesClick(paymentId)

const userClicked = (onUserClick, userId) => () => onUserClick(userId)

const paymentClicked = (onPaymentClick, paymentId) => () => onPaymentClick(paymentId)

const userPickerClicked = (onUserPickerClick, paymentId, user) => () => onUserPickerClick(paymentId, user)

const invoiceClicked = (onInvoiceClick, invoiceId) => () => onInvoiceClick(invoiceId)

const includeInBalanceChanged = (onChangeIncludeInBalance, paymentId, includeInBalance) => () => onChangeIncludeInBalance(paymentId, includeInBalance)

const statusClicked = (onStatusClick, id) => () => onStatusClick(id)

const statusChanged = (onChangeStatus) => (event) => onChangeStatus(event.target.value)

const statusSaveClicked = (onStatusSaveClick) => () => onStatusSaveClick()

const statusCancelClicked = (onCancelUpdatePaymentStatus) => () => onCancelUpdatePaymentStatus()

const paymentIdClicked = (onPaymentIdClick, paymentId) => () => onPaymentIdClick(paymentId)


const mapStateToProps = (state, ownProps) => {
  return{
    payments: state.payments.payments,
    fetching: state.payments.status == PaymentsStatuses.FETCHING_PAYMENTS || state.payments.view.payment.status == PaymentStatuses.UPDATING_PAYMENT,
    fetchingFailed: state.payments.status == PaymentsStatuses.FETCHING_PAYMENTS_FAILED || state.payments.view.payment.status == PaymentStatuses.UPDATING_PAYMENT_FAILED,
    status: state.payments.status,
    paymentId: state.payments.paymentId,
    page: state.payments.view.payments.table.page,
    pageSize: state.payments.view.payments.table.pageSize,
    fullSize: state.payments.view.payments.table.fullSize,
    filter: state.payments.view.payments.table.filter,
    showFilter: state.payments.view.payments.table.showFilter,
    columns: state.payments.view.payments.table.columns,
    showPagination: state.payments.view.payments.table.showPagination,
    isUserPickerModalOpen: state.payments.view.userPicker.isUserPickerModalOpen,
    isSaveUserEnabled: state.users.view.hasSuggestionChanged,
    paymentViewStatus: state.payments.view.payment.status,
    currentPaymentId: state.payments.view.payment.paymentId,
    isPaymentModalOpen: state.payments.view.isPaymentModalOpen,
    orderBy: state.payments.view.payments.table.orderBy,
    asc: state.payments.view.payments.table.asc
}}

const mapDispatchToProps = {
  fetchPayments,
  onLinkInvoice: linkInvoice,
  onUnlinkInvoices: unlinkInvoices,
  onSort: sortTable,
  onChangeFilter: changeFilter,
  onChangePage: changePage,
  onShowUser: showUser,
  onShowInvoice: showInvoiceByNumber,
  onPaymentClick: showPayment,
  onUserSelected: selectUserForPayment,
  onUserPickerClick: showUserPickerModal,
  onCloseUserPickerModal: hideUserPickerModal,
  onChangeIncludeInBalance: changeIncludeInBalance,
  onChangePagesize: changePagesizePayments,
  onStatusClick: editStatus,
  onChangeStatus: changePaymentStatus,
  onStatusSaveClick: updatePaymentStatus ,
  onCancelUpdatePaymentStatus: cancelUpdatePaymentStatus,
  onShowPayment: showPaymentById,
  onClosePaymentModal: closePaymentModal
}

const PaymentsContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(Payments)

export default PaymentsContainer
