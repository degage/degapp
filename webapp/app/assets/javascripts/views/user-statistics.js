import React, { Component } from 'react';
import { connect } from 'react-redux';
import { fetchUserStatistics, changeFilter, sortTable, changePage, changePagesizeUserStatistics } from './../actions/user-statistics'
import { showUser } from './../actions/user'
import { UserStatisticsStatuses } from './../reducers/user-statistics'
import Table from './table/table'
import UserModal from './user'
import Navigation from './navigation'

const UserStatistics = ({ orderBy, asc, onChangePagesize, route, userStatistics, fetching, fetchingFailed, page, pageSize, fullSize, columns, filter, showFilter,
  showPagination, onChangeFilter, onSort, onChangePage, onShowUser }) => {
  const rendererTableRowMethods = {
    onShowUser,
    onChangePagesize
  }
  return (
    <div>
      <Navigation route={route} />
      <Table title='' rows={userStatistics} fetching={fetching} fetchingFailed={fetchingFailed} page={page} pageSize={pageSize}
        fullSize={fullSize} filter={filter} showFilter={showFilter} showPagination={showPagination}
        columns={columns} onSort={onSort} onChangePage={onChangePage} orderBy={orderBy} asc={asc}
        onChangeFilter={onChangeFilter}
        rendererTableRow={rendererTableRow}
        rendererTableRowMethods={rendererTableRowMethods} />
      <UserModal />
    </div>)
}

const rendererTableRow = ({row, rendererTableRowMethods}) => <UserStatisticItem
    key={`user-stat-${row.user.id}`}
    userStatistic={row}
    onUserClick={rendererTableRowMethods.onShowUser}
  />

const UserStatisticItem = ({userStatistic, onUserClick}) =>
  <tr>
    <td><button className='btn btn-link' onClick={userClicked(onUserClick, userStatistic.user == null ? null : userStatistic.user.id)}>{userStatistic.user == null ? '-' : userStatistic.user.lastName + ' ' + userStatistic.user.firstName}</button></td>
    <td><p style={{width: '33%',textAlign: 'center'}}>€ {userStatistic.amountToPay.toFixed(2)}</p></td>
    <td><p style={{width: '33%',textAlign: 'center'}}>€ {userStatistic.amountPaid.toFixed(2)}</p></td>
    <td><p style={{width: '33%',textAlign: 'center'}}>€ {(userStatistic.amountToPay - userStatistic.amountPaid).toFixed(2)}</p></td>
  </tr>

const userClicked = (onUserClick, userId) => () => onUserClick(userId)

const mapStateToProps = (state, ownProps) => {
  return{
    userStatistics: state.userStatistics.userStatistics,
    fetching: state.userStatistics.status == UserStatisticsStatuses.FETCHING_USER_STATISTICS,
    fetchingFailed: state.userStatistics.status == UserStatisticsStatuses.FETCHING_USER_STATISTICS_FAILED,
    page: state.userStatistics.view.userStatistics.table.page,
    pageSize: state.userStatistics.view.userStatistics.table.pageSize,
    fullSize: state.userStatistics.view.userStatistics.table.fullSize,
    filter: state.userStatistics.view.userStatistics.table.filter,
    showFilter: state.userStatistics.view.userStatistics.table.showFilter,
    showPagination: state.userStatistics.view.userStatistics.table.showPagination,
    columns: state.userStatistics.view.userStatistics.table.columns,
    orderBy: state.userStatistics.view.userStatistics.table.orderBy,
    asc: state.userStatistics.view.userStatistics.table.asc
}}

const mapDispatchToProps = {
  onChangeFilter: changeFilter,
  onSort: sortTable,
  onChangePage: changePage,
  onShowUser: showUser,
  onChangePagesize: changePagesizeUserStatistics
}

const UserStatisticsContainer = connect(
  mapStateToProps,
  mapDispatchToProps
)(UserStatistics)

export default UserStatisticsContainer
