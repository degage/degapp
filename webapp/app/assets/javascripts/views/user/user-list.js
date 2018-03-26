import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import { showUser, fetchUsers, openModal, closeModal, changePageUsers, changeFilterUsers, sortTableUsers, changePagesizeUsers, createMembershipInvoice } from './../../actions/user'
import { UsersStatuses } from './../../reducers/user'
import { debounceEventHandler } from './../../util'
import Table from './../table/table'
import UserModal from './../user'
import Navigation from './../navigation'

const UsersTable = ({ orderBy, asc, onChangePagesize, route, users, fetching, fetchingFailed, page, pageSize, fullSize, columns, filter, showFilter, 
  showPagination, onChangeFilter, onSort, onChangePage, onShowUser, onCreateMembershipInvoice }) => {
  const rendererTableRowMethods = {
    onShowUser,
    onChangePagesize,
    onCreateMembershipInvoice
  }
  return (
    <div>
      <Navigation route={route} />
      <Table title='' rows={users} fetching={fetching} fetchingFailed={fetchingFailed} page={page} pageSize={pageSize}
        fullSize={fullSize} filter={filter} showFilter={showFilter} showPagination={showPagination}
        columns={columns} onSort={onSort} onChangePage={onChangePage} orderBy={orderBy} asc={asc}
        onChangeFilter={onChangeFilter}
        rendererTableRow={rendererTableRow}
        rendererTableRowMethods={rendererTableRowMethods} />
      <UserModal />
    </div>)
}

const rendererTableRow = ({ row, rendererTableRowMethods }) => <InvoiceItem
  key={`User-${row.id}`}
  user={row}
  onUserClick={rendererTableRowMethods.onShowUser}
  onCreateMembershipInvoice={rendererTableRowMethods.onCreateMembershipInvoice}
/>


const InvoiceItem = ({ user, onUserClick, onCreateMembershipInvoice }) =>
  <tr key={`UserItem-${user.userId}`}>
    <td><button className='btn btn-link' onClick={userClicked(onUserClick, user.id)}>{user.firstName + ' ' + user.lastName}</button></td>
    <td>{user.creditStatus}</td>
    <td>
      <i className={`fa ${user.sendReminder ? 'fa-check-square-o' : 'fa-square-o'}`} style={{fontSize:'18px', marginLeft: '70px'}}></i>{' '}
      <button onClick={userClicked(onCreateMembershipInvoice, user.id)}>Maak lidgeld factuur</button>
    </td>
  </tr>



const userClicked = (onUserClick, userId) => () => onUserClick(userId)

// AppContainer.js
const mapStateToProps = (state, ownProps) => {
  return{
    users: state.users.users,
    fetching: state.users.status == UsersStatuses.FETCHING_USERS || state.users.status == UsersStatuses.SEARCHING_USERS,
    fetchingFailed: state.users.status == UsersStatuses.FETCHING_USERS_FAILED || state.users.status == UsersStatuses.SEARCHING_USERS_FAILED,
    page: state.users.view.users.table.page,
    pageSize: state.users.view.users.table.pageSize,
    fullSize: state.users.view.users.table.fullSize,
    filter: state.users.view.users.table.filter,
    showFilter: state.users.view.users.table.showFilter,
    showPagination: state.users.view.users.table.showPagination,
    columns: state.users.view.users.table.columns,
    orderBy: state.users.view.users.table.orderBy,
    asc: state.users.view.users.table.asc
}}

const mapDispatchToProps = {
  fetchUsers,
  onChangeFilter: changeFilterUsers,
  onSort: sortTableUsers,
  onChangePage: changePageUsers,
  onShowUser: showUser,
  onCreateMembershipInvoice: createMembershipInvoice,
  onChangePagesize: changePagesizeUsers
}

const UsersTableContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(UsersTable)

export default UsersTableContainer
