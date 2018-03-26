import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import { linkPayment, unlinkPayments, showPaymentById, closePaymentModal } from './../actions/payment'
import { fetchInvoices, linkPaymentAndInvoice, searchInvoices, openModal, closeModal,
  changePageInvoices, changeFilterInvoices, sortTableInvoices, showInvoiceByNumber, changePagesizeInvoices } from './../actions/invoice'
import { InvoicesStatuses } from './../reducers/invoice'
import { debounceEventHandler } from './../util'
import Table from './table/table'
import { showUser } from './../actions/user'
import UserModal from './user'
import PaymentModal from './payment/payment-modal'
import InvoiceModal from './invoice-modal'
import SelectPaymentModal from './payment/select-payment-modal'
import Navigation from './navigation'

const InvoicesTable = ({ orderBy, asc, onChangePagesize, route, invoices, fetching, fetchingFailed, page, pageSize, fullSize, columns, filter,
  onLinkPayment, onUnlinkPayments, onShowPayment, isPaymentModalOpen, onClosePaymentModal, onShowInvoice,
  showFilter, showPagination, onChangeFilter, onSort, onChangePage, onShowUser }) => {
  const rendererTableRowMethods = {
    onLinkPayment,
    onUnlinkPayments,
    onShowPayment,
    onShowUser,
    onShowInvoice,
    onChangePagesize
  }
  return (
    <div>
      <Navigation route={route} />
      <Table title='' rows={invoices} fetching={fetching} fetchingFailed={fetchingFailed} page={page} pageSize={pageSize}
        fullSize={fullSize} filter={filter} showFilter={showFilter} showPagination={showPagination}
        columns={columns} onSort={onSort} onChangePage={onChangePage} orderBy={orderBy} asc={asc}
        onChangeFilter={onChangeFilter}
        rendererTableRow={rendererTableRow}
        rendererTableRowMethods={rendererTableRowMethods} />
      <SelectPaymentModal />
      <UserModal />
      <InvoiceModal />
      <PaymentModal isPaymentModalOpen={isPaymentModalOpen} onClosePaymentModal={onClosePaymentModal}/>
    </div>)
}

const rendererTableRow = ({row, rendererTableRowMethods}) =>
  <InvoiceItem
    key={`invoice-${row.invoice.invoiceId}`}
    onLinkPaymentClick={rendererTableRowMethods.onLinkPayment}
    onUnlinkPaymentsClick={rendererTableRowMethods.onUnlinkPayments}
    onPaymentClick={rendererTableRowMethods.onShowPayment}
    invoiceAndUser={row}
    onInvoiceClick={rendererTableRowMethods.onShowInvoice}
    onUserClick={rendererTableRowMethods.onShowUser}
  />


const InvoiceItem = ({invoiceAndUser, onInvoiceClick, onUserClick, onLinkPaymentClick, onUnlinkPaymentsClick, onPaymentClick}) =>
  <tr key={`InvoiceItem-${invoiceAndUser.invoice.invoiceId}`}>
    <td>
      <button className='btn btn-link btn-xs' onClick={invoiceClicked(onInvoiceClick, invoiceAndUser.invoice.number)}>
        {invoiceAndUser.invoice.number}
      </button>
      <DownloadButton invoice={invoiceAndUser.invoice}/>
    </td>
    <td><button className='btn btn-link' onClick={userClicked(onUserClick, invoiceAndUser.user == null ? null : invoiceAndUser.user.id)}>{invoiceAndUser.user == null ? '-' : invoiceAndUser.user.lastName + ' ' + invoiceAndUser.user.firstName}</button></td>
    <td>
      <div>
        <p className="pull-right">€ <b>{invoiceAndUser.invoice.amount.toFixed(2)}</b></p>
        <p className="pull-right">
          {(invoiceAndUser.invoice.amount - invoiceAndUser.invoice.paidAmount > 0) && invoiceAndUser.invoice.paidAmount > 0 ? `(tekort ${(invoiceAndUser.invoice.amount - invoiceAndUser.invoice.paidAmount).toFixed(2)})` : ''}
          {(invoiceAndUser.invoice.amount - invoiceAndUser.invoice.paidAmount < (-2)) && invoiceAndUser.invoice.paidAmount > 0 ? `(teveel ${(invoiceAndUser.invoice.paidAmount - invoiceAndUser.invoice.amount).toFixed(2)})` : ''}
        </p>
      </div>
    </td>
    <td>{invoiceAndUser.invoice.date}</td>
    <td>
      <p style={{...statusStyle, ...statusColor(invoiceAndUser.invoice.status)}}>{invoiceAndUser.invoice.status}</p>{' '}
      <p style={{...statusStyle, backgroundColor: '#ADBDBD'}}>{invoiceAndUser.invoice.type}</p>{' '}
      <Reminder reminder={invoiceAndUser.reminder}/>
    </td>
    <td><PaymentCell invoiceAndUser={invoiceAndUser} onLinkPaymentClick={onLinkPaymentClick} onUnlinkPaymentsClick={onUnlinkPaymentsClick} onPaymentClick={onPaymentClick}/></td>
    <td>{invoiceAndUser.invoice.comment}{invoiceAndUser.invoice.structuredCommunication}</td>
  </tr>

