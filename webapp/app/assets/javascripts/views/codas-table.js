import React, { Component } from 'react';
import Modal from 'react-modal'
import { connect } from 'react-redux';
import { openModal, closeModal, changePage, changeFilter, sortTable, changePagesizeCodas } from './../actions/coda'
import { CodasStatuses } from './../reducers/coda'
import Table from './table/table'
import { showUser } from './../actions/user'
import UserModal from './user'

const CodasTable = ({ onChangePagesize, codas, fetching, fetchingFailed, page, pageSize, fullSize, columns, filter, showFilter, showPagination,
  onChangeFilter, onSort, onChangePage, onShowUser }) => {
  const rendererTableRowMethods = {
    onShowUser,
    onChangePagesize
  }
  return (
    <div>
      <Table title='' rows={codas} fetching={fetching} fetchingFailed={fetchingFailed} page={page} pageSize={pageSize}
        fullSize={fullSize} filter={filter} showFilter={showFilter} showPagination={showPagination}
        columns={columns} onSort={onSort} onChangePage={onChangePage}
        onChangeFilter={onChangeFilter}
        rendererTableRow={rendererTableRow}
        rendererTableRowMethods={rendererTableRowMethods} />
      <UserModal />
    </div>)
}

const rendererTableRow = ({row, rendererTableRowMethods}) => <CodaItem
  key={`coda-${row.id}`}
  coda={row}
  onUserClick={rendererTableRowMethods.onShowUser}
/>


const CodaItem = ({coda, onUserClick}) =>
  <tr key={`CodaItem-${coda.id}`}>
    <td>{coda.id}</td>
    <td>{coda.filename}</td>
    <td>{coda.date}</td>
    <td><button className='btn btn-link' onClick={userClicked(onUserClick, coda.user == null ? null : coda.user.id)}>{coda.user == null ? '-' : coda.user.lastName + ' ' + coda.user.firstName}</button></td>
  </tr>

const userClicked = (onUserClick, userId) => () => onUserClick(userId)

// AppContainer.js
const mapStateToProps = (state, ownProps) => {
  return{
  codas: state.codas.codas,
  fetching: state.codas.status == CodasStatuses.FETCHING_INVOICES || state.codas.status == CodasStatuses.SEARCHING_INVOICES,
  fetchingFailed: state.codas.status == CodasStatuses.FETCHING_INVOICES_FAILED || state.codas.status == CodasStatuses.SEARCHING_INVOICES_FAILED,
  status: state.codas.status,
  page: state.codas.view.codas.table.page,
  pageSize: state.codas.view.codas.table.pageSize,
  fullSize: state.codas.view.codas.table.fullSize,
  filter: state.codas.view.codas.table.filter,
  showFilter: state.codas.view.codas.table.showFilter,
  showPagination: state.codas.view.codas.table.showPagination,
  columns: state.codas.view.codas.table.columns
}}

const mapDispatchToProps = {
  onChangeFilter: changeFilter,
  onSort: sortTable,
  onChangePage: changePage,
  onShowUser: showUser,
  onChangePagesize: changePagesizeCodas
}

const CodasTableContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(CodasTable)

export default CodasTableContainer
