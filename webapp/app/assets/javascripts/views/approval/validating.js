import React, { Component } from 'react';
import { connect } from 'react-redux';
import { fetchCarApprovals, sortTable, changePage, changeFilter } from './../../actions/car-approval'
import { showUser } from './../../actions/user'
import { CarApprovalsStatuses } from './../../reducers/car-approval'
import UserModal from './../user'
import Table from './../table/table'

const Validating = ({ carApprovals, fetching, fetchingFailed, page, pageSize, fullSize, columns, filter, showFilter,
  status, onChangeFilter, onShowUser, onSort, onChangePage }) => {
  const rendererTableRowMethods = {
    onShowUser
  }

  return (<div>
      <Table title='' rows={carApprovals} fetching={fetching} fetchingFailed={fetchingFailed} page={page} pageSize={pageSize}
        fullSize={fullSize} filter={filter} showFilter={showFilter} columns={columns} onSort={onSort} onChangePage={onChangePage}
        onChangeFilter={onChangeFilter}
        rendererTableRow={rendererTableRow}
        rendererTableRowMethods={rendererTableRowMethods}/>
      <UserModal />
    </div>)
}

const rendererTableRow = ({row, rendererTableRowMethods}) => <CarApprovalItem
    key={`carApproval-${row.id}`}
    carApprovalAndUser={row}
    onUserClick={rendererTableRowMethods.onShowUser}
  />

const CarApprovalItem = ({carApprovalAndUser, onUserClick}) =>
  <tr>
    <td>{carApprovalAndUser.name}{' - '}{carApprovalAndUser.active ? 'actief' : 'niet actief'}</td>
    <td><button className='btn btn-link' onClick={userClicked(onUserClick, carApprovalAndUser.ownerId)}>{carApprovalAndUser.ownerName}</button></td>
    <td>{`${carApprovalAndUser.brand} ${carApprovalAndUser.type} (${carApprovalAndUser.year})`}</td>
    <td>{carApprovalAndUser.licensePlate}</td>
    <td><button className='btn btn-default'>Contract</button><button className='btn btn-default'>Lidgeld</button></td>
  </tr>

const userClicked = (onUserClick, userId) => () => onUserClick(userId)

const mapStateToProps = (state, ownProps) => {
  return{
    carApprovals: state.carApprovals.carApprovals,
    fetching: state.carApprovals.status == CarApprovalsStatuses.FETCHING_CAR_APPROVALS || state.carApprovals.status == CarApprovalsStatuses.SEARCHING_CAR_APPROVALS,
    fetchingFailed: state.carApprovals.status == CarApprovalsStatuses.FETCHING_CAR_APPROVALS_FAILED || state.carApprovals.status == CarApprovalsStatuses.SEARCHING_CAR_APPROVALS_FAILED,
    status: state.carApprovals.status,
    page: state.carApprovals.view.carApprovals.table.page,
    pageSize: state.carApprovals.view.carApprovals.table.pageSize,
    fullSize: state.carApprovals.view.carApprovals.table.fullSize,
    filter: state.carApprovals.view.carApprovals.table.filter,
    showFilter: state.carApprovals.view.carApprovals.table.showFilter,
    columns: state.carApprovals.view.carApprovals.table.columns
}}

const mapDispatchToProps = {
  fetchCarApprovals,
  onSort: sortTable,
  onChangeFilter: changeFilter,
  onChangePage: changePage,
  onShowUser: showUser
}

const ValidatingContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(Validating)

export default ValidatingContainer
