import React, { Component } from 'react';
import { connect } from 'react-redux';
import { showInvoiceByNumber } from './../actions/invoice'
import { fetchReminders, sortTable, changePage, changeFilter, createReminders, changePagesizeReminders, mailReminder } from './../actions/reminder'
import { showUser } from './../actions/user'
import { RemindersStatuses } from './../reducers/reminder'
import UserModal from './user'
import InvoiceModal from './invoice-modal'
import Table from './table/table'
import Navigation from './navigation'

const Reminders = ({ orderBy, asc, onChangePagesize, route, reminders, creatingReminders, creatingRemindersFailed, fetching, fetchingFailed, page,
  pageSize, fullSize, columns, filter, showFilter, showPagination,
  status, onChangeFilter, onShowUser, onShowInvoice, onSort, onChangePage, onCreateRemindersClick, onMailClick }) => {
  const rendererTableRowMethods = {
    onShowUser,
    onShowInvoice,
    onChangePagesize,
    onMailClick
  }

  return (
    <div>
      <Navigation route={route} />
      <button className='btn btn-primary' onClick={createReminderClicked(onCreateRemindersClick)}>
        Rappels aanmaken{' '}{creatingReminders ? <i className='fa fa-spinner fa-spin' style={{fontSize:'18px'}}></i> : null}
      </button>
      <Table title='' rows={reminders} fetching={fetching} fetchingFailed={fetchingFailed} page={page} pageSize={pageSize}
        fullSize={fullSize} filter={filter} showFilter={showFilter} showPagination={showPagination}
        columns={columns} onSort={onSort} onChangePage={onChangePage} orderBy={orderBy} asc={asc}
        onChangeFilter={onChangeFilter}
        rendererTableRow={rendererTableRow}
        rendererTableRowMethods={rendererTableRowMethods}/>
      <InvoiceModal />
      <UserModal />
    </div>)
}

const rendererTableRow = ({row, rendererTableRowMethods}) => (
  row.reminder.status == 'PAID' && row.reminder.description != 'FIRST' ?
    null
  :
    <ReminderItem
    key={`reminder-${row.reminder.id}`}
    reminderAndUser={row}
    onLinkInvoiceClick={rendererTableRowMethods.onLinkInvoice}
    onUnlinkInvoicesClick={rendererTableRowMethods.onUnlinkInvoices}
    onUserClick={rendererTableRowMethods.onShowUser}
    onInvoiceClick={rendererTableRowMethods.onShowInvoice}
    onMailClick={rendererTableRowMethods.onMailClick}
  />
)
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
const descriptionColor = (description) =>
  description === 'FIRST' ? {backgroundColor: 'lightgrey'} : description === 'THIRD' ? {backgroundColor: '#d9534f'} : {backgroundColor: '#f0ad4e'}
const statusColor = (status) =>
  status === 'PAID' ? {backgroundColor: '#5cb85c'} : status === 'OPEN' ? {backgroundColor: '#d9534f'} : {backgroundColor: '#f0ad4e'}


const ReminderItem = ({reminderAndUser, onUserClick, onInvoiceClick, onMailClick}) =>
  <tr>
    <td>{reminderAndUser.reminder.id}</td>
    <td>{reminderAndUser.user == null ? '-' : <button className='btn btn-link' onClick={userClicked(onUserClick, reminderAndUser.user == null ? null : reminderAndUser.user.id)}>{reminderAndUser.user.lastName + ' ' + reminderAndUser.user.firstName}</button>}</td>
    <td><p style={{...statusStyle, ...descriptionColor(reminderAndUser.reminder.description)}}>{reminderAndUser.reminder.description}</p></td>
    <td>{reminderAndUser.invoice.date}</td>
    <td><p style={{...statusStyle, ...statusColor(reminderAndUser.reminder.status)}}>{reminderAndUser.reminder.status}</p></td>
    <td><InvoiceCell onInvoiceClick={onInvoiceClick} reminderAndUser={reminderAndUser}/></td>
    <td>{reminderAndUser.invoice.amount}</td>
    <td>{reminderAndUser.reminder.sendDate}</td>
    {reminderAndUser.reminder.sendDate == null && reminderAndUser.reminder.status !== 'PAID' ?
      <td><button className='btn btn-warning btn-link' onClick={mailClicked(onMailClick, reminderAndUser.reminder.id)}>Verstuur herinneringsmail naar gebruiker</button></td>
    : null }
  </tr>

const InvoiceCell = ({reminderAndUser, onInvoiceClick}) => (
  <span>{reminderAndUser.invoice != null ? reminderAndUser.invoice.number : null}</span>
)

const userClicked = (onUserClick, userId) => () => onUserClick(userId)

const invoiceClicked = (onInvoiceClick, invoiceId) => () => onInvoiceClick(invoiceId)

const createReminderClicked = (onCreateRemindersClick) => () => onCreateRemindersClick()

const mailClicked = (onMailClick, reminderId) => () => onMailClick(reminderId)

const mapStateToProps = (state, ownProps) => {
  return{
    route: ownProps.route,
    reminders: state.reminders.reminders,
    fetching: state.reminders.status == RemindersStatuses.FETCHING_REMINDERS || state.reminders.status == RemindersStatuses.MAILING_REMINDER,
    fetchingFailed: state.reminders.status == RemindersStatuses.FETCHING_REMINDERS_FAILED || state.reminders.status == RemindersStatuses.MAILING_REMINDER_FAILED,
    creatingReminders: state.reminders.view.createReminders.tatus == RemindersStatuses.CREATING_REMINDERS,
    creatingRemindersFailed: state.reminders.view.createReminders.tatus == RemindersStatuses.CREATING_REMINDERS_FAILED,
    status: state.reminders.status,
    page: state.reminders.view.reminders.table.page,
    pageSize: state.reminders.view.reminders.table.pageSize,
    fullSize: state.reminders.view.reminders.table.fullSize,
    filter: state.reminders.view.reminders.table.filter,
    showFilter: state.reminders.view.reminders.table.showFilter,
    showPagination: state.reminders.view.reminders.table.showPagination,
    columns: state.reminders.view.reminders.table.columns,
    orderBy: state.reminders.view.reminders.table.orderBy,
    asc: state.reminders.view.reminders.table.asc
}}

const mapDispatchToProps = {
  fetchReminders,
  onSort: sortTable,
  onChangeFilter: changeFilter,
  onChangePage: changePage,
  onShowUser: showUser,
  onShowInvoice: showInvoiceByNumber,
  onCreateRemindersClick: createReminders,
  onMailClick: mailReminder,
  onChangePagesize: changePagesizeReminders
}

const RemindersContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(Reminders)

export default RemindersContainer