const PaymentCell = ({invoiceAndUser, onLinkPaymentClick, onUnlinkPaymentsClick, onPaymentClick}) => (
  <span>
    {invoiceAndUser.invoice.paymentIds.map(paymentId =>
      <button key={'inv-nr-'+paymentId} onClick={paymentClicked(onPaymentClick, paymentId)} className='btn btn-link btn-xs'>
        {paymentId}
      </button>)}
    <button className='btn btn-primary btn-link' onClick={linkPaymentClicked(onLinkPaymentClick,  invoiceAndUser)}
      data-toggle="tooltip" data-placement="left" title="Link betaling aan afrekening">
      <i className='fa fa-link'></i>
    </button>
    {invoiceAndUser.invoice.paymentIds.length > 0 ?
      (<button className='btn btn-warning btn-xs btn-link' onClick={unlinkPaymentsClicked(onUnlinkPaymentsClick,  invoiceAndUser.invoice.invoiceId)}
        data-toggle="tooltip" data-placement="left" title="Verwijder link met betaling">
        <i className='fa fa-chain-broken' style={{color: 'grey'}}></i>
      </button>) : null
    }
  </span>
)

const DownloadButton = ({invoice}) => {
  if (invoice.type == 'CAR_MEMBERSHIP') {
    return (<a href={`/degapp/billing/pdf/invoice?id=${invoice.invoiceId}`} className='btn btn-link'>
      <i className='fa fa-file-pdf-o' style={{fontSize:'18px'}}></i>
    </a>)
  }
  if (invoice.carId == 0) {
    return (<a href={`/degapp/billing/user?id=${invoice.billingId}&uid=${invoice.userId}`} className='btn btn-link'>
      <i className='fa fa-file-pdf-o' style={{fontSize:'18px'}}></i>
    </a>)
  } else {
    return (<a href={`/degapp/billing/car?id=${invoice.billingId}&cid=${invoice.carId}`} className='btn btn-link'>
      <i className='fa fa-file-pdf-o' style={{fontSize:'18px'}}></i>
    </a>)
  }
}

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
const statusColor = (status) =>
  status === 'PAID' ? {backgroundColor: '#5cb85c'} : status === 'OVERDUE' ? {backgroundColor: '#d9534f'} : {backgroundColor: '#f0ad4e'}
const descriptionColor = (description) =>
  description === 'FIRST' ? {backgroundColor: 'lightgrey'} : description === 'THIRD' ? {backgroundColor: '#d9534f'} : {backgroundColor: '#f0ad4e'}

const Reminder = ({reminder}) =>
  <span>{reminder != null && reminder.sendDate != null ? <p style={{...statusStyle, ...descriptionColor(reminder.description)}}>{reminder.description}</p> : null}</span>

const createOnChange = onInvoiceSearchChanged => (event) => onInvoiceSearchChanged(event.target.value)

const linkPaymentClicked = (onLinkPaymentClick, invoice) => () =>  onLinkPaymentClick(invoice)

const unlinkPaymentsClicked = (onUnlinkPaymentsClick, invoiceId) => () => onUnlinkPaymentsClick(invoiceId)

const userClicked = (onUserClick, userId) => () => onUserClick(userId)

const paymentClicked = (onPaymentClick, paymentId) => () => onPaymentClick(paymentId)

const invoiceClicked = (onInvoiceClick, invoiceId) => () => onInvoiceClick(invoiceId)

const mapStateToProps = (state, ownProps) => {
  return{
  invoices: state.invoices.invoices,
  fetching: state.invoices.status == InvoicesStatuses.FETCHING_INVOICES,
  fetchingFailed: state.invoices.status == InvoicesStatuses.FETCHING_INVOICES_FAILED,
  status: state.invoices.status,
  page: state.invoices.view.invoices.table.page,
  pageSize: state.invoices.view.invoices.table.pageSize,
  fullSize: state.invoices.view.invoices.table.fullSize,
  filter: state.invoices.view.invoices.table.filter,
  showFilter: state.invoices.view.invoices.table.showFilter,
  showPagination: state.invoices.view.invoices.table.showPagination,
  columns: state.invoices.view.invoices.table.columns,
  isPaymentModalOpen: state.payments.view.isPaymentModalOpen,
  orderBy: state.invoices.view.invoices.table.orderBy,
  asc: state.invoices.view.invoices.table.asc
}}

const mapDispatchToProps = {
  fetchInvoices,
  onLinkPayment: linkPayment,
  onUnlinkPayments: unlinkPayments,
  onPaymentClick: showPaymentById,
  onChangeFilter: changeFilterInvoices,
  onSort: sortTableInvoices,
  onChangePage: changePageInvoices,
  onShowUser: showUser,
  onShowPayment: showPaymentById,
  onClosePaymentModal: closePaymentModal,
  onShowInvoice: showInvoiceByNumber,
  onChangePagesize: changePagesizeInvoices
}

const InvoicesTableContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(InvoicesTable)

export default InvoicesTableContainer
